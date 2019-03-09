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

public class ClientThread extends Thread {

	private InetAddress ipAddress;
	private static int index;
	private Socket socket;
	private int port;

	public ClientThread(InetAddress i, int c, Socket sock, int prt) {
		ipAddress = i;
		index = c;
		socket = sock;
		port = prt;
	}

	public static void main(String[] args) throws IOException {
    try {

			// socket
			InetAddress ip = InetAddress.getByName(args[0]);
			Socket s = new Socket(ip, 9090 + index);
			DataInputStream dis = new DataInputStream(s.getInputStream());
      DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			// get input from console
			Scanner scanner = new Scanner(System.in);

			// file
			String file = "transactions" + index + ".txt";
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
					System.out.println("Send to Server: " + sendToServer);
					dos.writeUTF(sendToServer);
					String received = dis.readUTF();
          System.out.println(received);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

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
