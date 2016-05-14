package pyromancers_model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

abstract public class UtilityBase implements Cloneable {

	private int __id;
	private boolean wasPrinted;
	private boolean enumerating;
	
	protected interface JsonWriter{
		public abstract void jsonWrite(String str);
	}
	
	protected class JsonFileWriter extends BufferedWriter implements JsonWriter{

		public JsonFileWriter(String arg0) throws IOException {
			super(new FileWriter(arg0));
		}

		public void jsonWrite(String str) {
			try {
				this.write(str);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public UtilityBase(JsonElement jsonElement, Map<Integer,UtilityBase> objectMap){
		this();
		__id = getElementId(jsonElement);
		objectMap.put(__id, this);
	}
	
	public UtilityBase(){
		__id = -1;
		wasPrinted = false;
		enumerating = false;
	}
	
	protected int get__id(){
		return __id;
	}
	
	public final void enumerate(){
		enumerate(1);
	}
	
	public final int enumerate(int starting_value){
		if(!enumerating){
			enumerating = true;
			starting_value = recursiveEnumerate(starting_value);
			enumerating = false;
		}
		return starting_value;
	}
	
	protected int recursiveEnumerate(int starting_value){
		__id = starting_value++;
		return starting_value;
	}
	
	public void append__type(JsonWriter writer, String __type){
		writer.jsonWrite("\"__type\":\"" + __type + "\"");
	}
	
	abstract public void toJasonFull(JsonWriter writer);
		
	abstract public String get__type();
			
	protected boolean reset(){
		boolean toReturn = wasPrinted;
		__id = -1;
		wasPrinted = false;
		return toReturn;
	}
	
	public final void toJason(JsonWriter writer){
		if(!wasPrinted){
			wasPrinted = true;
			toJasonFull(writer);
		} else {
			toJasonSmall(writer);
		}
	}
	
	public final void toJasonHead(JsonWriter writer){
		this.reset();
		this.enumerate();
		writer.jsonWrite("{");
		toJason(writer);
		writer.jsonWrite("}");
	}
	
	private final void toJasonSmall(JsonWriter writer){
		append__id(writer);
		writer.jsonWrite(",");
		append__type(writer,get__type());
	}
	
	public void append__id(JsonWriter writer){
		append__id(writer, __id);
	}

	public void append__id(JsonWriter writer, int value){
		writer.jsonWrite("\"__id\":" + value);
	}
	
	public void appendOnOff(JsonWriter writer, String name, boolean value){
		if(value) writer.jsonWrite("\"" + name + "\":\"on\"");
		else writer.jsonWrite("\"" + name + "\":\"off\"");
	}
		
	public static boolean parseOnOff(JsonElement jsonElement, String fieldName){
		String result = ((JsonObject) jsonElement).get(fieldName).getAsString();
		return result.equalsIgnoreCase("on");
	}
	
	public static int getElementId(JsonElement jsonElement){
		int toReturn;
		if(!jsonElement.isJsonObject()) return -1;
		else if(((JsonObject) jsonElement).get("__id").isJsonNull()) return -1;
		else toReturn = ((JsonObject) jsonElement).get("__id").getAsInt();
		return toReturn;
	}
	
	public static boolean mapContainsElement(JsonElement jsonElement, Map<Integer,UtilityBase> objectMap){
		int local_id = getElementId(jsonElement);
		return objectMap.containsKey(local_id);
	}
	
	public static UtilityBase getElementFromMap(JsonElement jsonElement, Map<Integer,UtilityBase> objectMap){
		int local_id = getElementId(jsonElement);
		if(local_id == -1) return null;
		return objectMap.get(local_id);
	}
	
	public static UtilityBase instanceFactory(JsonElement jsonElement, Map<Integer,UtilityBase> objectMap){
		if(jsonElement == null) return null;
		if(mapContainsElement(jsonElement,objectMap)){
			return getElementFromMap(jsonElement,objectMap);
		}
		if(!jsonElement.isJsonObject()) throw new IllegalArgumentException("Trying to parse JsonElement as object failed: " + jsonElement.toString()); 
		UtilityBase toReturn = null;
		switch(((JsonObject)jsonElement).get("__type").getAsString()){
			case Location.__type		: toReturn = new Location(jsonElement,objectMap); break;
			case Bunch.__type			: toReturn = new Bunch(jsonElement,objectMap); break;
			case Layer.__type			: toReturn = new Layer(jsonElement,objectMap); break;
			case Obstical.__type		: toReturn = new Obstical(jsonElement,objectMap); break;
			case Polygon.__type			: toReturn = new Polygon(jsonElement,objectMap); break;
			case Plot.__type			: toReturn = new Plot(jsonElement,objectMap); break;
			case Brush.__type			: toReturn = new Brush(jsonElement,objectMap); break;
			case Point.__type			: toReturn = new Point(jsonElement,objectMap); break;
			case MapItem.__type			: toReturn = new MapItem(jsonElement,objectMap); break;
			case TextureTree.__type		: toReturn = new TextureTree(jsonElement,objectMap); break;
			case TextureMode.__type		: toReturn = new TextureMode(jsonElement,objectMap); break;
			case TileGroup.__type		: toReturn = new TileGroup(jsonElement,objectMap); break;
			case EnumerableMap.__type	: toReturn = new EnumerableMap<UtilityBase>(jsonElement,objectMap); break;
			default: throw new IllegalArgumentException("Unknown Source Type: " + ((JsonObject)jsonElement).get("__type").getAsString());
		}
		return toReturn;
	}
	
	public static UtilityBase instanceFactoryByName(String name, JsonElement jsonElement, Map<Integer,UtilityBase> objectMap){
		JsonElement jsonActual = ((JsonObject) jsonElement).get(name);
		return instanceFactory(jsonActual,objectMap);
	}
	
}
