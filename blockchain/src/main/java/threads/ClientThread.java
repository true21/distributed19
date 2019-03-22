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
import java.util.concurrent.*;

public class ClientThread extends Thread {

	private static InetAddress ipAddress;
	private static int index;
	private static Socket socket;
	private static int port;
	public static  int n;

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
			n = Integer.parseInt(args[1]);
			// get all nodes ids
			ArrayList<Node> nodes = new ArrayList<Node>();
			ServerSocket ss = new ServerSocket(11000 + index);
			//System.out.println("Listening1 to " + 11000 + index);
      Socket s_nodeId = ss.accept();
			//System.out.println("Accepting2");
			ObjectInputStream ois = new ObjectInputStream(s_nodeId.getInputStream());
			Node temp_node;
			for (int i=0; i<n; i++) {
				temp_node = (Node) ois.readObject();
				nodes.add(temp_node);
			}
			//System.out.println("hey with listsize " + nodes.size());
			s_nodeId.close();
			ss.close();
			//System.out.println("server socket close");
			// check if you can connect to all servers
			/*for (int i=0; i<nodes.size(); i++) {
				System.out.println("Connecting3 to " + nodes.get(i).getPort());
				Socket s_check = new Socket(nodes.get(i).getIP(), nodes.get(i).getPort());
				System.out.println("Connected4");
			}*/
			// now we're good to go

			// socket arguments of client's server
			InetAddress ip = nodes.get(index).getIP();
			int port = nodes.get(index).getPort();

			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream oos21 = new ObjectOutputStream(bout);
			ObjectInputStream ois21 = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));

			List<ObjectOutputStream> outputs21 = new ArrayList<ObjectOutputStream>();
			List<ObjectInputStream> inputs21 = new ArrayList<ObjectInputStream>();

			if(index==0){
				//System.out.println("Sleeping..");
				TimeUnit.MILLISECONDS.sleep(500);
				//System.out.println("File read");
				for (int i=1; i<nodes.size(); i++) {
						//System.out.println("Connecting7 to " + 11000 + i);
						Socket s_ready = new Socket(nodes.get(i).getIP(), 14000 + i);
						oos21 = new ObjectOutputStream(s_ready.getOutputStream());
						ois21 = new ObjectInputStream(s_ready.getInputStream());
						outputs21.add(oos21);
						inputs21.add(ois21);
						//System.out.println("Connected8");
						//s_ready.close();
				}
			}
			else {
				ServerSocket ss_readyy = new ServerSocket(14000 + index);
				//System.out.println("File read");
				// be notified when you're ready to go
	      Socket s_readyy = ss_readyy.accept();
				oos21 = new ObjectOutputStream(s_readyy.getOutputStream());
				//oos21.writeObject(mes);
				ois21 = new ObjectInputStream(s_readyy.getInputStream());
				//String str = (String) ois21.readObject();
				//System.out.println("Accepting10");
				//s_readyy.close();
				//ss_readyy.close();
			}

			// send 100 noobcash coins to others if Bootstrap
			if (index == 0) {
				for (int i=1; i<nodes.size(); i++) {
					String tr100 = "t ";
					tr100 += i + " 100";
					Message mes = new Message(tr100);
					//System.out.println("Connecting5 to " + port);
					Socket s100 = new Socket(ip, port);
					//System.out.println("Connected6");
					ObjectOutputStream oos = new ObjectOutputStream(s100.getOutputStream());
					oos.writeObject(mes);
					ObjectInputStream ms = new ObjectInputStream(s100.getInputStream());
					String str = (String) ms.readObject();
					s100.close();
					//oos.close();
				}
				// say to others client's that they're ready to go
			/*	for (int i=1; i<nodes.size(); i++) {
					System.out.println("Connecting7 to " + 11000 + i);
					Socket s_ready = new Socket(nodes.get(i).getIP(), 11000 + i);
					System.out.println("Connected8");
					s_ready.close();
				}*/
			}/*
			else {

				ServerSocket ss_readyy = new ServerSocket(11000 + index);
				System.out.println("Listening9 to " + 11000 + index);
				// be notified when you're ready to go
	      Socket s_readyy = ss_readyy.accept();
				System.out.println("Accepting10");
				s_readyy.close();
				ss_readyy.close();
			}*/
			// be notified that 100 nbc coins have arrived everywhere
			ServerSocket ss_readyy2 = new ServerSocket(11000 + index);
			Socket s_readyy2 = ss_readyy2.accept();
			s_readyy2.close();
			ss_readyy2.close();

			// file
			System.out.println("Reading file");
			String file = "../../resources/main/transactions/" + n + "nodes/transactions" + index + ".txt";
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
  		while ((line = br.readLine()) != null) {
				Socket s = new Socket(ip, port);
				ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				String toSend = "t " + line;
				Message message = new Message(toSend);
				oos.writeObject(message);
				ois = new ObjectInputStream(s.getInputStream());
				String transMsg = (String) ois.readObject();
				//System.out.println(transMsg);
				s.close();
  		}

			System.out.println("Read file");

			if(index==0){
				for (int i=0; i<n-1; i++) {
					String someMsg = (String) inputs21.get(i).readObject();
					//System.out.println("Read " + i);
				}
				for (int i=0; i<n-1; i++) {
					outputs21.get(i).writeObject("go on");
					//System.out.println("Wrote " + i);
				}
			}
			else {
				oos21.writeObject("finished");
				//System.out.println("Wrote");
				String someOtherMsg = (String) ois21.readObject();
				//System.out.println("Read");
			}




			Message endmes = new Message("allDone");
			//System.out.println("ConnectingEnd1 to " + port);
			Socket ends = new Socket(ip, port);
			//System.out.println("ConnectedEnd2");
			ObjectOutputStream endoos = new ObjectOutputStream(ends.getOutputStream());
			endoos.writeObject(endmes);
			if(ends.getInputStream().read()==-1){
				ends.close();
			}
			//System.out.println("Sleeping..");
			//System.out.println("woke up");
			// get input from console
			Scanner scanner = new Scanner(System.in);

			//System.out.println("READY!");

			// cli
			while (true) {
				// read line
				String command = scanner.nextLine();
				//System.out.println("Connecting21 to " + port);
				Socket s = new Socket(ip, port);
				//System.out.println("Connected22");
				ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				String sendToServer = new String("");
				String help = "balance: see wallet\nview: see last block's transactions\nt <recipient_address> <amount>: send amount to recipient_address";
				String error = "Bad syntax.";
				List<String> cmd_args = Arrays.asList(command.trim().split("\\s+"));
				//System.out.println("gave " + cmd_args.get(0));
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
					Message message = new Message("help");
					oos.writeObject(message);
					if(s.getInputStream().read()==-1){
						s.close();
					}
				}
				else if (sendToServer.equals("help")) {
					System.out.println(help);
					Message message = new Message("help");
					oos.writeObject(message);
					if(s.getInputStream().read()==-1){
						s.close();
					}
				}
				else {
					Message message = new Message(sendToServer);
					oos.writeObject(message);
					ois = new ObjectInputStream(s.getInputStream());
					if (sendToServer.equals("balance")) {
						// balance
						//System.out.println("bal reading");
						String balance_str = (String) ois.readObject();
						float balance = Float.parseFloat(balance_str);
						//System.out.println("bal read");
						System.out.println("Your wallet's balance is: " + balance + " noobcash coins.");
						s.close();
					}
					else if (sendToServer.equals("view")) {
						// view
						String view = (String) ois.readObject();
						System.out.println("The last block's transactions are:\n" + view);
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
