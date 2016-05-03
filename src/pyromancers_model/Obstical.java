package pyromancers_model;

import java.awt.geom.Point2D;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Obstical extends LocatorObject {

	public static final String __type = "com.locator.objs::Obst";
	private int alpha;
	private double x;
	private double y;
	private boolean shadow;
	private boolean blur;
	private double angle;
	private Point begin;
	private Point end;
		
	public Obstical(JsonElement jsonElement, Map<Integer, UtilityBase> objectMap) {
		super(jsonElement,objectMap);
		//System.out.println(jsonElement.toString());
		if(jsonElement.isJsonObject()) {
			JsonObject jsonObject = (JsonObject) jsonElement;
			if(!jsonObject.get("alpha").isJsonNull()) alpha = jsonObject.get("alpha").getAsInt();
			x     	= jsonObject.get("x").getAsDouble();
			y     	= jsonObject.get("y").getAsDouble();
			shadow	= parseOnOff(jsonObject,"shadow");
			blur	= parseOnOff(jsonObject,"blur");
			angle  	= jsonObject.get("angle").getAsDouble();
			begin	= (Point) instanceFactoryByName("begin",jsonObject,objectMap);
			end		= (Point) instanceFactoryByName("end",jsonObject,objectMap);
		}
	}
	
	public Obstical(MapItem _actual, int _alpha, double _x, double _y, boolean _shadow, boolean _blur){
		super(_actual);
		alpha = _alpha;
		x = _x;
		y = _y;
		shadow = _shadow;
		blur = _blur;
	}
	
	public void position(Point2D point, double _angle){
		begin = new Point(point);
		angle = _angle;
	}
	
	protected boolean reset(){
		boolean wasReset = super.reset();
		if(wasReset){
			begin.reset();
			if(end != null) end.reset();
		}
		return wasReset;
	}
	
	public String get__type(){
		return __type;
	}
	
	protected int recursiveEnumerate(int starting_value) {
		starting_value = super.recursiveEnumerate(starting_value);
		starting_value = begin.enumerate(starting_value);
		if(end != null)starting_value = end.enumerate(starting_value);
		return starting_value;
	}
	
	public StringBuilder toJasonFull(){
		StringBuilder builder = new StringBuilder();
		builder.append("\"x\":" + x + ",");
		append__type(builder,__type);
		builder.append(",");
		appendOnOff(builder,"blur",blur);
		builder.append(",");
		builder.append("\"y\":" + y + ",");
		append__id(builder);
		builder.append(",\"alpha\":" + alpha + ",");
		builder.append(actualToJason());
		builder.append(",");
		builder.append("\"begin\":{");
		builder.append(begin.toJason());
		builder.append("},");
		if(end != null){
			builder.append("\"end\":{");
			builder.append(end.toJason());
			builder.append("},");
		}
		appendOnOff(builder,"shadow",shadow);
		builder.append(",\"angle\":" + angle);
		return builder;
	}
}
