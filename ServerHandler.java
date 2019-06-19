package testwebc;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger; 
public class ServerHandler extends ChannelInboundHandlerAdapter {
    //收到数据时调用
    @Override
    public  void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    
        try {
        	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
            ByteBuf in = (ByteBuf)msg;
            
           
            
            int readableBytes = in.readableBytes();
            byte[] bytes =new byte[readableBytes];
            in.readBytes(bytes);
            String body = new String(bytes,"UTF-8");      
            System.out.println(df.format(new Date())+"    服务端接受的消息 :   " + body);
            ctx.writeAndFlush(Unpooled.wrappedBuffer(new byte[]{0xc,0xd}));
        }finally {
            // 抛弃收到的数据
            ReferenceCountUtil.release(msg);
        }
    }
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
        Main.listmacs.remove(ctx);
    }
    /*
     * 建立连接时，返回消息
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //System.out.println("连接的客户端地址:" + ctx.channel().remoteAddress());
        ctx.writeAndFlush("client"+ InetAddress.getLocalHost().getHostName() + "success connected！ \n");
        System.out.println("connection");
        Main.listmacs.add(ctx);
        super.channelActive(ctx);
    }
    
    private static final Charset charset;
    //数组长度
    private static int table_length = 10;
    //初始id为从1开始
    private final static AtomicInteger common_id = new AtomicInteger(1);
    //在线人数
    private final static AtomicInteger onlineCount = new AtomicInteger(0);

    /**
     * 客户端管理类
     */
    static ConcurrentHashMap<Integer, Session>[] clients = new ConcurrentHashMap[table_length];
    static {
        for (int i = 0; i < table_length; i ++) {
            clients[i] = new ConcurrentHashMap<>();
        }
        charset = Charset.forName("UTF-8");
    }
    /**
     * socket客户端轻装类
     */
    static class Session {
        /**
         * id标识
         */
        private int id;
        /**
         * socket连接
         */
        private Socket socket;
        public Session(Socket socket) {
            id = common_id.getAndAdd(1);
            this.socket = socket;
        }
        public void close() throws IOException {
            this.socket.close();
        }
        public Integer getId() {
            return id;
        }
        public Socket getSocket() {
            return socket;
        }
    }

    /**
     * 获取对应的map
     * @param id
     * @return
     */
    private static ConcurrentHashMap<Integer, Session> getTable (int id) {
        return clients[id % table_length];
    }

    /**
     * 进入一个连接,因为id是均匀增加的，所以连接会均匀分布到10个map中
     * @param socket
     */
    public static Integer online (Socket socket) {
        Session session = new Session(socket);
        ConcurrentHashMap<Integer, Session> client = getTable(session.getId());
        client.put(session.getId(), session);
        onlineCount.incrementAndGet();
        return session.getId();
    }

    /**
     * 某一个id下线
     * @param id
     */
    public static void offline (int id) throws IOException {
        Session session = getTable(id).remove(id);
        if (session != null) {
            onlineCount.decrementAndGet();
            session.close();
        }
    }

    /**
     * 这里可以扩展字符编码
     * @param id
     * @param message
     * @throws IOException
     */
    public static void sendMessageOne (Integer id, String message) throws IOException {
        sendMessageOne(id, message.getBytes(charset));
    }

    public static void sendMessageOne(Integer id, byte[] message) throws IOException {
        Session session = getTable(id).get(id);
        if (session != null) {
            writeMsg(message, session.getSocket());
        }
    }

    /**
     * 千万不要关闭，不然长连接会断开
     * @param message
     * @param socket
     * @throws IOException
     */
    private static void writeMsg (byte[] message, Socket socket) throws IOException {
        OutputStream os = socket.getOutputStream();
        os.write(message);
        os.flush();
    }

    /**
     * 为一个map下群发消息
     * @param index
     */
    public static void sendMessageOneMap(int index, String message) {
        ConcurrentHashMap<Integer, Session> client = clients[index];
        Enumeration<Integer> keys = client.keys();
        while (keys.hasMoreElements()) {
            try {
                writeMsg(message.getBytes(charset), client.get(keys.nextElement()).getSocket());
            } catch (IOException e) {
                //可以对异常的连接进行处理，最终清理出离线人员。
            }
        }
    }

    /**
     * 广播消息
     * @param message
     */
    public static void broadcast(String message) {
        for (int i = 0; i < table_length; i ++) {
            sendMessageOneMap(i, message);
        }
    }

    /**
     * 获取当前在线人数
     * @return
     */
    public static int currentOnline() {
    	System.out.println("当前人数  "+onlineCount.get());
        return onlineCount.get();
    }
}
