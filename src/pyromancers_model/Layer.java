package pyromancers_model;

import java.util.LinkedList;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Layer extends MapListed {

	private String id;
	public static final String __type = "com.d_paint.data::Layer";
	private LocatorObject data;

	public Layer(JsonElement jsonElement, Map<Integer, UtilityBase> objectMap) {
		super(jsonElement,objectMap);
		//System.out.println(jsonElement.toString());
		if(jsonElement.isJsonObject()) {
			JsonObject jsonObject = (JsonObject) jsonElement;
			id = jsonObject.get("id").getAsString();
			data = (LocatorObject) instanceFactoryByName("data",jsonObject,objectMap);
		}
	}

	public Layer(String _id, LocatorObject _data){
		super();
		id = _id;
		data = _data;
	}
	
	protected boolean reset(){
		boolean wasReset = super.reset();
		if(wasReset){
			data.reset();
		}
		return wasReset;
	}
	
	public String get__type(){
		return __type;
	}
	

	protected int recursiveEnumerate(int starting_value) {
		starting_value = super.recursiveEnumerate(starting_value);
		starting_value = data.enumerate(starting_value);
		return starting_value;
	}

	public StringBuilder toJasonFull(){
		StringBuilder builder = new StringBuilder();
		builder.append("\"id\":\"" + id + "\",");
		builder.append("\"data\":{");
		builder.append(data.toJason());
		builder.append("},");
		append__type(builder,__type);
		builder.append(",");
		append__id(builder);
		return builder;
	}

	public LinkedList<TextureTree> getPacks() {
		LinkedList<TextureTree> list = new LinkedList<TextureTree>();
		list.add(data.getPack());
		return list;
	}

}
