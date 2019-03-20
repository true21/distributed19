package beans;

import java.util.Date;

import entities.Blockchain;
import entities.Transaction;
import utilities.StringUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.Gson;

/**
 * Block class represents the basic part of the blockchain
 *
 * Implements the Serializable inteface in order to be sent above
 * network when a new miner joins the blockchain network
 */
public class Block implements Serializable {

    private long timestamp;
    private List<Transaction> transactions = new ArrayList<Transaction>();
    private int nonce;
    private String hash;
    private String previousHash;


    // constructor for genesis block
    public Block() {
      this.previousHash = "1";
      this.nonce = 0;
      this.timestamp = new Date().getTime();
    }

  //Block Constructor.
  	public Block(String preHash) {
  		this.previousHash = preHash;
  		this.timestamp = new Date().getTime();

  		//this.hash = calculateHash(); //Making sure we do this after we set the other values.
  	}
    /*
     * todo:
     * Function that calculates the hash on the current block
     */
    public String calculateHash() throws Exception {
        //Gson parser = new Gson();
        //String jsonTransactions = parser.toJson(transactions);
        String calculatedHash = StringUtilities.applySha256(
          Long.toString(timestamp) +
          //jsonTransactions +
          Integer.toString(nonce) +
          previousHash
        );
        return calculatedHash;
    }

    /*
     * todo:
     * Function that adds a Transaction on the current block if it is valid
     */
    public boolean addTransaction(Transaction transaction, Blockchain blockchain, boolean creator) {
    	if(transaction == null) return false;
    	if(!(previousHash.equals("1"))) {
        boolean success;
	    	if (creator) success = transaction.processTransaction(blockchain);
        else success = true;

	    	if(success) {
          transaction.pT(blockchain);
	    		transactions.add(transaction);
	    		System.out.println("Transaction Successfully added to Block");
          return true;
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
      transactions.add(transaction);
      System.out.println("Transaction Successfully added to Genesis Block");
      return true;
    }

    public String getHash()
    {
      return hash;
    }

    public List<Transaction> getTrans()
    {
      return transactions;
    }

    public String getPreviousHash()
    {
      return previousHash;
    }

    public void setPreviousHash(String pHash) {
    	this.previousHash = pHash;
    }

    public void setNonce(int nonce) {
    	this.nonce = nonce;
    }

    public void setHash(String hash) {
    	this.hash = hash;
    }

  public boolean isValid(CopyOnWriteArrayList<Block> blockchain){
    try{
      boolean valid = true;

      /* check previous hash */
      int i = blockchain.indexOf(this);
      if (i != -1)
      {
        Block previousBlock = blockchain.get(i-1);
        if (!(previousHash.equals(previousBlock.getHash()))) {
          System.out.println("!(previousHash.equals(previousBlock.getHash())) fails in Block.java");
          valid = false;
        }
      }
      else {
        System.out.println("index is -1");
        valid = false;
      }

      return valid;
    }  catch (Exception e) { e.printStackTrace(); System.out.println("Exception"); return false;}
  }

}
