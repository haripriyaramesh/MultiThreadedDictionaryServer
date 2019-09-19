
// Java implementation of  Server side 
// It contains two classes : Server and ClientHandler 

import java.io.*;
import java.net.*;

public class DictionaryServer {

	static int count = 0;

	public static void main(String[] args) throws IOException {

		try {
			int socket = Integer.parseInt(args[0]);
			@SuppressWarnings("resource")
			ServerSocket ss = new ServerSocket(socket);
			String filepath = args[1];
			System.out.println("SERVER ONLINE");
			// running infinite loop for getting
			// client request
			while (true) {
				Socket s = null;

				// socket object to receive incoming client requests
				s = ss.accept();

				System.out.println("A new client is connected- client " + ++count + ": " + s);

				// obtaining input and out streams
				DataInputStream dis = new DataInputStream(s.getInputStream());
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());

				System.out.println("Assigning new thread for client " + count);

				// create a new thread object
				Thread t = new ClientHandler(s, dis, dos, filepath, count);
				// Invoking the start() method
				t.start();

			}
		} catch (BindException e) {
			System.out.println("The requested port may be in use by another process");

		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("invalid number of parameters provided");

		} catch (NumberFormatException e) {
			System.out.println("Invalid port number");

		} catch (InterruptedIOException e) {
			System.out.println("InterruptedIOException");

		} catch (SecurityException e) {
			System.out.println("Security Exception");

		} catch (IOException e) {
			System.out.println("Server Error " + e.getMessage());

		}
	}

}
