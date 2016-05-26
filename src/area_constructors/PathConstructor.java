package area_constructors;

import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.AbstractGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import org.jgrapht.graph.GraphPathImpl;
import org.jgrapht.traverse.ClosestFirstIterator;

import map_structure.Group;

public abstract class PathConstructor extends Constructor {

	private final static int threadcount = 5;
	
	protected static class GraphContainer{
		DirectedWeightedPseudograph<Point2D, ConstructorContainer> graph;
		private double separation;
		private final static Comparator<Point2D> pointComparitor = new Comparator<Point2D>(){
			@Override
   			public int compare(final Point2D lhs,Point2D rhs) {
				if(lhs.getY() == rhs.getY()){
					return Double.compare(lhs.getX(), rhs.getX());
				} else {
					return Double.compare(lhs.getY(), rhs.getY());
				}
			}
		};
			  
		
		private GraphContainer(DirectedWeightedPseudograph<Point2D, ConstructorContainer> _graph, double _separation){
			graph = _graph;
			separation = _separation;
		}
		
		protected void addVerticies(Collection<Point2D> verticies){
			for(Point2D point : verticies) graph.addVertex(point);
		}
		
		public double getSeparation(){
			return separation;
		}
		
		public void addEdge(Point2D source, Point2D target, Constructor constructor){
			this.addEdge(source, target, 1, constructor);
		}
		
		public void addEdge(Point2D source, Point2D target, double weight, Constructor constructor){
			this.__addEdge(source, target, weight, constructor);
			this.__addEdge(target, source, weight, constructor);
		}
		
		private void __addEdge(Point2D source, Point2D target, double weight, Constructor constructor){
			ConstructorContainer edge = new ConstructorContainer(constructor);
			graph.addEdge(source, target, edge);
			graph.setEdgeWeight(edge, weight);
		}

		public LinkedList<Point2D> getVerticies() {
			return new LinkedList<Point2D>(graph.vertexSet());
		}
		
		public List<Point2D> getVerticies(Area area) {
			LinkedList<Point2D> verticies = this.getVerticies();
			Iterator<Point2D> iterator = verticies.iterator();
			ArrayList<Line2D> areaLines = BasicShapeConstructor.getAreaLines(area, 0.1,true);
			while(iterator.hasNext()) {
				Point2D currentPoint = iterator.next();
				if(!area.contains(currentPoint.getX(),currentPoint.getY()) && !BasicShapeConstructor.intersectAnyLine(currentPoint,areaLines))
					iterator.remove();
			}
			return verticies;
		}
		
		protected ArrayList<ArrayList<Point2D>> getVerticiesAs2DArrayList(){
			LinkedList<Point2D> verticies = new LinkedList<Point2D>(graph.vertexSet());
			Collections.sort(verticies, pointComparitor);
			LinkedList<LinkedList<Point2D>> linkedList = new LinkedList<LinkedList<Point2D>>();
			Point2D previousPoint = verticies.remove();
			LinkedList<Point2D> currentRow = new LinkedList<Point2D>();
			linkedList.add(currentRow);
			currentRow.add(previousPoint);
			for(Point2D currentPoint : verticies){
				if(currentPoint.getY() == previousPoint.getY()){
					currentRow.add(currentPoint);
				} else {
					currentRow = new LinkedList<Point2D>();
					linkedList.add(currentRow);
					currentRow.add(currentPoint);
				}
			}
			ArrayList<ArrayList<Point2D>> toReturn = new ArrayList<ArrayList<Point2D>>();
			for(LinkedList<Point2D> currentList : linkedList){
				toReturn.add(new ArrayList<Point2D>(currentList));
			}
			
			return toReturn;
		}
	}
	
	private static <V,E> void clearEmptyVerticies(AbstractBaseGraph<V, E> graph){
		LinkedList<V> verticiesToRemove = new LinkedList<V>();
		for(V vertex : graph.vertexSet()){
			if(graph.inDegreeOf(vertex) + graph.outDegreeOf(vertex) == 0){
				verticiesToRemove.add(vertex);
			}
		}
		graph.removeAllVertices(verticiesToRemove);
	}
	
