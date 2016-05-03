package area_constructors;

import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import map_structure.Group;
import map_structure.Layer;
import materials.Material;
import materials.MaterialsCollection;

public final class BasicWaterConstructor extends Constructor {

	private final static Material water = MaterialsCollection.Water;
	private final static Material sand  = MaterialsCollection.Sand;
	
	private double width; 

	public Group construct(Area routeableArea, Group currentMap) {
		Area localRoutingArea = new Area(routeableArea);
		localRoutingArea.subtract(currentMap.blockingArea(this).getArea());
		return this.createComplexRiver(localRoutingArea);
	}
	
	public BasicWaterConstructor(double w){
		width = w;
	}
	
	private Group createComplexRiver(Area routeableArea){
		//Place River
		Path2D riverpath = BasicShapeModifier.distortPath2D(BasicWaterConstructor.createRandomRiverNetwork(routeableArea.getBounds()), 50, 50,10);
		Group river = new Group("BasicWaterConstructor",this);
		river = createSimpleRiver(river,riverpath,width);
		return river;
	}
	
	private static Group createSimpleRiver(Group river, Path2D path, double width){
		Layer waterLayer = new Layer(water,BasicShapeModifier.smoothArea(BasicShapeModifier.distortArea(BasicShapeConstructor.basicConnectedCircles(path, width/2),width/5,width/10),0.7,0.5));
		Layer sandLayer = new Layer(sand,new Area((new BasicStroke(1)).createStrokedShape(waterLayer)));
		river.add("water",waterLayer);
		river.add("sand",sandLayer);
		return river;
	}
	
	private static Path2D createRandomRiverNetwork(Rectangle bounds){
		//Create 4 points on the edges
		LinkedList<Point2D> points = new LinkedList<Point2D>(); 
		points.add( new Point2D.Double(random.nextDouble()*bounds.getWidth() + bounds.getMinX(),bounds.getMaxY()));
		points.add( new Point2D.Double(random.nextDouble()*bounds.getWidth() + bounds.getMinX(),bounds.getMinY()));
		points.add( new Point2D.Double(bounds.getMinX(), random.nextDouble()*bounds.getHeight() + bounds.getMinY()));
		points.add( new Point2D.Double(bounds.getMaxX(), random.nextDouble()*bounds.getHeight() + bounds.getMinY()));
		
		//Now Create center point
		Point2D center = getRandomPoint(bounds);
		
		//Now connect random points to the center
		Path2D path = new Path2D.Double();
		for(int iterationCount = random.nextInt(points.size() - 2)+2; iterationCount >0; iterationCount--){
			//Choose a point
			int selection = random.nextInt(points.size() -1);
			Point2D choice = points.remove(selection);
			path.append(new Line2D.Double(choice.getX(), choice.getY(), center.getX(), center.getY()), false);
		}
		return path;
	}

	public Group blockingArea(Constructor c, Group constructed) {
		return constructed;
	}

}
