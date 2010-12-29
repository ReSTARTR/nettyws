package com.restartr.nettyws

import java.security.MessageDigest

import org.jboss.netty.buffer._
import org.jboss.netty.channel._
import org.jboss.netty.handler.codec._
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.websocket._
import org.jboss.netty.handler.codec.http.HttpHeaders._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._ 
import org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1
import org.jboss.netty.util.CharsetUtil

class WebSocketServerHandler extends SimpleChannelUpstreamHandler {
  val WEBSOCKET_PATH = "/uppercase"
  
  /**
   * メッセージ受信時
   */
  @throws(classOf[Exception])
  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
    val msg:Object = e.getMessage()
    msg match {
      case frame: WebSocketFrame => {
          handleWebSocketFrame(ctx, frame)
        }
      case req: HttpRequest => {
          handleHttpRequest(ctx, req)
        }
    }
  }
  /**
   * HTTPリクエストの処理
   */
  @throws(classOf[Exception])
  def handleHttpRequest(ctx: ChannelHandlerContext, req: HttpRequest) {
    // GETリクエスト以外は処理しない
    // "/"にきたらWebSocketクライアント用ページを送信
    // "/uppercase"にきたらリクエスト文字列を大文字に変換して返す
    if (req.getMethod() != GET) {
      sendHttpResponse(
        ctx, 
        req, 
        new DefaultHttpResponse(HTTP_1_1, FORBIDDEN))
    } else if (req.getUri().equalsIgnoreCase("/")) {
      // default page
      val res = new DefaultHttpResponse(HTTP_1_1, OK)
      val content = 
        WebSocketServerIndexPage.getContent(getWebSocketLocation(req))
      
      res.setHeader(Names.CONTENT_TYPE, "text/html; charset=UTF-8")
      setContentLength(res, content.readableBytes())
      
      res.setContent(content)
      sendHttpResponse(ctx, req, res)
      
    } else if (req.getUri().equalsIgnoreCase(WEBSOCKET_PATH) &&
               Values.UPGRADE.equalsIgnoreCase(req.getHeader(Names.CONNECTION)) &&
               Values.WEBSOCKET.equalsIgnoreCase(req.getHeader(Names.UPGRADE))) {
      // WebSocketリクエスト時の処理
      val res = new DefaultHttpResponse(
        HTTP_1_1,
        new HttpResponseStatus(101, "Web Socket Protocol Handshake"))
      res.addHeader(Names.UPGRADE, Values.WEBSOCKET)
      res.addHeader(Names.CONNECTION, Values.UPGRADE)
      
      if (req.containsHeader(Names.SEC_WEBSOCKET_KEY1) &&
          req.containsHeader(Names.SEC_WEBSOCKET_KEY2)) {
        // new handshake method with a challenge
        res.addHeader(Names.SEC_WEBSOCKET_ORIGIN, req.getHeader(Names.ORIGIN))
        res.addHeader(Names.SEC_WEBSOCKET_LOCATION, getWebSocketLocation(req))
        
        val protocol = req.getHeader(Names.SEC_WEBSOCKET_PROTOCOL)
        if (protocol != null)
          res.addHeader(Names.WEBSOCKET_PROTOCOL, protocol)
        
        // calculate the answer of the challenge.
        val key1 = req.getHeader(Names.SEC_WEBSOCKET_KEY1)
        val key2 = req.getHeader(Names.SEC_WEBSOCKET_KEY2)
        val a = (key1.replaceAll("[^0-9]", "").toLong / 
          key1.replaceAll("[^ ]", "").length()).toInt
        val b = (key2.replaceAll("[^0-9]", "").toLong / 
          key2.replaceAll("[^ ]", "").length()).toInt
        val c = req.getContent().readLong()
        
        val input = ChannelBuffers.buffer(16)
        input.writeInt(a)
        input.writeInt(b)
        input.writeLong(c)
        
        val output = ChannelBuffers.wrappedBuffer(
          MessageDigest.getInstance("MD5").digest(input.array()))
        
        res.setContent(output)
      } else {
        res.addHeader(Names.WEBSOCKET_ORIGIN, req.getHeader(Names.ORIGIN))
        res.addHeader(Names.WEBSOCKET_LOCATION, getWebSocketLocation(req))
        val protocol = req.getHeader(Names.WEBSOCKET_PROTOCOL)
        if (protocol != null) 
          res.addHeader(Names.WEBSOCKET_PROTOCOL, protocol)
      }
      
      // ハンドラをHTTPからWebSocketに切り替え
      val p = ctx.getChannel().getPipeline()
      p.remove("aggregator")
      p.replace("decoder", "wsdecoder", new WebSocketFrameDecoder())
      
      ctx.getChannel().write(res)
      
      p.replace("encoder", "wsencoder", new WebSocketFrameEncoder())
    } else {
      sendHttpResponse(
        ctx, req, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN))
    }
  }
  /**
   * WebSocketリクエストの処理
   */
  @throws(classOf[Exception])
  def handleWebSocketFrame(ctx: ChannelHandlerContext, frame: WebSocketFrame) {
    // 大文字に変換するして、WebSocketFrameにのせてレスポンスを返す。
    ctx.getChannel().write(
      new DefaultWebSocketFrame(
        frame.getTextData().toUpperCase))
  }
  /**
   * HTTPレスポンスの送信
   */
  @throws(classOf[Exception])
  def sendHttpResponse(ctx: ChannelHandlerContext, req: HttpRequest, res: HttpResponse) {
    // generate an error page if response status is no OK(200)
    if (res.getStatus().getCode() != 200) {
      res.setContent(
        ChannelBuffers.copiedBuffer(
          res.getStatus().toString(), CharsetUtil.UTF_8))
      HttpHeaders.setContentLength(res, res.getContent().readableBytes())
    }
    
    // send the response and close the connection if necessary
    val f = ctx.getChannel().write(res)
    if (!HttpHeaders.isKeepAlive(req) || res.getStatus().getCode() != 200) {
      f.addListener(ChannelFutureListener.CLOSE)
    }
  }
  /**
   * 例外発生時の処理
   */
  @throws(classOf[Exception])
  override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
    // @todo
    println("server: exception caught: ")
    e.getCause().printStackTrace()
    e.getChannel().close()
  }
  /**
   * WebSocket接続情報
   */
  def getWebSocketLocation(req: HttpRequest) = 
    "ws://" + req.getHeader(HttpHeaders.Names.HOST) + WEBSOCKET_PATH
}
  

