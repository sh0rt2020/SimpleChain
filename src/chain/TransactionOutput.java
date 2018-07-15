package chain;

import java.security.PublicKey;

import utils.BaseBlockUtils;

public class TransactionOutput {

	public String id;
	public PublicKey recipient; //the new owner
	public float value; //
	public String parentTransId;
	
	//constructor
	public TransactionOutput(PublicKey recipient, float value, String parentTransId) {
		// TODO Auto-generated constructor stub
		this.recipient = recipient;
		this.value = value;
		this.parentTransId = parentTransId;
		this.id = BaseBlockUtils.sha256(BaseBlockUtils.getStringFromKey(recipient)+Float.toString(value)+parentTransId);
	}
	
	//is transaction/coin mine
	public boolean isMine(PublicKey publicKey) {
		return (publicKey == recipient);
	}
}
