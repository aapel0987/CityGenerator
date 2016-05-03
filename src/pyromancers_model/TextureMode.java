package pyromancers_model;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TextureMode extends UtilityBase {

	public static final String __type = "com.locator.logic::TextureMode";
	private EnumerableMap<TileGroup> groups;
	
	public TextureMode(JsonElement jsonElement, Map<Integer, UtilityBase> objectMap) {
		super(jsonElement,objectMap);
		//System.out.println(jsonElement.toString());
		if(jsonElement.isJsonObject()) {
			JsonObject jsonObject = (JsonObject) jsonElement;
			groups	= (EnumerableMap<TileGroup>) instanceFactoryByName("groups",jsonObject,objectMap);
		}
	}

	protected boolean reset(){
		boolean wasReset = super.reset();
		if(wasReset){
			groups.reset();
		}
		return wasReset;
	}
	
	protected int recursiveEnumerate(int starting_value) {
		starting_value = super.recursiveEnumerate(starting_value);
		starting_value = groups.enumerate(starting_value);
		return starting_value;
	}
	
	public String get__type(){
		return __type;
	}
	

	public StringBuilder toJasonFull(){
		StringBuilder builder = new StringBuilder();
		append__type(builder,__type);
		builder.append(",");
		builder.append("\"groups\":{");
		builder.append(groups.toJason());
		builder.append("},");
		append__id(builder);
		return builder;
	}

	public MapItem getMapItem(String folder, int id) {
		for(TileGroup group : groups.values()){
			MapItem item = group.getMapItem(folder,id);
			if(item != null) return item;
		}
		return null;
	}
}