	private static class ExtendedClosestFirstIterator<V,E> extends ClosestFirstIterator<V,E>{

		private V startVertex;
		private HashSet<V> vertexSet;
		
		protected ExtendedClosestFirstIterator(Graph<V, E> g, V _startVertex) {
			super(g, _startVertex);
			vertexSet = new HashSet<V>();
			startVertex = _startVertex;
			vertexSet.add(startVertex);
		}
		

		protected GraphPath<V,E>  getShortestPath(ExtendedClosestFirstIterator<V, E> point) {
			return getShortestPath(point.startVertex);
		}
		
		protected GraphPath<V,E> getShortestPath(V endVertex){
			Graph<V,E> graph = this.getGraph();
			LinkedList<E> edges = new LinkedList<E>();
			V currentVertex = endVertex;
			double weight = 0;
			while(!startVertex.equals(currentVertex)){
				E currentEdge = this.getSpanningTreeEdge(currentVertex);
				currentVertex = graph.getEdgeSource(currentEdge);
				weight += graph.getEdgeWeight(currentEdge);
				edges.add(currentEdge);
			}
			
			return new GraphPathImpl<V,E>(graph,startVertex,endVertex,edges,weight);
		}

		public boolean connectsTo(ExtendedClosestFirstIterator<V, E> target) {
			return connectsTo(target.startVertex);
		}

		private boolean connectsTo(V target) {
			return !Double.isInfinite(this.getShortestPathLength(target));
		}


		protected Set<V> evaluate() {
			while(this.hasNext()){
				V vertex = this.next();
				if(!vertexSet.contains(vertex)){
					vertexSet.add(vertex);
				}
			}
			return vertexSet;
		}
		
	}
	
	private static class ConstructorContainer extends DefaultWeightedEdge{
		private Constructor constructor;
		private ConstructorContainer(Constructor _constructor){
			constructor = _constructor;
		}
	}
	
	public Path2D getPath(Area routeableArea, Group map, Collection<Point2D> points, double seperation, double distortionFactor, int distortionIterations){
		// 1. Populate the Graph with points
		GraphContainer container = new GraphContainer(vertexPopulateGraph(routeableArea,seperation),seperation);
		container.addVerticies(points);
		// 2. Annotate the Graph
		annotateGraph(routeableArea,map,container);
		// 3. Get the Shortest path options
		DirectedMultigraph<Point2D, ConstructorContainer> shortestTree = steinerForestApproximation(container.graph,points);
		// 4. Distort the path
		shortestTree = iterativeDistortTree(container.graph,shortestTree,points,distortionFactor,distortionIterations);
		// 5. Convert the path to a Pat2D
		//Print Runtime
		return graphToPath2D(shortestTree);
	}
	
	private <V, E> DirectedMultigraph<V, E> iterativeDistortTree(
			DirectedWeightedPseudograph<V, E> graph, DirectedMultigraph<V, E> tree, Collection<V> immobilePoints, double factor, int i) {
		for(int iterations = 0; iterations < i; iterations++)
			tree = distortGraph(graph,tree,immobilePoints,factor, i);
		return tree;
	}
	
