package utils;

import java.security.Key;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;

import chain.SimpleTransaction;

public class BaseBlockUtils {
	
	public static String sha256(String input) {
		
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(input.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer();
			
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (Exception e) {
			// TODO: handle exception
			throw new RuntimeException(e);
		}
	}
	
	//generator transaction signature
	public static byte[] ecdsaSig(PrivateKey privateKey, String input) {
		
		Signature dsa;
		byte[] output = new byte[0];
		try {
			dsa = Signature.getInstance("ECDSA", "BC");
			dsa.initSign(privateKey);
			byte[] strByte = input.getBytes();
			dsa.update(strByte);
			byte[] realSig = dsa.sign();
			output = realSig;
		} catch (Exception e) {
			// TODO: handle exception
			throw new RuntimeException(e);
		}
		
		return output;
	}
	
	//verify transaction signature
	public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
		
		try {
			Signature ecdsaSig = Signature.getInstance("ECDSA", "BC");
			ecdsaSig.initVerify(publicKey);
			ecdsaSig.update(data.getBytes());
			return ecdsaSig.verify(signature);
		} catch (Exception e) {
			// TODO: handle exception
			throw new RuntimeException(e);
		}
	}
	
	//transform key to string
	public static String getStringFromKey(Key key) {
		
	    return java.util.Base64.getEncoder().encodeToString(key.getEncoded());
	}
	
	//get merkle root transaction id
	public static String getMerkleRoot(ArrayList<SimpleTransaction> transactions) {
		int count = transactions.size();
		ArrayList<String> previousTreeLayers = new ArrayList<String>();
		
		for (SimpleTransaction each : transactions) {
			previousTreeLayers.add(each.transactionId);
		}
		
		ArrayList<String> treeLayers = previousTreeLayers;
		while (count > 1) {
			treeLayers = new ArrayList<String>();
			for (int i = 1; i < previousTreeLayers.size(); i++) {
				treeLayers.add(sha256(previousTreeLayers.get(i-1) + previousTreeLayers.get(i)));
			}
			count = treeLayers.size();
			previousTreeLayers = treeLayers;
		}
		
		String merkleRoot = (treeLayers.size() == 1) ? treeLayers.get(0) : "";
		return merkleRoot;
	}
}
