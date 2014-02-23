package macchiato.finagle

import java.net.InetSocketAddress

import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.handler.codec.http.DefaultHttpResponse
import org.jboss.netty.handler.codec.http.HttpHeaders
import org.jboss.netty.handler.codec.http.HttpRequest
import org.jboss.netty.handler.codec.http.HttpResponse
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.jboss.netty.handler.codec.http.HttpVersion
import org.jboss.netty.util.CharsetUtil

import com.twitter.finagle.Http
import com.twitter.finagle.Service
import com.twitter.util.Await
import com.twitter.util.Future

object HttpServerSimple extends App {

  println("Macchiato Finagle HTTP Server start...")

  val service = new Service[HttpRequest, HttpResponse] {
    def apply(req: HttpRequest): Future[HttpResponse] = {
      val response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
      response.setContent(ChannelBuffers.copiedBuffer("WORKING", CharsetUtil.UTF_8))
      response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=utf-8")
      response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.getContent().readableBytes())
      Future.value(response)
    }
  }

  val server = Http.serve(new InetSocketAddress("127.0.0.1", 8080), service)
  Await.ready(server)

  sys.ShutdownHookThread {
    println("Macchiato Finagle HTTP Server shutdown...")
  }

}
