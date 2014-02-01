Macchiato Netty Sample
----------------------

http://netty.io/

*Netty is an asynchronous event-driven network application framework for rapid development of maintainable high performance protocol servers & clients.*


**Reference**

http://netty.io/4.0/api/io/netty/channel/ChannelPipeline.html

*A list of ChannelHandlers which handles or intercepts inbound events and outbound operations of a Channel. ChannelPipeline implements an advanced form of the Intercepting Filter pattern to give a user full control over how an event is handled and how the ChannelHandlers in a pipeline interact with each other.*

http://netty.io/4.0/api/io/netty/channel/ChannelHandler.html

*Handles or intercepts a ChannelInboundInvoker or ChannelOutboundInvoker operation, and forwards it to the next handler in a ChannelPipeline.*

http://netty.io/4.0/api/io/netty/channel/ChannelHandlerContext.html

*Enables a ChannelHandler to interact with its ChannelPipeline and other handlers. A handler can notify the next ChannelHandler in the ChannelPipeline, modify the ChannelPipeline it belongs to dynamically.*

**HTTP**

https://github.com/netty/netty/tree/4.0/example/src/main/java/io/netty/example/http/helloworld

http://netty.io/4.0/api/io/netty/handler/codec/http/package-summary.html

*An HTTP server that sends back the content of the received HTTP request in a pretty plaintext form.*

Advanced:

https://github.com/netty/netty/blob/4.0/example/src/main/java/io/netty/example/http/snoop/HttpSnoopServerHandler.java
