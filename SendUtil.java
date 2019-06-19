package testwebc;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class SendUtil {
	/**
	 * 想指定链接发送数据
	 * @param msg 消息
	 * @param channel 指定链接
	 * @return {@link String}
	 */
	public static String sendTest(String msg,Channel channel) {
	    try {
	        channel.writeAndFlush(new TextWebSocketFrame( "[系统API]" + msg));
	        return "success";
	    }catch (Exception e){
	        e.printStackTrace();
	        return "error";
	    }
	}
}
