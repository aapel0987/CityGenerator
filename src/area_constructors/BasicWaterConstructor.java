package area_constructors;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;

import map_structure.Group;
import map_structure.AreaLayer;
import materials.Material;
import materials.MaterialsCollection;
import test.TestGUIManager;

public final class BasicWaterConstructor extends PathConstructor {

	private final static Material water = MaterialsCollection.Water;
	private final static Material sand  = MaterialsCollection.Sand;
	
	private double width;
	private Group constructedGroup;

	public Group construct(Area routeableArea, Group currentMap) {
		Area localRoutingArea = new Area(routeableArea);
		localRoutingArea.subtract(currentMap.blockingArea(this).getArea());
		return this.createComplexRiver(localRoutingArea);
	}
	
	public BasicWaterConstructor(double w){
		width = w;
		constructedGroup = new Group("BasicWaterConstructor", this);
		constructedGroup.add("water",new AreaLayer(water,new Area()));
		constructedGroup.add("sand",new AreaLayer(sand,new Area()));
	}
		
	private Group createComplexRiver(Area routeableArea){
		//Place River
		Path2D riverpath = BasicShapeModifier.distortPath2D(BasicWaterConstructor.createRandomRiverNetwork(routeableArea.getBounds()), 50, 50,10);
		constructedGroup = createSimpleRiver(constructedGroup,riverpath,width);
		return constructedGroup;
	}
	
	private static Group createSimpleRiver(Group river, Path2D path, double width){
		Area waterArea = BasicShapeModifier.smoothArea(BasicShapeModifier.distortArea(BasicShapeConstructor.basicConnectedCircles(path, width/2),width/5,width/10, 5),0.7,0.5);
		return addToGroup(river,waterArea,new Area(waterArea.getBounds()));
	}
	
	private static Group addToGroup(Group group, Area waterArea, Area routeableArea){
		//Get the existing areas
		AreaLayer waterLayer = (AreaLayer) group.get("water");
		AreaLayer sandLayer = (AreaLayer) group.get("sand");
		
		//Create Sandbars
		final float sandWidth = 1;
		final BasicStroke stroke = new BasicStroke(sandWidth);
		Area sandArea = BasicShapeModifier.smoothArea(BasicShapeModifier.distortArea(new Area(stroke.createStrokedShape(waterArea)),sandWidth/5,sandWidth/10, 5),0.7,0.5);
		
		//Confine to route-able space
		waterLayer.intersect(routeableArea);
		sandLayer.intersect(routeableArea);
		
		//Add to existing layers
		waterLayer.add(waterArea);
		sandLayer.add(sandArea);
		sandLayer.subtract(waterLayer);
		return group;
	}
	
	private static Path2D createRandomRiverNetwork(Rectangle bounds){
		//Create 4 points on the edges
		LinkedList<Point2D> points = new LinkedList<Point2D>(); 
		points.add( new Point2D.Double(getRandomNormal(bounds.getMinX(),bounds.getMaxX()),bounds.getMaxY()));	//Top
		points.add( new Point2D.Double(getRandomNormal(bounds.getMinX(),bounds.getMaxX()),bounds.getMinY()));	//Bottom
		points.add( new Point2D.Double(bounds.getMinX(), getRandomNormal(bounds.getMinY(),bounds.getMaxY())));	//Left
		points.add( new Point2D.Double(bounds.getMaxX(), getRandomNormal(bounds.getMinY(),bounds.getMaxY())));	//Right
		
		//Now Create center point
		Point2D center = new Point2D.Double(
				getRandomNormal(bounds.getMinX(),bounds.getMaxX(),(bounds.getMinX()+bounds.getMaxX())/2),
				getRandomNormal(bounds.getMinY(),bounds.getMaxY(),(bounds.getMinY()+bounds.getMaxY())/2));
		
		//Now connect random points to the center
		Path2D path = new Path2D.Double();
		for(int iterationCount = random.nextInt(points.size() - 1)+2; iterationCount >0; iterationCount--){
			//Choose a point
			int selection = random.nextInt(points.size());
			Point2D choice = points.remove(selection);
			path.append(new Line2D.Double(choice.getX(), choice.getY(), center.getX(), center.getY()), false);
		}
		return path;
	}

	public Group addLakes(Area routeableArea, Group map, int count, double coverageTarget, double targetRatio){

		TestGUIManager gui = new TestGUIManager("addLakes");
		Area blockingArea = map.blockingArea(this).getArea();
		Area localRouteableArea = new Area(routeableArea);
		localRouteableArea.subtract(blockingArea);
		Rectangle bounds = localRouteableArea.getBounds();
		
		//Create the lake area
		Area lakeArea = new Area();
		while(count-->0){
			lakeArea.add(BasicShapeConstructor.ConstructComplexConnectedSquares(localRouteableArea,coverageTarget,targetRatio,3));
		}
		//Distort and crop
		lakeArea = BasicShapeModifier.distortArea(lakeArea,10,7,5);
		lakeArea.intersect(localRouteableArea);
		
		//Update the group
		addToGroup(constructedGroup,lakeArea,localRouteableArea);
		
		//Update the GUI
		gui.addShape(bounds, Color.gray);
		gui.addShape(blockingArea, Color.red);
		gui.addShape(localRouteableArea, Color.green);
		gui.addShape(lakeArea, Color.blue);
		return constructedGroup;
	}
	
	public Group blockingArea(Constructor c, Group constructed) {
		if(c.equals(this)) return new Group("BasicWaterConstructor_null_blockingArea", this);
		return constructed;
	}

	protected double getWeight(Point2D p0, Point2D p1){
		double weight = p0.distance(p1);
		weight *= (1 + (random.nextDouble()/2));
		return weight;
	}
}
