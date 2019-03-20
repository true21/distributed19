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
        //System.out.println("it's me");
        //Security.addProvider(new BouncyCastleProvider());
        /* Provider[] provs = Security.getProviders();
        for(int i=0; i<provs.length;i++){
            System.out.println(provs[i]);
        }*/
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");

        // Initialize KeyPairGenerator.
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(1024, random);

        // Generate Key Pairs, a private key and a public key.
        KeyPair keyPair = keyGen.generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
        /*KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
        System.out.println("it's you");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
        // Initialize the key generator and generate a KeyPair
        keyGen.initialize(ecSpec, random);   //256 bytes provides an acceptable security level
        KeyPair keyPair = keyGen.generateKeyPair();
        // Set the public and private keys from the keyPair
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic(); */
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

    public float getBalance(Blockchain blockchain) {
      UTXOs.clear();
      float total = 0;
      System.out.println("------- HashMap Blockchain's UTXOs -------");
      for (Map.Entry<String, TransactionOutput> item: blockchain.getUTXOs().entrySet()){
        TransactionOutput UTXO = item.getValue();
        System.out.println("------- " + UTXO.id + " -------");
        if(UTXO.isMine(publicKey)) { //if output belongs to me ( if coins belong to me )
          UTXOs.put(UTXO.id,UTXO); //add it to our list of unspent transactions.
          total += UTXO.value;
          System.out.println("UTXO.value = " + UTXO.value);
        }
      }
      return total;
    }

    public float getBalanceClient(Blockchain blockchain) {
      float total = 0;
      for (Map.Entry<String, TransactionOutput> item: blockchain.getUTXOs().entrySet()){
        TransactionOutput UTXO = item.getValue();
        if(UTXO.isMine(publicKey)) { //if output belongs to me ( if coins belong to me )
          total += UTXO.value;
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
    public Transaction sendFunds(PublicKey _recipient, float value, Blockchain blockchain/*HashMap<String,TransactionOutput> allUTXOs*/) {
      System.out.println("My balance is: " + getBalanceClient(blockchain) + ", and I wanna send: " + value);

      if(getBalance(blockchain) < value) { //gather balance and check funds.
        System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
        return null;
      }
      System.out.println("#Enough funds to send transaction. Transaction not Discarded.");
      //create array list of inputs
      ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

      float total = 0;
      for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()) {
        TransactionOutput UTXO = item.getValue();
        total += UTXO.value;
        inputs.add(new TransactionInput(UTXO.id));
        System.out.println("------------------------------------ Added input: " + UTXO.id);
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
