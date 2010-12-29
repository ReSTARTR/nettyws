package com.restartr.nettyws

import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

import java.util.logging.Level
import java.util.logging.Logger

import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.buffer.ChannelBuffer
import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.ChannelPipeline
import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.channel.Channels
import org.jboss.netty.channel.ExceptionEvent
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.channel.SimpleChannelUpstreamHandler
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory

object EchoServer {
  @throws(classOf[Exception])
  def main(args:Array[String]) {
		// configure the server
    val bootstrap = new ServerBootstrap(
      new NioServerSocketChannelFactory(
        Executors.newCachedThreadPool(),
        Executors.newCachedThreadPool()))
    
		// Set up the pipeline factory
    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      def getPipeline():ChannelPipeline = 
        Channels.pipeline(new EchoServerHandler())
    })
    
    // Bind and start to accept incoming connections.
    bootstrap.bind(new InetSocketAddress(8080))
  }
}

class EchoServerHandler extends SimpleChannelUpstreamHandler {
  val logger = java.util.logging.Logger.getLogger("EchoServerHandler")
  val transferredBytes = new AtomicLong()
  
  def getTransferredBytes() = transferredBytes.get()
	
  override def messageReceived(ctx:ChannelHandlerContext, e:MessageEvent) {
    transferredBytes.addAndGet( (e.getMessage()).asInstanceOf[ChannelBuffer].readableBytes() )
    println("echo_server: message received: " + e.getMessage())
    e.getChannel().write(e.getMessage())
  }

	override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
		// Close the connection when an exception is raised.
		logger.log(Level.WARNING, "Unexpected exception from downstream.", e.getCause())
		e.getChannel().close()
	}
}