	private <V, E> DirectedMultigraph<V, E> distortGraph(
			DirectedWeightedPseudograph<V, E> graph, DirectedMultigraph<V, E> tree, Collection<V> immobilePoints, double factor, int recursionCount) {
		
		//Choose two points in tree
		LinkedList<V> points = new LinkedList<V>(tree.vertexSet());
		int sourceIndex = random.nextInt(points.size() -1);
		ExtendedClosestFirstIterator<V,E> source = new ExtendedClosestFirstIterator<V,E>(tree,points.get(sourceIndex));
		points = new LinkedList<V>(source.evaluate());
		int targetIndex = random.nextInt(points.size() -1) +1;
		V target = points.get(targetIndex);
		//Get the current path between them
		GraphPath<V,E> currentPath = source.getShortestPath(target);
		
		//At this point we need to decide on real set of points that have no branches between them
		//It could be this pair, but it might not be. So we iterate over the path, looking for separating nodes
		LinkedList<V> terminalVerticies = new LinkedList<V>();
		for(E edge : currentPath.getEdgeList()){
			V currentTarget = tree.getEdgeTarget(edge);
			if(tree.inDegreeOf(currentTarget) + tree.outDegreeOf(currentTarget) > 2 || immobilePoints.contains(currentTarget)){
				terminalVerticies.add(currentTarget);
			}
		}
		terminalVerticies.add(target);
		
		//Remove the previous path
		tree.removeAllEdges(currentPath.getEdgeList());
		
		//Now we have a list of options for starting and stopping the distortion, and we can distort between them
		Iterator<V> vertexIterator = terminalVerticies.iterator();
		V previousVertex = source.startVertex;
		while(vertexIterator.hasNext()){
			V nextVertex = vertexIterator.next();
			GraphPath<V,E> newPath = __simpleDistortTree(graph, previousVertex,nextVertex,factor,recursionCount -1);
			for(E newEdge : newPath.getEdgeList()){
				V newSource = graph.getEdgeSource(newEdge);
				V newTarget = graph.getEdgeTarget(newEdge);
				tree.addVertex(newSource);
				tree.addVertex(newTarget);
				tree.addEdge(newSource, newTarget, newEdge);
			}
			previousVertex = nextVertex;
		}

		//Remove vertices with no edges
		clearEmptyVerticies(tree);
		
		return tree;
	}


	private <V,E> GraphPath<V,E> __simpleDistortTree(DirectedWeightedPseudograph<V, E> graph, V source, V target, double factor, int recursionCount) {
		ExtendedClosestFirstIterator<V,E> sourceIter = new ExtendedClosestFirstIterator<V,E>(graph,source);
		sourceIter.evaluate();
		return __simpleDistortTree(graph,sourceIter,target,factor,recursionCount);
	}

	private <V,E> GraphPath<V,E> __simpleDistortTree(DirectedWeightedPseudograph<V, E> graph, ExtendedClosestFirstIterator<V,E> sourceIter, V target, double factor, int recursionCount) {
		ExtendedClosestFirstIterator<V,E> targetIter = new ExtendedClosestFirstIterator<V,E>(graph,target);
		targetIter.evaluate();
		return __simpleDistortTree(graph,sourceIter,targetIter,factor,recursionCount);
	}
	
	private <V,E> GraphPath<V,E> __simpleDistortTree(DirectedWeightedPseudograph<V, E> graph, V source, ExtendedClosestFirstIterator<V,E> targetIter, double factor, int recursionCount) {
		ExtendedClosestFirstIterator<V,E> sourceIter = new ExtendedClosestFirstIterator<V,E>(graph,source);
		sourceIter.evaluate();
		return __simpleDistortTree(graph,sourceIter,targetIter,factor,recursionCount);
	}
	
	private <V, E> GraphPath<V,E> __simpleDistortTree(DirectedWeightedPseudograph<V, E> graph, ExtendedClosestFirstIterator<V, E> source,
			ExtendedClosestFirstIterator<V, E> target, double factor, int recursionCount) {
		//Find the current path.
		GraphPath<V,E> currentPath = source.getShortestPath(target);
		
		//Determine the new acceptable point set
		double acceptableWeight = currentPath.getWeight() * (1+factor);
		LinkedList<V> options = new LinkedList<V>();
		for(V check : graph.vertexSet()){
			double checkweight = source.getShortestPathLength(check);
			checkweight += target.getShortestPathLength(check);
			if(checkweight <= acceptableWeight) options.add(check);
		}
		//Select a point at random
		V selectedOption = options.get(random.nextInt(options.size()));
		GraphPath<V,E> firstHalfPath = null;
		GraphPath<V,E> secondHalfPath = null;
		if(recursionCount > 0){
			firstHalfPath = __simpleDistortTree(graph,source,selectedOption,factor,recursionCount-1);
			secondHalfPath = __simpleDistortTree(graph,selectedOption,target,factor,recursionCount-1);
		} else {
			firstHalfPath = source.getShortestPath(selectedOption);
			ExtendedClosestFirstIterator<V,E> optionIter = new ExtendedClosestFirstIterator<V,E>(graph,selectedOption);
			optionIter.evaluate();
			secondHalfPath = optionIter.getShortestPath(target);
		}
		return __addPaths(firstHalfPath,secondHalfPath);
	}

