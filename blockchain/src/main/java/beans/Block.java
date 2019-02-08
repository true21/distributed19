package beans;


import entities.Blockchain;
import entities.Transaction;

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



    /*
     * todo:
     * Function that calculates the hash on the current block
     */
    public String calculateHash() throws Exception {
        Gson parser = new Gson();
        String jsonTransactions = parser.toJson(transactions);
        String calculatedHash = StringUtil.applySha256(
          Integer.toString(index) +
          Long.toString(timestamp) +
          jsonTransactions +
          Integer.toString(nonce) +
          hash +
          previousHash
        );
        return calculatedHash;
    }

    /*
     * todo:
     * Function that adds a Transaction on the current block if it is valid
     */
    public boolean addTransaction(Transaction transaction, Blockchain blockchain) {

        return true;
    }

}
