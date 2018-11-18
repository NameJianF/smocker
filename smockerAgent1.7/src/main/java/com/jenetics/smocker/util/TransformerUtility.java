package com.jenetics.smocker.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.jenetics.smocker.util.io.TeeInputStream;
import com.jenetics.smocker.util.io.TeeOutputStream;
import com.jenetics.smocker.util.network.RemoteServerChecker;
import com.jenetics.smocker.util.network.ResponseReader;
import com.jenetics.smocker.util.network.RestClientSmocker;

public class TransformerUtility {
	private static String callerApp = null;
	private static Long javaAppId = null;

	private static Hashtable<Object, SmockerContainer> smockerContainerBySocket = new Hashtable<Object, SmockerContainer>();
	private static Hashtable<Object, Long> connectionIdBySocket = new Hashtable<Object, Long>();

	private TransformerUtility() {
		super();
	}

	public static Hashtable<Object, Long> getConnectionIdBySocket() {
		return connectionIdBySocket;
	}

	public static Long getJavaAppId() {
		return javaAppId;
	}

	public static String getCallerApp() {
		if (callerApp == null) {
			callerApp = "unknown";
			Map<Thread,StackTraceElement[]> stackTraceMap = Thread.getAllStackTraces();
			for (Thread t : stackTraceMap.keySet())
			{
				if ("main".equals(t.getName()))
				{
					StackTraceElement[] mainStackTrace = stackTraceMap.get(t);
					callerApp = mainStackTrace[mainStackTrace.length - 1].getClassName();
				}
			}
		}
		return callerApp;
	}

