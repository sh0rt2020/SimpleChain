package utils;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import chain.SimpleTransaction;

//customize gson serialization
public class TransactionAdapter implements JsonSerializer<SimpleTransaction> {
	
	@Override
	public JsonElement serialize(SimpleTransaction src, Type type, JsonSerializationContext context) {
		// TODO Auto-generated method stub
		
		JsonObject jsonObj = new JsonObject();
		jsonObj.addProperty("transactionId", src.transactionId);;
		jsonObj.addProperty("value", src.value);
		jsonObj.addProperty("sender", BaseBlockUtils.getStringFromKey(src.sender));
		jsonObj.addProperty("recipient", BaseBlockUtils.getStringFromKey(src.recipient));
		
		jsonObj.remove("signature");
		jsonObj.addProperty("signature", src.getStringFromBytes());
		
		jsonObj.add("inputs",  new Gson().toJsonTree(src.inputs));
		jsonObj.add("outputs", new Gson().toJsonTree(src.outputs));
		return jsonObj;
	}
	
	
}
