package area_constructors;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jgrapht.graph.DirectedMultigraph;

import map_structure.AreaLayer;
import map_structure.Group;
import materials.Material;

public class BasicRoadConstructor extends PathConstructor {
	
	private Material roadSurface;
	private double roadWidth;
	
	public BasicRoadConstructor(double _roadWidth, Material _roadSurface){
		roadWidth = _roadWidth;
		roadSurface = _roadSurface;
	}
	
	
	
	protected double getWeight(Point2D p0, Point2D p1){
		double weight = p0.distance(p1);
		weight *= (1 + (random.nextDouble()/2));
		return weight;
	}
	
	@Override
	public Group blockingArea(Constructor c, Group constructed) {
		if(c.equals(this)) return new Group("BasicWaterConstructor_null_blockingArea", this);
		return constructed;
	}

	@Override
	public Group construct(Area routeableArea, Group currentMap) {
		Area localRoutingArea = new Area(routeableArea);
		Area blockingArea = currentMap.blockingArea(this).getArea();
		Stroke keepoutCreator = new BasicStroke((float) roadWidth);
		blockingArea.add(new Area(keepoutCreator.createStrokedShape(blockingArea)));
		localRoutingArea.subtract(blockingArea);
		//Get the points on the edge of the map that we want to connect
		LinkedList<Point2D> edgeOptions = new LinkedList<Point2D>(BasicShapeConstructor.getAreaEdgePoints(new Area(localRoutingArea.getBounds()), 1));
		List<Line2D> routeableAreaBoundry = BasicShapeConstructor.getAreaLines(routeableArea, 0.1, false);
		Iterator<Point2D> edgeOptionIter = edgeOptions.iterator();
		while(edgeOptionIter.hasNext()){
			if(!BasicShapeConstructor.intersectAnyLine(edgeOptionIter.next(), routeableAreaBoundry)){
				edgeOptionIter.remove();
			}
		}
		//edgeOptions now contains only points that are on the outer edge of the route-able area, select a few
		LinkedList<Point2D> startingPoints = new LinkedList<Point2D>();
		int pointsToKeep = random.nextInt(4 -2 +1) +2;
		for(int iterationCount = 0; iterationCount < pointsToKeep && edgeOptions.size() > 0; iterationCount++){
			int chosenPoint = random.nextInt(edgeOptions.size());
			startingPoints.add(edgeOptions.remove(chosenPoint));
		}
		//Create a path between them
		DirectedMultigraph<Point2D, ConstructorContainer> shortestTree = getShortestTree(localRoutingArea, currentMap, startingPoints, 1, 0.1, 3);
		Path2D roadPath = graphToPath2D(shortestTree,this);
		//Return a group based on the path constructed
		return createRoadOnPath(roadPath);
	}

	private Group createRoadOnPath(Path2D roadPath) {
		Group roadGroup = new Group("road_group",this);
		AreaLayer roadLayer = new AreaLayer(roadSurface);
		roadGroup.add("road_layer",roadLayer);
		if(!roadPath.getBounds().isEmpty()) roadLayer.add(BasicShapeModifier.smoothArea(BasicShapeConstructor.basicConnectedCircles(roadPath, roadWidth),0.9,0.5));
		return roadGroup;
	}

}
