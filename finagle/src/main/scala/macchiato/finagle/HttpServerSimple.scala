package macchiato.finagle

import java.util.concurrent.Executors

import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.handler.codec.http.DefaultHttpResponse
import org.jboss.netty.handler.codec.http.HttpHeaders
import org.jboss.netty.handler.codec.http.HttpRequest
import org.jboss.netty.handler.codec.http.HttpResponse
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.jboss.netty.handler.codec.http.HttpVersion

import com.twitter.finagle.Http
import com.twitter.finagle.Service
import com.twitter.io.Charsets
import com.twitter.util.Await
import com.twitter.util.Future
import com.twitter.util.FuturePool

object HttpServerSimple extends App {

  println("Macchiato Finagle HTTP Server start...")

  val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
  val async = FuturePool(executor)

  val RESPONSE_RAW = ChannelBuffers.wrappedBuffer("WORKING".getBytes(Charsets.Utf8))
  val RESPONSE = ChannelBuffers.unmodifiableBuffer(RESPONSE_RAW);

  val service = new Service[HttpRequest, HttpResponse] {
    def apply(req: HttpRequest): Future[HttpResponse] = async {
      val response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
      response.setContent(RESPONSE)
      response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=utf-8")
      response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.getContent().readableBytes())
      response
    }
  }

  val server = Http.serve("127.0.0.1:8080", service)
  Await.ready(server)

  sys.ShutdownHookThread {
    println("Macchiato Finagle HTTP Server shutdown...")
  }

}
