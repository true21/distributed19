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
	public static final int n = 5;

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
      Socket s_nodeId = ss.accept();
			ObjectInputStream ois = new ObjectInputStream(s_nodeId.getInputStream());
			Node temp_node;
			for (int i=0; i<n; i++) {
				temp_node = (Node) ois.readObject();
				nodes.add(temp_node);
			}
			s_nodeId.close();
			// check if you can connect to all servers
			for (int i=0; i<nodes.size(); i++) {
				Socket s_check = new Socket(nodes.get(i).getIP(), nodes.get(i).getPort());
			}
			// now we're good to go
			// socket
			InetAddress ip = InetAddress.getByName(args[0]);
			Socket s = new Socket(ip, 9090 + index);
			DataInputStream dis = new DataInputStream(s.getInputStream());
      DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			// get input from console
			Scanner scanner = new Scanner(System.in);

			// file
			String file = "../../resources/transactions/" + n + "nodes/transactions" + index + ".txt";
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
  		while ((line = br.readLine()) != null) {
				dos.writeUTF("t " + line.replace("id", ""));
				String received = dis.readUTF();
				System.out.println(received);
  		}

			// cli
			while (true) {
				String sendToServer = new String("");
				String help = "explain stuff";
				String error = "Bad syntax.";
				// read line
				String command = scanner.nextLine();
				List<String> cmd_args = Arrays.asList(command.trim().split("\\s+"));
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
					if (cmd_args.size() == 3) {
						int rec_id = Integer.parseInt(cmd_args.get(1).replace("ind", ""));
						float value = Float.parseFloat(cmd_args.get(2));
						PublicKey sender_pk = nodes.get(index).getPublicKey();
						PublicKey receiver_pk = nodes.get(rec_id).getPublicKey();
						// we've got issue with last argument of constructor here
						Transaction transaction = new Transaction(sender_pk, receiver_pk, value, null);
						Message message = new Message("transaction", transaction);
						//message.setType("transaction");
						//message.setTransaction(transaction);
					}
					else {
						//message.setType(cmd_args.get(0));
						Message message = new Message(cmd_args.get(0));
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
