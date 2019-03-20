package beans;

import java.io.Serializable;
import entities.*;

/*
 * Todo : Message Class Contains anything that will be sent above the network
 */
public class Message implements Serializable {

  private String type;
  private Transaction transaction;
  private Block block;
  private Blockchain blockchain;


  public Message (String typ, Transaction trans) {
    this.type = typ;
    this.transaction = trans;
  }

  public Message (String typ, Block blo) {
    this.type = typ;
    this.block = blo;
  }

  public Message (String typ, Blockchain bc) {
    this.type = typ;
    this.blockchain = bc;
  }

  public Message (String typ) {
    this.type = typ;
  }

  public String getType() {
    return this.type;
  }

  public void setType(String typ) {
    this.type = typ;
  }

  public Transaction getTransaction() {
    return this.transaction;
  }

  public void setTransaction(Transaction trans) {
    this.transaction = trans;
  }

  public Block getBlock() {
    return this.block;
  }

  public void setBlock(Block blo) {
    this.block = blo;
  }

  public Blockchain getBlockchain() {
    return this.blockchain;
  }

}
