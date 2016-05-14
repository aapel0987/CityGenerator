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

	public void toJasonFull(JsonWriter writer){
		writer.jsonWrite("\"group_folder\":\"" + group_folder + "\",");
		writer.jsonWrite("\"pack\":{");
		pack.toJason(writer);
		writer.jsonWrite("},");
		append__type(writer,__type);
		writer.jsonWrite(",");
		append__id(writer);
		writer.jsonWrite(",\"id\":" + id);
		writer.jsonWrite(",\"tilex\":" + tilex);
		writer.jsonWrite(",\"tiley\":" + tiley);
	}

	public TextureTree getPack() {
		return pack;
	}
}
