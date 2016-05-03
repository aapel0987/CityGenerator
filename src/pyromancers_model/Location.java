package pyromancers_model;

import java.io.FileNotFoundException;
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
	
	public Location(Group group){
		layers = (Bunch) group.render();
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
	
	public StringBuilder toJasonFull(){
		StringBuilder builder = new StringBuilder();
		append__type(builder,__type);
		builder.append(",");
		builder.append("\"layers\":{");
		builder.append(layers.toJason());
		builder.append("},");
		append__id(builder);
		return builder;
	}
	
	public LinkedList<TextureTree> getPacks(){
		return layers.getPacks();
	}
	
	public void writeToFile(String filename){ 
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(filename, "UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	writer.println(this.toJasonHead());
    	writer.close();
	}
	
	public static void writeToFile(Group map, String filename){
		(new Location(map)).writeToFile(filename);
	}
}
