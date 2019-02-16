package threads;

import beans.Message;
import beans.MessageType;
import entities.NodeMiner;
import entities.Transaction;

import javax.sound.sampled.LineEvent;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientThread extends Thread {
	
	private InetAddress ipAddress;    
	private int index;
	private Socket socket;  
	private int port;
	
	public ClientThread(InetAddress i, int c, Socket sock, int prt){ 
		ipAddress = i; 
		index = c;  
		socket = sock;
		int port = prt;
	}
	
	public static void main(String[] args) throws IOException {
        System.out.println("Enter the IP address of a machine running the date server:");
        Socket soct = new Socket(InetAddress.getByName(args[0]), 9090, InetAddress.getByName(args[0]), args[1]); //176.58.247.145 or 127.0.0.1 ???, port = 1000 ++
		int cnt = args[1]-1000; //index
		System.out.println("Spawning " + cnt);
        Thread t = new ClientThread(InetAddress.getByName(args[0]), cnt, soct, args[1]);
        t.start();
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
		//create new nodeminer , genesis block 
		BufferedReader in = new BufferedReader (new InputStreamReader(socket.getInputStream()));
		String str = in.readLine();
    }
}
