package area_constructors;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;

import map_structure.Group;
import test.TestGUIManager;

public class DefaultPathConstructor extends PathConstructor {

	//TestGUIManager gui = new TestGUIManager("DefaultPathConstructor");
	//BasicStroke stroke = new BasicStroke((float) 0.05);
	
	/*@Override
	protected void annotateGraph(Area routeableArea, Group map,
			GraphContainer graph) {
		//gui.addShape(routeableArea, Color.GREEN);
		//gui.addPoints(graph.getVerticies(), 0.25, Color.DARK_GRAY);
		
		//Find the general separation of the Graph
		
		double seperation = graph.getSeparation();
		double connectRadius = seperation * 1.5;
		
		ArrayList<Line2D> areaLines = BasicShapeConstructor.getAreaLines(routeableArea, 0.1,false);
		LinkedList<Point2D> points = new LinkedList<Point2D>(graph.getVerticies(routeableArea));
		for(Point2D source = points.remove(); points.size() > 0; source = points.remove()){
			for(Point2D target : points){
				Line2D line = new Line2D.Double(source, target);
				double distance = source.distance(target);
				//System.out.println("Source: " + source.toString() + "\tTarget: " + target.toString() + "\tDistance: " + distance + "\tRconnect: " + connectRadius);
				if(distance <= connectRadius && !BasicShapeConstructor.intersectAnyLine(line,areaLines)){
					//System.out.println("\t\tConnected!");
					graph.addEdge(source, target, distance + ((seperation/2)*random.nextDouble()), this);
					//gui.addShape(stroke.createStrokedShape(new Line2D.Double(source.getX(), source.getY(), target.getX(), target.getY())), Color.BLACK);
				} else if(distance <= connectRadius)  {
					//gui.addShape(stroke.createStrokedShape(new Line2D.Double(source.getX(), source.getY(), target.getX(), target.getY())), Color.RED);
				}
			}
			//gui.addPoint(source, 0.25, Color.CYAN);
		}
	}*/

	protected double getWeight(Point2D p0, Point2D p1){
		double weight = p0.distance(p1);
		weight *= (1 + (random.nextDouble()/2));
		return weight;
	}
	
	@Override
	public Group blockingArea(Constructor c, Group constructed) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Group construct(Area routeableArea, Group currentMap) {
		// TODO Auto-generated method stub
		return null;
	}

}
