package com.restartr.nettyws

import org.jboss.netty.buffer._
import org.jboss.netty.util.CharsetUtil

object WebSocketServerIndexPage {  
  def getContent(webSocketLocation: String) = {
    val script = """
      var socket; 
      if (window.WebSocket) {
        socket = new WebSocket( '""" + webSocketLocation + """' );
        socket.onmessage = function(event) { log(event.data) }
        socket.onopen = function(event) { log('web socket opened') }
        socket.onclose = function(event) { log('web socket closed') }
      } else {
        alert('your browser does not supported web socket')
      }
      function log(message) {
        p = document.createElement('p');
        p.innerHTML = message
        document.getElementById('log').appendChild(p) ;
      }
      function send(message) {
        if (!window.WebSocket) { return; }
        if (socket.readyState == WebSocket.OPEN) {
          socket.send(message)
        } else {
          alert('the socket is not open.')
        }
      }
    """
    ChannelBuffers.copiedBuffer(
      (<html>
        <head><title>web socket test</title></head>
        <body>
          <script type="text/javascript">{script}</script>
          <form onsubmit="return false;">
            <input type="text" name="message" value="hello, world" />
            <input type="button" value="send web socket data" onclick="send(this.form.message.value)" />
          </form>
          <div id="log" />
        </body>
      </html>)toString(), CharsetUtil.US_ASCII)
  }
}
