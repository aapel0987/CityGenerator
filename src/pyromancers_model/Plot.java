package pyromancers_model;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Plot extends LocatorObject {


	public static final String __type = "com.locator.objs::Plot";
	private Point begin;
	private Point end;
	
	public Plot(JsonElement jsonElement, Map<Integer, UtilityBase> objectMap) {
		super(jsonElement,objectMap);
		//System.out.println(jsonElement.toString());
		if(jsonElement.isJsonObject()) {
			JsonObject jsonObject = (JsonObject) jsonElement;
			begin	= (Point) instanceFactoryByName("begin",jsonObject,objectMap);
			end		= (Point) instanceFactoryByName("end",jsonObject,objectMap);
		}
	}
	
	protected boolean reset(){
		boolean wasReset = super.reset();
		if(wasReset){
			begin.reset();
			end.reset();
		}
		return wasReset;
	}
	
	public String get__type(){
		return __type;
	}
	
	protected int recursiveEnumerate(int starting_value) {
		starting_value = super.recursiveEnumerate(starting_value);
		starting_value = begin.enumerate(starting_value);
		starting_value = end.enumerate(starting_value);
		return starting_value;
	}
	
	public void toJasonFull(JsonWriter writer){
		writer.jsonWrite("\"end\":{");
		end.toJason(writer);
		writer.jsonWrite("},");
		actualToJason(writer);
		writer.jsonWrite(",");
		append__type(writer,__type);
		writer.jsonWrite(",");
		writer.jsonWrite("\"begin\":{");
		begin.toJason(writer);
		writer.jsonWrite("},");
		append__id(writer);
	}

}
