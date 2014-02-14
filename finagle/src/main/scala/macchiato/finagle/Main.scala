package macchiato.finagle

import java.net.InetSocketAddress

import org.jboss.netty.buffer.ChannelBuffer
import org.jboss.netty.handler.codec.http.DefaultHttpResponse
import org.jboss.netty.handler.codec.http.HttpHeaders
import org.jboss.netty.handler.codec.http.HttpRequest
import org.jboss.netty.handler.codec.http.HttpResponse
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.jboss.netty.handler.codec.http.HttpVersion

import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.builder.Server
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.http.Http
import com.twitter.finagle.redis.Client
import com.twitter.finagle.redis.Redis
import com.twitter.finagle.redis.util.StringToChannelBuffer
import com.twitter.util.Future

object Main {

  def main(args: Array[String]) {
    println("Macchiato Finagle start...")

    //    val redis: Client = Redis.newRichClient("127.0.0.1:6379")

    val redis: Client = Client.apply(ClientBuilder()
      .hosts("127.0.0.1:6379")
      .hostConnectionLimit(20)
      .codec(Redis())
      .daemon(true)
      .build())

    val service = new Service[HttpRequest, HttpResponse] {

      def ok(raw: ChannelBuffer): Future[HttpResponse] = {
        val response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.setContent(raw);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.getContent().readableBytes());
        Future.value(response)
      }

      def error(): Future[HttpResponse] = {
        val response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        Future.value(response)
      }

      def convert(raw: Option[ChannelBuffer]): Future[HttpResponse] = {
        raw match {
          case Some(n) => ok(n)
          case None => error()
        }
      }
      def apply(req: HttpRequest): Future[HttpResponse] = {
        var key = req.getUri().substring(1);
        val result = redis.get(StringToChannelBuffer(key))
        result.flatMap(convert)
      }
    }

    val server: Server = ServerBuilder()
      .codec(Http())
      .bindTo(new InetSocketAddress("127.0.0.1", 8080))
      .name("Macchiato")
      .build(service)

    sys.ShutdownHookThread {
      println("Macchiato Finagle shutdown...")
    }
  }

}
