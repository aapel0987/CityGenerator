package pyromancers_model;

import java.util.Map;
import java.util.Random;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import pyromancers_model.UtilityBase.JsonWriter;

public abstract class LocatorObject extends UtilityBase {

	protected MapItem actual;
	protected final static Random random = new Random();
	
	public LocatorObject(JsonElement jsonElement,  Map<Integer, UtilityBase> objectMap) {
		super(jsonElement,objectMap);
		if(jsonElement.isJsonObject()) {
			JsonObject jsonObject = (JsonObject) jsonElement;
			actual = (MapItem) instanceFactoryByName("actual",jsonObject,objectMap);
		}
	}
	
	public LocatorObject(MapItem _actual){
		setActual(_actual);
	}
	
	public void setActual(MapItem _actual){
		actual = _actual;
	}
	
	protected boolean reset(){
		boolean wasReset = super.reset();
		if(wasReset){
			actual.reset();
		}
		return wasReset;
	}
	
	protected int recursiveEnumerate(int starting_value) {
		starting_value = super.recursiveEnumerate(starting_value);
		starting_value = actual.enumerate(starting_value);
		return starting_value;
	}

	public TextureTree getPack() {
		return actual.getPack();
	}

	public void actualToJason(JsonWriter writer){
		writer.jsonWrite("\"actual\":{");
		actual.toJason(writer);
		writer.jsonWrite("}");
	}
}
