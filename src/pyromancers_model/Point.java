package pyromancers_model;

import java.awt.geom.Point2D;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Point extends UtilityBase {

	public static final String __type = "flash.geom::Point";
	private double x;
	private double y;

	public Point(JsonElement jsonElement, Map<Integer, UtilityBase> objectMap) {
		super(jsonElement,objectMap);
		//System.out.println(jsonLayers.toString());
		if(jsonElement.isJsonObject()) {
			JsonObject jsonObject = (JsonObject) jsonElement;
			x     	= jsonObject.get("x").getAsDouble();
			y     	= jsonObject.get("y").getAsDouble();
		}
	}
	
	public Point(Point2D point){
		x = point.getX();
		y = point.getY();
	}
	
	public String get__type(){
		return __type;
	}

	public StringBuilder toJasonFull(){
		StringBuilder builder = new StringBuilder();
		builder.append("\"x\":" + x + ",");
		append__type(builder,__type);
		builder.append(",");
		builder.append("\"y\":" + y + ",");
		append__id(builder);
		return builder;
	}
}
