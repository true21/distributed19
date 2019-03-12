package threads;

import beans.Message;
import beans.MessageType;
import entities.NodeMiner;
import entities.Transaction;

import javax.sound.sampled.LineEvent;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner;
import java.security.*;

public class ClientThread extends Thread {

	private static InetAddress ipAddress;
	private static int index;
	private static Socket socket;
	private static int port;
	public static final int n = 2;

	public ClientThread(InetAddress i, int c, Socket sock, int prt) {
		ipAddress = i;
		index = c;
		socket = sock;
		port = prt;
	}

	// args[0] is index of client
	public static void main(String[] args) throws IOException {
		try{
			index = Integer.parseInt(args[0]);

			// get all nodes ids
			ArrayList<Node> nodes = new ArrayList<Node>();
			ServerSocket ss = new ServerSocket(7070 + index);
			System.out.println("Listening1 to " + 7070 + index);
      Socket s_nodeId = ss.accept();
			System.out.println("Accepting2");
			ObjectInputStream ois = new ObjectInputStream(s_nodeId.getInputStream());
			Node temp_node;
			for (int i=0; i<n; i++) {
				temp_node = (Node) ois.readObject();
				nodes.add(temp_node);
			}
			//System.out.println("hey with listsize " + nodes.size());
			s_nodeId.close();
			// check if you can connect to all servers
			for (int i=0; i<nodes.size(); i++) {
				System.out.println("Connecting3 to " + nodes.get(i).getPort());
				Socket s_check = new Socket(nodes.get(i).getIP(), nodes.get(i).getPort());
				System.out.println("Connected4");
			}
			// now we're good to go

			// socket arguments of client's server
			InetAddress ip = nodes.get(index).getIP();
			int port = nodes.get(index).getPort();



			// send 100 noobcash coins to others if Bootstrap
			if (index == 0) {
				String tr100 = "t ";
				for (int i=1; i<nodes.size(); i++) {
					tr100 += i + " 100";
					Message mes = new Message(tr100);
					System.out.println("Connecting5 to " + port);
					Socket s100 = new Socket(ip, port);
					System.out.println("Connected6");
					ObjectOutputStream oos = new ObjectOutputStream(s100.getOutputStream());
					oos.writeObject(mes);
					//oos.close();
				}
				// say to others client's that they're ready to go
				for (int i=1; i<nodes.size(); i++) {
					System.out.println("Connecting7 to " + 7070 + i);
					Socket s_ready = new Socket(nodes.get(i).getIP(), 7070 + i);
					System.out.println("Connected8");
				}
			}
			else {

				ServerSocket ss_readyy = new ServerSocket(7070 + index);
				System.out.println("Listening9 to " + 7070 + index);
				// be notified when you're ready to go
	      Socket s_readyy = ss_readyy.accept();
				System.out.println("Accepting10");
			}

			// get input from console
			Scanner scanner = new Scanner(System.in);
/*
			// file
			String file = "../../resources/transactions/" + n + "nodes/transactions" + index + ".txt";
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
  		while ((line = br.readLine()) != null) {
				Socket s = new Socket(ip, port);
				ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				String toSend = "t " + line.replace("id", "");
				Message message = new Message(toSend);
				oos.writeObject(message);
  		}
*/
			// cli
			while (true) {
				System.out.println("Connecting21 to " + port);
				Socket s = new Socket(ip, port);
				System.out.println("Connected22");
				ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				String sendToServer = new String("");
				String help = "explain stuff";
				String error = "Bad syntax.";
				// read line
				String command = scanner.nextLine();
				List<String> cmd_args = Arrays.asList(command.trim().split("\\s+"));
				System.out.println("gave " + cmd_args.get(0));
				if (cmd_args.size() == 1) {
					if (cmd_args.get(0).equals("view")) {
						sendToServer = "view";
					}
					else if (cmd_args.get(0).equals("balance")) {
						sendToServer = "balance";
					}
					else if (cmd_args.get(0).equals("help")) {
						sendToServer = "help";
					}
				}
				else if (cmd_args.size() == 3) {
					if (cmd_args.get(0).equals("t")) {
						sendToServer = cmd_args.get(0) + " " + cmd_args.get(1) + " " + cmd_args.get(2);
					}
				}
				if (sendToServer.equals("")) {
					System.out.println(error);
					System.out.println(help);
				}
				else if (sendToServer.equals("help")) {
					System.out.println(help);
				}
				else {
					Message message = new Message(sendToServer);
					oos.writeObject(message);
					ois = new ObjectInputStream(s.getInputStream());
					if (sendToServer.equals("balance")) {
						// balance
						float balance = ois.readFloat();
						System.out.println("Your wallet's balance is: " + balance + " noobcash coins.");
						s.close();
					}
					else if (sendToServer.equals("view")) {
						// view
						String view = (String) ois.readObject();
						System.out.println("The last blocks' transactions are:" + view);
						s.close();
					}
					else {
						// transaction
						String transMsg = (String) ois.readObject();
						System.out.println(transMsg);
						s.close();
					}
				}
			}
		}  catch (Exception e) { e.printStackTrace();}

  }

  /**
   * Add data to sent to a Message object
   * @return the Message
   */
  private Message createMessage() {
  	return null;
  }

  @Override
  public void run() {

  }

}
