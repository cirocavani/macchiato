package macchiato.netty;

import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Values.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.io.IOException;

public final class Main {

	private Main() {
	}

	static class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

		private static final ByteBuf RESPONSE_BUFFER = Unpooled.wrappedBuffer(new byte[] { 'W', 'o', 'r', 'k', 'i', 'n', 'g' });
		private static final ByteBuf RESPONSE = Unpooled.unreleasableBuffer(RESPONSE_BUFFER);

		@Override
		protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest req) throws Exception {
			if (is100ContinueExpected(req)) {
				ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
			}

			final FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, RESPONSE);
			response.headers().set(CONTENT_TYPE, "text/plain");
			response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

			if (isKeepAlive(req)) {
				response.headers().set(CONNECTION, KEEP_ALIVE);
				ctx.writeAndFlush(response);
			} else {
				ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
			}
		}

		@Override
		public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
			if (cause.getClass() == IOException.class && cause.getMessage().endsWith("Connection reset by peer")) {
				// ignore
			} else {
				cause.printStackTrace();
			}
			ctx.close();
		}
	}

	static class HttpServer implements AutoCloseable {

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

			mainGroup = new EpollEventLoopGroup(1);
			workerGroup = new EpollEventLoopGroup(Runtime.getRuntime().availableProcessors());
			try {
				final ServerBootstrap server = new ServerBootstrap();
				server.option(ChannelOption.SO_BACKLOG, 1024);
				server.option(ChannelOption.TCP_NODELAY, true);
				server.option(EpollChannelOption.TCP_CORK, true);
				server.group(mainGroup, workerGroup);
				server.channel(EpollServerSocketChannel.class);
				server.childHandler(new ChannelInitializer<SocketChannel>() {

					@Override
					public void initChannel(final SocketChannel ch) throws Exception {
						final ChannelPipeline pipe = ch.pipeline();
						pipe.addLast(new HttpServerCodec());
						pipe.addLast(new HttpObjectAggregator(1048576));
						pipe.addLast(new HttpHandler());
					}

				});

				server.bind(host, port);
			} catch (final Exception e) {
				close();
				throw e;
			}
		}

		@Override
		public void close() {
			if (!isStarted()) {
				return;
			}

			try {
				mainGroup.shutdownGracefully();
			} finally {
				mainGroup = null;
			}
			try {
				workerGroup.shutdownGracefully();
			} finally {
				workerGroup = null;
			}
		}

	}

	public static void main(final String... args) throws Exception {
		System.out.println("Macchiato Netty start...");

//		ResourceLeakDetector.setLevel(Level.ADVANCED);

		final HttpServer server = new HttpServer("127.0.0.1", 8080);
		server.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				System.out.println("Macchiato Netty shutdown...");
				server.close();
			}

		});
	}
}
