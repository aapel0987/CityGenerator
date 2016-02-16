package area_constructors;

import java.awt.BasicStroke;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import map_structure.Generateable;
import map_structure.Group;
import map_structure.Layer;
import materials.Material;
import materials.MaterialsCollection;

public final class BasicWaterConstructor extends Constructor {

	private final static Material water = MaterialsCollection.Water;
	private final static Material sand  = MaterialsCollection.Sand;
	
	public static Group createSimpleRiver(double points[], double width){
		if(points.length%2 != 0)
			throw new IllegalArgumentException("Each Entry in points needs to reflect and X and Y coordinate.");
		LinkedList<Point2D> pointsList = new LinkedList<Point2D>();
		for(int index = 0; index < points.length; index+=2){
			pointsList.add(new Point2D.Double(points[index],points[index+1]));
		}
		return createSimpleRiver(pointsList,width);
	}
	
	public static Group createSimpleRiver(List<Point2D> points, double width){
		Group river = new Group();
		Layer waterLayer = new Layer(water,BasicShapeConstructor.basicConnectedCircles(points, width/2));
		Layer sandLayer = new Layer(sand,new Area((new BasicStroke(1)).createStrokedShape(waterLayer)));
		river.add(waterLayer);
		river.add(sandLayer);
		return river;
	}

	@Override
	Constructor crossingConstructor(Constructor c) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	Group crossArea(Area crossing, Generateable constructed) {
		// TODO Auto-generated method stub
		return null;
	}
}
