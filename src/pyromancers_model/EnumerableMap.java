package pyromancers_model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class EnumerableMap<V extends UtilityBase> extends UtilityBase implements Map<String,V> {

	public static final String __type = "Object";
	private HashMap<String,V> localMap; 

	public EnumerableMap(JsonElement jsonElement, Map<Integer, UtilityBase> objectMap) {
		super(jsonElement,objectMap);
		//System.out.println(jsonElement.toString());
		localMap = new HashMap<String,V>();
		if(jsonElement.isJsonObject()) {
			JsonObject jsonObject = (JsonObject) jsonElement;
			for (Map.Entry<String,JsonElement> entry : jsonObject.entrySet()){
				//System.out.println("Key: " + entry.getKey() + "\nValue: " + jsonObject.get(entry.getKey()).toString());
				if(!entry.getKey().equalsIgnoreCase("__type") && !entry.getKey().equalsIgnoreCase("__id")){
					localMap.put(entry.getKey(), (V) instanceFactoryByName(entry.getKey(),jsonObject,objectMap));
				}
			}
		}
	}
	
	protected boolean reset(){
		boolean wasReset = super.reset();
		if(wasReset){
			for(UtilityBase entry : localMap.values()) entry.reset();
		}
		return wasReset;
	}
	
	public String get__type(){
		return __type;
	}
	
	protected int recursiveEnumerate(int starting_value) {
		starting_value = super.recursiveEnumerate(starting_value);
		for(UtilityBase entry : localMap.values()) starting_value = entry.enumerate(starting_value);
		return starting_value;
	}
	
	public StringBuilder toJasonFull(){
		StringBuilder builder = new StringBuilder();
		append__type(builder,__type);
		builder.append(",");
		append__id(builder);
		for(String key : localMap.keySet()){
			builder.append(",");
			builder.append("\"" + key + "\":{");
			builder.append(localMap.get(key).toJason());
			builder.append("}");
		}
		return builder;
	}

	public void clear() {
		localMap.clear();
	}

	public boolean containsKey(Object arg0) {
		return localMap.containsKey(arg0);
	}

	public boolean containsValue(Object arg0) {
		return localMap.containsValue(arg0);
	}

	public Set<java.util.Map.Entry<String, V>> entrySet() {
		return localMap.entrySet();
	}

	public V get(Object arg0) {
		return localMap.get(arg0);
	}

	public boolean isEmpty() {
		return localMap.isEmpty();
	}

	public Set<String> keySet() {
		return localMap.keySet();
	}

	public V put(String arg0, V arg1) {
		return localMap.put(arg0, arg1);
	}

	public void putAll(Map<? extends String, ? extends V> arg0) {
		localMap.putAll(arg0);		
	}

	public V remove(Object arg0) {
		return localMap.remove(arg0);
	}

	public int size() {
		return localMap.size();
	}

	public Collection<V> values() {
		return localMap.values();
	}

}
