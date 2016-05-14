package pyromancers_model;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import area_constructors.BasicShapeConstructor;
import materials.MaterialPoint;
import materials.MaterialPoly;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Polygon extends LocatorObject{
	public static final String __type = "com.locator.objs::Polygon";
	private boolean ended;
	private Point begin;		//Top left Corner?
	private LinkedList<Point> points;
	
	public Polygon(JsonElement jsonElement, Map<Integer, UtilityBase> objectMap) {
		super(jsonElement,objectMap);
		//System.out.println(jsonElement.toString());
		if(jsonElement.isJsonObject()) {
			JsonObject jsonObject = (JsonObject) jsonElement;
			ended		= parseOnOff(jsonObject,"ended");
			begin		= (Point) instanceFactoryByName("begin",jsonObject,objectMap);
			JsonArray pointsArray = jsonObject.get("points").getAsJsonArray();
			points = new LinkedList<Point>();
			for(JsonElement sourceElement : pointsArray){
				points.add((Point) instanceFactory(sourceElement,objectMap));
			}
		}
	}
	
	public Polygon(Area area, MaterialPoly material){
		this(area,Packs.getMapItem(material));
	}
	
	public Polygon(Area area, MapItem mapItem){
		super(mapItem);
		List<List<Point2D>> listOfPointSets = new LinkedList<List<Point2D>>(); 
		for(Path2D path : BasicShapeConstructor.getAreaPaths(area)){
			listOfPointSets.add(BasicShapeConstructor.pathToPoints(path,0.1));
		}
		begin = new Point(listOfPointSets.get(0).get(0));
		points = new LinkedList<Point>();
		for(List<Point2D> list : listOfPointSets){
			points.add(begin);
			for(Point2D point : list) points.add(new Point(point));
			points.add(begin);
		}
	}
	
	protected boolean reset(){
		boolean wasReset = super.reset();
		if(wasReset){
			begin.reset();
			for(Point point : points) point.reset();
		}
		return wasReset;
	}
	
	protected int recursiveEnumerate(int starting_value) {
		starting_value = super.recursiveEnumerate(starting_value);
		starting_value = begin.enumerate(starting_value);
		for(Point point : points) starting_value = point.enumerate(starting_value);  
		return starting_value;
	}

	
	public String get__type(){
		return __type;
	}
	

	public void toJasonFull(JsonWriter writer){
		actualToJason(writer);
		writer.jsonWrite(",");
		append__type(writer,__type);
		writer.jsonWrite(",");
		append__id(writer);
		writer.jsonWrite(",");
		appendOnOff(writer,"ended",ended);
		writer.jsonWrite(",\"begin\":{");
		begin.toJason(writer);
		writer.jsonWrite("},");
		writer.jsonWrite("\"points\":[");
		for(Iterator<Point> iter = points.iterator(); iter.hasNext(); ){
			writer.jsonWrite("{");
			iter.next().toJason(writer);
			writer.jsonWrite("}");
			if(iter.hasNext()) writer.jsonWrite(",");
		}
		writer.jsonWrite("]");
	}
}
