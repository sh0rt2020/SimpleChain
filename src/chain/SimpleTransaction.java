package chain;

import java.security.*;
import java.util.ArrayList;

import utils.BaseBlockUtils;

public class SimpleTransaction {
	
	public String transactionId; //the hash of transaction
	public PublicKey sender;
	public PublicKey recipient;
	public float value;
	public byte[] signature;
	
	public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	
	private static int sequence = 0; //a rough count of how many transactions have been generated
	
	//constructor
	public SimpleTransaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.recipient = to;
		this.value = value;
		this.inputs = inputs;
	}
	
	
	//calculate transaction hash to be id
	private String calTransId() {
		
		sequence++;
		
		return BaseBlockUtils.sha256(
				BaseBlockUtils.getStringFromKey(sender) + 
				BaseBlockUtils.getStringFromKey(recipient) + 
				Float.toString(value) + 
				sequence
				);
	}
	
	
	public void genSig(PrivateKey privateKey) {
		
		String data = BaseBlockUtils.getStringFromKey(sender) +
				BaseBlockUtils.getStringFromKey(recipient) +
				Float.toString(value);
		signature = BaseBlockUtils.ecdsaSig(privateKey, data);
	}
	
	
	public boolean verifySig() {
		
		String data = BaseBlockUtils.getStringFromKey(sender) +
				BaseBlockUtils.getStringFromKey(recipient) +
				Float.toString(value);
		
		return BaseBlockUtils.verifyECDSASig(sender, data, signature);
	}
	
	
	public boolean processTrans() {
		
		if (verifySig() == false) {
			System.out.println("transaction signature failed to verify.");
			return false;
		}
		
		for (TransactionInput each : inputs) {
			each.UTXO = SimpleChain.UTXOs.get(each.transOutputId);
		}
		
		if (getInputsValue() < SimpleChain.minimumTransaction) {
			System.out.println("inputs utxo's value sum is too small: " + getInputsValue());
			return false;
		}
		
		float leftValue = getInputsValue()-value;  //value left
		transactionId = calTransId();
		//construct transactionOutput for recipient and sender 
		outputs.add(new TransactionOutput(this.recipient, value, transactionId));
		outputs.add(new TransactionOutput(this.sender, leftValue, transactionId));
		
		for (TransactionOutput each : outputs) {
			SimpleChain.UTXOs.put(each.id, each);
		}
		
		//remove utxo already spent from chain
		for (TransactionInput each : inputs) {
			if (each.UTXO == null) {
				continue ;
			}
			SimpleChain.UTXOs.remove(each.UTXO.id);
		}
		return true;
	}
	
	//get all inputs utxo's value sum
	public float getInputsValue() {
		float total = 0;
		for (TransactionInput each: inputs) {
			if (each.UTXO == null) {
				continue ;
			}
			total += each.UTXO.value;
		}
		return total;
	}
	
	//get all outputs value sum
	public float getOutputsValue() {
		float total = 0;
		for (TransactionOutput each: outputs) {
			total += each.value;
		}
		return total;
	}
	
	
	//helper: transform byte[] to string
	public String getStringFromBytes() {
		
		StringBuffer hexString = new StringBuffer(); 
		for (int i = 0; i < signature.length; i++) {
		String hex = Integer.toHexString(0xff & signature[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}
	
	
//	@Override
//	public String toString() {
//		
//		return "{" +
//				"transactionId : " + this.transactionId + "," +
//				"sender : " + this.sender + "," +
//				"recipient : " + this.recipient + "," +
//				"value : " + this.value + "," +
//				"signature : " + getStringFromBytes() + "," +
//				"}";
//	}
	
}
