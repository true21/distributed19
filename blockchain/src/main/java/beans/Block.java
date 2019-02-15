package beans;

import java.util.Date;

import entities.Blockchain;
import entities.Transaction;
import utilities.StringUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

/**
 * Block class represents the basic part of the blockchain
 *
 * Implements the Serializable inteface in order to be sent above
 * network when a new miner joins the blockchain network
 */
public class Block implements Serializable {

    private int index;
    private long timestamp;
    private List<Transaction> transactions = new ArrayList<Transaction>();
    private int nonce;
    private String hash;
    private String previousHash;


  //Block Constructor.  
  	public Block(Block previousBlock) {
  		this.index = previousBlock.index + 1;
  		this.previousHash = previousBlock.hash;
  		this.timeStamp = new Date().getTime();
  		//this.hash = calculateHash(); //Making sure we do this after we set the other values.
  	}
    /*
     * todo:
     * Function that calculates the hash on the current block
     */
    public String calculateHash() throws Exception {
        Gson parser = new Gson();
        String jsonTransactions = parser.toJson(transactions);
        String calculatedHash = StringUtilities.applySha256(
          Integer.toString(index) +
          Long.toString(timestamp) +
          jsonTransactions + 
          Integer.toString(nonce) +
          previousHash
        );
        return calculatedHash;
    }

    /*
     * todo:
     * Function that adds a Transaction on the current block if it is valid
     */
    public boolean addTransaction(Transaction transaction, Blockchain blockchain) {
    	if(transaction == null) return false;	
    	if((previousHash != "0")) {
	    	boolean success = transaction.processTransaction(blockchain);
	    	if(success) {
	    		transactions.add(transaction);
	    		System.out.println("Transaction Successfully added to Block");
	    	}
	    	else {
	    		System.out.println("Transaction failed to process. Discarded.");
	    		return false;
	    	}
    	}
    	/*if(transactions.size() == blockchain.getMaxTrans()) {
    		int diff = blockchain.getDifficulty();
    		//mineeeeeeer
    		//mineBlock(this,diff);
    	}*/
    		
        return true;
    }

    public String getHash()
    {
      return hash;
    }
    
    void setNonce(int nonce) {
    	this.nonce = nonce;
    }
    
    void setHash(int hash) {
    	this.hash = hash;
    }

    public boolean isValid(List<Block> blockchain){
      boolean valid = true;

      /* check current hash */
      if (!hash.equals(calculateHash()))
        valid = false;

      /* check previous hash */
      int i = blockchain.indexOf(this);
      if (i != -1)
      {
        Block previousBlock = blockchain.get(i-1);
        if (!previousHash.equals(previousBlock.getHash()))
          valid = false;
      }
      else valid = false;

      return valid;
    }

}
