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
	
	public Brush(MaterialPoint material,List<Point2D> _points){ 
		this(Packs.getMapItem(material),_points);
	}
	
	public Brush(MapItem _actual,List<Point2D> _points){ 
		super(_actual);
		alpha = 30;
		x = _points.get(0).getX();
		y = _points.get(0).getY();
		shadow = true;
		blur = false;
		position = new Point(_points.get(0));
		points = new LinkedList<Obstical>();
		for(Point2D point : _points){
			Obstical obst = new Obstical(actual,point,alpha,shadow,blur);
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
	
	public void toJasonFull(JsonWriter writer){
		StringBuilder builder = new StringBuilder();
		writer.jsonWrite("\"x\":" + x + ",");
		append__type(writer,__type);
		writer.jsonWrite(",");
		appendOnOff(writer,"blur",blur);
		writer.jsonWrite(",\"y\":" + y + ",");
		append__id(writer);
		writer.jsonWrite(",\"alpha\":" + alpha + ",");
		
		actualToJason(writer);
		writer.jsonWrite(",");

		writer.jsonWrite("\"position\":{");
		position.toJason(writer);
		writer.jsonWrite("},");
		
		writer.jsonWrite("\"points\":[");
		for(Iterator<Obstical> iter = points.iterator(); iter.hasNext(); ){
			writer.jsonWrite("{");
			iter.next().toJason(writer);
			writer.jsonWrite("}");
			if(iter.hasNext()) writer.jsonWrite(",");
		}
		writer.jsonWrite("],");
		appendOnOff(writer,"shadow",shadow);
	}
}
