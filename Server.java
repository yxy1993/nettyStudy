package testwebc;
 
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
 
public class Server  implements Runnable {
    private static final int port = 9091;
    public void run(){
        //NioEventLoopGroup是用来处理IO操作的多线程事件循环器
        EventLoopGroup bossGroup  = new NioEventLoopGroup();  // 用来接收进来的连接
        EventLoopGroup workerGroup  = new NioEventLoopGroup();// 用来处理已经被接收的连接
        try{
            ServerBootstrap server =new ServerBootstrap();//是一个启动NIO服务的辅助启动类
            server.group(bossGroup,workerGroup )
                    .channel(NioServerSocketChannel.class)  // 这里告诉Channel如何接收新的连接
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 自定义处理类
                            ch.pipeline().addLast(new ServerHandler());
                        }
                    });
            server.option(ChannelOption.SO_BACKLOG,1024);
            server.childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f=null;
			try {
				f = server.bind(port).sync();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}// 绑定端口，开始接收进来的连接
            System.out.println("TCP服务端启动成功...");
            // 监听服务器关闭监听
            try {
				f.channel().closeFuture().sync();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }finally {
            bossGroup.shutdownGracefully(); ////关闭EventLoopGroup，释放掉所有资源包括创建的线程
            workerGroup.shutdownGracefully();
        }
    }
}
