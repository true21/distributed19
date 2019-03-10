package entities;

import java.io.Serializable;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Wallet implements Serializable {
    public static Logger LOGGER = Logger.getLogger(Wallet.class.getName());

    private PrivateKey privateKey;
    private PublicKey publicKey;

    public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>(); //only UTXOs owned by this wallet.

	public PublicKey getPublicKey(){
		return publicKey;
	}

  public PrivateKey getPrivateKey(){
    return privateKey;
  }
    /**
     * Function generating a new Keypair of public and private key for this wallet
     */
    public void generateKeyPair() {
      try {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
        // Initialize the key generator and generate a KeyPair
        keyGen.initialize(ecSpec, random);   //256 bytes provides an acceptable security level
        KeyPair keyPair = keyGen.generateKeyPair();
        // Set the public and private keys from the keyPair
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
		    }
        catch(Exception e) {
          throw new RuntimeException(e);
        }
    }


    /**
     * Get the balance on this wallet
     * @param allUTXOs (unspent transactions)
     * @return the balance as float
     */
    /*public float getBalance(HashMap<String,TransactionOutput> allUTXOs) {
      float total = 0;
      for (Map.Entry<String, TransactionOutput> item: allUTXOs.entrySet()){
        TransactionOutput UTXO = item.getValue();
        if(UTXO.isMine(publicKey)) { //if output belongs to me ( if coins belong to me )
          UTXOs.put(UTXO.id,UTXO); //add it to our list of unspent transactions.
          total += UTXO.value ;
        }
      }
      return total;
    }*/

    public float getBalance() {
      float total = 0;
      for (Map.Entry<String, TransactionOutput> item: Blockchain.getUTXOs().entrySet()){
        TransactionOutput UTXO = item.getValue();
        if(UTXO.isMine(publicKey)) { //if output belongs to me ( if coins belong to me )
          UTXOs.put(UTXO.id,UTXO); //add it to our list of unspent transactions.
          total += UTXO.value ;
        }
      }
      return total;
    }

    /**
     * Return and creates a transaction from this wallet to a recipient knowing its public key
     * @param _recipient
     * @param value
     * @param allUTXOs
     * @return
     */
    public Transaction sendFunds(PublicKey _recipient, float value/*HashMap<String,TransactionOutput> allUTXOs*/) {
      if(getBalance() < value) { //gather balance and check funds.
        System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
        return null;
      }
      //create array list of inputs
      ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

      float total = 0;
      for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
        TransactionOutput UTXO = item.getValue();
        total += UTXO.value;
        inputs.add(new TransactionInput(UTXO.id));
        if(total > value) break;
      }

      Transaction newTransaction = new Transaction(publicKey, _recipient , value, inputs);
      newTransaction.generateSignature(privateKey);

      for(TransactionInput input: inputs){
        UTXOs.remove(input.transactionOutputId);
      }
      return newTransaction;
    }

}
