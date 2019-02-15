package entities;

import beans.Block;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Blockchain will be part a node-miner. It should be able to be sent to
 * a new miner joining the network
 */
public class Blockchain implements Serializable {

    private List<Block> blockchain;
    private int difficulty;
    private int maxTransactionInBlock;
    private HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
    private float minimumTransaction;


    /**
     * Method checking if the list of blocks contained in this object is
     * creates a valid blockchain
     *
     * @return True, if the blockchain is valid, else false
     */
    public boolean isValid() throws Exception {
      Block currentBlock;

      for(int i=1; i<blockchain.size(); i++)
      {
        currentBlock = blockchain.get(i);
        if(!currentBlock.isValid(blockchain))
          return false;
        //check if hash is solved
  			if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
          System.out.println("#This block hasn't been mined");
  				return false;
  			}

        //loop thru blockchains transactions:
        TransactionOutput tempOutput;
        for(int t=0; t <currentBlock.transactions.size(); t++) {
          Transaction currentTransaction = currentBlock.transactions.get(t);

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

          if( currentTransaction.outputs.get(0).reciepient != currentTransaction.receiver_address) {
            System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
            return false;
          }
          if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender_address) {
            System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
            return false;
          }
        }
      }
      System.out.println("Blockchain is valid");
      return true;
    }


}
