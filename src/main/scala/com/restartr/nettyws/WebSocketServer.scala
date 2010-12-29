package com.restartr.nettyws

import java.net.InetSocketAddress
import java.util.concurrent.Executors

import org.jboss.netty.bootstrap._
import org.jboss.netty.channel.socket.nio._
import org.jboss.netty.channel.ChannelPipelineFactory

object WebSocketServer {
  def main(args: Array[String]) {
    // サーバーのセットアップ
    val bootstrap = new ServerBootstrap(
      new NioServerSocketChannelFactory(
        Executors.newCachedThreadPool(), // bossExecutor
        Executors.newCachedThreadPool()  // workerExecutor
      ))
    
    // WebSocket用ハンドラを登録
    bootstrap.setPipelineFactory(new WebSocketServerPipelineFactory())
    
    // 8080番で待ち受け開始
    bootstrap.bind(new InetSocketAddress("localhost", 8080))
  }
}

