package app;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.Vector;
import javax.swing.*;

import util.Conf;
import vo.Customer;
import vo.Message;

public class Server extends JFrame implements Runnable{
	
    /*客户端连接*/
	private Socket socket = null;
	/*服务器接收连接*/
	private ServerSocket serverSocket = null;
	/*保存客户端的线程*/
	private Vector<ChatThread> clients = new Vector<ChatThread>();
	/*保存在线用户*/
	private Vector<Customer> userList = new Vector<Customer>();
	private JButton jbt = new JButton("关闭服务器");
	private boolean canRun = true;
	public Server() throws Exception{
		this.setTitle("服务器端");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.add(jbt,BorderLayout.NORTH);
		jbt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		this.setBackground(Color.yellow);
		this.setSize(500, 100);
		this.setVisible(true);
		/*服务器端开放端口，接受连接*/
	    serverSocket = new ServerSocket(9999);
	    
	    /*接受客户连接的循环开始运行*/
	    new Thread(this).start();
	}
	public void run() {
		try {
			while(canRun) {
				
				socket = serverSocket.accept();
				ChatThread ct = new ChatThread(socket,this);
				/*线程开始运行*/
				ct.start();
			}
		}catch(Exception ex) {
			canRun = false;
			try {
				serverSocket.close();
			}catch(Exception ex1) {}
		}
	}
	
	
	public Vector<ChatThread> getClients(){
		return clients;
	}
	public Vector<Customer>getUserList(){
		return userList;
	}
}
