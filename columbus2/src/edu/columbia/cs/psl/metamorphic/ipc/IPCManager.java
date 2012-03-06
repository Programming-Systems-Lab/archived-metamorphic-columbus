package edu.columbia.cs.psl.metamorphic.ipc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import edu.columbia.cs.psl.metamorphic.struct.MethodInvocation;

public class IPCManager implements Runnable {
	public boolean isChild = false;
	private static IPCManager instance;
	public Thread serverThread;
	public void stop()
	{
		try {
			server.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static IPCManager getInstance()
	{
		if(instance == null)
		{
			instance = new IPCManager();
		}
		return instance;
	}
	private ServerSocket server;
	public IPCManager() {
		try
		{
			server = new ServerSocket();
			server.bind(null);
			serverThread = new Thread(this);
			serverThread.setDaemon(true);
			serverThread.start();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	public Socket getAClientSocket() throws UnknownHostException, IOException
	{
		Socket ret = new Socket();
		ret.connect(new InetSocketAddress("127.0.0.1", getPort()));
		return ret;
	}
	public void sendToParent(Object obj,Socket clientSocket) throws IllegalAccessError, IOException
	{
		if(!isChild)
			throw new IllegalAccessError("Only the child process can send to the parent");
		ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
		oos.writeObject(obj);
		oos.close();
		clientSocket.close();
	}
	public int getPort()
	{
		return server.getLocalPort();
	}
	public int registerInvocation(MethodInvocation inv)
	{
		int ret;
		synchronized (invocationID) {
			ret = invocationID;
			invocationID++;
		}
		pendingInvocations.put(ret, inv);
		return ret;
	}
	private Integer invocationID = 1;
	private HashMap<Integer, MethodInvocation> pendingInvocations = new HashMap<Integer, MethodInvocation>();
	
	@Override
	public void run() {
		while(true)
		{
			try
			{
				Socket sock = server.accept();
				ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
				MethodInvocation rx  = (MethodInvocation) ois.readObject();
				MethodInvocation pa = pendingInvocations.remove(rx.childRemoteId);
				synchronized (pa) {
					pa.childReturnValue=rx.childReturnValue;
					pa.childThrownExceptions=rx.childThrownExceptions;
					pa.childRemoteId=0;
					pa.notifyAll();
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
}
