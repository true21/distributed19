package threads;

import beans.*;
import entities.*;
import utilities.MessageUtilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.InetAddress;
import java.security.*;
import java.util.*;

public class ServerThread extends Thread {

    public static Blockchain blockchain;
    private NodeMiner miner;
  	private ArrayList<Node> nodes = new ArrayList<Node>();
    private int index;
    private String operation;
    private InetAddress ipAddress;
    private Socket socket;
    private int port;
    public final int n = 5;

    public ServerThread(Socket sock, String op, InetAddress ip, int prt){
  		socket = sock;
      operation = op;
      ipAddress = ip;
      port = prt;
  	}

    public static void main(String[] args) throws IOException {
          System.out.println("Server is running");
          miner = new NodeMiner(-1,InetAddress.getByName(args[0]),args[1]);
          try (ServerSocket listener = new ServerSocket(9090)) {
              Socket socket = listener.accept();
              System.out.println("Spawning thread for client communication");
              Thread t = new ServerThread(socket, "client",InetAddress.getByName(args[0]),args[1]);
              t.start();
              if (args[1]!=10000) {
                Socket socket = new Socket(InetAddress.getByName(args[0]), 10000, InetAddress.getByName(args[0]), args[1]);
                System.out.println("Spawning thread for towards bootstrap communication ");
                //Thread t = new ServerThread(soct,"notboot",InetAddress.getByName(args[0]),args[1]);
                ipAddress = InetAddress.getByName(args[0]);
                port = args[1];
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
          			Node node = new Node();
          			node.setIndex(-1);
          			node.setIP(ipAddress);
          			node.setPort(port);
          			node.setKey(miner.getWallet().getPublicKey());
          			objectOutputStream.writeObject(node); //new node sends its identification
          			objectOutputStream.close();
          			InputStream inputstream = new ObjectInputStream(socket.getInputStream());
          			miner.setIndex(inputstream.read()); //new node sets its id, set by bootstrap
          			inputstream.close();
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
          			Node node3;
          			for (int i=0; i<n; i++) {
          				node3 = (Node) ois.readObject();
          				nodes.add(node3); //receive list by bootstrap
          			}
                blockchain = (Blockchain) ois.readObject();
                //t.start();
              }
              else {
                // Bootstrap node here
                blockchain = new Blockchain();
                Node node = new Node(0,ipAddress,port,miner.getWallet().getPublicKey());
                nodes.add(node);
                Wallet w = new Wallet();
                w.generateKeyPair();
                Transaction gen_trans = new Transaction(w.getPublicKey(), node.getPublicKey(), 100.0*n, null);
                gen_trans.generateSignature(w.getPrivateKey());
                gen_trans.setTransId("0");
                gen_trans.setTransOut(node.getPublicKey(),100.0*n, gen_trans.getTransId());
                blockchain.setUTXOs(gen_trans.getTransOut().get(0).getId(), gen_trans.getTransOut().get(0));
                Block gen_block = new Block();
                gen_block.addTransaction(gen_trans, blockchain);
                blockchain.addBlock(gen_block, miner);
                ServerSocket listener = new ServerSocket(10000);
                List<Socket> sockets = new ArrayList<Socket>();
                int c = 0;
                while (c<n) {
                    Socket socket = listener.accept();
                    sockets.add(socket);
                    System.out.println("Spawning thread for bootstrap incoming communication " );
                    //Thread t = new ServerThread(socket,"boot",InetAddress.getByName(args[0]),args[1]);
                    ipAddress = InetAddress.getByName(args[0]);
                    port = args[1];
                    ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                    Node node2;
                    node2 = (Node) objectInputStream.readObject();
                    node2.setIndex(nodes.size());
                    nodes.add(node2); //bootstrap receives node's id, adds it to list and sends his id
                    OutputStream output = new ObjectOutputStream(socket.getOutputStream());
                    output.write(node2.getIndex());
                    output.close();
                    objectInputStream.close();
                    //t.start();
                    c++;
                }
                for (int j=0; j<sockets.size(); j++) {
                  ObjectOutputStream oos = new ObjectOutputStream(sockets.get(j).getOutputStream());
            			for(i=0;i<nodes.size();i++){
            				oos.writeObject(nodes.get(i)); //broadcast list
            			}
                  oos.writeObject(blockchain);
            			oos.close();
                }


              }
      }  catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Handle an incoming message
     * @param Message msg
     */
    private void handleMessage(Message msg) {
    }

    @Override
    public void run() {
      if(operation.equals("boot")) {


      }
      else if(operation.equals("notboot")) {

      }
    }
}
