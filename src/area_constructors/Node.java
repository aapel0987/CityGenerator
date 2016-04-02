package area_constructors;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import test.TestGUIManager;

public class Node extends Point2D.Double {
		private LinkedList<Node> connections;
		private boolean covered;
		
		public boolean equals(Object obj){
			return super.equals(obj);
		}
		
		public Node(Point2D point){
			this.setLocation(point.getX(), point.getY());
			connections = new LinkedList<Node>();
			covered = false;
		}
		
		public Node(Point2D point, Node conns[]){
			this.setLocation(point.getX(), point.getY());
			connections = new LinkedList<Node>();
			covered = false;
			for(int index = 0; index < conns.length; index++){
				connections.add(conns[index]);
			}
		}

		public Node(Point2D point, List<Node> conns){
			this.setLocation(point.getX(), point.getY());
			connections = new LinkedList<Node>(conns);
			covered = false;
		}
		
		public void connect(Node n){
			if(this != n && !connections.contains(n)){
				connections.add(n);
				n.connections.add(this);
			}
		}
		
		public boolean connectsTo(Node n){
			return connections.contains(n);
		}
		
		public void connect(List<Node> nodes){
			Iterator<Node> nodeIter = nodes.iterator();
			while(nodeIter.hasNext()){
				this.connect(nodeIter.next());
			}
		}
		
		public static void connectAll(List<Node> nodes){
			Iterator<Node> nodeIter = nodes.iterator();
			while(nodeIter.hasNext()){
				nodeIter.next().connect(nodes);
			}
		}
		
		public void randomReposition(double maxMove){
			double angle = (((double)2)*Math.PI)*Math.random();
			double distance = maxMove*Math.random();
			this.setLocation(getX() + distance*Math.cos(angle), getY() + distance*Math.sin(angle));
		}
		
		public boolean isCovered(){
			return covered;
		}
		
		public void cover(){
			covered = true;
		}
		
		public static void cover(List<Node> nodes){
			Iterator<Node> nodeIter = nodes.iterator();
			while(nodeIter.hasNext()){
				nodeIter.next().cover();
			}
		}
		
		public void uncover(){
			covered = false;
		}
		
		public static void uncover(List<Node> nodes){
			Iterator<Node> nodeIter = nodes.iterator();
			while(nodeIter.hasNext()){
				nodeIter.next().uncover();
			}
		}
		
		public static List<Node> uncoveredNodes(List<Node> nodes){
			LinkedList<Node> uncoveredNodes = new LinkedList<Node>();
			Iterator<Node> nodeIter = nodes.iterator();
			while(nodeIter.hasNext()){
				Node currNode = nodeIter.next();
				if(!currNode.covered){
					uncoveredNodes.add(currNode);
				}
			}
			return uncoveredNodes;
		}
		
		public void disconnect(Node n){
			connections.remove(n);
			n.connections.remove(this);
		}
		
		public void disconnect(){
			disconnect(connections);
		}

		public void disconnect(List<Node> nodes){
			Iterator<Node> nodeIter = nodes.iterator();
			while(nodeIter.hasNext()){
				nodeIter.next().disconnect(this);
			}
		}
		
		public static void disconnectAll(List<Node> nodes){
			Iterator<Node> nodeIter = nodes.iterator();
			while(nodeIter.hasNext()){
				nodeIter.next().disconnect(nodes);
			}
		}
		
		public String superToString(){
			return super.toString();
		}
		
		public String toString(){
			String toReturn = super.toString();
			Iterator<Node> nodeIter = connections.iterator();
			if(nodeIter.hasNext()){
				while(nodeIter.hasNext()){
					toReturn += "\n\t" + nodeIter.next().superToString();
				}
			} else {
				toReturn += "\n\tNo Connections.";
			}
			return toReturn;
		}
		
		public static String nodesToString(List<Node> nodes){
			String toReturn = "";
			Iterator<Node> nodeIter = nodes.iterator();
			while(nodeIter.hasNext()){
				toReturn += nodeIter.next().toString();
				if(nodeIter.hasNext()) toReturn += "\n";
			}
			return toReturn;
		}
		
		public Iterator<Node> getConnectionsIterator(){
			return connections.iterator();
		}
		
		public void paintNode(TestGUIManager gui, Color nodeColor, Color edgeColor){
			Iterator<Node> connIter = this.getConnectionsIterator();
			gui.addPoints(new Point2D[]{this}, 0.25, nodeColor);
			while(connIter.hasNext()){
				gui.addShape((new BasicStroke((float) 0.1)).createStrokedShape(new Line2D.Double(this, connIter.next())), edgeColor);
			}
		}
		
		public static void paintNodes(List<Node> nodes, TestGUIManager gui, Color nodeColor, Color edgeColor){
			Iterator<Node> nodeIter = nodes.iterator();
			while(nodeIter.hasNext()){
				nodeIter.next().paintNode(gui, nodeColor, edgeColor);
			}
		}

		public LinkedList<Node> getConnections() {
			return new LinkedList<Node>(connections);
		}

		public static List<Node> pointsToNodes(List<Point2D> points){
			Iterator<Point2D> pointIter = points.iterator();
			LinkedList<Node> nodes = new LinkedList<Node>();
			while(pointIter.hasNext()){
				nodes.add(new Node(pointIter.next()));
			}
			return nodes;
		}
		
