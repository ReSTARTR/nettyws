package com.restartr.nettyws

import java.net.InetSocketAddress
import java.util.concurrent.Executors

import org.jboss.netty.bootstrap._
import org.jboss.netty.channel.socket.nio._
import org.jboss.netty.channel.ChannelPipelineFactory

object WebSocketServer {
  def main(args: Array[String]) {
    val bootstrap = new ServerBootstrap(
      new NioServerSocketChannelFactory(
        Executors.newCachedThreadPool(),
        Executors.newCachedThreadPool()))
    
    bootstrap.setPipelineFactory(new WebSocketServerPipelineFactory())
    
    bootstrap.bind(new InetSocketAddress("localhost", 8080))
  }
}

