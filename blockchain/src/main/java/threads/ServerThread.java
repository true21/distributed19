package threads;

import beans.*;
import entities.*;
import utilities.MessageUtilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.InetAddress;
import java.security.*;
import java.util.*;

public class ServerThread extends Thread {

    public static Blockchain blockchain;
    private static NodeMiner miner;
  	private static ArrayList<Node> nodes = new ArrayList<Node>();
    private int index;
    private String operation;
    private static InetAddress ipAddress;
    private static Socket socket;
    private static int port;
    public static final int n = 5;

    public ServerThread(Socket sock, String op, InetAddress ip, int prt, Blockchain blockchain, NodeMiner miner, ArrayList<Node> nodes){
  		this.socket = sock;
      this.operation = op;
      this.ipAddress = ip;
      this.port = prt;
      this.blockchain = blockchain;
      this.miner = miner;
      this.nodes = nodes;
  	}

    // args[0] is IP of node, args[1] is port of node, args[2] is IP of boot node
    // boot node has port 10000
    public static void main(String[] args) throws IOException {
      try{
          System.out.println("Server is running");
          InetAddress myIp = InetAddress.getByName(args[0]);
          int myPort = Integer.parseInt(args[1]);
          InetAddress bootIp = InetAddress.getByName(args[2]);
          miner = new NodeMiner(-1, myIp, myPort);
          // cases not_bootstrap, bootstrap below
          if (myPort != 10000) {
            // Non-boot nodes here
            Socket socket = new Socket(bootIp, 10000);
            System.out.println("Connected to bootstrap node!");
            //System.out.println("Spawning thread for towards bootstrap communication ");
            //Thread t = new ServerThread(soct,"notboot",InetAddress.getByName(args[0]),args[1]);
            //ipAddress = InetAddress.getByName(args[0]);
            //port = args[1];
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
      			Node node = new Node(-1,myIp,myPort,miner.getWallet().getPublicKey());
      			/*node.setIndex(-1);
      			node.setIP(myIp);
      			node.setPort(myPort);
      			node.setKey(miner.getWallet().getPublicKey());*/
            // new node sends its identification
      			objectOutputStream.writeObject(node);
      			objectOutputStream.close();
      			InputStream inputstream = new ObjectInputStream(socket.getInputStream());
            // new node sets its id, sent by bootstrap
      			miner.setIndex(inputstream.read());
      			inputstream.close();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
      			Node node3;
      			for (int i=0; i<n; i++) {
      				node3 = (Node) ois.readObject();
      				nodes.add(node3); //receive list by bootstrap
      			}
            blockchain = (Blockchain) ois.readObject();
            ois.close();
            socket.close();
            //t.start();
          }
          else {
            // Bootstrap node here
            blockchain = new Blockchain();
            Node node = new Node(0, myIp, myPort, miner.getWallet().getPublicKey());
            nodes.add(node);
            Wallet w = new Wallet();
            w.generateKeyPair();
            Transaction gen_trans = new Transaction(w.getPublicKey(), node.getPublicKey(), (float) 100.0*n, null);
            gen_trans.generateSignature(w.getPrivateKey());
            gen_trans.setTransId("0");
            gen_trans.setTransOut(node.getPublicKey(), (float) 100.0*n, gen_trans.getTransId());
            blockchain.setUTXOs(gen_trans.getTransOut().get(0).getId(), gen_trans.getTransOut().get(0));
            Block gen_block = new Block();
            gen_block.addTransaction(gen_trans, blockchain);
            blockchain.addBlock(gen_block, miner);
            ServerSocket listener = new ServerSocket(10000);
            List<Socket> sockets = new ArrayList<Socket>();
            int c = 0;
            while (c<n) {
                Socket socket_boot = listener.accept();
                sockets.add(socket_boot);
                System.out.println("Spawning thread for bootstrap incoming communication " );
                //Thread t = new ServerThread(socket,"boot",InetAddress.getByName(args[0]),args[1]);
                //ipAddress = InetAddress.getByName(args[0]);
                //port = args[1];
                ObjectInputStream objectInputStream = new ObjectInputStream(socket_boot.getInputStream());
                Node node2;
                node2 = (Node) objectInputStream.readObject();
                node2.setIndex(nodes.size());
                nodes.add(node2);
                // bootstrap receives node's id, adds it to list and sends his index
                OutputStream output = new ObjectOutputStream(socket_boot.getOutputStream());
                output.write(node2.getIndex());
                output.close();
                objectInputStream.close();
                //t.start();
                c++;
            }
            for (int j=0; j<sockets.size(); j++) {
              ObjectOutputStream oos = new ObjectOutputStream(sockets.get(j).getOutputStream());
        			for(int i=0;i<nodes.size();i++) {
                // broadcast list of nodes ids
        				oos.writeObject(nodes.get(i));
        			}
              oos.writeObject(blockchain);
        			oos.close();
            }
          }
          // send nodes list to client so he can broadcast
          Socket socket_cli = new Socket(myIp, 7070 + miner.getIndex());
          ObjectOutputStream oos = new ObjectOutputStream(socket_cli.getOutputStream());
          for(int i=0;i<nodes.size();i++) {
            // broadcast list of nodes ids
            oos.writeObject(nodes.get(i));
          }
          // check if all clients can connect with you
          ServerSocket ss = new ServerSocket(myPort);
          for (int i=0; i<n; i++) {
            Socket s = ss.accept();
            s.close();
          }
          // connections done (let's hope so)

          // now server waits to receive (transactions, blocks, etc)
          ServerSocket ss_await = new ServerSocket(myPort);
          while (true) {
            Socket s_cli = ss_await.accept();
            ObjectInputStream ois = new ObjectInputStream(s_cli.getInputStream());
            Message message = (Message) ois.readObject();
            if (message.getType().equals("this")) {
              // do this
            }
            if (message.getType().equals("that")) {
              // do that
            }
          }
          /* for(int i=0; i<n; i++) {
              if(i == miner.getIndex()) continue;
              Thread t = new ServerThread(null, "client", InetAddress.getByName(args[0]), args[1], blockchain, miner, nodes);
              t.start();
            }
            //list of sockets
            for(int i=0; i<n; i++) {
              if(i == miner.getIndex()) continue;
              Socket socket = new Socket(nodes.get(i).getIP(), nodes.get(i).getPort(), InetAddress.getByName(args[0]), args[1]);
            } */
        }  catch (Exception e) { e.printStackTrace();}

    }

    /**
     * Handle an incoming message
     * @param Message msg
     */
    private static void handleMessage(Message msg) {
    }

    @Override
    public void run() {
      try{
        ServerSocket listener = new ServerSocket(9090);
        Socket socket = listener.accept();
        if(operation.equals("boot")) {


        }
        else if(operation.equals("notboot")) {

        }
    }  catch (Exception e) { e.printStackTrace();}
  }
}
