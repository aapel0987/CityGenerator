package area_constructors;

import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import test.TestGUIManager;

public final class BasicPathFinder {

	private static LinkedList<Path2D> __getAreaPaths__recursive__paths = new LinkedList<Path2D>();
	private static TestGUIManager gui = new TestGUIManager("BasicPathFinder");
	
	public static List<Path2D> getAreaPaths(List<Point2D> toConnect, Area routeableArea, double pathWidth, double maxReach){
		//First check that the input is valid
		Iterator<Point2D> pointIter = toConnect.iterator();
		while(pointIter.hasNext()){
			if(!routeableArea.contains(pointIter.next())){
				throw new IllegalArgumentException("Connection Point outside of routeable area.");
			}
		}
		
		if(maxReach <= pathWidth){
			throw new IllegalArgumentException("Need to be able to find points outside of the current path.");
		}
		
		//gui.addShape(routeableArea, Color.green);
		
		//Now get all of the nodes in the routeableArea, Divide maxReach by Sqrt(2) to bring points closer in, max is 45deg diagonal
		gui.addShape(routeableArea, Color.green);
		List<Node> nodes = Node.pointsToNodes(BasicShapeConstructor.getPointsInArea(routeableArea, pathWidth/Math.sqrt(2)));
		//Node.paintNodes(nodes,gui,Color.blue,Color.gray);
		List<Node> toConnectNodes = Node.pointsToNodes(toConnect);
		nodes.addAll(toConnectNodes);
		
		//Now connect all of the nodes to each other in regions
		connectNeighborhoodNodes(nodes,maxReach);
		List<Node> newNodes = Node.connectNodesAtIntersections(nodes);
		System.out.println("# New Nodes: " + newNodes.size());
		for(Node n : newNodes) System.out.println("\tNew Node: " + n.toString());
		nodes.addAll(newNodes);
		Node.paintNodes(nodes,gui,Color.red,Color.black);
		
		Node startNode = toConnectNodes.get(0);
		toConnectNodes.remove(0);
		startNode.cover();
		List<Path2D> toReturn = new LinkedList<Path2D>(__getAreaPaths__recursive(true,startNode,nodes,toConnectNodes, new LinkedList<Node>()));
		startNode.uncover();
		return toReturn;
	}
	
	private static List<Path2D> __getAreaPaths__recursive(boolean reset, Node currentNode, List<Node> areaNodes, List<Node> remainingNodes, List<Node> currentPath){

		if(reset) __getAreaPaths__recursive__paths.clear();
		
		//This node is now blocked for sub paths
		LinkedList<Node> localCurrentPath = new LinkedList<Node>(currentPath);
		LinkedList<Node> localRemainingNodes = new LinkedList<Node>(remainingNodes);
		localCurrentPath.add(currentNode);
		
		//gui.addPoint((Node) currentNode.clone(), 4, Color.PINK);
		
		//If this is one of the current nodes, but not the last node, then remove this from current nodes and try to add paths from previous nodes
		if(localRemainingNodes.remove(currentNode)){
			if(localRemainingNodes.isEmpty()){
				//All nodes found, no further work from this node on is needed
				__getAreaPaths__recursive__paths.addAll(BasicShapeModifier.nodesToPaths(localCurrentPath));
				//Node.paintNodes(localCurrentPath,gui,Color.orange,Color.black);
				return __getAreaPaths__recursive__paths;
			}
			
			//Since a goal node was just covered include previous nodes as path start points
			Iterator<Node> prevNodeIter = currentPath.iterator();
			while(prevNodeIter.hasNext()){
				__getAreaPaths__recursive(false,prevNodeIter.next(),areaNodes,localRemainingNodes,localCurrentPath);
			}
		}
		
		
		Iterator<Node> connectionIter = currentNode.getConnectionsIterator();
		while(connectionIter.hasNext()){
			Node nextNode = connectionIter.next();
			if(!nextNode.isCovered()){
				nextNode.cover();
				__getAreaPaths__recursive(false,nextNode,areaNodes,localRemainingNodes,localCurrentPath);
				nextNode.uncover();
			}
		}
		
		//System.out.println("Finished reaching in this area!");
		//gui.addShape(reachCircle, Color.BLUE);
		return __getAreaPaths__recursive__paths;
	}
	
	private static void connectNeighborhoodNodes(List<Node> nodes, double neighborhoodSize){
		Iterator<Node> graphIter = nodes.iterator();
		while(graphIter.hasNext()){
			Node currentNode = graphIter.next();
			currentNode.cover();
			Iterator<Node> neighborhoodIter = nodesInArea(nodes,BasicShapeConstructor.basicCircle(currentNode, neighborhoodSize)).iterator();
			while(neighborhoodIter.hasNext()){
				Node currentNeighbor = neighborhoodIter.next();
				if(!currentNeighbor.isCovered()){
					currentNode.connect(currentNeighbor);
				}
			}
		}
		Node.uncover(nodes);
	}
	
	private static List<Node> nodesInArea(List<Node> nodes, Area area){
		LinkedList<Node> internalPoints = new LinkedList<Node>();
		Iterator<Node> pointIter = nodes.iterator();
		while(pointIter.hasNext()){
			Node currPoint = pointIter.next();
			if(area.contains(currPoint)){
				internalPoints.add(currPoint);
			}
		}
		return internalPoints;
	}
}
