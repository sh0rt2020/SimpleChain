package chain;
import java.util.ArrayList;
import java.util.Date;

import utils.BaseBlockUtils;

public class BaseBlock {
	
	public String hash;
	public String previousHash;
//	private String data;  //stored in block chain.
	private long timeStamp; 
	private int nonce;
	
	public String merkleRoot;
	public ArrayList<SimpleTransaction> transactions= new ArrayList<SimpleTransaction>();;
	
	//constructor
	public BaseBlock(String previousHash) {
//		this.data = data;
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
		this.hash = calHash();
	}
	
	//calculate hash 
	public String calHash() {
		
		String caledHash = BaseBlockUtils.sha256(
				previousHash + 
				Long.toString(timeStamp) + 
				Integer.toString(nonce) +
				merkleRoot);
		return caledHash;
	}
	
	//mine
	public void mine(int difficulty) {
		
		merkleRoot = BaseBlockUtils.getMerkleRoot(transactions);
		String target = new String(new char[difficulty]).replace('\0', '0');
		while (!hash.substring(0, difficulty).equals(target)) {
			
			nonce++;
			hash = calHash();
		}
		System.out.println("New block mined : " + hash);
	}
	
	
	//add transaction to this block
	public boolean addTransaction(SimpleTransaction transaction) {
		
		if (transaction == null) {
			return false;
		}
		
		if (previousHash != "0") {
			if (!transaction.processTrans()) {
				System.out.println("Failed to process transaction.");
				return false;
			}
		}
		
		transactions.add(transaction);
		System.out.println("Succeed add transaction to block.");
		return true;
	}
}
