package pyromancers_model;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class LocatorObject extends UtilityBase {

	protected MapItem actual;
	
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

	public StringBuilder actualToJason(){
		StringBuilder builder = new StringBuilder();
		builder.append("\"actual\":{");
		builder.append(actual.toJason());
		builder.append("}");
		return builder;
	}
}