	private <V, E> GraphPath<V, E> __addPaths(GraphPath<V, E> firstPath, GraphPath<V, E> secondPath) {
		Graph<V,E> graph = firstPath.getGraph();
		V startVertex = firstPath.getStartVertex();
		V endVertex = secondPath.getEndVertex();
		LinkedList<E> edges = new LinkedList<E>(firstPath.getEdgeList());
		edges.addAll(secondPath.getEdgeList());
		double weight = firstPath.getWeight() + secondPath.getWeight();
		
		return new GraphPathImpl<V,E>(graph,startVertex,endVertex,edges,weight);
	}

	private <V,E> V getGraphCenter(Collection<ExtendedClosestFirstIterator<V,E>> sources, DirectedWeightedPseudograph<V,E> graph){
		V center = null;
		double centerWeight = Double.MAX_VALUE;
		for(V currentTarget : graph.vertexSet()){
			double currentTargetWeight = 0;
			for(ExtendedClosestFirstIterator<V,E> currentSource : sources){
				currentTargetWeight += currentSource.getShortestPathLength(currentTarget);
			}
			
			if(currentTargetWeight < centerWeight){
				center = currentTarget;
				centerWeight = currentTargetWeight;
			}
		}
		
		return center;
	}
	
	private <V,E> List<ExtendedClosestFirstIterator<V,E>> createSources(DirectedWeightedPseudograph<V, E> graph, Collection<V> points){
		LinkedList<ExtendedClosestFirstIterator<V,E>> sources = new LinkedList<ExtendedClosestFirstIterator<V,E>>();
		for(V currentSource : points){
			ExtendedClosestFirstIterator<V,E> newSourceIterator = new ExtendedClosestFirstIterator<V,E>(graph,currentSource);
			sources.add(newSourceIterator);
			while(newSourceIterator.hasNext()){
				newSourceIterator.next();
			}
		}
		return sources;
	}
	
	private <V,E> DirectedMultigraph<V, E> steinerForestApproximation(DirectedWeightedPseudograph<V, E> graph, Collection<V> points){
		//Construct all required paths
		List<ExtendedClosestFirstIterator<V,E>> sources = createSources(graph,points);

		//Create the tree/forest graph to return
		DirectedMultigraph<V, E> shortestTree = new DirectedMultigraph<V, E>((Class) null);
		
		//Separate the sources into connectable sets
		
		List<List<ExtendedClosestFirstIterator<V,E>>> connectableSourceSets = divideSourceSets(sources);
		
		for(List<ExtendedClosestFirstIterator<V,E>> connectableSourceSet : connectableSourceSets){
		
			//Get the center
			V center = getGraphCenter(connectableSourceSet,graph);
			
			//Add the center as a potential target
			shortestTree.addVertex(center);
			
			// Now annotate the shortestTree with best path options
			annotateShortestTree( connectableSourceSet, shortestTree);
		}
		return shortestTree;
	}

