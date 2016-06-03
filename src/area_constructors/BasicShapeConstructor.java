package area_constructors;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import test.TestGUIManager;

final public class BasicShapeConstructor {

	private final static int threadcount = 5;
	public final static double pointOnLineError = 0.1;
	
	public static Area basicConnectedCircles(List<Point2D> points, double radius) {
		double[] radii = new double[points.size()];
		for(int index = 0; index < radii.length; index++){
			radii[index] = radius;
		}
		return basicConnectedCircles(points,radii);
	}

	public static Area basicConnectedCircles(Point2D points[], double radius) {
		double[] radii = new double[points.length];
		for(int index = 0; index < radii.length; index++){
			radii[index] = radius;
		}
		return basicConnectedCircles(points,radii);
	}
	
	public static Area basicConnectedCircles(Point2D points[], double radii[]) {
		ArrayList<Point2D> pointsList = new ArrayList<Point2D>();
		for(int index = 0; index < points.length; index++){
			pointsList.add(points[index]);	
		}
		return basicConnectedCircles(pointsList,radii);
	}
	
	public static Area basicConnectedCircles(List<Point2D> points, double radii[]) {
		//First, connect all of the points
		double widths[] = new double[radii.length];
		for(int index = 0; index < widths.length; index++){
			widths[index] = radii[index]*((double)2.0);
		}
		Area toReturn = connectPoints(points,widths);
		// Now for each point add a circle of the correct size
		Iterator<Point2D> iterator = points.iterator();
		LinkedList<Area> returnAreas = new LinkedList<Area>();
		int index = 0;
		while(iterator.hasNext()){
			Point2D point = iterator.next();
			returnAreas.add(basicCircle(point,radii[index++]));
		}
		toReturn.add(combineAreasParallel(returnAreas));
		return toReturn;
	}
	
	public static Area basicConnectedCircles(Path2D path, double radius){
		return basicConnectedCircles(path, new double[] {radius});
	}
	
	public static Area basicConnectedCircles(Path2D path, double radii[]){
		double seperation = radii[0];
		for(double value : radii) if(seperation < value) seperation = value;
		return basicConnectedCircles(path, radii, seperation);
	}
	
	public static Area basicConnectedCircles(Path2D path, double radii[], double seperation){
		LinkedList<Area> returnAreas = new LinkedList<Area>();
		double[] coords = new double[6];
		Point2D startPoint = null, previousPoint = null;
		double startRadius;
		
		//This is how we will track the current radius. We will also use the lowe
		int currentRadiusIndex = 0;
		
		for (PathIterator iter = path.getPathIterator(null, seperation); !iter.isDone(); iter.next()) {
		    // The type will be SEG_LINETO, SEG_MOVETO, or SEG_CLOSE
		    // Because the Area is composed of straight lines
			int type = iter.currentSegment(coords);
			Point2D nextPoint = new Point2D.Double(coords[0], coords[1]);
			
			//Select the radii to be used
			double previousRadius = radii[currentRadiusIndex];
			double nextRadius = radii[currentRadiusIndex];
			if(currentRadiusIndex < radii.length -1) nextRadius = radii[++currentRadiusIndex];
		    switch(type){
		    	case PathIterator.SEG_MOVETO:
		    		startPoint  = (Point2D) nextPoint.clone();
		    		startRadius = previousRadius;
		    		returnAreas.add(basicCircle(startPoint,startRadius));
		    		break;
		    	case PathIterator.SEG_CLOSE:
		    		nextPoint = startPoint;
		    		break;
		    	default:	//This will be some form of line
		    		returnAreas.add(basicCircle(nextPoint,nextRadius));
		    		returnAreas.add(connectPoints(previousPoint,2*previousRadius,nextPoint,2*nextRadius));
		    }
		    
		    previousPoint = nextPoint;
		}

		return combineAreasParallel(returnAreas);
	}
	
	public static Area basicCircle(Point2D center, double radius){
		return new Area(new Ellipse2D.Double(center.getX() - radius, center.getY() - radius, 2*radius, 2*radius));
	}
	
