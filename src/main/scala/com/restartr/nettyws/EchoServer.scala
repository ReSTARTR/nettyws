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
    // サーバーのセットアップ
    val bootstrap = new ServerBootstrap(
      new NioServerSocketChannelFactory(
        Executors.newCachedThreadPool(), // bossExecutor
        Executors.newCachedThreadPool()  // workerExecutor
      ))
    
    bootstrap.setPipelineFactory(
      // リクエストをそのまま返すハンドラを実装して登録
      new ChannelPipelineFactory() {
        def getPipeline() = Channels.pipeline(new EchoServerHandler())
      }
    )
    
    // 8080番で待ち受け開始
    bootstrap.bind(new InetSocketAddress(8080))
  }
}

class EchoServerHandler extends SimpleChannelUpstreamHandler {
  val logger = java.util.logging.Logger.getLogger("EchoServerHandler")
  val transferredBytes = new AtomicLong()
  
  /*
  // そのまま受け取る
  def getTransferredBytes() = transferredBytes.get()
  */
  // そのまま返す
  override def messageReceived(ctx:ChannelHandlerContext, e:MessageEvent) {
    transferredBytes.addAndGet(
      e.getMessage().asInstanceOf[ChannelBuffer].readableBytes())
    
    println("echo_server: message received: " + e.getMessage())
    // レスポンスを返す
    e.getChannel().write(e.getMessage())
  }
  
  // 例外発生時はここにくる
  override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
    logger.log(Level.WARNING, 
               "Unexpected exception from downstream.",
               e.getCause())
    
    e.getChannel().close()
  }
}
