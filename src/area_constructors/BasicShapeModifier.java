package area_constructors;

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

final public class BasicShapeModifier {

	private class Node extends Point2D.Double {
		private LinkedList<Node> connections;
		
		protected Node(Point2D point, Node conns[]){
			connections = new LinkedList<Node>();
			for(int index = 0; index < conns.length; index++){
				connections.add(conns[index]);
			}
		}

		protected Node(Point2D point, List<Node> conns){
			this.setLocation(point.getX(), point.getY());
			connections = new LinkedList<Node>(conns); 
		}
	}
	
	public static Area distortArea(Area originalArea, double seperation, double maxDistance){
		Area resultingArea = new Area();
		
		
		return resultingArea;
	}
	
}
