package pyromancers_model;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MapItem extends UtilityBase {

	private int id;
	private String group_folder;
	private TextureTree pack;
	public static final String __type = "com.locator.data::MapItem";
	
	//Ignored values?
	private static final int tilex = 0;
	private static final int tiley = 0;

	public MapItem(JsonElement jsonElement, Map<Integer, UtilityBase> objectMap) {
		super(jsonElement,objectMap);
		//System.out.println(jsonElement.toString());
		if(jsonElement.isJsonObject()) {
			JsonObject jsonObject = (JsonObject) jsonElement;
			id	 			= jsonObject.get("id").getAsInt();
			group_folder	= jsonObject.get("group_folder").getAsString();
			pack = (TextureTree) instanceFactoryByName("pack",jsonObject,objectMap);
		}
	}
	
	protected boolean reset(){
		boolean wasReset = super.reset();
		if(wasReset){
			pack.reset();
		}
		return wasReset;
	}
	
	public String get__type(){
		return __type;
	}

	public int getMapItemID(){
		return id;
	}

	protected int recursiveEnumerate(int starting_value) {
		starting_value = super.recursiveEnumerate(starting_value);
		starting_value = pack.enumerate(starting_value);
		return starting_value;
	}

	public StringBuilder toJasonFull(){
		StringBuilder builder = new StringBuilder();
		builder.append("\"group_folder\":\"" + group_folder + "\",");
		builder.append("\"pack\":{");
		builder.append(pack.toJason());
		builder.append("},");
		append__type(builder,__type);
		builder.append(",");
		append__id(builder);
		builder.append(",\"id\":" + id);
		builder.append(",\"tilex\":" + tilex);
		builder.append(",\"tiley\":" + tiley);
		return builder;
	}

	public TextureTree getPack() {
		return pack;
	}
}
