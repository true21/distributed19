package threads;

import beans.Message;
import beans.MessageType;
import entities.NodeMiner;
import utilities.MessageUtilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerThread extends Thread {

	private Socket incoming;
	private int counter;

	public ServerThread(Socket i, int c){
		incoming = i;
		counter = c;
	}

	public static void main(String[] args) throws IOException {
		ServerSocket listener = new ServerSocket(9090);
		try {
    	System.out.println("Server is running");
			int i = 0;
      while (true) {
	      Socket socket = listener.accept();
	      System.out.println("Spawning " + i);
	      Thread t = new ServerThread(socket, i);
	      t.start();
	      i++;
      }
		} catch (Exception e) { e.printStackTrace(); }
  }


  /**
   * Handle an incoming message
   * @param Message msg
   */
  private void handleMessage(Message msg) {
  }

  @Override
  public void run() {
	try {
	  ObjectInputStream ois = new ObjectInputStream(new FileInputStream("Message.txt")); //dont have a clue
   	while (true){
    	handleMessage((Message) ois.readObject());
    }
	  ois.close();
	  incoming.close();
	  } catch (Exception e){  e.printStackTrace();  }
	}

}
