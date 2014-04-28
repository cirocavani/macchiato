package macchiato.finagle

import java.net.InetSocketAddress
import java.util.Random

import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.jboss.netty.util.CharsetUtil

import com.twitter.finagle.Service
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.http.Http
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Response
import com.twitter.finagle.http.RichHttp
import com.twitter.finagle.http.service.NullService
import com.twitter.finagle.http.service.RoutingService
import com.twitter.util.Future

object HttpServerRoute extends App {

  println("Macchiato Finagle HTTP Server Route start...")

  val healthcheck = new Service[Request, Response] {
    def apply(req: Request): Future[Response] = {
      val response = Response(HttpResponseStatus.OK)
      response.setContent(ChannelBuffers.copiedBuffer("WORKING", CharsetUtil.UTF_8))
      Future.value(response)
    }
  }

  val rnd = new Random();

  val rand = new Service[Request, Response] {
    def apply(req: Request): Future[Response] = {
      val value = s"${rnd.nextLong()}";
      val response = Response(HttpResponseStatus.OK)
      response.setContent(ChannelBuffers.copiedBuffer(value, CharsetUtil.UTF_8))
      Future.value(response)
    }
  }

  val service = RoutingService.byPath {
    case "/" => NullService
    case "/healthcheck" => healthcheck
    case "/number" => rand
  }

  val server = ServerBuilder()
    .codec(RichHttp[Request](Http()))
    .bindTo(new InetSocketAddress("127.0.0.1", 8080))
    .name("Macchiato")
    .build(service)

  sys.ShutdownHookThread {
    println("Macchiato Finagle HTTP Server Route shutdown...")
  }

}