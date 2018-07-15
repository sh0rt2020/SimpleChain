package chain;

import utils.BaseBlockUtils;
import utils.TransactionAdapter;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.google.gson.GsonBuilder;

public class SimpleChain {
	
	public static ArrayList<BaseBlock> simpleChain = new ArrayList<BaseBlock>();
	public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();
	public static int difficulty = 5;
	public static float minimumTransaction = 0.1f;
	
	public static SimpleWallet wa;
	public static SimpleWallet wb;
	
	public static SimpleTransaction genesisTransaction;
	
	public static void main(String[] args) {
		
		
		Security.addProvider(new BouncyCastleProvider());
		
		wa = new SimpleWallet();
		wb = new SimpleWallet();
		SimpleWallet coinbase = new SimpleWallet();
		
		
		//create genesis transaction
		genesisTransaction = new SimpleTransaction(coinbase.publicKey, wa.publicKey, 100f, null);
		genesisTransaction.genSig(coinbase.privateKey);		
		genesisTransaction.transactionId = "0"; 
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionId));
		UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
		
		System.out.println("Creating and Mining Genesis block...");
		BaseBlock genesisBlock = new BaseBlock("0");
		genesisBlock.addTransaction(genesisTransaction);
		addBlock(genesisBlock);
		
		
		//test send coins
		BaseBlock block1 = new BaseBlock(genesisBlock.hash);
		System.out.println("\nWalletA's balance is: " + wa.getBalance());
		System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
		block1.addTransaction(wa.send(wb.publicKey, 40f));
		addBlock(block1);
		System.out.println("\nWalletA's balance is: " + wa.getBalance());
		System.out.println("WalletB's balance is: " + wb.getBalance());
		
		BaseBlock block2 = new BaseBlock(block1.hash);
		System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
		block2.addTransaction(wa.send(wb.publicKey, 1000f));
		addBlock(block2);
		System.out.println("\nWalletA's balance is: " + wa.getBalance());
		System.out.println("WalletB's balance is: " + wb.getBalance());
		
		BaseBlock block3 = new BaseBlock(block2.hash);
		System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
		block3.addTransaction(wb.send(wa.publicKey, 20));
		System.out.println("\nWalletA's balance is: " + wa.getBalance());
		System.out.println("WalletB's balance is: " + wb.getBalance());
				
		
//		System.out.println("==============wa keypairs==============");
//		System.out.println("wa private key: " + BaseBlockUtils.getStringFromKey(wa.privateKey));
//		System.out.println("wa public key: " + BaseBlockUtils.getStringFromKey(wb.publicKey));
		
//		NoobTransaction transaction = new NoobTransaction(wa.publicKey, wb.publicKey, 5, null);
//		transaction.genSig(wa.privateKey);
//		System.out.println("is signature verified: " + transaction.verifySig());
		
		
		
//		BaseBlock genesisBlock = new BaseBlock("I am the genesis block.", "0");
//		simpleChain.add(genesisBlock);
//		System.out.println("preparing mining");
//		genesisBlock.mine(difficulty);
//		
//		BaseBlock secBlock = new BaseBlock("I am the sec block.", genesisBlock.hash);
//		simpleChain.add(secBlock);
//		System.out.println("preparing mining");
//		secBlock.mine(difficulty);
//		
//		BaseBlock thirBlock = new BaseBlock("I am the thir block.", secBlock.hash);
//		simpleChain.add(thirBlock);
//		System.out.println("preparing mining");
//		thirBlock.mine(difficulty);

		System.out.println("Is chain valid: " + Boolean.toString(isChainValid()));
		
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(SimpleTransaction.class, new TransactionAdapter());
		String simpleChainJson  = builder.setPrettyPrinting().create().toJson(simpleChain);
		System.out.println("===========chain==============");
		System.out.println(simpleChainJson);
//		for (BaseBlock each : simpleChain) {
//			System.out.println(each.transactions);
//			System.out.println("\n");
//		}
	}
	
	
	//check hash in chain
	public static Boolean isChainValid() {
		
		BaseBlock currentBlock;
		BaseBlock previousBlock;
		
		String miningTarget = new String(new char[difficulty]).replace('\0', '0');
		
		HashMap<String, TransactionOutput> tmpUTXOs = new HashMap<String, TransactionOutput>();
		tmpUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
		
		for (int i = 0; i < simpleChain.size()-1; i++) {
			previousBlock = simpleChain.get(i);
			currentBlock = simpleChain.get(i+1);
			
			if (!previousBlock.hash.equals(currentBlock.previousHash)) {
				System.out.println("previous hash does not equal.");
				return false;
			}
			
			if (!currentBlock.hash.equals(currentBlock.calHash())) {
				System.out.println("current hash does not equal.");
				return false;
			}
			
			if (!currentBlock.hash.substring(0, difficulty).equals(miningTarget)) {
				System.out.println("this block was been mined incorrectly.");
				return false;
			}
			
			
			//check chain's transaction
			TransactionOutput tmpOutput;
			for (int j = 0; j < currentBlock.transactions.size(); j++) {
				
				SimpleTransaction curTransaction = currentBlock.transactions.get(j);
				
				if (!curTransaction.verifySig()) {
					System.out.println("signature of transaction (" + j + ") is invalid.");
					return false;
				}
				
				if (curTransaction.getInputsValue() != curTransaction.getOutputsValue()) {
					System.out.println("inputs and outputs are not equal on transaction (" + j + ")");
					return false;
				}
				
				for (TransactionInput each : curTransaction.inputs) {
					tmpOutput = tmpUTXOs.get(each.transOutputId);
					
					if(tmpOutput == null) {
						System.out.println("#Referenced input on Transaction(" + j + ") is Missing");
						return false;
					}
					
					if(each.UTXO.value != tmpOutput.value) {
						System.out.println("#Referenced input Transaction(" + j + ") value is Invalid");
						return false;
					}
					
					tmpUTXOs.remove(each.transOutputId);
				}
				
				for(TransactionOutput each : curTransaction.outputs) {
					tmpUTXOs.put(each.id, each);
				}
				
				if( curTransaction.outputs.get(0).recipient != curTransaction.recipient) {
					System.out.println("#Transaction(" + j + ") output reciepient is not who it should be");
					return false;
				}
				if( curTransaction.outputs.get(1).recipient != curTransaction.sender) {
					System.out.println("#Transaction(" + j + ") output 'change' is not sender.");
					return false;
				}
			}
		}
		return true;
	}
	
	//add new block to chain
	public static void addBlock(BaseBlock newBlock) {
		newBlock.mine(difficulty);
		simpleChain.add(newBlock);
	}
}
