package threads;
import java.net.InetAddress;
import java.security.*;
import java.io.Serializable;


public class Node implements Serializable{
	private int idx;
	private InetAddress ip;
	private int prt;
	private PublicKey publicKey;

	public Node(int index, InetAddress i, int port, PublicKey key){
		idx = index;
		ip = i;
		prt = port;
		publicKey = key;
	}

	public int getPort(){
		return prt;
	}

	public void setIndex(int i){
		idx = i;
	}

	public void setIP(InetAddress i){
		ip = i;
	}

	public InetAddress getIP(){
		return this.ip;
	}

	public int getIndex(){
		return this.idx;
	}

	public PublicKey getPublicKey(){
		return this.publicKey;
	}

	public void setPort(int i){
		prt = i;
	}

	public void setKey(PublicKey i){
		publicKey = i;
	}

}
