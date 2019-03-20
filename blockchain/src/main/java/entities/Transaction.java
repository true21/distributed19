package entities;

import utilities.StringUtilities;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
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

    public Transaction (PublicKey send, PublicKey rec, float val, ArrayList<TransactionInput> inp){
      this.sender_address = send;
      this.receiver_address = rec;
      this.value = val;
      this.transaction_inputs = inp;
    }

    public void setTransId(String tr){
      this.transaction_id = tr;
    }

    public String getTransId(){
      return this.transaction_id;
    }

    public float getValue(){
      return this.value;
    }

    public PublicKey getRecAddr(){
      return this.receiver_address;
    }

    public PublicKey getSendAddr(){
      return this.sender_address;
    }

    public void setTransOut(PublicKey rec, float val, String id){
      this.transaction_outputs.add(new TransactionOutput(rec, val, id));
    }

    public ArrayList<TransactionOutput> getTransOut(){
      return this.transaction_outputs;
    }

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
      String data = StringUtilities.getStringFromKey(sender_address) + StringUtilities.getStringFromKey(receiver_address) + Float.toString(value);
      return StringUtilities.verifyECDSASig(sender_address, data, signature);
    }

    //Returns true if new transaction could be created.
    // It also checks the validity of a transaction
    public boolean processTransaction(Blockchain blockchain) {
      if(verifiySignature() == false) {
        System.out.println("#Transaction Signature failed to verify");
        return false;
      }

      boolean abe = false;
      //gather transaction inputs (Make sure they are unspent):
      for(TransactionInput i : transaction_inputs) {
        i.UTXO = blockchain.getUTXOs().get(i.transactionOutputId);
        System.out.println("------------------------------- The input: " + i.transactionOutputId + " gets value: " + i.UTXO);
        abe = true;
      }

      if (!abe) System.out.println("BBBBBBBBBBB");

      //check if transaction is valid:
      if(getInputsValue() < blockchain.getMinimumTransaction()) {
        System.out.println("#Transaction Inputs to small: " + getInputsValue());
        return false;
      }

      transaction_outputs = new ArrayList<TransactionOutput>();
      //generate transaction outputs:
      float leftOver = getInputsValue() - value; //get value of inputs then the left over change:
      transaction_id = calulateHash();
      transaction_outputs.add(new TransactionOutput( this.receiver_address, value,transaction_id)); //send value to recipient
      transaction_outputs.add(new TransactionOutput( this.sender_address, leftOver,transaction_id)); //send the left over 'change' back to sender

      //add outputs to Unspent list
      /*for(TransactionOutput o : transaction_outputs) {
        blockchain.getUTXOs().put(o.id , o);
      }

      //remove transaction inputs from UTXO lists as spent:
      for(TransactionInput i : transaction_inputs) {
        if(i.UTXO == null) continue; //if Transaction can't be found skip it
        blockchain.getUTXOs().remove(i.UTXO.id);
      } */

      return true;
    }

    public void pT (Blockchain blockchain) {
      for(TransactionOutput o : transaction_outputs) {
        blockchain.getUTXOs().put(o.id , o);
      }

      //remove transaction inputs from UTXO lists as spent:
      for(TransactionInput i : transaction_inputs) {
        if(i.UTXO == null) continue; //if Transaction can't be found skip it
        blockchain.getUTXOs().remove(i.UTXO.id);
      }
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

    public boolean fixInputs(Blockchain blockchain) {
      System.out.println("Sender's balance is: " + getWalletBalance(blockchain, sender_address) + ", and he wants to send: " + value);

      if(getWalletBalance(blockchain, sender_address) < value) { //gather balance and check funds.
        System.out.println("#Sender has not enough funds to send transaction. Transaction Discarded.");
        return false;
      }
      System.out.println("#Enough funds to send transaction. Transaction not Discarded.");
      //create array list of inputs
      ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

      float total = 0;
      for (Map.Entry<String, TransactionOutput> item: blockchain.getUTXOs().entrySet()) {
        TransactionOutput UTXO = item.getValue();
        if(UTXO.isMine(sender_address)) {
          total += UTXO.value;
          inputs.add(new TransactionInput(UTXO.id));
          System.out.println("------------------------------------ Added input: " + UTXO.id);
          if(total > value) break;
        }
      }

      transaction_inputs = inputs;

      return true;
    }

    public float getWalletBalance(Blockchain blockchain, PublicKey pk) {
      float total = 0;
      for (Map.Entry<String, TransactionOutput> item: blockchain.getUTXOs().entrySet()){
        TransactionOutput UTXO = item.getValue();
        if(UTXO.isMine(pk)) { //if output belongs to me ( if coins belong to me )
          total += UTXO.value;
        }
      }
      return total;
    }


}