	public static Area connectPoints(List<Point2D> points, double widths[]){
		//The format of the input array is assumed to be as follows
		//For each element in the array there is a sub-array with three values:
		//	The first is the X-Coordinate
		//	The second is the Y-Coordinate
		//	The third is the width at that point
		//If there is only one element, nothing is done
		if(points.size() != widths.length){
			throw new IllegalArgumentException("Array lengths provided to 'addConnections' must match.");
		}
		Iterator<Point2D> iterator = points.iterator();
		LinkedList<Area> returnAreas = new LinkedList<Area>();
		int index = 0;
		if(iterator.hasNext()){
			Point2D startPoint = iterator.next();
			while( iterator.hasNext()){
				//So at this stage there are two points and two widths,
				//This will allow us to construct the four points needed
				//to create a polygon, which is what we will do, and then add
				//it to our own area.
				//To do this properly we need to calculate all of the relevant points,
				//which involves some geometry. Pretty sure this is correct.
				//Pull values for easier reading
				Point2D endPoint = iterator.next();
				returnAreas.add(connectPoints(startPoint,widths[index],endPoint,widths[++index]));
				startPoint = endPoint;
			}
		}
		
		return combineAreasParallel(returnAreas);
	}
	
	public static Area connectPoints(Point2D point1, double w1, Point2D point2, double w2){
		//Angle used to position all new points
		double theta = Math.PI/2 - Math.atan((point1.getY()-point2.getY())/(point1.getX()-point2.getX()));
		//Now since there is no double precision polygon, we use a double path instead
		Path2D path = new Path2D.Double();
		//Now manually add all of the points to that path
		Point2D p1 = new Point2D.Double(point1.getX()+(w1/2)*Math.cos(theta), point1.getY()-(w1/2)*Math.sin(theta));
		Point2D p2 = new Point2D.Double(point2.getX()+(w2/2)*Math.cos(theta), point2.getY()-(w2/2)*Math.sin(theta));
		Point2D p3 = new Point2D.Double(point2.getX()-(w2/2)*Math.cos(theta), point2.getY()+(w2/2)*Math.sin(theta));
		Point2D p4 = new Point2D.Double(point1.getX()-(w1/2)*Math.cos(theta), point1.getY()+(w1/2)*Math.sin(theta));
		path.moveTo(p1.getX(),p1.getY());
		path.lineTo(p2.getX(),p2.getY());
		path.lineTo(p3.getX(),p3.getY());
		path.lineTo(p4.getX(),p4.getY());
		path.closePath();
		//Lastly, add this to the current Area
		return new Area(path);
	}
	
	public static Area createBullseye(Point2D center, double radius, int filledRings, boolean fillCenter){
		int totalAreas = 2*(filledRings +1);
		if(!fillCenter){
			totalAreas -= 1;
		}
		double radiusIncriment = radius/ ((double) totalAreas);
		
		Area bullseye = new Area();
		//Start from the outside and work in
		for(int circleIndex = totalAreas -1; circleIndex > 0; circleIndex -=2){
			double currentRadius = radiusIncriment*circleIndex;
			bullseye.add(new Area(new Ellipse2D.Double(center.getX() -currentRadius, center.getY() -currentRadius, 2*currentRadius, 2*currentRadius)));
			currentRadius = radiusIncriment*(circleIndex -1);
			bullseye.subtract(new Area(new Ellipse2D.Double(center.getX() -currentRadius, center.getY() -currentRadius, 2*currentRadius, 2*currentRadius)));
		}
		
		return bullseye;
	}
	
	public static Area createBullseye(double radius, int filledRings, boolean fillCenter){
		return createBullseye(new Point2D.Double(0,0), radius, filledRings, fillCenter);
	}
	
	public static Area createBullseye(double radius, int filledRings){
		return createBullseye( radius, filledRings, true);
	}
	
