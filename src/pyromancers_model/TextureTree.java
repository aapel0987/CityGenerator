package pyromancers_model;

import java.util.ArrayList;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TextureTree extends UtilityBase {

	private String title;
	private boolean is_local;
	public static final String __type = "com.locator.logic::TextureTree";
	private double shadow;
	private String folder;
	private boolean enable_printing;
	private EnumerableMap<TextureMode> modes;
	
	//Ignored values?
	private static final int pa = 200;
	private static final int pb = 200;

	public TextureTree(JsonElement jsonElement, Map<Integer, UtilityBase> objectMap) {
		super(jsonElement,objectMap);
		//System.out.println(jsonElement.toString());
		if(jsonElement.isJsonObject()) {
			JsonObject jsonObject = (JsonObject) jsonElement;
			title 			= jsonObject.get("title").getAsString();
			folder 			= jsonObject.get("folder").getAsString();
			shadow 			= jsonObject.get("shadow").getAsDouble();
			enable_printing	= parseOnOff(jsonObject,"enable_printing");
			modes			= (EnumerableMap<TextureMode>) instanceFactoryByName("modes",jsonObject,objectMap);
		}
	}
	
	public ArrayList<MapItem> getMapItem(String folder, int id){
		for(TextureMode mode : modes.values()){
			MapItem item = mode.getMapItem(folder,id);
			if(item != null){
				ArrayList<MapItem> list = new ArrayList<MapItem>();
				list.add(item);
				return list;
			}
		}
		return null;
	}
	
	public ArrayList<MapItem> getMapItems(String folder){
		for(TextureMode mode : modes.values()){
			ArrayList<MapItem> list = mode.getMapItems(folder);
			if(list != null){
				return list;
			}
		}
		return null;
	}
	
	protected boolean reset(){
		boolean wasReset = super.reset();
		if(wasReset){
			modes.reset();
		}
		return wasReset;
	}
	
	public String get__type(){
		return __type;
	}

	public String get_title(){
		return title;
	}
	
	protected int recursiveEnumerate(int starting_value) {
		starting_value = super.recursiveEnumerate(starting_value);
		starting_value = modes.enumerate(starting_value);
		return starting_value;
	}
	
	public void toJasonFull(JsonWriter writer){
		writer.jsonWrite("\"folder\":\"" + folder + "\",");
		append__type(writer,__type);
		writer.jsonWrite(",");
		append__id(writer);
		writer.jsonWrite(",\"title\":\"" + title + "\",");
		appendOnOff(writer,"enable_printing",enable_printing);
		writer.jsonWrite(",");
		writer.jsonWrite("\"modes\":{");
		modes.toJason(writer);
		writer.jsonWrite("},\"pa\":" + pa);
		writer.jsonWrite(",\"shadow\":" + shadow);
		writer.jsonWrite(",\"is_local\":" + is_local);
		writer.jsonWrite(",\"pb\":" + pb);
	}
}
