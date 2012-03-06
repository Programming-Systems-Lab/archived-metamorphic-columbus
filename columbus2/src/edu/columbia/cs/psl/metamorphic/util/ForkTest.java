package edu.columbia.cs.psl.metamorphic.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class ForkTest extends SimpleChannelHandler {

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		ChannelBuffer buf = (ChannelBuffer) e.getMessage();
//		ByteBuffer bufIn = ByteBuffer.allocate(48);
		try {
//			ByteBuffer b = buf.toByteBuffer();
			System.out.println(buf.readableBytes());
//			ObjectInputStream ois = new ObjectInputStream(bin);

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		e.getCause().printStackTrace();

		Channel ch = e.getChannel();
		ch.close();
	}

	public static void main(String[] args) {
		ForkTest test = new ForkTest();
		// test.go();
		try {
			test.loop();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void makeServer() {
		final ForkTest parent = this;
		ChannelFactory factory = new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());

		ServerBootstrap bootstrap = new ServerBootstrap(factory);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			@Override
			public ChannelPipeline getPipeline() throws Exception {
				// TODO Auto-generated method stub
				return org.jboss.netty.channel.Channels.pipeline(parent);
			}
		});

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		bootstrap.bind(new InetSocketAddress(4356));
	}

	private int flag = 0;

	public void loop() throws Exception {
		System.out.println("parent " + flag);
		makeServer();
		int pid = Forker.fork();

		if (pid == 0) {
			flag = 3;
			System.out.println("child " + flag);
			SocketChannel channel = null;
			try {
				channel = SocketChannel.open();
				System.out.println("Connecting");
				channel.connect(new InetSocketAddress("localhost", 4356));
				System.out.println("Connected");
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			flag = 10;
			try {
				// Thread.currentThread().sleep(100);
				String newData = "New String to write to file..."
						+ System.currentTimeMillis();

				ByteBuffer buf = ByteBuffer.allocate(480);
				buf.clear();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				oos.writeObject(newData);
				buf.put(bos.toByteArray());

				buf.flip();

				while (buf.hasRemaining()) {
					channel.write(buf);
				}
				channel.close();
				System.out.println("Closed channel");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Forker.exit();
		} else {
			try {
				// SocketChannel channel = SocketChannel.open();
				// channel.connect(new InetSocketAddress("localhost", 4356));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			System.out.println("Parent pid: " + pid);
			flag = 1;
			System.out.println("Set flag");
			// try {
			// Thread.currentThread().sleep(0);
			// } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}

	}

	public void go() {
		try {
			Forker forker = new Forker();

			// do the fork
			int pid = forker.fork();

			// System.out.println(pid);
			if (pid == 0) {
				try {
					// this is the child
					System.out.println("CHILD");

					PrintWriter out = new PrintWriter(new File("foo.txt"));
					System.out.println("Created PrintWriter");

					out.println("Hello world!!!");
					out.flush();
					System.out.println("done writing");

					/*
					 * Socket connect = new Socket("localhost", 1234);
					 * System.out.println("created a socket");
					 * 
					 * // get the input stream Scanner in = new
					 * Scanner(connect.getInputStream()); // get the output
					 * stream PrintWriter out = new
					 * PrintWriter(connect.getOutputStream());
					 * 
					 * out.write("5\n"); out.flush();
					 * System.out.println("wrote");
					 * 
					 * System.out.println(in.nextLong());
					 * 
					 * out.close();
					 */

				} finally {
					System.out.println("About to call EXIT");
					forker.exit();
					System.exit(0);
					System.out.println("Still... alive");
				}
			} else {
				System.out.println("PARENT");
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		try {
			Thread.sleep(1000);
		} catch (Exception e) {
		}

	}
}