	private <V,E> List<List<ExtendedClosestFirstIterator<V, E>>> divideSourceSets(
			List<ExtendedClosestFirstIterator<V, E>> sources) {
		List<List<ExtendedClosestFirstIterator<V, E>>> connectableSourceSets = new LinkedList<List<ExtendedClosestFirstIterator<V, E>>>();
		
		Iterator<ExtendedClosestFirstIterator<V, E>> sourceIterator = sources.iterator();
		//Add the first set to the list
		connectableSourceSets.add(new LinkedList<ExtendedClosestFirstIterator<V, E>>());
		connectableSourceSets.get(0).add(sourceIterator.next());
		
		//So now there is at least one 'root' source available for connecting to.
		//Try to match every source to a source in a set. If this process fails,
		// create a new list and add it.
		while(sourceIterator.hasNext()){
			ExtendedClosestFirstIterator<V, E> currentSource = sourceIterator.next();
			boolean createNewList = true;
			//Try and match the currentSource to an existing set of sources
			Iterator<List<ExtendedClosestFirstIterator<V, E>>> setIterator = connectableSourceSets.iterator();
			while(setIterator.hasNext() && createNewList){
				List<ExtendedClosestFirstIterator<V, E>> currentSet = setIterator.next();
				if(currentSet.get(0).connectsTo(currentSource)){
					currentSet.add(0, currentSource);
					createNewList = false;
				}
			}
			
			//If no match was found, add the new list
			if(createNewList){
				LinkedList<ExtendedClosestFirstIterator<V, E>> listToAdd = new LinkedList<ExtendedClosestFirstIterator<V, E>>();
				listToAdd.add(currentSource);
				connectableSourceSets.add(listToAdd);
			}
		}
		
		return connectableSourceSets;
	}

	private <V, E> DirectedMultigraph<V, E> annotateShortestTree(List<ExtendedClosestFirstIterator<V,E>> sources, DirectedMultigraph<V, E> shortestTree){
		LinkedList<ExtendedClosestFirstIterator<V,E>> localSources = new LinkedList<ExtendedClosestFirstIterator<V,E>>(sources);
		
		//Burn down all sources
		while(localSources.size()>0){
			//Find the closest source/target pair
			ExtendedClosestFirstIterator<V,E> selectedSource = null;
			V selectedTarget = null;
			double selectedSourceTargetWeight = Double.MAX_VALUE;
			//Iterate over all pairs
			for(ExtendedClosestFirstIterator<V,E> currentSource : localSources){
				for(V currentTarget : shortestTree.vertexSet()){
					double currentSourceTargetWeight = currentSource.getShortestPathLength(currentTarget);
					if(currentSourceTargetWeight < selectedSourceTargetWeight){
						selectedSourceTargetWeight = currentSourceTargetWeight;
						selectedSource = currentSource;
						selectedTarget = currentTarget;
					}
				}
			}
			
			//With the current source/target pair, add the resulting path to the graph
			shortestTree = addGraphPath(selectedSource.getShortestPath(selectedTarget),shortestTree);
			localSources.remove(selectedSource);
		}
		return shortestTree;
	}
	
	private static <V, E> DirectedMultigraph<V, E> addGraphPath(GraphPath<V,E> path, DirectedMultigraph<V, E> shortestTree){
		Graph<V,E> graph = path.getGraph();
		for(E edge : path.getEdgeList()){
			V source = graph.getEdgeSource(edge);
			V target = graph.getEdgeTarget(edge);
			double weight = graph.getEdgeWeight(edge);
			shortestTree.addVertex(source);
			shortestTree.addVertex(target);
			shortestTree.addEdge(source, target, edge);
			shortestTree.setEdgeWeight(edge, weight);
		}
		return shortestTree;
	}
	
	private static <T_Edge> Path2D graphToPath2D(AbstractBaseGraph<Point2D, T_Edge> graph){
		Path2D path = new Path2D.Double();
		for(T_Edge edge : graph.edgeSet()){
			Point2D source = graph.getEdgeSource(edge);
			Point2D target = graph.getEdgeTarget(edge);
			path.moveTo(source.getX(), source.getY());
			path.lineTo(target.getX(), target.getY());
		}
		return path;
	}
	
	protected DirectedWeightedPseudograph<Point2D, ConstructorContainer> vertexPopulateGraph(Area area ,double separation){
		DirectedWeightedPseudograph<Point2D, ConstructorContainer> graph = new DirectedWeightedPseudograph<Point2D, ConstructorContainer>(ConstructorContainer.class);
		for(Point2D point : BasicShapeConstructor.getPointsInArea(area,separation)){
			graph.addVertex(point);
		}
		return graph;
	}
	
	protected abstract void annotateGraph(Area routeableArea, Group map, GraphContainer graph);
	
	@Override
	public abstract Group blockingArea(Constructor c, Group constructed);

	@Override
	public abstract Group construct(Area routeableArea, Group currentMap);

}
