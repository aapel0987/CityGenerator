package materials;

import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import area_constructors.BasicShapeConstructor;
import test.TestGUIManager;

public class MaterialPoint extends MaterialBase {

	private double radius;

	public MaterialPoint(Color c, double rad) {
		super(c);
		radius = rad;
	}

	@Override
	public void renderFill(TestGUIManager gui, Area area) {
		renderPoints(gui,getFillPoints(area,getSeperation(radius)));
	}
	
	private void renderPoints(TestGUIManager gui, ArrayList<Point2D> points){
		Area area = BasicShapeConstructor.combineAreasParallel(getAreas(points));
		gui.addShape(area,color);
	}
	
	private ArrayList<Area> getAreas(ArrayList<Point2D> points){
		ArrayList<Area> areas = new ArrayList<Area>();
		Iterator<Point2D> iterator = points.iterator();
		while(iterator.hasNext()){
			Point2D currPoint = iterator.next();
			areas.add(new Area(new Ellipse2D.Double(currPoint.getX()-radius,currPoint.getY()-radius,2*radius,2*radius)));
		}
		return areas;
	}
	
	static private double getSeperation(double rad){
		return Math.sqrt(Math.pow(rad, 2)/2);
	}
	
	static private ArrayList<Point2D> getFillPoints(Area area, double separation){
		Rectangle2D bounds = area.getBounds2D();
		ArrayList<Point2D> internalPoints = new ArrayList<Point2D>();
		double x1 = bounds.getMinX();
		double x2 = bounds.getMaxX();
		double y1 = bounds.getMinY();
		double y2 = bounds.getMaxY();

		//Fully Center the Proposed Grid on the Shape
		x1 += ((x2-x1)%separation/((double) 2));
		y1 += ((y2-y1)%separation/((double) 2));
		
		//Generate all the points in the bounds, check if it's in the area, and then add if it is
		for(double currentY = y1; currentY <= y2; currentY += separation){
			for(double currentX = x1; currentX <= x2; currentX += separation){
				Point2D point = new Point2D.Double(currentX,currentY);
				if(area.contains(point)){
					internalPoints.add(point);
				}
			}
		}

		//Next step is to iterate over all of the vertical and horizontal lines and collect any intersections
		List<Line2D> edges = BasicShapeConstructor.getAreaLines(area, separation);
		for(double currentY = y1; currentY <= y2; currentY += separation){
			Line2D currentLine = new Line2D.Double(x1, currentY, x2, currentY);
			Iterator<Line2D> iterator = edges.iterator();
			Point2D previousIntersection = null;
			while(iterator.hasNext()){
				Line2D areaLine = iterator.next();
				Point2D newIntersection = getIntersectionPoint(currentLine,areaLine);
				if(newIntersection != null && newIntersection != previousIntersection){	//Prevent Duplicates
					internalPoints.add(newIntersection);
				}
			}
		}

		for(double currentX = x1; currentX <= x2; currentX += separation){
			Line2D currentLine = new Line2D.Double(currentX, y1, currentX, y2);
			Iterator<Line2D> iterator = edges.iterator();
			Point2D previousIntersection = null;
			while(iterator.hasNext()){
				Line2D areaLine = iterator.next();
				Point2D newIntersection = getIntersectionPoint(currentLine,areaLine);
				if(newIntersection != null && newIntersection != previousIntersection){	//Prevent Duplicates
					internalPoints.add(newIntersection);
				}
			}
		}
		
		return internalPoints;
	}
	
	// Code taken from https://community.oracle.com/thread/1264395?start=0&tstart=0
	//With modifications. Accessed 6/2/2016
	public static Point2D getIntersectionPoint(Line2D line1, Line2D line2) {
		if (! line1.intersectsLine(line2) ) return null;
			double 	px = line1.getX1(),
			        py = line1.getY1(),
			        rx = line1.getX2()-px,
			        ry = line1.getY2()-py;
			double 	qx = line2.getX1(),
			        qy = line2.getY1(),
			        sx = line2.getX2()-qx,
			        sy = line2.getY2()-qy;

			double det = sx*ry - sy*rx;
			if (det == 0) {
				return null;
			} else {
				double z = (sx*(qy-py)+sy*(px-qx))/det;
				if (z==0 ||  z==1) return null;  // intersection at end point!
		        return new Point2D.Double((px+z*rx), (py+z*ry));
			}
 } // end intersection line-line
	
}
