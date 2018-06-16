package app;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import util.Conf;
import util.FileOpe;
import vo.Customer;
import vo.Message;
/*Ϊĳ���ͻ��˷��񣬸��������Ϣ��������Ϣ*/
public class ChatThread extends Thread{
    private Socket socket = null;
    private ObjectInputStream ois = null;
    private ObjectOutputStream oos = null;
    private Customer customer = null;
    private Server server;
    private boolean canRun = true;
    public ChatThread(Socket socket,Server server)throws Exception{
    	this.socket = socket;
    	this.server = server;
    	oos = new ObjectOutputStream(socket.getOutputStream());
    	ois = new ObjectInputStream(socket.getInputStream());
    }
    public void run() {
    	try {
    		while(canRun) {
    			Message msg = (Message)ois.readObject();
    			/*����֮��ת��*/
    			String type = msg.getType();
    			if(type.equals(Conf.LOGIN)) {
    				this.handleLogin(msg);
    			}else if(type.equals(Conf.REGISTER)) {
    				this.handleRegister(msg);
    			}else if(type.equals(Conf.MESSAGE)) {
    				this.handleMessage(msg);
    			}
    		}
    	}catch(Exception ex) {
    		this.handleLogout();
    	}
    }
    /* �����¼��Ϣ*/
    public void handleLogin(Message msg)throws Exception{
    	Customer loginCustomer = (Customer)msg.getContent();
    	String account = loginCustomer.getAccount();
    	String password = loginCustomer.getPassword();
    	Customer cus = FileOpe.getCustomerByAccount(account);
        Message newMsg = new Message();
        if(cus == null|| !cus.getPassword().equals(password)) {
        	newMsg.setType(Conf.LOGINFAIL);
        	oos.writeObject(newMsg);     //������¼�û�
        	canRun = false;
        	socket.close();
        	return;
        }
        this.customer = cus;
        /* �����̷߳���clients����*/
        server.getClients().add(this);
        /* ��customer���뵽userList��*/
        server.getUserList().add(this.customer);
        /* ע�⣬Ӧ���ǽ����е������û���Ҫת�����ͻ���*/
        newMsg.setType(Conf.USERLIST);
        newMsg.setContent(server.getUserList().clone());
        //���û���¼����Ϣ�������пͻ�
        this.sendMessage(newMsg, Conf.ALL);
        server.setTitle("��ǰ���ߣ�"+server.getClients().size()+"��");
    }
    /*��msg�����������������Ϣ��ʽת��*/
    public void handleRegister(Message msg) throws Exception{
    	Customer registerCustomer = (Customer)msg.getContent();
    	String account = registerCustomer.getAccount();
    	Customer cus = FileOpe.getCustomerByAccount(account);
    	Message newMsg = new Message();
    	if(cus!=null) {
    		newMsg.setType(Conf.REGISTERFAIL);
    	}else {
    		String password = registerCustomer.getPassword();
    		String name = registerCustomer.getName();
    		String dept = registerCustomer.getDept();
    		FileOpe.insertCustomer(account,password,name ,dept);
    		newMsg.setType(Conf.REGISTERSUCCESS);
    		oos.writeObject(newMsg);           //����ע���û�
    	}
    	oos.writeObject(newMsg);               //����ע���û�
        canRun = false;
        socket.close();
    }
    /*��msg�����������������Ϣ��ʽת��*/
    public void handleMessage(Message msg) throws Exception{
        String to = msg.getTo();
        sendMessage(msg, to);
    }
    /*�����������ͻ��˷���һ���ÿͻ������ߵ���Ϣ*/
    public void handleLogout() {
    	Message logoutMessage = new Message();
    	logoutMessage.setType(Conf.LOGOUT);
    	logoutMessage.setContent(this.customer);
    	server.getClients().remove(this);         //�����Լ���clients��ȥ��
    	server.getUserList().remove(this.customer);
    	try {
    		sendMessage(logoutMessage, Conf.ALL);
    		canRun = false;
    		socket.close();
    	}catch(Exception ex) {
    		ex.printStackTrace();
    	}
    	server.setTitle("��ǰ����:"+server.getClients().size()+"��");
    }

	/*����Ϣ����ĳ���ͻ���*/
    public void sendMessage(Message msg,String to)throws Exception{
    	for (ChatThread ct:server.getClients()) {
    		if(ct.customer.getAccount().equals(to)||to.equals(Conf.ALL)) {
    			ct.oos.writeObject(msg);
    		}
    	}
    }
}
