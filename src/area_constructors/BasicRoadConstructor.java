package area_constructors;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;

import org.jgrapht.graph.DirectedMultigraph;

import map_structure.AreaLayer;
import map_structure.Group;
import materials.Material;
import test.TestGUIManager;

public class BasicRoadConstructor extends PathConstructor {
	
	private Material roadSurface;
	private double roadWidth;
	BasicLineCrossingConstructor crossingConstructor;
	
	public BasicRoadConstructor(double _roadWidth, Material _roadSurface){
		roadWidth = _roadWidth;
		roadSurface = _roadSurface;
	}
	
	public BasicRoadConstructor(double _roadWidth, Material _roadSurface, BasicLineCrossingConstructor _crossingConstructor) {
		this(_roadWidth,_roadSurface);
		crossingConstructor = _crossingConstructor;
	}
	
	protected void annotateGraph(Area routeableArea, Group map, GraphContainer graph){
		super.annotateGraph(routeableArea, map, graph);
		if(crossingConstructor != null) {
			Area crossableRoutingArea = getBlockingArea(routeableArea, map);
			Stroke edgeInclusionCreator = new BasicStroke((float) crossingConstructor.getWidth());
			crossableRoutingArea.add(new Area(edgeInclusionCreator.createStrokedShape(crossableRoutingArea)));
			
			TestGUIManager gui = new TestGUIManager("BasicRoadConstructor_annotateGraph_crossingConstructor");
			gui.addShape(routeableArea.getBounds(), Color.GREEN);
			gui.addShape(routeableArea, Color.GRAY);
			gui.addShape(crossableRoutingArea, Color.RED);
			LinkedList<Point2D> pointsInArea = BasicShapeConstructor.getPointsInArea(crossableRoutingArea, graph.getVerticies());
			System.err.println("Connecting " + pointsInArea.size() + " points in area.");
			gui.addPoints(pointsInArea, 1, Color.BLACK);
			crossingConstructor.annotateGraph(crossableRoutingArea, map, graph);
		}
	}
	
	protected double getWeight(Point2D p0, Point2D p1){
		double weight = p0.distance(p1);
		weight *= (1 + (random.nextDouble()/2));
		return weight;
	}
	
	@Override
	public Group blockingArea(Constructor c, Group constructed) {
		if(c.equals(this)) return new Group("BasicRoadConstructor_null_blockingArea", this);
		return constructed;
	}

	@Override
	public Group construct(Area routeableArea, Group currentMap) {
		Area localRoutingArea = new Area(routeableArea);
		Area blockingArea = getBlockingArea(routeableArea, currentMap);
		localRoutingArea.subtract(blockingArea);
		//Get the points on the edge of the map that we want to connect
		int pointsToKeep = random.nextInt(4 -2 +1) +2;
		LinkedList<Point2D> startingPoints = getEdgePointsNormallyDistributed( localRoutingArea, pointsToKeep);
		//Create a path between them
		DirectedMultigraph<Point2D, ConstructorContainer> shortestTree = getShortestTree(localRoutingArea, currentMap, startingPoints, 1, 0.1, 3);

		Path2D roadPath = graphToPath2D(shortestTree,this);
		//Return a group based on the path constructed
		if(crossingConstructor == null) {
			return createRoadOnPath(roadPath);
		}
		//Create a super group for the road and the crossings
		Group fullGroup = new Group("Complete Road", this);
		fullGroup.add(createRoadOnPath(roadPath));
		fullGroup.add(crossingConstructor.createCrossingOnPath(graphToPath2D(shortestTree,crossingConstructor)));
		return fullGroup;
	}

	protected Area getBlockingArea(Area routeableArea, Group currentMap){
		Group blockingGroup = currentMap.blockingArea(this);
		Area blockingArea = blockingGroup.getArea();
		Stroke keepoutCreator = new BasicStroke((float) roadWidth);
		blockingArea.add(new Area(keepoutCreator.createStrokedShape(blockingArea)));
		return blockingArea;
	}
	
	private Group createRoadOnPath(Path2D roadPath) {
		Group roadGroup = new Group("road_group",this);
		AreaLayer roadLayer = new AreaLayer(roadSurface);
		roadGroup.add("road_layer",roadLayer);
		if(!roadPath.getBounds().isEmpty()) roadLayer.add(BasicShapeModifier.smoothArea(BasicShapeConstructor.basicConnectedCircles(roadPath, roadWidth),0.9,0.5));
		return roadGroup;
	}

}
