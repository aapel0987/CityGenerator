package pyromancers_model;

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import materials.MaterialPoint;

public class Brush extends LocatorObject {
	public static final String __type = "com.locator.objs::Brush";
	private int alpha;
	private boolean shadow;
	private boolean blur;
	private Point position;
	private LinkedList<Obstical> points;
	
	//Unused?
	private double x = 0;
	private double y = 0;
	
	public Brush(JsonElement jsonElement, Map<Integer, UtilityBase> objectMap) {
		super(jsonElement,objectMap);
		//System.out.println(jsonElement.toString());
		if(jsonElement.isJsonObject()) {
			JsonObject jsonObject = (JsonObject) jsonElement;
			alpha 		= jsonObject.get("alpha").getAsInt();
			x     		= jsonObject.get("x").getAsDouble();
			y     		= jsonObject.get("y").getAsDouble();
			shadow		= parseOnOff(jsonObject,"shadow");
			blur		= parseOnOff(jsonObject,"blur");
			position	= (Point) instanceFactoryByName("position",jsonObject,objectMap);
			JsonArray pointsArray = jsonObject.get("points").getAsJsonArray();
			points = new LinkedList<Obstical>();
			for(JsonElement sourceElement : pointsArray){
				points.add((Obstical) instanceFactory(sourceElement,objectMap));
			}
		}
	}
	
	public Brush(List<Point2D> _points, MaterialPoint material){ 
		super(Packs.getMapItem(material));
		alpha = 30;
		x = _points.get(0).getX();
		y = _points.get(0).getY();
		shadow = true;
		blur = false;
		position = new Point(_points.get(0));
		points = new LinkedList<Obstical>();
		for(Point2D point : _points){
			Obstical obst = new Obstical(actual,alpha,point.getX(),point.getY(),shadow,blur);
			obst.position(point, ((double) 360) * Math.random());
			points.add(obst);
		}
	}
	
	protected boolean reset(){
		boolean wasReset = super.reset();
		if(wasReset){
			position.reset();
			for(Obstical point : points) point.reset();
		}
		return wasReset;
	}
	
	public String get__type(){
		return __type;
	}
	
	protected int recursiveEnumerate(int starting_value) {
		starting_value = super.recursiveEnumerate(starting_value);
		starting_value = position.enumerate(starting_value);
		for(Obstical point : points) starting_value = point.enumerate(starting_value);  
		return starting_value;
	}
	
	public StringBuilder toJasonFull(){
		StringBuilder builder = new StringBuilder();
		builder.append("\"x\":" + x + ",");
		append__type(builder,__type);
		builder.append(",");
		appendOnOff(builder,"blur",blur);
		builder.append(",\"y\":" + y + ",");
		append__id(builder);
		builder.append(",\"alpha\":" + alpha + ",");
		
		builder.append(actualToJason());
		builder.append(",");

		builder.append("\"position\":{");
		builder.append(position.toJason());
		builder.append("},");
		
		builder.append("\"points\":[");
		for(Iterator<Obstical> iter = points.iterator(); iter.hasNext(); ){
			builder.append("{");
			builder.append(iter.next().toJason());
			builder.append("}");
			if(iter.hasNext()) builder.append(",");
		}
		builder.append("],");
		appendOnOff(builder,"shadow",shadow);
		
		return builder;
	}
}
