package com.jenetics.smocker.util.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.jenetics.smocker.configuration.MemoryConfiguration;
import com.jenetics.smocker.configuration.SystemPropertyConfiguration;
import com.jenetics.smocker.util.MessageLogger;

public class SmockerServer {
	
	private ServerSocket serverSocket = null;

    public void startServer() {
    	if (serverSocket != null) {
    		MessageLogger.logError("Already started", SmockerServer.class);
    		return;
    	}
    	final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);
        Runnable serverTask = new Runnable() {

            public void run() {
                try {
                    serverSocket = new ServerSocket(SystemPropertyConfiguration.getCommPort());
                    MessageLogger.logMessage("Waiting for clients to connect...", SmockerServer.class);
                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        clientProcessingPool.submit(new ClientTask(clientSocket));
                    }
                } catch (IOException e) {
                	MessageLogger.logErrorWithMessage("Unable to process client request", e, SmockerServer.class);
                }
            }
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();

    }
    
    //close the socket
    public void release() {
    	if (serverSocket != null) {
    		try {
				serverSocket.close();
			} catch (IOException e) {
				MessageLogger.logErrorWithMessage("Unable to close server socket", e, SmockerServer.class);
			}
    	}
    }

    private class ClientTask implements Runnable {
        private final Socket clientSocket;

        private ClientTask(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            
            // Do whatever required to process the client's request

            try {
                //System.out.println("Got a client !");
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                String line = in.readLine();
                manageAction(line);
    			out.println("OK");
    			clientSocket.close();
            } catch (IOException e) {
            	MessageLogger.logErrorWithMessage("Unable to process client request", e, SmockerServer.class);
            }
        }

		private void manageAction(String line) {
			if (line.startsWith("WATCH")) {
				String connection = line.substring(6);
				String[] connectionsItem = connection.split(MemoryConfiguration.SEP_CONNECTION);
				if (connectionsItem.length != 2) {
					MessageLogger.logError("Bad format of WATCH message", SmockerServer.class);
					return;
				}
				MemoryConfiguration.setConnecctionWatched(connectionsItem[0], Integer.parseInt(connectionsItem[1]));
			} 
			if (line.startsWith("MUTE")) {
				String connection = line.substring(5);
				String[] connectionsItem = connection.split(MemoryConfiguration.SEP_CONNECTION);
				if (connectionsItem.length != 2) {
					MessageLogger.logError("Bad format of MUTE message", SmockerServer.class);
					return;
				}
				MemoryConfiguration.setConnecctionMute(connectionsItem[0], Integer.parseInt(connectionsItem[1]));
			} 
			if (line.startsWith("MODE")) {
				String mode = line.substring(5);
				MemoryConfiguration.switchMode();
			} 
		}
    }

}