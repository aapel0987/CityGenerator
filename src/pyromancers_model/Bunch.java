package pyromancers_model;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import map_structure.Generateable;

public class Bunch extends MapListed {

	private String id;
	public static final String __type = "com.bongo.layers.logic::Bunch";
	private LinkedList<MapListed> source;

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
	
	public Bunch(String name, Collection<Generateable> members){
		super();
		id = name;
		source = new LinkedList<MapListed>();
		List<Generateable> localMembers = new LinkedList<Generateable>(members);
		Collections.reverse(localMembers); 
		for(Generateable member : localMembers){
			source.add(member.render());
		}
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
