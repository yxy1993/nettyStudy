package com.jadeStone.nettyStudyDemo;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
public class SocketClientManager {
	
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
            clients[i] = new ConcurrentHashMap<Integer, Session>();
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
