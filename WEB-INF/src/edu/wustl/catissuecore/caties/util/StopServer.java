
package edu.wustl.catissuecore.caties.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import edu.wustl.common.util.logger.Logger;

/**
 * Common class which is a thread to stop caties servers depending on there port.
 * @author vijay_pande
 */
public class StopServer extends Thread
{

	/**
	 * logger.
	 */
	private transient final Logger logger = Logger.getCommonLogger(StopServer.class);
	/**
	 * port.
	 */
	private final String port;

	/**
	 * Constructor for the class.
	 * @param port key to fetch port number from catissueCore-properties.xml file
	 */
	public StopServer(String port)
	{
		this.port = port;
	}

	/**
	 * Default method for the thread, which listen to socket and stop current thread when recieve any responce.
	 */
	@Override
	public void run()
	{
		try
		{
			// get port number for server from catissueCore-properties.xml file
			final int port = Integer.parseInt(CaTIESProperties.getValue(this.port));
			final ServerSocket serv = new ServerSocket(port);
			BufferedReader r;
			final Socket sock = serv.accept();
			r = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			new PrintWriter(new OutputStreamWriter(sock.getOutputStream()), true);
			r.readLine();

			this.logger.info("Stopping server");
			r.close();
			sock.close();
			// close server socket
			serv.close();
			// stop server
			System.exit(0);
		}
		catch (final Exception e)
		{
			this.logger.error("Error stopping server "+e.getMessage(), e);
			e.printStackTrace();
		}
	}
}
