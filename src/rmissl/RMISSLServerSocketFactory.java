package rmissl;

import java.io.*;
import java.net.*;
import java.rmi.server.*;
import javax.net.ssl.*;

public class RMISSLServerSocketFactory implements RMIServerSocketFactory {

	/*
	 * Create one SSLServerSocketFactory, so we can reuse sessions created by
	 * previous sessions of this SSLContext.
	 */
	
	public ServerSocket createServerSocket(int port) throws IOException {
		SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(port);
		serverSocket.setNeedClientAuth(true);
		return serverSocket;
	}
}