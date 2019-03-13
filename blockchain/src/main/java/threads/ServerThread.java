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

    private static volatile boolean keepGoing;

    private Block myBlock;
    private static Blockchain blockchain;
    private static NodeMiner miner;
  	private static ArrayList<Node> nodes = new ArrayList<Node>();
    //private int index;
    //private String operation;
    private static InetAddress ipAddress;
    //private static Socket socket;
    private static int port;
    private static final int n = 2;

    private String Case;
    private Message Msg;

    public ServerThread(/*Socket sock, String op, InetAddress ip, int prt, */String cs, Message msg, Block block, Blockchain blockchain, NodeMiner miner, ArrayList<Node> nodes){
  		//this.socket = sock;
      //this.operation = op;
      //this.ipAddress = ip;
      //this.port = prt;
      this.Case = cs;
      this.Msg = msg;
      this.myBlock = block;
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
            System.out.println("Connecting11 to " + 10000);
            Socket socket = new Socket(bootIp, 10000);
            System.out.println("Connected12");
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

            //objectOutputStream.reset();
            //System.out.println(node.getIP() + " " + node.getPublicKey());
      			//objectOutputStream.close();
      			ObjectInputStream inputstream = new ObjectInputStream(socket.getInputStream());
            // new node sets its id, sent by bootstrap
            int ind = inputstream.readInt();
      			miner.setIndex(ind);
      			//inputstream.close();
            //ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
      			Node node3;
      			for (int i=0; i<n; i++) {
      				node3 = (Node) inputstream.readObject();
      				nodes.add(node3); //receive list by bootstrap
      			}
            blockchain = (Blockchain) inputstream.readObject();
            //ois.close();
            //t.start();
            socket.close(); /////////////////
            System.out.println("socket closed");
          }
          else {
            // Bootstrap node here
            miner.setIndex(0);
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
            List<ObjectOutputStream> outputs = new ArrayList<ObjectOutputStream>();
            int c = 1; // mallon
            while (c<n) {
                System.out.println("Listening13 to " + 10000);
                Socket socket_boot = listener.accept();
                System.out.println("Accepting14");
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
                //System.out.println(node2.getIndex() + " " + node2.getPublicKey() + " " + nodes.size());
                // bootstrap receives node's id, adds it to list and sends his index
                ObjectOutputStream output = new ObjectOutputStream(socket_boot.getOutputStream());
                outputs.add(output);
                output.writeInt(node2.getIndex());
                System.out.println("hey" + node2.getIndex());
                //output.close();
                //objectInputStream.close();
                //t.start();
                c++;
            }
            for (int j=0; j<outputs.size(); j++) {
              //ObjectOutputStream oos = new ObjectOutputStream(sockets.get(j).getOutputStream());
        			for(int i=0;i<nodes.size();i++) {
                // broadcast list of nodes ids
        				outputs.get(j).writeObject(nodes.get(i));
        			}
              outputs.get(j).writeObject(blockchain);
        			//oos.close();
            }
            for(int i=0; i<sockets.size(); i++){
              if(sockets.get(i).getInputStream().read()==-1){
                sockets.get(i).close();
                System.out.println("server socket close");
              }
            }
            listener.close();
            // send 100 noobcash coins to each of the others
            /*Block block;
            for (int i=1; i<nodes.size(); i++) {
              if (i%blockchain.getMaxTrans() == 1) {
                block = new Block(blockchain.getBlockchain().get(blockchain.getBlockchain().size()-1).getHash());
              }
              Transaction tran = miner.getWallet().sendFunds(nodes.get(i).getPublicKey(), 100f);
              block.addTransaction(tran);
              Message mes = new Message("transaction", tran);
              for (int j=1; j<nodes.size(); j++) {
                oos = new ObjectOutputStream(sockets.get(j-1).getOutputStream());
                oos.writeObject(mes);
                oos.close();
              }

            } */
          }
          // send nodes list to client so he can broadcast
          //System.exit(0);
          int poort = 7070 + miner.getIndex();
          System.out.println("Connecting15 to " + poort);
          Socket socket_cli = new Socket(myIp, poort);
          System.out.println("Connected16");
          ObjectOutputStream oos = new ObjectOutputStream(socket_cli.getOutputStream());
          for(int i=0;i<nodes.size();i++) {
            // broadcast list of nodes ids
            oos.writeObject(nodes.get(i));
          }
          if(socket_cli.getInputStream().read()==-1){
            socket_cli.close();
            System.out.println("socket close");
          }
          // check if all clients can connect with you
          /*ServerSocket ss = new ServerSocket(myPort);
          for (int i=0; i<n; i++) {
            System.out.println("Listening17 to " + myPort);
            Socket s = ss.accept();
            System.out.println("Accepting18");
            //s.close();
          }*/
          // connections done (let's hope so)
          /*
          if (myPort == 10000) {
            String tr100 = "t ";
            for (int i=1; i<nodes.size(); i++) {
              tr100 += i + " 100";
              Message mes = new Message(tr100);
              Socket s100 = new Socket(myIp, myPort);
              oos = new ObjectOutputStream(s100.getOutputStream());
              oos.writeObject(mes);
              //oos.close();
            }

          }
          */

          Block block = new Block(blockchain.getBlockchain().get(0).getHash());
          System.out.println("genesis previous hash "+blockchain.getBlockchain().get(0).getPreviousHash());
          // now server waits to receive (transactions, blocks, etc)
          ServerSocket ss_await = new ServerSocket(myPort);
          while (true) {
            System.out.println("Listening19 to " + myPort);
            Socket s_cli = ss_await.accept();
            System.out.println("Accepting20");
            ObjectInputStream ois = new ObjectInputStream(s_cli.getInputStream());
            Message message = (Message) ois.readObject();
            if (message.getType().equals("balance")) {
              oos = new ObjectOutputStream(s_cli.getOutputStream());
              float balance = miner.getWallet().getBalance(blockchain);
              oos.writeObject(Float.toString(balance));
              if(s_cli.getInputStream().read()==-1){
                s_cli.close();
    						System.out.println("server socket close");
    					}
            }
            else if (message.getType().equals("help")) {
              s_cli.close();
              System.out.println("server socket close");
            }
            else if (message.getType().equals("view")) {
              // do view
              Block last = blockchain.getBlockchain().get(blockchain.getBlockchain().size()-1);
              System.out.println("blockchain length: " + blockchain.getBlockchain().size());
              String view = new String("");
              PublicKey sKey;
              PublicKey rKey;
              int sk = -1;
              int rk = -1;
              for(int i=0; i<last.getTrans().size(); i++){
                sKey = last.getTrans().get(i).getSendAddr();
                rKey = last.getTrans().get(i).getRecAddr();
                for (int j=0; j<nodes.size(); j++) {
                  if (nodes.get(j).getPublicKey().equals(sKey))
                    sk = j;
                  if (nodes.get(j).getPublicKey().equals(rKey))
                    rk = j;
                }
                view += "From: node" + sk + "  To: node" + rk + "  Value: " + last.getTrans().get(i).getValue() + "\n";
              }
              oos = new ObjectOutputStream(s_cli.getOutputStream());
              oos.writeObject(view);
              if(s_cli.getInputStream().read()==-1){
    						s_cli.close();
    						System.out.println("server socket close");
    					}
            }
            else if (message.getType().startsWith("t ")) {
              String[] parts = message.getType().split(" ");
              String id_str = parts[1];
              String value_str = parts[2];
              int id = Integer.parseInt(id_str);
              float value = Float.parseFloat(value_str);
              boolean valid_id = true;
              Transaction tran;
              if (id == miner.getIndex() || id < 0 || id >= n) {
                valid_id = false;
              }
              if (valid_id) {
                tran = miner.getWallet().sendFunds(nodes.get(id).getPublicKey(), value, blockchain);
              }
              else {
                tran = null;
              }
              String return_msg;
              if (!valid_id) {
                return_msg = "Invalid receiver id. Aborting transaction..";
              }
              else if (tran == null) {
                return_msg = "You don't have enough coins for the transaction.";
              }
              else {
                Message msg = new Message("transaction", tran);
                Thread t = new ServerThread("broadcast", msg, block, blockchain, miner, nodes);
                t.start();
                /*for (int i=0; i<nodes.size(); i++) {
                  Socket s = new Socket(nodes.get(i).getIP(), nodes.get(i).getPort());
                  oos = new ObjectOutputStream(s.getOutputStream());
                  oos.writeObject(msg);
                  //oos.close();
                }*/
                return_msg = "Transaction was completed successfully.";
              }
              oos = new ObjectOutputStream(s_cli.getOutputStream());
              oos.writeObject(return_msg);
              if(s_cli.getInputStream().read()==-1){
    						s_cli.close();
    						System.out.println("server socket close");
    					}
            }
            else if (message.getType().equals("transaction")) {
              System.out.println("transaction value: " + message.getTransaction().value);
              block.addTransaction(message.getTransaction(), blockchain);
              if (block.getTrans().size() == blockchain.getMaxTrans()) {
                Thread t = new ServerThread("aek", null, block, blockchain, miner, nodes);
                keepGoing = true;
                t.start();
                // create new block with invalid previous hash
                // gonna fix it when its previous enters blockchain
                block = new Block("21");
              }
            }
            else if (message.getType().equals("block")) {
              System.out.println("got into block handler");
              keepGoing = false;
              System.out.println("hashes: " + message.getBlock().getPreviousHash() + ", " + blockchain.getBlockchain().get(blockchain.getBlockchain().size()-1).getHash());
              blockchain.getBlockchain().add(message.getBlock());
              boolean isValid = blockchain.isValid();
              if (!isValid) {
                System.out.println("block in not valid");
                blockchain.getBlockchain().remove(blockchain.getBlockchain().size()-1);
                // consensus
                Message cons_msg = new Message("consensus");
                Thread t = new ServerThread("broadcast", cons_msg, block, blockchain, miner, nodes);
                t.start();
                /*for (int i=0; i<nodes.size(); i++) {
                  Socket s = new Socket(nodes.get(i).getIP(), nodes.get(i).getPort());
                  oos = new ObjectOutputStream(s.getOutputStream());
                  oos.writeObject(cons_msg);
                }*/
              }
              else {
                System.out.println("block in valid");
                block.setPreviousHash(message.getBlock().getHash());
              }
            }
            else if (message.getType().equals("consensus")) {
              Message bc_msg = new Message("blockchain", blockchain);
              for (int i=0; i<nodes.size(); i++) {
                if (i == miner.getIndex()) continue;
                Socket s = new Socket(nodes.get(i).getIP(), nodes.get(i).getPort());
                oos = new ObjectOutputStream(s.getOutputStream());
                oos.writeObject(bc_msg);
              }
            }
            else if (message.getType().equals("blockchain")) {
              if (blockchain.getBlockchain().size() < message.getBlockchain().getBlockchain().size()) {
                blockchain = message.getBlockchain();
              }
              else if (blockchain.getBlockchain().size() == message.getBlockchain().getBlockchain().size()) {
                int comp_str = blockchain.getBlockchain().toString().compareTo(message.getBlockchain().toString());
                if (comp_str > 0) {
                  blockchain = message.getBlockchain();
                }
              }
            }
            else if (message.getType().equals("this")) {
              // do this
            }
            else if (message.getType().equals("that")) {
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
        if (Case.equals("broadcast")) {
          for (int i=0; i<nodes.size(); i++) {
            Socket s = new Socket(nodes.get(i).getIP(), nodes.get(i).getPort());
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            oos.writeObject(Msg);
          }
        }
        else {
          String target = new String("");
  	    	Random rand = new Random();
  	    	int rand_int1;
  				String hash = new String("");
  	    	for(int i=0; i<blockchain.getDifficulty(); i++)
  	    		target += "0";
          boolean win = false;
          int count = 0;
          int cnt = miner.getIndex();
  	    	while(keepGoing) {
            if (count%50 == 0) System.out.println("mining...");
            count++;
            cnt += n;
  	    		//rand_int1 = rand.nextInt(2147483647); //or nonce++
	        	//myBlock.setNonce(rand_int1);
            myBlock.setNonce(cnt);
	        	hash = myBlock.calculateHash();
	        	if(hash.substring(0, blockchain.getDifficulty()).equals(target)) {
              win = true;
              break;
            }
  	    	}
          if (win) {
            myBlock.setHash(hash);
    	    	System.out.println("Block Mined!!! : " + hash);
            for (int i=0; i<nodes.size(); i++) {
              Socket s = new Socket(nodes.get(i).getIP(), nodes.get(i).getPort());
              ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
              Message msg = new Message("block", myBlock);
              oos.writeObject(msg);
              //oos.close();
            }
          }
        }
      }  catch (Exception e) { e.printStackTrace();}
  }
}
