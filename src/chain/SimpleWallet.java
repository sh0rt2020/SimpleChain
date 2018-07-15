package chain;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SimpleWallet {
	
	public PrivateKey privateKey;
	public PublicKey publicKey;
	
	public HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();
	
	//constructor
	public SimpleWallet() {
		generateKeyPair();
	}
	
	//generate key pair
	public void generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			
			keyGen.initialize(ecSpec, random);
			KeyPair keyPair = keyGen.generateKeyPair();
			
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();
		} catch (Exception e) {
			// TODO: handle exception
			throw new RuntimeException(e);
		}
	}
	
	//get wallet balance
	public float getBalance() {
		
		float balance = 0;
		for (Map.Entry<String, TransactionOutput> each : SimpleChain.UTXOs.entrySet()) {
			TransactionOutput utxo = each.getValue();
			if (utxo.isMine(publicKey)) {
				UTXOs.put(utxo.id, utxo);
				balance += utxo.value;
			}
		}
		return balance;
	}
	
	//generate new transaction from this wallet
	public SimpleTransaction send(PublicKey _recipient, float value) {
		
		if (getBalance() < value) {
			System.out.println("has no enough coins. transaction discard.");
			return null;
		}
		
		ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
		
		float total = 0;
		for (Map.Entry<String, TransactionOutput> each : UTXOs.entrySet()) {
			total += each.getValue().value;
			inputs.add(new TransactionInput(each.getValue().id));
			if (total > value) {
				break;
			}
		}
		
		SimpleTransaction newTrans = new SimpleTransaction(publicKey, _recipient, value, inputs);
		newTrans.genSig(privateKey);
		
		for (TransactionInput each : inputs) {
			UTXOs.remove(each.transOutputId);
		}
		
		return newTrans;
	}
}
