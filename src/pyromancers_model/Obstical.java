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
	
	public Obstical(MapItem _actual, Point2D point){
		this(_actual,point,30,true,false);
	}
	
	public Obstical(MapItem _actual, Point2D point, int _alpha, boolean _shadow, boolean _blur){
		this(_actual,point,_alpha,_shadow,_blur,((double) 360) * random.nextDouble());
	}
	
	public Obstical(MapItem _actual, Point2D point, int _alpha, boolean _shadow, boolean _blur, double _angle){
		super(_actual);
		alpha = _alpha;
		x = point.getX();
		y = point.getY();
		shadow = _shadow;
		blur = _blur;
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
	
	public void toJasonFull(JsonWriter writer){
		writer.jsonWrite("\"x\":" + x + ",");
		append__type(writer,__type);
		writer.jsonWrite(",");
		appendOnOff(writer,"blur",blur);
		writer.jsonWrite(",");
		writer.jsonWrite("\"y\":" + y + ",");
		append__id(writer);
		writer.jsonWrite(",\"alpha\":" + alpha + ",");
		actualToJason(writer);
		writer.jsonWrite(",");
		writer.jsonWrite("\"begin\":{");
		begin.toJason(writer);
		writer.jsonWrite("},");
		if(end != null){
			writer.jsonWrite("\"end\":{");
			end.toJason(writer);
			writer.jsonWrite("},");
		}
		appendOnOff(writer,"shadow",shadow);
		writer.jsonWrite(",\"angle\":" + angle);
	}
}
