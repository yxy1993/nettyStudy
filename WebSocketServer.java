package testwebc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 
 *
 */
public class WebSocketServer implements Runnable{
		@Override
		public void run() {
			 /*
			  * websocket端
			  */
		        EventLoopGroup bossGroup = new NioEventLoopGroup();
		        EventLoopGroup workerGroup = new NioEventLoopGroup();
		        try {
		            ServerBootstrap b = new ServerBootstrap();
		            b.group(bossGroup, workerGroup)
		                    .channel(NioServerSocketChannel.class)
		                    .childHandler(new ChannelInitializer<SocketChannel>() {

		                        @Override
		                        protected void initChannel(SocketChannel ch)
		                                throws Exception {
		                            ChannelPipeline pipeline = ch.pipeline();
		                            pipeline.addLast("http-codec",
		                                    new HttpServerCodec());
		                            pipeline.addLast("aggregator",
		                                    new HttpObjectAggregator(65536));
		                            ch.pipeline().addLast("http-chunked",
		                                    new ChunkedWriteHandler());
		                            pipeline.addLast("handler",
		                                    new WebSocketServerHandler());
		                        }
		                    });

		            Channel ch=null;
					try {
						ch = b.bind(9090).sync().channel();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
		            System.out.println("Web socket server started at port " + 9090
		                    + '.');
		            System.out
		                    .println("Open your browser and navigate to http://localhost:"
		                            + 9090 + '/');

		            try {
						ch.closeFuture().sync();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        } finally {
		            bossGroup.shutdownGracefully();
		            workerGroup.shutdownGracefully();
		        }
		}
}
