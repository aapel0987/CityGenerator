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
	
	public StringBuilder toJasonFull(){
		StringBuilder builder = new StringBuilder();
		builder.append("\"end\":{");
		builder.append(end.toJason());
		builder.append("},");
		builder.append(actualToJason());
		builder.append(",");
		append__type(builder,__type);
		builder.append(",");
		builder.append("\"begin\":{");
		builder.append(begin.toJason());
		builder.append("},");
		append__id(builder);
		return builder;
	}

}
