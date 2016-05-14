package materials;

import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import area_constructors.BasicShapeConstructor;
import test.TestGUIManager;

public class MaterialPoint extends Material {

	private double radius;

	public MaterialPoint(Color c, double rad) {
		super(c);
		radius = rad;
	}

	public void renderFill(TestGUIManager gui, Area area) {
		gui.addShape(area,color);
		renderFill(gui,BasicShapeConstructor.getAreaEdgePoints(area,getSeperation(radius)));
	}
	
	public void renderFill(TestGUIManager gui, Collection<Point2D> points) {
		renderPoints(gui,points);
	}
	
	private void renderPoints(TestGUIManager gui, Collection<Point2D> points){
		if(points.size() > 0){
			Area area = BasicShapeConstructor.combineAreasParallel(getAreas(points));
			gui.addShape(area,color);
		}
	}
	
	private ArrayList<Area> getAreas(Collection<Point2D> points){
		ArrayList<Area> areas = new ArrayList<Area>();
		for(Point2D currPoint : points){
			areas.add(new Area(new Ellipse2D.Double(currPoint.getX()-radius,currPoint.getY()-radius,2*radius,2*radius)));
		}
		return areas;
	}
	
	static private double getSeperation(double rad){
		return Math.sqrt(Math.pow(rad, 2)/2);
	}
	
	
	static private List<Point2D> getFillPoints(Area area, double separation){
		List<Point2D> internalPoints = BasicShapeConstructor.getPointsInArea(area,separation);
		internalPoints.addAll(BasicShapeConstructor.getAreaEdgePoints(area,separation));
		
		return internalPoints;
	}

	public ArrayList<Point2D> getFillPoints(Area area){
		return new ArrayList(getFillPoints(area,radius));
	}
}
