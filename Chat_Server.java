import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Vector;

public class Chat_Server {
	// the server is only one object to run all clients
    // one to listen
	private static ServerSocket serverSocket = null;
	// one to connect to client
	private static Socket clientSocket = null;
	// to keep all client threads
	private static Vector<ServerThread> threads = new Vector<ServerThread>();

	public static void main(String args[]) {
		int portNumber = 5000;
		System.out.println("The ChatServer is running.\n"
				+ "Now using port number=" + portNumber);

		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			System.out.println(e);
		}

		while (true) {
			try {
				// the server socket has accepted the connection request 
				clientSocket = serverSocket.accept();
				// accept method betedy ioException
				// a class of type server thread
				ServerThread current = new ServerThread(clientSocket, threads);
				threads.add(current);
				current.start();
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}

class ServerThread extends Thread {

	private String clientName = null;
	private BufferedReader is = null;
	private PrintWriter os = null;
	private Socket clientSocket = null;
	private final Vector<ServerThread> threads;

	public ServerThread(Socket clientSocket, Vector<ServerThread> threads2) {
		this.clientSocket = clientSocket;
		this.threads = threads2;
	}

	public void run() {
		Vector<ServerThread> threads = this.threads;

		try {
			is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			os = new PrintWriter(clientSocket.getOutputStream(), true);
			String name;

			os.println("Enter your name:");
			name = is.readLine().trim();
			os.println("Welcome "
					+ name
					+ " to our chat room.\nTo leave enter QUIT in a new line."
					+ "\nTo view a list containing the members in the room enter MEMBER_LIST_REQUEST.");
			synchronized (this) {
				for (int i = 0; i < threads.size(); i++) {
					if (threads.get(i) != null && threads.get(i) == this) {
						clientName = "@" + name;
						break;
					}
				}
				for (int i = 0; i < threads.size(); i++) {
					if (threads.get(i) != null && threads.get(i) != this) {
						threads.get(i).os.println("*** A new user " + name
								+ " entered the chat room !!! ***");
					}
				}
			}
			while (true) {
				String line = is.readLine();
				
				if (line.startsWith("QUIT")) {
					break;
				}
				
				if (line.endsWith("MEMBER_LIST_REQUEST")) {
					synchronized (this) {
						String names = "";
						for (int i = 0; i < threads.size(); i++) {
							if (threads.get(i) != null) {
								// not terminated abruptly
								if (threads.get(i).isAlive()) {
									names += "  " + threads.get(i).clientName;
								} else {
									String name2 = threads.get(i).clientName;
									
									for (int j = 0; j < threads.size(); j++) {
										
									if (threads.get(j) != null && !threads.get(j).clientName.equals(name2)&& threads.get(j).clientName != null) {
											threads.get(j).os.println("***Unfortunately, the user "
															+ name2
															+ " lost the connection to the chat room !!! ***");
										}
									}
									threads.remove(i--);
								}
							}
						}
						this.os.println("MEMBER_LIST_RESPONSE " + names);
					}
				} else {
					if(line.contains("LOL")){
						String[] spliter = line.split(" ");
					    line = "";
						for(int i=0; i<spliter.length; i++){
							if(spliter[i].equals("LOL")){
								spliter[i] = "Laugh Out Loud";
							}
						}
						for(int i=0; i<spliter.length;i++){
							line += spliter[i] + " ";
						}
					}else{
					synchronized (this) {
						for (int i = 0; i < threads.size(); i++) {
							if (threads.get(i) != null && threads.get(i).clientName != null) {
								threads.get(i).os.println(line);
							}
						}
					}
				}
			}
			}
			synchronized (this) {
				for (int i = 0; i < threads.size(); i++) {
					if (threads.get(i) != null && threads.get(i) != this && threads.get(i).clientName != null) {
						threads.get(i).os.println("*** The user " + name
								+ " is leaving the chat room !!! ***");
					}
				}
			}
			synchronized (this) {
				for (int i = 0; i < threads.size(); i++) {
					if (threads.get(i) == this) {
						threads.remove(i);
					}
				}
			}
			is.close();
			os.close();
			clientSocket.close();
		} catch (IOException e) {
		}
	}
}