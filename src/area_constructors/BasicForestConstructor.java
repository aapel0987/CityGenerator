package area_constructors;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Point2D;

import map_structure.Generateable;
import map_structure.Group;
import map_structure.Layer;
import materials.Material;
import materials.MaterialPoint;
import materials.MaterialsCollection;

public class BasicForestConstructor extends Constructor {

	private static final Material tree = MaterialsCollection.Tree;
	
	private double coverageTarget;
	private double targetRatio; 
	private int count;
	
	public BasicForestConstructor(Area a, double t, double r, int c){
		coverageTarget = t;
		targetRatio = r;
		count = c;
	}
	
	public Group construct(Area routeableArea, Group currentMap) {
		Area localRoutingArea = new Area(routeableArea);
		localRoutingArea.subtract(currentMap.blockingArea(this).getArea());
		return ConstructComplexForest(localRoutingArea);
	}
	
	private Group ConstructComplexForest(Area routeableArea){
		Group forest = new Group("ConstructComplexForest",this);
		Layer trees = new Layer(tree);
		int iterations = count;
		while(iterations-- > 0){
			trees.add(ConstructComplexForestArea(routeableArea));
		}
		forest.add("trees", trees);
		return forest;
	}
	
	private Area ConstructComplexForestArea(Area routeableArea){
		Area forestArea = new Area();
		Rectangle bounds = routeableArea.getBounds();
		double targetVolume = bounds.getHeight()*bounds.getWidth()*coverageTarget;
		//Iterative process, get a random point, then get another random point, then fill with forest
		Point2D previousPoint = getRandomPoint(bounds);
		while(targetVolume > 0){
			double ratioedVolume = Math.max(targetVolume*targetRatio,1);
			double length = Math.sqrt(ratioedVolume/GOLDEN_RATIO);
			Point2D nextPoint = BasicShapeModifier.pointRandomReposition(previousPoint, length);
			double width = Math.random()*GOLDEN_RATIO*length;
			//Beauty check
			if(previousPoint.distance(nextPoint)/width < GOLDEN_RATIO && width/previousPoint.distance(nextPoint) < GOLDEN_RATIO){
				forestArea.add(BasicShapeConstructor.connectPoints(previousPoint, width, nextPoint, width));
				targetVolume -= width*previousPoint.distance(nextPoint)*0.5;
				previousPoint = nextPoint;
			}
			//System.out.println("targetVolume: " + targetVolume+"\tratioedVolume: " + ratioedVolume);
		}
		//Apply Distortion
		forestArea = BasicShapeModifier.distortArea(forestArea,10,10,5);
		forestArea.intersect(routeableArea);
		return forestArea;
	}

	public Group blockingArea(Constructor c, Group constructed) {
		return constructed;
	}

}
