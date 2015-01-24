package macchiato.finagle

import java.net.InetSocketAddress

import org.jboss.netty.buffer.ChannelBuffer
import org.jboss.netty.handler.codec.http.HttpResponseStatus

import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.http.Http
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Response
import com.twitter.finagle.http.RichHttp
import com.twitter.finagle.http.filter.ExceptionFilter
import com.twitter.finagle.redis.Client
import com.twitter.finagle.redis.Redis
import com.twitter.finagle.redis.util.StringToChannelBuffer
import com.twitter.util.Future

object HttpServerRedis extends App {

  println("Macchiato Finagle HTTP Server Redis start...")

  //val redis = Redis.newRichClient("127.0.0.1:6379")

  val redis = Client.apply(ClientBuilder()
    .hosts("127.0.0.1:6379")
    .hostConnectionLimit(20)
    .codec(Redis())
    .daemon(true)
    .build())

  val redisService = new Service[Request, Response] {
    def ok(raw: ChannelBuffer): Future[Response] = {
      val response = Response(HttpResponseStatus.OK)
      response.setContent(raw)
      Future.value(response)
    }
    def error(): Future[Response] = {
      Future.value(Response(HttpResponseStatus.NOT_FOUND))
    }
    def convert(raw: Option[ChannelBuffer]): Future[Response] = {
      raw match {
        case Some(n) => ok(n)
        case None => error()
      }
    }
    def apply(req: Request): Future[Response] = {
      var key = req.getUri().substring(1)
      val result = redis.get(StringToChannelBuffer(key))
      result.flatMap(convert)
    }
  }

  val errorFilter = new ExceptionFilter[Request]()

  val service = errorFilter andThen redisService

  val server = ServerBuilder()
    .codec(RichHttp[Request](Http()))
    .bindTo(new InetSocketAddress("127.0.0.1", 8080))
    .name("Macchiato")
    .build(service)

  sys.ShutdownHookThread {
    println("Macchiato Finagle HTTP Server Redis shutdown.")
  }

}