	public synchronized static int socketChannelWrite (SocketChannel socketChannel, ByteBuffer buffer) 
			throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if (socketChannel != null && !filterSmockerBehavior() && RemoteServerChecker.isRemoteServerAlive()) {
			int write = buffer.capacity();
			
			ByteBuffer bufferDup = buffer.duplicate();
			byte[] array = new byte[bufferDup.capacity()];
			bufferDup.get(array);
			
			SmockerContainer smockerContainer = smockerContainerBySocket.get(socketChannel);
			if (smockerContainer == null) {
				InetSocketAddress remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
				smockerContainer = addSmockerContainer(socketChannel, remoteAddress.getHostName(), remoteAddress.getPort());
				smockerContainer.resetAll();
			}
			if (smockerContainer.isResetMatchNextWrite()) {
				smockerContainer.resetAll();
				smockerContainer.setResetMatchNextWrite(false);
			}
			
			if (smockerContainer.isPostAtNextWrite()) {
				smockerContainer.postCommunication();
				smockerContainer.resetAll();
				smockerContainer.setPostAtNextWrite(false);
			}
			if (!smockerContainer.isApplyMock()) {
				write = writeSocketChannel(buffer, 	socketChannel);
			}
			
			
			smockerContainer.getSmockerSocketOutputStream().write(array);
			return write;
		}
		else {
			return writeSocketChannel(buffer, socketChannel);
		}
	}

	public synchronized static int socketChannelRead (SocketChannel socketChannel,  ByteBuffer bytebuffer) 
			throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		SmockerContainer smockerContainer = smockerContainerBySocket.get(socketChannel);
		
		if (smockerContainer != null && socketChannel != null && !filterSmockerBehavior() && RemoteServerChecker.isRemoteServerAlive()) {
			if (smockerContainer.isApplyMock()) {
				String matchMock = smockerContainer.getMatchMock();
				//match applied
				if (matchMock != null) {
					smockerContainer.setResetMatchNextWrite(true);
					int index = smockerContainer.getIndexForArrayCopy();
					byte[] bytesMock = matchMock.getBytes();
					byte[] targetBytes = new byte[bytebuffer.limit()];
					int lengthToCopy = Math.min(bytebuffer.limit(), bytesMock.length - index);
					System.arraycopy(bytesMock, index, targetBytes, 0, lengthToCopy);
					smockerContainer.setIndexForArrayCopy(index + lengthToCopy);
					bytebuffer.clear();
					bytebuffer.put(targetBytes);
					if (bytesMock.length - index <= bytebuffer.limit()) {
						return -1;
					}
					else {
						return lengthToCopy;
					}
				}
				else if (!smockerContainer.isStreamResent()) {
					ByteBuffer buffer = ByteBuffer.wrap(smockerContainer.getSmockerSocketOutputStream().getBytes());
					writeSocketChannel(buffer, socketChannel);
					smockerContainer.setStreamResent(true);
				}
				return readSocketChannel(bytebuffer, socketChannel);
			}
			smockerContainer.setApplyMock(false);
			int readen = readSocketChannel(bytebuffer, socketChannel);
			smockerContainer.getSmockerSocketInputStream().write(bytebuffer.array());
			
			smockerContainer.setPostAtNextWrite(true);
			return readen;
		}
		if (bytebuffer != null && socketChannel != null) {
			return readSocketChannel(bytebuffer, socketChannel);
		}
		return -1;

	}
	

	private static void managePostCommunication(ByteBuffer bytebuffer, SmockerContainer smockerContainer, int readen)
			throws IOException {
		if (smockerContainer.getSmockerSocketInputStream() == null) {
			smockerContainer.resetSmockerSocketInputStream();
		}
		smockerContainer.getSmockerSocketInputStream().write(bytebuffer.array());
		if (readen == -1) {
			smockerContainer.setOutputToBesend(
					smockerContainer.getSmockerSocketOutputStream().getSmockerOutputStreamData().getString());
			smockerContainer.postCommunication();
			smockerContainer.resetAll();
		}
	}


	private static int readSocketChannel(ByteBuffer buffer, SocketChannel socketChannel) 
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method readNew = socketChannel.getClass().
				getMethod("readNew", new Class[] {java.nio.ByteBuffer.class});
		return (int) readNew.invoke(socketChannel, new Object[] {buffer});

	}

	private static int writeSocketChannel(ByteBuffer buffer, SocketChannel socketChannel) 
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method writeNew = socketChannel.getClass().
				getMethod("writeNew", new Class[] {java.nio.ByteBuffer.class});
		return (int) writeNew.invoke(socketChannel, new Object[] {buffer});
	}

	public synchronized static void socketChannelClosed (Object o) throws IOException {
		socketClosed(o);
	}

	public synchronized static InputStream manageInputStream(InputStream is, Socket source) throws IOException {
		if (!filterSmockerBehavior() && RemoteServerChecker.isRemoteServerAlive()) {
			if (!smockerContainerBySocket.containsKey(source)) {
				addSmockerContainer(source, source.getInetAddress().getHostName(), source.getPort());
			}
			SmockerContainer smockerContainer = smockerContainerBySocket.get(source);
			if (smockerContainer.getSmockerSocketInputStream() == null) {
				smockerContainer.resetSmockerSocketInputStream();
				TeeInputStream teeInputStream = new TeeInputStream(is, smockerContainer.getSmockerSocketInputStream(), true, smockerContainer);
				smockerContainer.setTeeInputStream(teeInputStream);
			}
			return smockerContainer.getTeeInputStream();
		}
		return is;
	}

	public synchronized static OutputStream manageOutputStream(OutputStream os, Socket source) throws IOException {
		if (!filterSmockerBehavior() && RemoteServerChecker.isRemoteServerAlive()) {
			SmockerContainer container = null;
			if (!smockerContainerBySocket.containsKey(source)) {
				container = addSmockerContainer(source, source.getInetAddress().getHostName(),
						source.getPort());
			}
			else {
				container = smockerContainerBySocket.get(source);
			}
			container.resetSmockerSocketOutputStream();
			TeeOutputStream teeOutputStream = new TeeOutputStream(os, container.getSmockerSocketOutputStream());
			container.setTeeOutputStream(teeOutputStream);

			if (container.isApplyMock()) {
				teeOutputStream.setForwardToRealStream(false);
			}
			
			return container.getTeeOutputStream();
		}
		return os;
	}

	public synchronized static void socketClosed(Object source) throws UnsupportedEncodingException {
		if (!filterSmockerBehavior()) {
			if (smockerContainerBySocket.get(source) != null) {
				smockerContainerBySocket.get(source).postCommunication();
			}
			smockerContainerBySocket.remove(source);
		}
	}


	private static SmockerContainer addSmockerContainer(Object source, String host, int port) throws IOException {
		String stackTrace = getStackTrace();
		SmockerContainer smockerContainer = new SmockerContainer(host,port, stackTrace, source);
		String allResponse = RestClientSmocker.getInstance().getAll();
		String existingId = ResponseReader.findExistingAppId(allResponse);
		// only if the javaAppId was not found
		if ((javaAppId == null || existingId == null) && allResponse != null) {
			if (existingId != null) {
				javaAppId = Long.valueOf(existingId);
			} else {
				String response = RestClientSmocker.getInstance().postJavaApp();
				updateJavaAppId(response);
			}
		}
		if (javaAppId != null) {
			String response = RestClientSmocker.getInstance().postConnection(smockerContainer, javaAppId);
			// check the status
			String status = ResponseReader.readStatusCodeFromResponse(response);
			if (status.equals(ResponseReader.CONFLICT)) {
				allResponse = RestClientSmocker.getInstance().getAll();
				String existingConnectionId = ResponseReader.findExistingConnectionId(allResponse,
						smockerContainer.getHost(), smockerContainer.getPort());
				connectionIdBySocket.put(source, Long.valueOf(existingConnectionId));
			} else {
				String idConnection = ResponseReader.readValueFromResponse(response, "id");
				if (idConnection != null) {
					connectionIdBySocket.put(source, Long.valueOf(idConnection));
				}
			}
		}

		smockerContainerBySocket.put(source, smockerContainer);
		return smockerContainer;
	}

	private synchronized static void updateJavaAppId(String response) throws IOException {
		String id = ResponseReader.readValueFromResponse(response, "id");
		if (id != null) {
			javaAppId = Long.parseLong(id);
		}
	}

	private static String getStackTrace() {
		StringBuilder sb = new StringBuilder();
		String[] stackTraceAsArray = getStackTraceAsArray();
		for (int i = 0; i < stackTraceAsArray.length; i++) {
			sb.append(stackTraceAsArray[i]);
			if (i < stackTraceAsArray.length - 1) {
				sb.append(System.getProperty("line.separator"));
			}
		}
		return sb.toString();
	}

	public static String[] getStackTraceAsArray() {
		List<String> arrRet = new ArrayList<String>();
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			arrRet.add(ste.toString());
		}
		String[] ret = new String[arrRet.size()];
		ret = arrRet.toArray(ret);
		return ret;
	}

	public static boolean filterSmockerBehavior() {
		return inSocketFromSSL() 
				|| inStack("com.jenetics.smocker.util.network.RestClientSmocker")
				|| inStack("com.jenetics.smocker.util.network.RestClientAdminChecker")
				|| inStack("com.jenetics.smocker.util.network.SmockerServer$ClientTask")
				|| inStack("com.jenetics.smocker.util.network.RemoteServerChecker");
	}

	protected static boolean inStack(String className) {
		StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		for (int i = 1; i < stackTrace.length; i++) {
			if (stackTrace[i].getClassName().equals(className)) {
				return true;
			}
		}
		return false;
	}

	protected static boolean inSocketFromSSL() {
		StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		// we are in socket coming from SSLSocket socket
		boolean socketTrace = false;
		for (int i = 1; i < stackTrace.length; i++) {
			if (!socketTrace) {
				socketTrace = stackTrace[i].getClassName().equals("java.net.Socket");
			}
			if (!socketTrace && stackTrace[i].getClassName().equals("sun.security.ssl.SSLSocketImpl")) {
				return false;
			} else if (socketTrace && stackTrace[i].getClassName().equals("sun.security.ssl.SSLSocketImpl")) {
				return true;
			}
		}
		return false;
	}
}
