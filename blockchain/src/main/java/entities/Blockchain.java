package entities;

import beans.Block;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Blockchain will be part a node-miner. It should be able to be sent to
 * a new miner joining the network
 */
public class Blockchain implements Serializable {

    private CopyOnWriteArrayList<Block> blockchain = new CopyOnWriteArrayList<Block>();
    private int difficulty;
    private int maxTransactionInBlock;
    // in order to compile static - else Wallet.getBalance() problematic
    private HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>(); //static CAREFUUUUUUL
    private float minimumTransaction = 0.01f;

    public Blockchain(int diff, int max){
      this.difficulty = diff;
      this.maxTransactionInBlock = max;
    }

   public float getMinimumTransaction() {
     return this.minimumTransaction;
   }

   public CopyOnWriteArrayList<Block> getBlockchain() {
     return this.blockchain;
   }

   public void setUTXOs(String id, TransactionOutput out){
      this.UTXOs.put(id,out);
   }

   public HashMap<String,TransactionOutput> getUTXOs() {
     return UTXOs;
   }

	 public int getDifficulty() {
    	return difficulty;
    }

    public int getMaxTrans() {
    	return maxTransactionInBlock;
    }

    public void addBlock(Block newBlock, NodeMiner miner) {
      try{
        if (!newBlock.getPreviousHash().equals("1")) {
          miner.mineBlock(newBlock, difficulty);
        }
        else {
          newBlock.setHash(newBlock.calculateHash());
          //newBlock.setHash("qwerty");
        }
  		  //blockchain.add(newBlock);
      }  catch (Exception e) { e.printStackTrace();}
    }

    /**
     * Method checking if the list of blocks contained in this object is
     * creates a valid blockchain
     *
     * @return True, if the blockchain is valid, else false
     */
     public boolean isValid() throws Exception {
        Block currentBlock;
    		Block previousBlock;
    		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
    		HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
        Transaction genesisTransaction = blockchain.get(0).getTrans().get(0);
        tempUTXOs.put(genesisTransaction.transaction_outputs.get(0).id, genesisTransaction.transaction_outputs.get(0));

    		//loop through blockchain to check hashes:
    		for(int i=1; i < blockchain.size(); i++) {

    			currentBlock = blockchain.get(i);
    			previousBlock = blockchain.get(i-1);
    			//compare registered hash and calculated hash:
    			if(!currentBlock.getHash().equals(currentBlock.calculateHash())){
    				System.out.println("#Current Hashes not equal");
    				return false;
    			}
    			//compare previous hash and registered previous hash
    			if(!previousBlock.getHash().equals(currentBlock.getPreviousHash()) ) {
    				System.out.println("#Previous Hashes not equal");
    				return false;
    			}
    			//check if hash is solved
    			if(!currentBlock.getHash().substring( 0, difficulty).equals(hashTarget)) {
    				System.out.println("#This block hasn't been mined");
    				return false;
    			}

    			//loop thru blockchains transactions:
    			TransactionOutput tempOutput;
    			for(int t=0; t <currentBlock.getTrans().size(); t++) {
    				Transaction currentTransaction = currentBlock.getTrans().get(t);

    				if(!currentTransaction.verifiySignature()) {
    					System.out.println("#Signature on Transaction(" + t + ") is Invalid");
    					return false;
    				}
    				if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
    					System.out.println("#Inputs are not equal to outputs on Transaction(" + t + ") at block " + i);
              System.out.println("Input is: " + currentTransaction.getInputsValue() + ", and Output is: " + currentTransaction.getOutputsValue());
    					return false;
    				}

    				for(TransactionInput input: currentTransaction.transaction_inputs) {
    					tempOutput = tempUTXOs.get(input.transactionOutputId);

    					if(tempOutput == null) {
    						System.out.println("#Referenced input on Transaction(" + t + ") is Missing, Block: " + i);
    						return false;
    					}

    					if(input.UTXO.value != tempOutput.value) {
    						System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
    						return false;
    					}

              System.out.println("------------------- Removing input: " + input.transactionOutputId);
    					tempUTXOs.remove(input.transactionOutputId);
    				}

    				for(TransactionOutput output: currentTransaction.transaction_outputs) {
    					tempUTXOs.put(output.id, output);
              System.out.println("------------------- Adding input: " + output.id);
    				}

    				if( currentTransaction.getTransOut().get(0).reciepient != currentTransaction.getRecAddr()) {
    					System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
    					return false;
    				}
    				if( currentTransaction.getTransOut().get(1).reciepient != currentTransaction.getSendAddr()) {
    					System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
    					return false;
    				}

    			}

    		}
    		System.out.println("Blockchain is valid");
    		return true;
     }
/*
    public boolean isValid() throws Exception {
      Block currentBlock;
      HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
      Transaction genesisTransaction = blockchain.get(0).getTrans().get(0);
      tempUTXOs.put(genesisTransaction.transaction_outputs.get(0).id, genesisTransaction.transaction_outputs.get(0));

      for(int i=1; i<blockchain.size(); i++)
      {
        currentBlock = blockchain.get(i);
        if(!currentBlock.isValid(blockchain)) {
          return false;
        }
        //check if hash is solved
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
  			if(!currentBlock.getHash().substring( 0, difficulty).equals(hashTarget)) {
          System.out.println("#This block hasn't been mined");
  				return false;
  			}

        //loop thru blockchains transactions:
        TransactionOutput tempOutput;
        for(int t=0; t <currentBlock.getTrans().size(); t++) {
          Transaction currentTransaction = currentBlock.getTrans().get(t);

          if(!currentTransaction.verifiySignature()) {
            System.out.println("#Signature on Transaction(" + t + ") is Invalid");
            return false;
          }
          if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
            System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
            return false;
          }

          for(TransactionInput input: currentTransaction.transaction_inputs) {
            tempOutput = tempUTXOs.get(input.transactionOutputId);

            if(tempOutput == null) {
              System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
              return false;
            }

            if(input.UTXO.value != tempOutput.value) {
              System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
              return false;
            }

            tempUTXOs.remove(input.transactionOutputId);
          }

          for(TransactionOutput output: currentTransaction.transaction_outputs) {
            tempUTXOs.put(output.id, output);
          }

          if( currentTransaction.getTransOut().get(0).reciepient != currentTransaction.getRecAddr()) {
            System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
            return false;
          }
          if( currentTransaction.getTransOut().get(1).reciepient != currentTransaction.getSendAddr()) {
            System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
            return false;
          }
        }
      }
      System.out.println("Blockchain is valid");
      return true;
    }
    */


}
