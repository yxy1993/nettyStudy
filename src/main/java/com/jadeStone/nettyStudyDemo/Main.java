package com.jadeStone.nettyStudyDemo;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;

/*
@author YHL
@qq: 1357098586
@version 创建时间：2019年3月21日 下午4:09:22 

 */

public class Main {

	public static List<ChannelHandlerContext> listmacs=new ArrayList<ChannelHandlerContext>();
	public static List<ChannelHandlerContext> li=new ArrayList<ChannelHandlerContext>();
	public static void main(String[] args) {
		
		/*new Thread(new WebSocketServer()).start();//启动websocket
*/		new Thread(new Server()).start();//启动tcpsocket
		new Thread(new SimpleServer()).start();//启动websocket
		
	}

}
