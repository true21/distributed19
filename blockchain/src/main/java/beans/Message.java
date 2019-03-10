package beans;

import java.io.Serializable;
import entities.*;

/*
 * Todo : Message Class Contains anything that will be sent above the network
 */
public class Message implements Serializable {

  public String type;
  public Transaction transaction;

  public Message (String typ, Transaction trans) {
    this.type = typ;
    this.transaction = trans;
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

}
