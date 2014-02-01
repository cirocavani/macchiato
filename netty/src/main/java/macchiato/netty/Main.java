package macchiato.netty;

import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerCodec;

import java.io.IOException;

public final class Main {

	private Main() {
	}

	static class HttpHandler extends ChannelInboundHandlerAdapter {

		private static final byte[] CONTENT = { 'W', 'o', 'r', 'k', 'i', 'n', 'g' };

		@Override
		public void channelReadComplete(final ChannelHandlerContext ctx) {
			ctx.flush();
		}

		@Override
		public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
			if (msg instanceof HttpRequest) {
				final HttpRequest req = (HttpRequest) msg;

				if (is100ContinueExpected(req)) {
					ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
				}
				final boolean keepAlive = isKeepAlive(req);
				final FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(CONTENT));
				response.headers().set(CONTENT_TYPE, "text/plain");
				response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

				if (!keepAlive) {
					ctx.write(response).addListener(ChannelFutureListener.CLOSE);
				} else {
					response.headers().set(CONNECTION, Values.KEEP_ALIVE);
					ctx.write(response);
				}
			}
		}

		@Override
		public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
			if (IOException.class == cause.getClass() && "Connection reset by peer".equals(cause.getMessage())) {
				// ignore
			} else {
				cause.printStackTrace();
			}
			ctx.close();
		}

	}

	static class HttpServer {

		private final String host;
		private final int port;

		private EventLoopGroup mainGroup;
		private EventLoopGroup workerGroup;

		public HttpServer(final String host, final int port) {
			this.host = host;
			this.port = port;
		}

		public boolean isStarted() {
			return mainGroup != null;
		}

		public void start() {
			if (isStarted()) {
				return;
			}

			mainGroup = new NioEventLoopGroup();
			workerGroup = new NioEventLoopGroup();
			try {
				final ServerBootstrap server = new ServerBootstrap();
//				server.option(ChannelOption.SO_BACKLOG, 1024);
				server.group(mainGroup, workerGroup);
				server.channel(NioServerSocketChannel.class);
				server.childHandler(new ChannelInitializer<SocketChannel>() {

					@Override
					public void initChannel(final SocketChannel ch) throws Exception {
						final ChannelPipeline pipe = ch.pipeline();
						pipe.addLast(new HttpServerCodec());
						pipe.addLast(new HttpHandler());
					}

				});

				server.bind(host, port);
			} catch (final Exception e) {
				shutdown();
				throw e;
			}
		}

		public void shutdown() {
			if (!isStarted()) {
				return;
			}

			mainGroup.shutdownGracefully();
			mainGroup = null;
			workerGroup.shutdownGracefully();
			workerGroup = null;
		}

	}

	public static void main(final String... args) throws Exception {
		System.out.println("Macchiato Netty start...");

		final HttpServer server = new HttpServer("127.0.0.1", 8080);
		server.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				System.out.println("Macchiato Netty shutdown...");
				server.shutdown();
			}

		});
	}
}
