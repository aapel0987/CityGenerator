package area_constructors;

import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jgrapht.graph.DirectedMultigraph;

import map_structure.Group;
import map_structure.AreaLayer;
import materials.Material;
import materials.MaterialsCollection;

public final class BasicWaterConstructor extends PathConstructor {

	private final static Material water = MaterialsCollection.Water;
	private final static Material sand  = MaterialsCollection.Sand;
	
	private Group constructedGroup;
	private LinkedList<List<Point2D>> waterBodyEdgePointSets;

	public Group construct(Area routeableArea, Group currentMap) {
		return constructedGroup;
	}
	
	public BasicWaterConstructor(){
		constructedGroup = new Group("BasicWaterConstructor", this);
		constructedGroup.add("water",new AreaLayer(water));
		constructedGroup.add("sand",new AreaLayer(sand));
	}
		
	public Group createComplexRiver(Area routeableArea, Group map, double width){
		Area localRoutingArea = getLocalRoutingArea(routeableArea,map);
		//Place River
		Path2D riverpath = BasicShapeModifier.distortPath2D(BasicWaterConstructor.createRandomRiverNetwork(routeableArea.getBounds()), 50, 50,10);
		constructedGroup = createSimpleRiver(localRoutingArea, constructedGroup,riverpath,width);
		return constructedGroup;
	}
	
	private static Group createSimpleRiver(Area routeableArea, Group river, Path2D path, double width){
		Area waterArea = BasicShapeModifier.smoothArea(BasicShapeModifier.distortArea(BasicShapeConstructor.basicConnectedCircles(path, width/2),width/5,width/10, 5),0.7,0.5);
		waterArea.intersect(routeableArea);
		Area groupRouteableArea = new Area(waterArea.getBounds());
		groupRouteableArea.add(routeableArea);
		return addToGroup(river,waterArea,groupRouteableArea);
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

		Area blockingArea = map.blockingArea(this).getArea();
		Area localRouteableArea = new Area(routeableArea);
		localRouteableArea.subtract(blockingArea);
		
		//Create the lake area
		Area lakeArea = new Area();
		while(count-->0){
			lakeArea.add(BasicShapeConstructor.ConstructComplexConnectedSquares(localRouteableArea,coverageTarget,targetRatio,3));
		}
		//Distort and crop
		lakeArea = BasicShapeModifier.distortArea(lakeArea,10,7,5);
		lakeArea.intersect(localRouteableArea);
		
		//Update the group
		constructedGroup = addToGroup(constructedGroup,lakeArea,localRouteableArea);
		
		//Update the GUI
		/*TestGUIManager gui = new TestGUIManager("addLakes");
		Rectangle bounds = localRouteableArea.getBounds();
		gui.addShape(bounds, Color.gray);
		gui.addShape(blockingArea, Color.red);
		gui.addShape(localRouteableArea, Color.green);
		gui.addShape(lakeArea, Color.blue);*/
		return constructedGroup;
	}
	
	public Group connectWaterBodies(Area routeableArea, Group map, double width){
		Area localRoutingArea = new Area(routeableArea);
		Area blockingArea = map.blockingArea(this).getArea();
		Stroke keepoutCreator = new BasicStroke((float) width);
		blockingArea.add(new Area(keepoutCreator.createStrokedShape(blockingArea)));
		localRoutingArea.subtract(blockingArea);
		waterBodyEdgePointSets = getWaterBodyEdgePointSets(localRoutingArea);
		if(waterBodyEdgePointSets.size() < 2) return constructedGroup;
		LinkedList<Point2D> requiredPoints = new LinkedList<Point2D>();
		LinkedList<Point2D> waterBodyEdgePoints = new LinkedList<Point2D>();
		for(List<Point2D> pointSet : waterBodyEdgePointSets) {
			requiredPoints.add(pointSet.get(0));
			waterBodyEdgePoints.addAll(pointSet);
		}
		DirectedMultigraph<Point2D, ConstructorContainer> shortestTree = getShortestTree(localRoutingArea, map, requiredPoints, waterBodyEdgePoints, 1, 1, 3);
		Path2D waterPath = graphToPath2D(shortestTree,this);
		constructedGroup = createSimpleRiver(localRoutingArea,constructedGroup, waterPath, width);
		
		//Test GUI stuff
		/*TestGUIManager gui = new TestGUIManager("connectWaterBodies");
		gui.addShape(routeableArea, Color.GREEN);
		gui.addPoints(waterBodyEdgePoints, 1, Color.gray);
		gui.addPoints(requiredPoints, 1, Color.black);
		BasicStroke stroke = new BasicStroke((float) (0.5) );
		gui.addShape(stroke.createStrokedShape(waterPath), Color.BLUE);*/
		return constructedGroup;
	}
	
	protected void annotateGraph(Area routeableArea, Group map, GraphContainer graph){
		super.annotateGraph(routeableArea, map, graph);
		for(List<Point2D> waterBodyEdgePoints : waterBodyEdgePointSets){
			NullPathConstructor.NULL_PATH_CONSTRUCTOR_ZERO.connectAll(waterBodyEdgePoints, graph);
		}
	}
	
	private LinkedList<Area> getWaterBodies(Area routeableArea){
		//Get the existing areas
		AreaLayer waterLayer = (AreaLayer) constructedGroup.get("water");
		Area localRouteableArea = new Area(routeableArea);
		localRouteableArea.subtract(waterLayer);
		//Separate out the disconnected bodies of water
		ArrayList<Path2D> waterBodyPaths = BasicShapeConstructor.getAreaPaths(waterLayer);
		//Create Areas for each
		LinkedList<Area> waterBodies = new LinkedList<Area>();
		for(Path2D path : waterBodyPaths) waterBodies.add(new Area(path));
		return waterBodies;
	}
	
	private LinkedList<List<Point2D>> getWaterBodyEdgePointSets(Area routeableArea){
		//Get a separate set of edge points for each
		LinkedList<List<Point2D>> waterBodyEdgePointSets = new LinkedList<List<Point2D>>();
		for(Area waterBody : getWaterBodies(routeableArea)) waterBodyEdgePointSets.add(BasicShapeConstructor.getAreaEdgePoints(waterBody, 1));
		return waterBodyEdgePointSets;
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
	
	public Group getConstructedGroup(){
		return constructedGroup;
	}
}
