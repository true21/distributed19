package entities;

import utilities.StringUtilities;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Transaction implements Serializable {
    public static Logger LOGGER = Logger.getLogger(Transaction.class.getName());

    public PublicKey sender_address;
    public PublicKey receiver_address;
    public float value;
    public String transaction_id;
    public ArrayList<TransactionInput> transaction_inputs = new ArrayList<TransactionInput>();
    public ArrayList<TransactionOutput> transaction_outputs = new ArrayList<TransactionOutput>();
    public byte[] signature;

    private static int sequence = 0; // a rough count of how many transactions have been generated

    // This Calculates the transaction hash (which will be used as its Id)
    private String calulateHash() {
      sequence++;
      String calculatedHash = StringUtilities.applySha256(
        StringUtilities.getStringFromKey(sender_address) +
        StringUtilities.getStringFromKey(receiver_address) +
        Float.toString(value) +
        sequence
      );
      return calculatedHash;
    }

    //Signs all the data we dont wish to be tampered with.
    public void generateSignature(PrivateKey privateKey) {
      String data = StringUtilities.getStringFromKey(sender_address) + StringUtilities.getStringFromKey(receiver_address) + Float.toString(value);
      signature = StringUtilities.applyECDSASig(privateKey,data);
    }

    //Verifies the data we signed hasnt been tampered with
    public boolean verifiySignature() {
      String data = StringUtil.getStringFromKey(sender_address) + StringUtilities.getStringFromKey(receiver_address) + Float.toString(value);
      return StringUtilities.verifyECDSASig(sender_address, data, signature);
    }

    //Returns true if new transaction could be created.
    // It also checks the validity of a transaction
    public boolean processTransaction(Blockchain blockchain) {
      if(verifiySignature() == false) {
        System.out.println("#Transaction Signature failed to verify");
        return false;
      }

      //gather transaction inputs (Make sure they are unspent):
      for(TransactionInput i : transaction_inputs) {
        i.UTXO = blockchain.UTXOs.get(i.transactionOutputId);
      }

      //check if transaction is valid:
      if(getInputsValue() < blockchain.minimumTransaction) {
        System.out.println("#Transaction Inputs to small: " + getInputsValue());
        return false;
      }

      //generate transaction outputs:
      float leftOver = getInputsValue() - value; //get value of inputs then the left over change:
      transactionId = calulateHash();
      outputs.add(new TransactionOutput( this.receiver_address, value,transactionId)); //send value to recipient
      outputs.add(new TransactionOutput( this.sender_address, leftOver,transactionId)); //send the left over 'change' back to sender

      //add outputs to Unspent list
      for(TransactionOutput o : transaction_outputs) {
        blockchain.UTXOs.put(o.id , o);
      }

      //remove transaction inputs from UTXO lists as spent:
      for(TransactionInput i : transaction_inputs) {
        if(i.UTXO == null) continue; //if Transaction can't be found skip it
        blockchain.UTXOs.remove(i.UTXO.id);
      }

      return true;
    }

    //returns sum of inputs(UTXOs) values
    public float getInputsValue() {
      float total = 0;
      for(TransactionInput i : transaction_inputs) {
        if(i.UTXO == null) continue; //if Transaction can't be found skip it
        total += i.UTXO.value;
      }
      return total;
    }

    //returns sum of outputs:
    public float getOutputsValue() {
      float total = 0;
      for(TransactionOutput o : transaction_outputs) {
        total += o.value;
      }
      return total;
    }


}
