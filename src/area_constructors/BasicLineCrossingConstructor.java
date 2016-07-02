package area_constructors;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import org.jgrapht.graph.DirectedMultigraph;

import map_structure.AreaLayer;
import map_structure.Group;
import materials.Material;

public class BasicLineCrossingConstructor extends PathConstructor {
	
	private Material surface;
	private double width;
	PathConstructor crossingConstructor;
	Constructor toCross;
	
	public BasicLineCrossingConstructor(double _width, Material _surface, Constructor _toCross){
		width = _width;
		surface = _surface;
		toCross = _toCross;
	}
	
	protected void annotateGraph(Area routeableArea, Group map, GraphContainer graph){
		super.annotateGraph(routeableArea, map, graph, Double.MAX_VALUE,graph.getEdgeLines());
	}
	
	protected double getWeight(Point2D p0, Point2D p1){
		double weight = p0.distance(p1) *2;
		weight *= (1 + (random.nextDouble()/2));
		return weight;
	}
	
	@Override
	public Group blockingArea(Constructor c, Group constructed) {
		if(c.equals(this)) return new Group("BasicLineCrossingConstructor_null_blockingArea", this);
		return constructed;
	}

	@Override
	public Group construct(Area routeableArea, Group currentMap) {
		Area localRoutingArea = getLocalRoutingArea(routeableArea, currentMap);
		//Get the points on the edge of the map that we want to connect
		int pointsToKeep = random.nextInt(4 -2 +1) +2;
		LinkedList<Point2D> startingPoints = getEdgePointsNormallyDistributed( localRoutingArea, pointsToKeep);
		//Create a path between them
		DirectedMultigraph<Point2D, ConstructorContainer> shortestTree = getShortestTree(localRoutingArea, currentMap, startingPoints, 1, 0.1, 3);
		Path2D roadPath = graphToPath2D(shortestTree,this);
		//Return a group based on the path constructed
		return createCrossingOnPath(roadPath);
	}

	protected Area getLocalRoutingArea(Area routeableArea, Group currentMap){
		Area localRoutingArea = new Area(routeableArea);
		Group blockingGroup = currentMap.blockingArea(this);
		blockingGroup.removeGroupsByConstructor(toCross);
		Area blockingArea = blockingGroup.getArea();
		Stroke keepoutCreator = new BasicStroke((float) width);
		blockingArea.add(new Area(keepoutCreator.createStrokedShape(blockingArea)));
		localRoutingArea.subtract(blockingArea);
		return localRoutingArea;
	}
	
	Group createCrossingOnPath(Path2D crossingPath) {
		Group crossingGroup = new Group("crossingGroup",this);
		
		List<Line2D> lines = BasicShapeConstructor.path2DToLines(crossingPath, 0.1);
		
		int count = 0;
		for(Line2D line : lines){
			AreaLayer crossingLayer = new AreaLayer(surface);
			crossingGroup.add("crossingLayer_" + count++,crossingLayer);
			if(!crossingPath.getBounds().isEmpty())
				crossingLayer.add(BasicShapeConstructor.connectPoints(line.getP1(), width, line.getP2(), width));
		}
		
		return crossingGroup;
	}

	public double getWidth(){
		return width;
	}
}
