package com.restartr.nettyws

import org.jboss.netty.channel._
import org.jboss.netty.handler.codec.http._

class WebSocketServerPipelineFactory extends ChannelPipelineFactory{
  @throws(classOf[Exception])
  def getPipeline(): ChannelPipeline = {
    val pipeline = Channels.pipeline()
    
    pipeline.addLast("decoder"    , new HttpRequestDecoder())
    pipeline.addLast("aggregator" , new HttpChunkAggregator(65536))
    pipeline.addLast("encoder"    , new HttpResponseEncoder())
    pipeline.addLast("handler"    , new WebSocketServerHandler())
    
    pipeline
  }
}