package area_constructors;

import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import test.TestGUIManager;

final public class BasicShapeModifier {

	
	
	public static Area distortArea(Area originalArea, double seperation, double maxMove){
		return new Area(distortPath2D(new Path2D.Double(originalArea), seperation, maxMove));
	}

	public static Path2D distortPath2D(Path2D path, double seperation, double maxMove){
		Path2D distortedPath = new Path2D.Double();
		double[] coords = new double[6];
		Point2D startPoint = null, previousPoint = null;
		for (PathIterator iter = path.getPathIterator(null, seperation); !iter.isDone(); iter.next()) {
		    // The type will be SEG_LINETO, SEG_MOVETO, or SEG_CLOSE
		    // Because the Area is composed of straight lines
			int type = iter.currentSegment(coords);
			Point2D nextPoint = new Point2D.Double(coords[0], coords[1]);
		    switch(type){
		    	case PathIterator.SEG_MOVETO:
		    		startPoint = (Point2D) nextPoint.clone();
		    		distortedPath.moveTo(startPoint.getX(), startPoint.getY());
		    		break;
		    	case PathIterator.SEG_CLOSE:
		    		distortedPath.append(getDistortedPathBetweenPoints(previousPoint,startPoint, seperation, maxMove), true);
		    		break;
		    	default:	//This will be some form of line
		    		distortedPath.append(getDistortedPathBetweenPoints(previousPoint,nextPoint, seperation, maxMove), true);
		    }
		    
		    previousPoint = nextPoint;
		}

		return distortedPath;
	}
	
	private static Path2D getDistortedPathBetweenPoints(Point2D start, Point2D end, double seperation, double maxMove){
		Path2D distortedPath = new Path2D.Double();
		distortedPath.moveTo(start.getX(), start.getY());
		double theta = Math.atan((end.getY()-start.getY())/(end.getX()-start.getX()));
		if(end.getX() < start.getX()) {theta += Math.PI;}
		Point2D currentPoint = new Point2D.Double(start.getX(),start.getY());
		for(int count = (int) Math.floor(start.distance(end)/seperation); count > 0; count--){
			currentPoint.setLocation(currentPoint.getX() + seperation*Math.cos(theta), currentPoint.getY() + seperation*Math.sin(theta));
			Point2D movedPoint = pointRandomReposition(currentPoint,maxMove);
			distortedPath.lineTo(movedPoint.getX(), movedPoint.getY());
		}
		distortedPath.lineTo(end.getX(), end.getY());
		return distortedPath;
	}
	
	private static Point2D pointRandomReposition(Point2D point, double maxMove){
		double angle = (((double)2)*Math.PI)*Math.random();
		double distance = maxMove*Math.random();
		return new Point2D.Double(point.getX() + distance*Math.cos(angle), point.getY() + distance*Math.sin(angle));
	}
	
	public static Path2D iterativeDistortPath(Path2D path, double spacing, double movement, int iterations){
		Path2D distortedPath = new Path2D.Double(path);
		while(iterations-- > 0){
			distortedPath = distortPath2D(distortedPath,spacing,movement);
			spacing /= 2;
			movement /= 3;
		}
		return distortedPath;
	}
}
