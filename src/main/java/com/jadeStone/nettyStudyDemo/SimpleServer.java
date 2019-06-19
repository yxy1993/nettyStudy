package com.jadeStone.nettyStudyDemo;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleServer implements Runnable {
	
	public void run() {
			// 创建一个ServerSocket，用于监听客户端Socket的连接请求
			ServerSocket ss;
			try {
				ss = new ServerSocket(9090);
				// 采用循环不断接受来自客户端的请求
				while (true)
				{
					// 每当接受到客户端Socket的请求，服务器端也对应产生一个Socket
					Socket s = ss.accept();
					OutputStream os = s.getOutputStream();
					os.write(" hello world！\n"
						.getBytes("utf-8"));
					// 关闭输出流，关闭Socket
					os.close();
					s.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
		}

}