	public static Area combineAreasParallel(List<Area> areas){
		if(areas.size() < threadcount){
			return combineAreasRecursive(areas);
		}
		
		final ArrayList<Area>[] partitionedAreas = (ArrayList<Area>[]) new ArrayList[threadcount];
		int nextStartIndex = 0;
		for(int index = 0; index < partitionedAreas.length -1; index++){		//Skip the last entry
			int endIndex = nextStartIndex + areas.size()/threadcount;
			partitionedAreas[index] = new ArrayList<Area>(areas.subList(nextStartIndex, endIndex));
			nextStartIndex = endIndex;
		}
		//Handle Last Entry
		partitionedAreas[threadcount -1] = new ArrayList<Area>(areas.subList(nextStartIndex, areas.size()));
		final Area results[] = new Area[threadcount];
		//Now all of the areas have been partitioned. Launch Threads to merge
		final CountDownLatch latch = new CountDownLatch(threadcount);
		for(int threadIndex = 0; threadIndex < threadcount; threadIndex++){
			final int localThreadIndex = threadIndex;
			Thread combiningThread = new Thread("UIHandler"){
		        @Override
		        public void run(){
		        	results[localThreadIndex] = combineAreasRecursive(partitionedAreas[localThreadIndex]);
		            latch.countDown(); // Release await() in the test thread.
		        }
		    };
		    combiningThread.start();
		}
		//Wait for results to return
		try {
			latch.await();
		} catch (InterruptedException e) {
			System.err.println("Waiting was Interrupted! Exiting."); 
			e.printStackTrace();
			System.exit(2);
		}
		
	    //Perform Final merge of results
	    Area result = new Area();
	    for(int resultsIndex = 0; resultsIndex < results.length; resultsIndex++){
	    	result.add(results[resultsIndex]);
	    }
	    
		return result;
	}
	
	public static Area combineAreasRecursive(List<Area> list){
		if(list.size() == 0){
			return null;
		}else if(list.size() == 1){
			return list.get(0);
		}
		
		Area result = combineAreasRecursive(list.subList(0, list.size()/2));
		result.add(combineAreasRecursive(list.subList(list.size()/2, list.size())));
		return result;
	}