		private static void __copyConnections(Map<Node, Node> nodes){
			Iterator<Node> origNodeIterator = nodes.keySet().iterator();
			while(origNodeIterator.hasNext()){
				Node originalNode = origNodeIterator.next();
				Iterator<Node> connectionIterator = originalNode.connections.iterator();
				Node newNode = nodes.get(originalNode);
				while(connectionIterator.hasNext()){
					Node newConnectionNode = nodes.get(connectionIterator.next());
					if(newConnectionNode != null){
						newNode.connect(newConnectionNode);
					}
				}
			}
		}
		
		public static List<Node> pruneAbsentNodes(List<Node> nodes){
			LinkedList<Node> prunedList = new LinkedList<Node>();
			HashMap<Node, Node> nodePairs = new HashMap<Node, Node>();
			Iterator<Node> nodeIter = nodes.iterator();
			while(nodeIter.hasNext()){
				Node currentNode = nodeIter.next();
				Node newNode = new Node(currentNode);
				prunedList.add(newNode);
				nodePairs.put(currentNode,newNode);
			}
			__copyConnections(nodePairs);
			return prunedList;
		}
		
		private static List<Node> __getAllNodesInGraph(List<Node> nodes){
			cover(nodes);
			LinkedList<Node> fullGraph = new LinkedList<Node>(nodes);
			LinkedList<Node> remainingSearchNodes = new LinkedList<Node>(nodes);
			while(!remainingSearchNodes.isEmpty()){
				Iterator<Node> searchConnections = remainingSearchNodes.removeFirst().connections.iterator();
				while(searchConnections.hasNext()){
					Node currentConnection = searchConnections.next();
					if(!currentConnection.isCovered()){
						currentConnection.cover();
						fullGraph.add(currentConnection);
						remainingSearchNodes.add(currentConnection);
					}
				}
			}
			uncover(fullGraph);	//Includes all original nodes
			return fullGraph;
		}
		
		public static List<Node> connectNodesAtIntersections(List<Node> nodes){
			LinkedList<Node> newNodes = new LinkedList<Node>();
			uncover(nodes);
			Iterator<Node> checkNodeIter = nodes.iterator();
			while(checkNodeIter.hasNext()){
				Node checkNode = checkNodeIter.next();
				checkNode.cover();
				Iterator<Node> connectionIter = (new LinkedList(checkNode.connections)).iterator();
				while(connectionIter.hasNext()){
					Node connectionNode = connectionIter.next();
					
					if(!connectionNode.isCovered() && checkNode.connectsTo(connectionNode)){
						//At this point we have a pair of nodes that we want to check the connection for
						//Now we have to iterate over all other uncovered node pairs in the system and check for
						//those intersections
						newNodes.addAll(__mergeNodePairWithGraphConnections(checkNode,connectionNode,nodes));
					}
				}
			}
			uncover(nodes);
			return newNodes;
		}
		
		private static List<Node> __mergeNodePairWithGraphConnections(Node a1, Node a2, List<Node> nodes) {
			LinkedList<Node> newNodes = new LinkedList<Node>();
			Iterator<Node> checkNodeIter = nodes.iterator();
			while(checkNodeIter.hasNext()){
				Node checkNode = checkNodeIter.next();
				if(!checkNode.isCovered() && !checkNode.equals(a1) && !checkNode.equals(a2)){
					Iterator<Node> connectionIter = (new LinkedList<Node>(checkNode.connections)).iterator();
					while(connectionIter.hasNext()){
						Node connectionNode = connectionIter.next();
						if(!connectionNode.isCovered() && !connectionNode.equals(a1) && !connectionNode.equals(a2) && checkNode.connectsTo(connectionNode)){
							//At this point we have a pair of nodes that we want to check the connection for
							//Now we have to iterate over all other uncovered node pairs in the system and check for
							//those intersections
							newNodes.addAll(__mergeOverlappingConnections(a1,a2,checkNode,connectionNode));
						}
					}
				}
			}
			return newNodes;
		}

		private static List<Node> __mergeOverlappingConnections(Node a1, Node a2, Node b1, Node b2){
			LinkedList<Node> newNodes = new LinkedList<Node>(); 
			//Create the representative lines
			Line2D lineA = new Line2D.Double(a1,a2);
			Line2D lineB = new Line2D.Double(b1,b2);
			//First check: If the lines intersect, something needs to be done
			if(lineA.intersectsLine(lineB)){
				//So now we know something needs to be done
				//In reality we should handle overlapping line segments, but for now we wont, because that's hard to code
				//and I just don't feel like it right now. We'll just assume they intersect at a single point and
				//move on with our lives. It's good enough for what I am doing with it right now.
				Point2D intersectionPoint = BasicShapeConstructor.getIntersectionPoint(lineA, lineB);
				//System.out.println("{" + lineA.getP1().toString() + "," + lineA.getP2().toString() + "},{" + lineB.getP1().toString() + "," + lineB.getP2().toString() + "}");
				Node newNode = null;
				LinkedList<Node> originalNodes = new LinkedList<Node>();
				if(intersectionPoint.equals(a1))      newNode = a1;
				else if(intersectionPoint.equals(a2)) newNode = a2;
				else if(intersectionPoint.equals(b1)) newNode = b1;
				else if(intersectionPoint.equals(b2)) newNode = b2;
				else {
					newNode = new Node(intersectionPoint);
					newNodes.add(newNode);
					System.out.println("Added New Node: " + newNode.toString());
				}
				originalNodes.add(a1);
				originalNodes.add(a2);
				originalNodes.add(b1);
				originalNodes.add(b2);
				a1.disconnect(a2);
				b1.disconnect(b2);
				newNode.connect(originalNodes);
			}
			
			return newNodes;
		}
		
	}