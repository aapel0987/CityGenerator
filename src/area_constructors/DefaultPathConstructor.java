package area_constructors;

import java.awt.geom.Area;
import java.awt.geom.Point2D;

import map_structure.Group;

public class DefaultPathConstructor extends PathConstructor {

	protected double getWeight(Point2D p0, Point2D p1){
		double weight = p0.distance(p1);
		weight *= (1 + (random.nextDouble()/2));
		return weight;
	}
	
	@Override
	public Group blockingArea(Constructor c, Group constructed) {
		throw new UnsupportedOperationException("This is a test class only.");
	}

	@Override
	public Group construct(Area routeableArea, Group currentMap) {
		throw new UnsupportedOperationException("This is a test class only.");
	}

}