	public static ArrayList<Path2D> getAreaPaths(Area area){
		ArrayList<Path2D> paths = new ArrayList<Path2D>();
		double[] coords = new double[6];
		Path2D currentPath = null;
		for (PathIterator iter = area.getPathIterator(null); !iter.isDone(); iter.next()) {
		    // The type will be SEG_LINETO, SEG_MOVETO, or SEG_CLOSE
		    // Because the Area is composed of straight lines
			int type = iter.currentSegment(coords);
		    switch(type){
		    	case PathIterator.SEG_MOVETO:
		    		if(currentPath != null){
		    			currentPath.closePath();
		    		}
		    		currentPath = new Path2D.Double();
		    		currentPath.moveTo(coords[0], coords[1]);
		    		paths.add(currentPath);
		    		break;
		    	case PathIterator.SEG_CLOSE:
		    		currentPath.closePath();
		    		break;
		    	case PathIterator.SEG_LINETO:
		    		currentPath.lineTo ( coords[0], coords[1]);
		    		break;
		    	case PathIterator.SEG_QUADTO:
		        	currentPath.quadTo ( coords[0], coords[1], coords[2], coords[3]);
		        	break;
		        case PathIterator.SEG_CUBICTO:
		        	currentPath.curveTo( coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
		        	break;
		    }
		}
		return paths;
	}
	
	public static List<Point2D> pathToPoints(Path2D path, double separation){
		ArrayList<Point2D> points = new ArrayList<Point2D>();
		double[] coords = new double[6];
		Point2D startPoint = null;
		for (PathIterator iter = path.getPathIterator(null, separation); !iter.isDone(); iter.next()) {
		    // The type will be SEG_LINETO, SEG_MOVETO, or SEG_CLOSE
		    // Because the Area is composed of straight lines
			int type = iter.currentSegment(coords);
			Point2D currentPoint = new Point2D.Double(coords[0], coords[1]);
		    switch(type){
		    	case PathIterator.SEG_MOVETO:
		    		startPoint = (Point2D) currentPoint.clone();
		    	case PathIterator.SEG_CLOSE:
		    		points.add(startPoint);
		    		break;
		    	default:	//This will be some form of line
		    		points.add(currentPoint);
		    }
		}

		return points;
	}
	
	public static Path2D pointsToPath2D(List<Point2D> points){
		Path2D path = new Path2D.Double();
		path.moveTo(points.get(0).getX(), points.get(0).getX());
		for(Point2D point : points){
			path.lineTo(point.getX(), point.getY());
		}
		return path;
	}
	
	public static ArrayList<Line2D> shapeToLines(Shape shape, double separation){
		return path2DToLines(new Path2D.Double(shape),separation);
	}
	
	public static ArrayList<Line2D> path2DToLines(Path2D path, double separation){
		ArrayList<Line2D> pathSegments = new ArrayList<Line2D>();
		double[] coords = new double[6];
		Point2D startPoint = null, previousPoint = null;
		for (PathIterator iter = path.getPathIterator(null, separation); !iter.isDone(); iter.next()) {
		    // The type will be SEG_LINETO, SEG_MOVETO, or SEG_CLOSE
		    // Because the Area is composed of straight lines
			int type = iter.currentSegment(coords);
			Point2D currentPoint = new Point2D.Double(coords[0], coords[1]);
		    switch(type){
		    	case PathIterator.SEG_MOVETO:
		    		startPoint = (Point2D) currentPoint.clone();
		    		break;
		    	case PathIterator.SEG_CLOSE:
		    		pathSegments.add(new Line2D.Double(previousPoint,startPoint));
		    		break;
		    	default:	//This will be some form of line
		    		pathSegments.add(new Line2D.Double(previousPoint,currentPoint));
		    }
		    
		    previousPoint = currentPoint;
		}

		return pathSegments;
	}
		
	public static boolean intersectAnyLine(Line2D line, Collection<Line2D> lines){
		for(Line2D checkLine : lines)
			if(		line.intersectsLine(checkLine) &&
					checkLine.ptLineDist(line.getP1()) > pointOnLineError &&
					checkLine.ptLineDist(line.getP2()) > pointOnLineError ) return true;
		return false;
	}
	
	public static boolean intersectAnyLine(Point2D point, Collection<Line2D> lines){
		for(Line2D checkLine : lines)
			if(checkLine.ptLineDist(point) <= pointOnLineError )
				return true;
		return false;
	}
	
	public static ArrayList<Line2D> getAreaLines(Area area, double separation, boolean connectParts){
		ArrayList<Line2D> areaSegments = new ArrayList<Line2D>();
		List<Path2D> paths = getAreaPaths(area);
		Iterator<Path2D> pathsIter = paths.iterator();
		if(pathsIter.hasNext()){
			areaSegments.addAll(path2DToLines(pathsIter.next(),separation));
			int index = 0;
			//TestGUIManager gui = new TestGUIManager("getAreaLines: " + index++);
			//gui.addLines(areaSegments,Color.black);
			while(pathsIter.hasNext()){
				List<Line2D> newLines = path2DToLines(pathsIter.next(),separation);
				//gui = new TestGUIManager("getAreaLines: " + index++);
				//gui.addLines(newLines,Color.black);
				if(newLines.size() > 0){
					if(connectParts){
						if(areaSegments.get(0).getP1() != areaSegments.get(areaSegments.size()-1).getP2()){
							areaSegments.add(new Line2D.Double(areaSegments.get(areaSegments.size()-1).getP2(),areaSegments.get(0).getP1()));
						}
						areaSegments.add(new Line2D.Double(areaSegments.get(0).getP1(),newLines.get(0).getP1()));
					}
					areaSegments.addAll(newLines);
				}
			}
			if(areaSegments.get(0).getP1() != areaSegments.get(areaSegments.size()-1).getP2() && connectParts){
				areaSegments.add(new Line2D.Double(areaSegments.get(areaSegments.size()-1).getP2(),areaSegments.get(0).getP1()));
			}
		}
		return areaSegments;
	}
	
	
	public static List<Point2D> getPointsInArea(Area area, double separation){
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
		
		return internalPoints;
	}

	public static List<Point2D> getAreaEdgePoints(Area area, double separation){
		Rectangle2D bounds = area.getBounds2D();
		ArrayList<Point2D> internalPoints = new ArrayList<Point2D>();
		double x1 = bounds.getMinX();
		double x2 = bounds.getMaxX();
		double y1 = bounds.getMinY();
		double y2 = bounds.getMaxY();
		
		//Next step is to iterate over all of the vertical and horizontal lines and collect any intersections
		List<Line2D> edges = BasicShapeConstructor.getAreaLines(area, separation,false);
		//TestGUIManager gui = new TestGUIManager("getAreaEdgePoints");
		//gui.addLines(edges,Color.black);
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
		        return new Point2D.Double((px+z*rx), (py+z*ry));
			}
	} // end intersection line-line
}
