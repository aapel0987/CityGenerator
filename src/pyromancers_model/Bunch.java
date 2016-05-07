package pyromancers_model;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Bunch extends MapListed {

	private String id;
	public static final String __type = "com.bongo.layers.logic::Bunch";
	private LinkedList<MapListed> source;

	public Bunch(String name){
		super();
		id = name;
		source = new LinkedList<MapListed>();
	}
	
	public Bunch(String name, Collection<MapListed> mapListedCollection){
		this(name);
		source.addAll(mapListedCollection);
	}
	
	public Bunch(JsonElement jsonElement, Map<Integer,UtilityBase> objectMap) {
		super(jsonElement,objectMap);
		source = new LinkedList<MapListed>();
		JsonObject jsonObject = (JsonObject) jsonElement;
		id = jsonObject.get("id").getAsString();
		JsonArray sourceArray = jsonObject.get("source").getAsJsonArray();
		for(JsonElement sourceElement : sourceArray){
			source.add((MapListed) instanceFactory(sourceElement,objectMap));
		}
	}
	
	protected void addSource(MapListed item){
		source.add(item);
	}
	
	protected boolean reset(){
		boolean wasReset = super.reset();
		if(wasReset){
			for(MapListed item : source) item.reset();
		}
		return wasReset;
	}
	
	public String get__type(){
		return __type;
	}
	

	protected int recursiveEnumerate(int starting_value) {
		starting_value = super.recursiveEnumerate(starting_value);
		for(MapListed item : source)
			starting_value = item.enumerate(starting_value);
		return starting_value;
	}

	public StringBuilder toJasonFull(){
		StringBuilder builder = new StringBuilder();
		builder.append("\"id\":\"" + id + "\",");
		append__type(builder,__type);
		builder.append(",");
		append__id(builder);
		builder.append(",");
		builder.append("\"source\":[");
		for(Iterator<MapListed> iter = source.iterator(); iter.hasNext(); ){
			builder.append("{");
			builder.append(iter.next().toJason());
			builder.append("}");
			if(iter.hasNext()) builder.append(",");
		}
		builder.append("]");
		return builder;
	}

	public LinkedList<TextureTree> getPacks() {
		LinkedList<TextureTree> list = new LinkedList<TextureTree>(); 
		for(MapListed item : source) list.addAll(item.getPacks());
		return list;
	}

}
