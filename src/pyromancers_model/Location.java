package pyromancers_model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import map_structure.Group;

public class Location extends UtilityBase {
	
	private Bunch layers;
	public static final String __type = "com.d_paint.data::Location";
		
	public Location(JsonElement jsonElement, Map<Integer,UtilityBase> objectMap) {
		super(jsonElement,objectMap);
		if(jsonElement.isJsonObject()) {
			JsonObject jsonObject = (JsonObject) jsonElement;
			layers = (Bunch) instanceFactoryByName("layers",jsonObject,objectMap);
			//System.out.println(jsonLayers.toString());
		}
	}
	
	public Location(Bunch map){
		layers = map;
	}
	
	protected boolean reset(){
		boolean wasReset = super.reset();
		if(wasReset){
			layers.reset();
		}
		return wasReset;
	}
	
	public String get__type(){
		return __type;
	}
	
	protected int recursiveEnumerate(int starting_value) {
		starting_value = super.recursiveEnumerate(starting_value);
		starting_value = layers.enumerate(starting_value);
		return starting_value;
	}
	
	public void toJasonFull(JsonWriter writer){
		append__type(writer,__type);
		writer.jsonWrite(",");
		writer.jsonWrite("\"layers\":{");
		layers.toJason(writer);
		writer.jsonWrite("},");
		append__id(writer);
	}
	
	public LinkedList<TextureTree> getPacks(){
		return layers.getPacks();
	}
	
	public void writeToFile(String filename){ 
		JsonFileWriter writer = null;
		try {
			writer = new JsonFileWriter(filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	this.toJasonHead(writer);
    	try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
