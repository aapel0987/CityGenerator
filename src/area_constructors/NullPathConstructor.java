package area_constructors;

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.List;

import area_constructors.PathConstructor.GraphContainer;
import map_structure.Group;

public class NullPathConstructor extends PathConstructor {

	public static final NullPathConstructor NULL_PATH_CONSTRUCTOR_ZERO = new NullPathConstructor(Double.MIN_VALUE);
	
	private double weight;
	
	public NullPathConstructor(double _weight){
		weight = _weight;
	}
	
	protected double getWeight(Point2D p0, Point2D p1){
		return weight;
	}
	
	@Override
	public Group blockingArea(Constructor c, Group constructed) {
		throw new UnsupportedOperationException("This calss is used to construct null segments in paths, and should never be called on it's own.");
	}

	@Override
	public Group construct(Area routeableArea, Group currentMap) {
		throw new UnsupportedOperationException("This calss is used to construct null segments in paths, and should never be called on it's own");
	}

	public void connectAll(List<Point2D> points, GraphContainer graph){
		if(points.size() > 1){
			Iterator<Point2D> pointIter = points.iterator();
			Point2D centerPoint = pointIter.next();
			while(pointIter.hasNext()){
				graph.addEdge(centerPoint, pointIter.next(), weight, this);
			}
		}
	}
}
