package entities;

import beans.Block;
import beans.MessageType;
import threads.ClientThread;
import threads.ServerThread;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.InetAddress;

/*
 * Class that represents a miner.
 */
public class NodeMiner {

	private int index;
	private InetAddress ipAddress;
	private int port;
	private Wallet wallet = new Wallet();

	public NodeMiner(int c, InetAddress i, int prt){
		index = c;
		ipAddress = i;
		port = prt;
		wallet.generateKeyPair();
	}

	public int getIndex(){
		return index;
	}

	public void setIndex(int idx){
		index = idx;
	}

	public Wallet getWallet(){
		return wallet;
	}
    /*
     * todo : utility to mine a new Block
     */
    public void mineBlock(Block newBlock, int diff) throws Exception {
			try{
	    	String target = new String("");
	    	Random rand = new Random();
	    	int rand_int1;
				String hash = new String("");
	    	for(int i=0;i<diff;i++)
	    		target += "0";
	    	while(true) {
	    		rand_int1 = rand.nextInt(2147483647); //or nonce++
	        	newBlock.setNonce(rand_int1);
	        	hash = newBlock.calculateHash();
	        	if(hash.substring(0, diff).equals(target))
	        		break;
	    	}
	    	newBlock.setHash(hash);
	    	System.out.println("Block Mined!!! : " + hash);
	    	//broadcastBlock(newBlock);
			}  catch (Exception e) { e.printStackTrace();}
    }

    /**
     * todo : Utility to initiliaze any network connections. Call upon start
     */
    public void initiliazeNetworkConnections()  {
    }

    public void broadcastBlock(Block block) {
    	//?how to broadcast
    }

    /**
     * Function adding a new transaction to blockchain
     * @param transaction
     * @param broadcast
     * @return whether the transaction was added or not
     */
    public boolean addTransactionToBlockchain(Transaction transaction, boolean broadcast) {
        return false;
    }

}
