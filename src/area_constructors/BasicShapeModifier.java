package area_constructors;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import test.TestGUIManager;

final public class BasicShapeModifier {

	
	
	public static Area distortArea(Area originalArea, double seperation, double maxMove){
		
		List<Node> nodes = pathsToNodes(BasicShapeConstructor.getAreaPaths(originalArea),seperation);
		Iterator<Node> nodeIter = nodes.iterator();
		while(nodeIter.hasNext()){
			Node currentNode = nodeIter.next();
			currentNode.randomReposition(maxMove);
		}
		
		return new Area(appendPaths(nodesToPaths(nodes)));
	}
	
	private static Path2D appendPaths(List<Path2D> paths){
		Path2D path = new Path2D.Double();
		Iterator<Path2D> pathIter = paths.iterator();
		while(pathIter.hasNext()){
			path.append(pathIter.next(),false);
		}
		return path;
	}
	
	public static List<Path2D> nodesToPaths(List<Node> nodes){
		List<Node> prunedNodes = Node.pruneAbsentNodes(nodes);
		Iterator<Node> nodeIter = prunedNodes.iterator();
		while(nodeIter.hasNext()){
			if(nodeIter.next().getConnections().size() != 2){
				return nodesToPaths__open(prunedNodes);
			}
		}
		
		return nodesToPaths__closed(prunedNodes);
	}
	
	private static List<Path2D> nodesToPaths__open(List<Node> nodes){
		LinkedList<Path2D> paths = new LinkedList<Path2D>();
		Iterator<Node> nodeIter = nodes.iterator();
		while(nodeIter.hasNext()){
			Node currNode = nodeIter.next();
			if(!currNode.isCovered()){
				paths.add(nodesToPaths__spanningTree_recursive(currNode));
			}
		}
		return paths;
	}
	
	private static Path2D nodesToPaths__spanningTree_recursive(Node startNode){
		startNode.cover();
		Path2D toReturn = new Path2D.Double();
		toReturn.moveTo(startNode.getX(), startNode.getY());
		Iterator<Node> nodeIter = startNode.getConnectionsIterator();
		while(nodeIter.hasNext()){
			Node currNode = nodeIter.next();
			if(!currNode.isCovered()){
				toReturn.lineTo(currNode.getX(), currNode.getY());
				toReturn.append(nodesToPaths__spanningTree_recursive(currNode), false);
				toReturn.moveTo(startNode.getX(), startNode.getY());
			}
		}
		return toReturn;
	}
	
	private static List<Path2D> nodesToPaths__closed(List<Node> nodes){
		List<Path2D> paths = new LinkedList<Path2D>();
		Iterator<Node> nodeIter = nodes.iterator();
		while(nodeIter.hasNext()){
			Node startNode = nodeIter.next();
			if(!startNode.isCovered()){
				Path2D currentPath = new Path2D.Double();
				startNode.cover();
				currentPath.moveTo(startNode.getX(), startNode.getY());
				Node currentNode = startNode.getConnections().getFirst();
				Node prevNode = startNode; 
				while(currentNode != startNode){
					currentNode.cover();
					currentPath.lineTo(currentNode.getX(), currentNode.getY());
					//Get the next node
					Iterator<Node> connectionIter = currentNode.getConnections().iterator();
					Node nextNode = null;
					do{
						nextNode = connectionIter.next();
					}while(nextNode.equals(prevNode));
					prevNode = currentNode;
					currentNode = nextNode;
				}
				
				//Now all that is left is to close the path and add it to the list
				currentPath.closePath();
				paths.add(currentPath);
			}
		}
		
		return paths;
	}
	
	public static List<Node> pathsToNodes(List<Path2D> paths, double seperation){
		Iterator<Path2D> pathIter = paths.iterator();
		List<Node> nodes = new LinkedList<Node>();
		while(pathIter.hasNext()){
			nodes.addAll(pathToNodes(pathIter.next(),seperation));
		}
		return nodes;
	}
	
	public static List<Node> pathToNodes(Path2D path, double seperation){
		List<Line2D> lines = BasicShapeConstructor.path2DToLines(path,seperation);
		Iterator<Line2D> lineIter = lines.iterator();
		List<Node> nodes = new LinkedList<Node>();
		if(lineIter.hasNext()){
			nodes.addAll(line2DToNodes(lineIter.next(),seperation));
			while(lineIter.hasNext()){
				List<Node> newNodes = line2DToNodes(lineIter.next(),seperation);
				if(nodes.get(nodes.size()-1).equals(newNodes.get(0))){
					newNodes.get(0).disconnect();
					newNodes.remove(0);
				}
				
				//Since we just deleted a node it's possible that there are no nodes left
				if(!newNodes.isEmpty()){
					nodes.get(nodes.size()-1).connect(newNodes.get(0));
					nodes.addAll(newNodes);
				}
			}
			if(nodes.get(nodes.size()-1).equals(nodes.get(0))){
				nodes.get(0).disconnect();
				nodes.remove(0);
			}
			nodes.get(nodes.size()-1).connect(nodes.get(0));
		}
		
		return nodes;
	}
	
	public static List<Node> line2DToNodes(Line2D line, double seperation){
		LinkedList<Node> nodes = new LinkedList<Node>();
		Node currentNode = new Node(line.getP1());
		double xIncriment, yIncriment;
		if(line.getP2().getX() == line.getP1().getX()){	//Handle the 0 x translation specially
			xIncriment = 0;
			if(line.getP2().getY() > line.getP1().getY()){
				yIncriment = seperation;
			} else {
				yIncriment = ((double) -1)*seperation;
			}
		} else {
			double slope = (line.getP2().getY() - line.getP1().getY())/(line.getP2().getX() - line.getP1().getX());
			double angle = Math.atan(slope);
			xIncriment = seperation*Math.cos(angle);
			yIncriment = seperation*Math.sin(angle);
			//Now handle reversing direction
			if(line.getP2().getX() < line.getP1().getX()){
				xIncriment *= ((double) -1);
				yIncriment *= ((double) -1);
			}
		}

		Node nextNode = new Node(new Point2D.Double(currentNode.getX() + xIncriment, currentNode.getY() + yIncriment));
		nodes.add(currentNode);
		for(int count = (int) Math.floor(line.getP1().distance(line.getP2())/seperation); count > 0 ; count --){
			currentNode.connect(nextNode);
			currentNode = nextNode;
			nodes.add(currentNode);
			nextNode = new Node(new Point2D.Double(currentNode.getX() + xIncriment, currentNode.getY() + yIncriment));
		}
		
		//If the last node was not equal to the end point
		if(!currentNode.equals(line.getP2())){
			nextNode = new Node(line.getP2());
			currentNode.connect(nextNode);
			nodes.add(nextNode);
		}
		return nodes;
	}
}
