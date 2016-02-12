package area_constructors;

import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

final public class BasicShapeConstructor {

	private final static int threadcount = 5;
	
	public static Area BasicConnectedCircles(ArrayList<Point2D> points, double radius) {
		double[] radii = new double[points.size()];
		for(int index = 0; index < radii.length; index++){
			radii[index] = radius;
		}
		return BasicConnectedCircles(points,radii);
	}
	
	public static Area BasicConnectedCircles(ArrayList<Point2D> points, double radii[]) {
		//First, connect all of the points
		Area toReturn = connectPoints(points,radii);
		// Now for each point add a circle of the correct size
		Iterator<Point2D> iterator = points.iterator();
		int index = 0;
		while(iterator.hasNext()){
			Point2D point = iterator.next();
			toReturn.add(new Area(new Ellipse2D.Double(point.getX(), point.getY(), 2*radii[index], 2*radii[index++])));
		}
		return toReturn;
	}
	
	public static Area connectPoints(ArrayList<Point2D> points, double widths[]){
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
		Area returnArea = new Area();
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
				returnArea.add(connectPoints(startPoint,widths[index],endPoint,widths[++index]));
				startPoint = endPoint;
			}
		}
		
		return returnArea;
	}
	
	public static Area connectPoints(Point2D point1, double w1, Point2D point2, double w2){
		//Angle used to position all new points
		double theta = Math.PI/2 - Math.atan((point1.getY()-point2.getY())/(point1.getX()-point2.getX()));
		//Now since there is no double precision polygon, we use a double path instead
		Path2D path = new Path2D.Double();
		//Now manually add all of the points to that path
		path.moveTo(point1.getX()+(w1/2)*Math.cos(theta), point1.getY()+(w1/2)*Math.sin(theta));
		path.lineTo(point2.getX()+(w2/2)*Math.cos(theta), point2.getY()+(w2/2)*Math.sin(theta));
		path.lineTo(point2.getX()-(w2/2)*Math.cos(theta), point2.getY()-(w2/2)*Math.sin(theta));
		path.lineTo(point1.getX()-(w1/2)*Math.cos(theta), point1.getY()-(w1/2)*Math.sin(theta));
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
	

	public static ArrayList<Line2D> getAreaLines(Area area, double separation){
		//Code Taken From Stackoverflow, with modifications
		//http://stackoverflow.com/questions/8144156/using-pathiterator-to-return-all-line-segments-that-constrain-an-area
		//Accessed: 1/28/2016
		ArrayList<double[]> areaPoints = new ArrayList<double[]>();
		ArrayList<Line2D> areaSegments = new ArrayList<Line2D>();
		double[] coords = new double[6];

		for (PathIterator pi = area.getPathIterator(null, separation); !pi.isDone(); pi.next()) {
		    // The type will be SEG_LINETO, SEG_MOVETO, or SEG_CLOSE
		    // Because the Area is composed of straight lines
		    int type = pi.currentSegment(coords);
		    // We record a double array of {segment type, x coord, y coord}
		    double[] pathIteratorCoords = {type, coords[0], coords[1]};
		    areaPoints.add(pathIteratorCoords);
		}

		double[] start = new double[3]; // To record where each polygon starts

		for (int i = 0; i < areaPoints.size(); i++) {
		    // If we're not on the last point, return a line from this point to the next
		    double[] currentElement = areaPoints.get(i);

		    // We need a default value in case we've reached the end of the ArrayList
		    double[] nextElement = {-1, -1, -1};
		    if (i < areaPoints.size() - 1) {
		        nextElement = areaPoints.get(i + 1);
		    }

		    // Make the lines
		    if (currentElement[0] == PathIterator.SEG_MOVETO) {
		        start = currentElement; // Record where the polygon started to close it later
		    } 

		    if (nextElement[0] == PathIterator.SEG_LINETO) {
		        areaSegments.add(
		                new Line2D.Double(
		                    currentElement[1], currentElement[2],
		                    nextElement[1], nextElement[2]
		                )
		            );
		    } else if (nextElement[0] == PathIterator.SEG_CLOSE) {
		        areaSegments.add(
		                new Line2D.Double(
		                    currentElement[1], currentElement[2],
		                    start[1], start[2]
		                )
		            );
		    }
		}
		return areaSegments;
	}
	


	public static ArrayList<Line2D> MyGetAreaLines(Area area, double separation, boolean connectParts){
		ArrayList<Line2D> areaSegments = new ArrayList<Line2D>();
		double[] coords = new double[6];
		Point2D centerPoint = null, startPoint = null, previousPoint = null;
		for (PathIterator iter = area.getPathIterator(null, separation); !iter.isDone(); iter.next()) {
		    // The type will be SEG_LINETO, SEG_MOVETO, or SEG_CLOSE
		    // Because the Area is composed of straight lines
			int type = iter.currentSegment(coords);
			Point2D currentPoint = new Point2D.Double(coords[0], coords[1]);
		    switch(type){
		    	case PathIterator.SEG_MOVETO:
		    		startPoint = (Point2D) currentPoint.clone();
		    		if(connectParts){
		    			if(centerPoint == null) centerPoint = (Point2D) startPoint.clone();
		    			else {
		    				if(!previousPoint.equals(centerPoint)){
		    					areaSegments.add(new Line2D.Double(previousPoint,centerPoint));
		    					//System.out.println("Prev to Center");
		    				}
		    				areaSegments.add(new Line2D.Double(centerPoint,startPoint));
		    				//System.out.println("Center to Start");
		    			}
		    		}
		    		break;
		    	case PathIterator.SEG_CLOSE:
		    		areaSegments.add(new Line2D.Double(previousPoint,startPoint));
		    		break;
		    	default:	//This will be some form of line
		    		areaSegments.add(new Line2D.Double(previousPoint,currentPoint));
		    }
		    
		    previousPoint = currentPoint;
		}

		//Now that we have all edges, the last step is to connect the last section to the center
		if(centerPoint != null && !centerPoint.equals(previousPoint)){
			areaSegments.add(new Line2D.Double(previousPoint,centerPoint));
			//System.out.println("Last to Center");
		}
		
		return areaSegments;
	}
}
