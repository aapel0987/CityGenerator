package area_constructors;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import area_constructors.Constructor.DistanceFromPointComparitor;
import map_structure.Generateable;
import map_structure.Group;

public abstract class Constructor {

	protected final static double GOLDEN_RATIO = 1.61803399;
	protected final static Random random = new Random();
	
	protected static Point2D getRandomPoint(Rectangle bounds){
		return new Point2D.Double(random.nextDouble()*bounds.getWidth() + bounds.getMinX(), random.nextDouble()*bounds.getHeight() + bounds.getMinY());
	}
		
	public abstract Group blockingArea(Constructor c, Group constructed );
	
	public abstract Group construct(Area routeableArea, Group currentMap);
	
	public Group construct(Area routeableArea){
		return construct(routeableArea,new Group("Constructor_construct_no_group_arg",this));
	}
	
	protected Group conditionalGetBlockingArea(String name, Constructor c, Group constructed ){
		Generateable retreievedMember = constructed.get(name);
		if(retreievedMember instanceof Group) return ((Group) retreievedMember).blockingArea(c);
		else return new Group("conditionalGetCrossableArea_null", this);
	}
	
	protected Area mergeGroupsToArea(Collection<Generateable> mapItems){
		Area toReturn = new Area();
		for(Generateable mapItem : mapItems) toReturn.add(mapItem.getArea());
		return toReturn;
	}
	

	protected static double getRandomUniform(double max){
		return getRandomUniform(0,max);
	}
	
	protected static double getRandomUniform(double min, double max){
		return random.nextDouble()*(max-min) + min;
	}
	
	protected static double getRandomNormal(double maximum){
		return getRandomNormal(0,maximum);
	}
	
	protected static double getRandomNormal(double minimum, double maximum){
		return getRandomNormal(minimum,maximum,(minimum+maximum)/2);
	}
	
	protected static double getRandomNormal(double minimum, double maximum, double mean){
		double standardDeviation = Math.max((mean-minimum)/3, (maximum-mean)/3);
		return getRandomNormal(minimum,maximum,mean,standardDeviation);
	}
	
	protected static double getRandomNormal(double minimum, double maximum, double mean, double standardDeviation){
		return Math.min(Math.max(random.nextGaussian()*standardDeviation + mean,minimum),maximum);
	}
	
	protected Area getLocalRoutingArea(Area routeableArea, Group currentMap){
		Area localRoutingArea = new Area(routeableArea);
		localRoutingArea.subtract(currentMap.blockingArea(this).getArea());
		return localRoutingArea;
	}
	
	protected class DistanceFromPointComparitor implements Comparator<Point2D> {
		
		Iterable<Point2D> points;
		
		DistanceFromPointComparitor(Iterable<Point2D> _points){
			points = _points;
		}
		
		@Override
		public int compare(Point2D a, Point2D b) {
			double distanceToA = 0, distanceToB = 0;

			for(Point2D point : points) {
				distanceToA += point.distance(a);
				distanceToB += point.distance(b);
			}
			
			return Double.compare(distanceToA, distanceToB);
		}
	}
	
	protected LinkedList<Point2D> getEdgePointsNormallyDistributed(Area area, int count){
		LinkedList<Point2D> selectedPoints = new LinkedList<Point2D>();
		if(count < 0){
			throw new IllegalArgumentException("Cannot return a negative number of points.");
		} else if (count == 0) {
			return selectedPoints;
		}
		
		LinkedList<Point2D> edgeOptions = new LinkedList<Point2D>(BasicShapeConstructor.getAreaEdgePoints(new Area(area.getBounds()), 1));
		List<Line2D> routeableAreaBoundry = BasicShapeConstructor.getAreaLines(area, 0.1, false);
		Iterator<Point2D> edgeOptionIter = edgeOptions.iterator();
		while(edgeOptionIter.hasNext()){
			if(!BasicShapeConstructor.intersectAnyLine(edgeOptionIter.next(), routeableAreaBoundry)){
				edgeOptionIter.remove();
			}
		}
		//edgeOptions now contains only points that are on the outer edge of the route-able area, select the first
		if(edgeOptions.size() > 0){
			int chosenPoint = random.nextInt(edgeOptions.size());
			selectedPoints.add(edgeOptions.remove(chosenPoint));
		}
		
		//Now add in any follow on points
		Comparator<Point2D> comparitor = Collections.reverseOrder(new DistanceFromPointComparitor(selectedPoints));
		
		for(int iterationCount = 0; iterationCount < (count -1) && edgeOptions.size() > 0; iterationCount++){
			//First, sort the points based on distance from the starting point
			Collections.sort(edgeOptions, comparitor);
			
			int chosenPoint = (int) Math.floor(Math.abs(getRandomNormal(-1*(edgeOptions.size() -1),edgeOptions.size() -1)));
			selectedPoints.add(edgeOptions.remove(chosenPoint));
		}
		return selectedPoints;
	}
}
