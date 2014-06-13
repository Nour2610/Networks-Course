import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Chat_Client {
// define instance attributes
	private Socket clientSocket = null;
	private String name = "";
	private PrintWriter os = null;
	private BufferedReader is = null;
	private BufferedReader inputLine = null;
	private boolean closed = false;
	private final static int portNumber = 5000;
	private final static String host = "localhost";

//constructor
	public Chat_Client() {

	}

	public Chat_Client(Socket socket) throws IOException {
		clientSocket = socket;
		inputLine = new BufferedReader(new InputStreamReader(System.in));
		os = new PrintWriter(clientSocket.getOutputStream(), true);
		is = new BufferedReader(new InputStreamReader(
				clientSocket.getInputStream()));
		// Socket.getout/inputStream() throws IOException
	}

	public static void main(String[] args) {
		System.out.println("The class ChatClient is running.\n"
				+ "Now using host=" + host + ", portNumber=" + portNumber);
		try {
			//instantiate an instance
			Chat_Client temp = new Chat_Client(new Socket(host, portNumber));
			// new Socket(host, portnumber) throws UnknownHostException,
			// IOException
			if (temp.clientSocket != null && temp.os != null && temp.is != null) {
				// new Thread(temp).start();
				temp.run();
			}
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + host);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to the host "
							+ host);
		}

	}

	public void run() {
		// the run starts the process of reading and writing
		String responseLine; // input stream from the server
		try {
			// the server starts by asking you to provide the name , u take it from there
			System.out.println(is.readLine());
			name = inputLine.readLine().trim();
			os.println(name);
			while (!closed) {
				if (is.ready()) {
					responseLine = is.readLine();
					// response of server to client request of member list
					if (responseLine.startsWith("MEMBER_LIST_RESPONSE")) {
						System.out.println("# The members in the chating room are :");
						System.out.println(responseLine.substring(20));
					} else {
						System.out.println(responseLine);
					}
				}
				if (inputLine.ready()) {
					String msg = inputLine.readLine();
					if (msg != null) {
						msg = msg.trim();
						if (msg.equals("MEMBER_LIST_REQUEST")) {
							os.println(msg);
						} else if (msg.compareToIgnoreCase("QUIT") != 0)
							os.println("<" + name + "> : " + msg);
						else {
							os.println("QUIT " + name);
							closed = true;
							break;
						}
					}
				}
			}
			os.close();
			is.close();
			clientSocket.close();
			// Socket.close() throws ioException
		} catch (IOException e) {
			System.err.println("IOException:  " + e);
		}
	}
}
