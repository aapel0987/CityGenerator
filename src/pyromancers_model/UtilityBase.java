package pyromancers_model;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

abstract public class UtilityBase implements Cloneable {

	private int __id;
	private boolean wasPrinted;
	private boolean enumerating;
	
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
	
	public StringBuilder append__type(StringBuilder builder, String __type){
		return builder.append("\"__type\":\"" + __type + "\"");
	}
	
	abstract public StringBuilder toJasonFull();
		
	abstract public String get__type();
			
	protected boolean reset(){
		boolean toReturn = wasPrinted;
		__id = -1;
		wasPrinted = false;
		return toReturn;
	}
	
	public final StringBuilder toJason(){
		if(!wasPrinted){
			wasPrinted = true;
			return toJasonFull();
		}
		return toJasonSmall();
	}
	
	public final StringBuilder toJasonHead(){
		StringBuilder builder = new StringBuilder();
		this.reset();
		this.enumerate();
		builder.append("{");
		builder.append(toJason());
		builder.append("}");
		return builder;
	}
	
	private StringBuilder toJasonSmall(){
		StringBuilder builder = new StringBuilder();
		append__id(builder);
		builder.append(",");
		append__type(builder,get__type());
		return builder;
	}
	
	public StringBuilder append__id(StringBuilder builder){
		return append__id(builder, __id);
	}

	public StringBuilder append__id(StringBuilder builder, int value){
		return builder.append("\"__id\":" + value);
	}
	
	public StringBuilder appendOnOff(StringBuilder builder, String name, boolean value){
		if(value) return builder.append("\"" + name + "\":\"on\"");
		return builder.append("\"" + name + "\":\"off\"");
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
