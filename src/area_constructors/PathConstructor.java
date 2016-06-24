package area_constructors;

import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

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
import utils.PeekableIterator;

public abstract class PathConstructor extends Constructor {

	private final static int THREADCOUNT = 5;
	
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
		
		protected LinkedList<LinkedList<Point2D>> getVerticiesAs2DLinkedList(){
			return getVerticiesAs2DLinkedList(null);
		}
		
		protected LinkedList<LinkedList<Point2D>> getVerticiesAs2DLinkedList(Area area){
			LinkedList<Point2D> verticies = new LinkedList<Point2D>();
			if(area != null) verticies.addAll(this.getVerticies(area));
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
			
			return linkedList;
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
		
		protected ExtendedClosestFirstIterator(Graph<V, E> g, V _startVertex){
			this(g,_startVertex,Double.POSITIVE_INFINITY);
		}
		
		protected ExtendedClosestFirstIterator(Graph<V, E> g, V _startVertex, double radius) {
			super(g, _startVertex, radius);
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

		public static <V, E> void evaluateAll(Iterable<ExtendedClosestFirstIterator<V, E>> iterators) {
			final Iterator<ExtendedClosestFirstIterator<V, E>> iterator = iterators.iterator();
			final Semaphore iterSem = new Semaphore(1);
			Thread threads[] = new Thread[THREADCOUNT];
			
			for(int threadindex = 0; threadindex < THREADCOUNT; threadindex++){
				threads[threadindex] = new Thread("evaluateAll_thread_" + threadindex){
			        @Override
			        public void run(){
			        	ExtendedClosestFirstIterator<V, E> currentIter = null;
			        	try {
							iterSem.acquire();
							if(iterator.hasNext()) currentIter = iterator.next();
							iterSem.release();
						} catch (InterruptedException e) {
							System.err.println("No one should be interruppting this thread: " + this.getName());
							System.err.flush();
							System.exit(1);
						}
			        	while(currentIter != null){
			        		currentIter.evaluate();
							currentIter = null;
				        	try {
								iterSem.acquire();
								if(iterator.hasNext()) currentIter = iterator.next();
								iterSem.release();
							} catch (InterruptedException e) {
								System.err.println("No one should be interruppting this thread: " + this.getName());
								System.err.flush();
								System.exit(1);
							}
			        	}
			        }
			    };
				threads[threadindex].start();
			}
			
			for(int threadindex = 0; threadindex < THREADCOUNT; threadindex++){
				try {
					threads[threadindex].join();
				} catch (InterruptedException e) {
					System.err.println("No one should be interruppting this thread. annotateGraph()");
					System.err.flush();
					System.exit(1);
				}
			}
		}

		public Set<V> getVerticies() {
			return vertexSet;
		}
	}
	
	private static class ConstructorContainer extends DefaultWeightedEdge{
		/**
		 * 
		 */
		private static final long serialVersionUID = -7469406552292482475L;
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
		shortestTree = distortGraph(container.graph,shortestTree,points,distortionFactor,distortionIterations);
		// 5. Convert the path to a Pat2D
		//Print Runtime
		return graphToPath2D(shortestTree);
	}
	
	private <V, E> DirectedMultigraph<V, E> distortGraph(
			DirectedWeightedPseudograph<V, E> graph, DirectedMultigraph<V, E> tree, Collection<V> immobilePoints, double factor, int recursionCount) {
		
		TreeDistorter<V, E> treeDistorter = new TreeDistorter<V, E>(graph, tree, immobilePoints, factor, recursionCount);
		Thread threads[] = new Thread[THREADCOUNT];
		for(int threadindex = 0; threadindex < THREADCOUNT; threadindex++){
			threads[threadindex] = new Thread(treeDistorter,"distortGraph_Thread_" + threadindex);
			threads[threadindex].start();
		}
		
		for(int threadindex = 0; threadindex < THREADCOUNT; threadindex++){
			try {
				threads[threadindex].join();
			} catch (InterruptedException e) {
				System.err.println("No one should be interruppting this thread. distortGraph()");
				System.err.flush();
				System.exit(1);
			}
		}
		return treeDistorter.getDistortedResult();
	}

	private class TreeDistorter<V, E> implements Runnable{
		
		DirectedWeightedPseudograph<V, E> graph;
		Collection<V> immobilePoints;
		double factor;
		int recursionCount;
		
		Semaphore pathPairsSem;
		LinkedList<PathPair> pathPairs;
		
		Semaphore distortedResultSem;
		DirectedMultigraph<V, E> distortedResult;
		
		private void addPathPair(PathPair arg0){
			try {
				pathPairsSem.acquire();
			} catch (InterruptedException e) {
				System.err.println("No one should be interruppting this thread. addPathPairs()");
				System.err.flush();
				System.exit(1);
			}
			pathPairs.addFirst(arg0);
			pathPairsSem.release();
		}
		
		private PathPair getNextPathPair(){
			try {
				pathPairsSem.acquire();
			} catch (InterruptedException e) {
				System.err.println("No one should be interruppting this thread. getNextPathPair()");
				System.err.flush();
				System.exit(1);
			}
			System.out.println("Got next Pair, count was: " + pathPairs.size());
			PathPair nextPathPair = null;
			if(!pathPairs.isEmpty()) nextPathPair = pathPairs.removeFirst();
			pathPairsSem.release();
			return nextPathPair;
		}
		
		private class PathPair{
			private ExtendedClosestFirstIterator<V,E> source;
			private ExtendedClosestFirstIterator<V,E> target;
			private int counter;

			protected PathPair(ExtendedClosestFirstIterator<V,E> _source, ExtendedClosestFirstIterator<V,E> _target){
				this(_source,_target,0);
			}
			
			protected PathPair(ExtendedClosestFirstIterator<V,E> _source, ExtendedClosestFirstIterator<V,E> _target, int _counter){
				source = _source;
				target = _target;
				counter = _counter;
			}
			
			protected void split(double factor){
				//Find all options for points within range
				Set<V> sourceSet = source.getVerticies();
				Set<V> targetSet = target.getVerticies();
				double maxWeight = source.getShortestPathLength(target.startVertex) * (1+factor);
				LinkedList<V> options = new LinkedList<V>();
				for(V optionVertex : sourceSet){
					if(targetSet.contains(optionVertex) && (source.getShortestPathLength(optionVertex) + target.getShortestPathLength(optionVertex) < maxWeight)){
						options.add(optionVertex);
					}
				}
				//Select one
				if(options.size() == 0){
					System.out.println("No options available:");
					System.out.println("\tSource: " + source.startVertex);
					System.out.println("\tTarget: " + target.startVertex);
				}
				V selectedOption = options.get(random.nextInt(options.size()));
				//Create a new iterator for that point
				ExtendedClosestFirstIterator<V,E> selectedOptionIter =
						new ExtendedClosestFirstIterator<V,E>(source.getGraph(),selectedOption,
								Math.max(source.getShortestPathLength(selectedOption), target.getShortestPathLength(selectedOption)) * (1+factor));
				selectedOptionIter.evaluate();
				//add two new pairs to the pool
				System.out.println("Splitting: " + counter);
				if(!source.startVertex.equals(selectedOptionIter.startVertex)) addPathPair(new PathPair(source,selectedOptionIter,counter +1));
				if(!target.startVertex.equals(selectedOptionIter.startVertex)) addPathPair(new PathPair(selectedOptionIter,target,counter +1));
			}
			
			protected int getCounter(){
				return counter;
			}
			
			protected GraphPath<V,E> getPath(){
				return source.getShortestPath(target);
			}
		}
		
		protected DirectedMultigraph<V, E> getDistortedResult(){
			return distortedResult;
		}
		
		protected TreeDistorter(DirectedWeightedPseudograph<V, E> _graph, DirectedMultigraph<V, E> _tree, Collection<V> _immobilePoints, double _factor, int _recursionCount){
			graph = _graph;
			immobilePoints = _immobilePoints;
			factor = _factor;
			recursionCount = _recursionCount;
			
			pathPairsSem = new Semaphore(1);
			pathPairs = getInitialPathPairs(_tree);

			distortedResultSem = new Semaphore(1);
			distortedResult = new DirectedMultigraph<V, E>((Class) null);
		}
		
		private LinkedList<TreeDistorter<V, E>.PathPair> getInitialPathPairs(DirectedMultigraph<V, E> tree) {
			//Add in immobile points as needed
			HashSet<V> localImmobilePoints = new HashSet<V>(immobilePoints);
			for(V vertex : tree.vertexSet()){
				if((tree.inDegreeOf(vertex) != 1 || tree.outDegreeOf(vertex) != 1) && !localImmobilePoints.contains(vertex)){
					localImmobilePoints.add(vertex);
				}
			}
			//Create pairs based on outgoing degree and what is reachable
			//This works because we assume the following
			//1) The graph is directed, as implied by type
			//2) The graph has no loops, as implied by type
			//Therefore All nodes with an outgoing edge of odd degree must have a path to a node with odd degree
			HashMap<V,Double> largestWeights = new HashMap<V,Double>();
			HashMap<V,LinkedList<V>> targetVerticies = new HashMap<V,LinkedList<V>>();
			for(V immobileVertex : localImmobilePoints){
				largestWeights.put(immobileVertex, (double) 0);
				targetVerticies.put(immobileVertex, new LinkedList<V>());
			}
			//Now find all pairs as verticies
			for(V sourceVertex : localImmobilePoints){
				System.out.println("Source: " + sourceVertex.toString());
				System.out.println("\tOutgoingEdgeCount: " + tree.outgoingEdgesOf(sourceVertex));
				for(E initialEdge : tree.outgoingEdgesOf(sourceVertex)){
					V targetVertex = tree.getEdgeTarget(initialEdge);
					double currentWeight = tree.getEdgeWeight(initialEdge);
					while(!localImmobilePoints.contains(targetVertex)){
						//Ummmmmmmm not sure this is the best, but it should work because all verticies with anything other than 1 outgoing edge are immobile
						E nextEdge = tree.outgoingEdgesOf(targetVertex).iterator().next();
						currentWeight += tree.getEdgeWeight(nextEdge);
						targetVertex = tree.getEdgeTarget(nextEdge);
					}
					//Update maps
					targetVerticies.get(sourceVertex).add(targetVertex);
					if(largestWeights.get(sourceVertex) < currentWeight) largestWeights.put(sourceVertex, currentWeight);
					if(largestWeights.get(targetVertex) < currentWeight) largestWeights.put(targetVertex, currentWeight);
				}
			}
			//Create new iterators on the immobile points based on maximum weights, and update them
			HashMap<V,ExtendedClosestFirstIterator<V,E>> newIterators = new HashMap<V,ExtendedClosestFirstIterator<V,E>>();
			for(Entry<V, Double> entry : largestWeights.entrySet()){
				newIterators.put(entry.getKey(), new ExtendedClosestFirstIterator<V,E>(graph, entry.getKey(), entry.getValue()));
			}
			ExtendedClosestFirstIterator.evaluateAll(newIterators.values());
			//Lastly, create the initial pairs
			LinkedList<PathPair> localPathPairs = new LinkedList<PathPair>();
			for(Entry<V, LinkedList<V>> sourceEntry : targetVerticies.entrySet()){
				for(V target : sourceEntry.getValue()){
					localPathPairs.add(new PathPair(newIterators.get(sourceEntry.getKey()),newIterators.get(target)));
				}
			}
			System.out.println("Initial Path Pairs: " + localPathPairs.size());
			return localPathPairs;
		}
		
		private void evaluatePathPairs(){
			PathPair nextPathPair = getNextPathPair();
			while(nextPathPair != null){
				if(nextPathPair.getCounter() >= recursionCount){
					GraphPath<V,E> path = nextPathPair.getPath();
					try {
						distortedResultSem.acquire();
					} catch (InterruptedException e) {
						System.err.println("No one should be interruppting this thread. getNextPathPair()");
						System.err.flush();
						System.exit(1);
					}
					//Add verticies and edges to the newly distorted tree
					for(E edge : path.getEdgeList()){
						V source = graph.getEdgeSource(edge);
						V target = graph.getEdgeTarget(edge);
						distortedResult.addVertex(source);
						distortedResult.addVertex(target);
						distortedResult.addEdge(source, target, edge);
						distortedResult.setEdgeWeight(edge, path.getWeight());
					}
					
					distortedResultSem.release();
					System.out.println("Adding to Tree.");
				} else {
					nextPathPair.split(factor);
				}
				nextPathPair = getNextPathPair();
			}
		}
		
		@Override
		public void run() {
			evaluatePathPairs();
		}
		
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
		}
		ExtendedClosestFirstIterator.evaluateAll(sources);
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
	
	protected void annotateGraph(Area routeableArea, Group map, GraphContainer graph){
		annotateGraph(routeableArea, map, graph, graph.getSeparation() * 1.5);
	}
	
	protected void annotateGraph(Area routeableArea, Group map, GraphContainer graph, double connectRadius){
		LinkedList<Line2D> blockingLines = new LinkedList<Line2D>(BasicShapeConstructor.getAreaLines(routeableArea, BasicShapeConstructor.pointOnLineError, false));
		LinkedList<LinkedList<Point2D>> allPoints = graph.getVerticiesAs2DLinkedList(routeableArea);
		GraphAnnotator annotator = new GraphAnnotator(blockingLines,allPoints,graph, this, connectRadius);
		Thread threads[] = new Thread[THREADCOUNT];
		for(int threadindex = 0; threadindex < THREADCOUNT; threadindex++){
			threads[threadindex] = new Thread(annotator,"annotateGraph_Thread_" + threadindex);
			threads[threadindex].start();
		}
		
		for(int threadindex = 0; threadindex < THREADCOUNT; threadindex++){
			try {
				threads[threadindex].join();
			} catch (InterruptedException e) {
				System.err.println("No one should be interruppting this thread. annotateGraph()");
				System.err.flush();
				System.exit(1);
			}
		}
	}
	
	private class GraphAnnotator implements Runnable {

		//As a rule the input arguments that are stored for local use cannot be modified, with the exception
		//of the input graph being annotated.
		LinkedList<Line2D> blockingLines;
		PathConstructor parent;
		double connectionDistance;
		
		//These fields are protected by semaphore
		Semaphore graphSem;
		GraphContainer graph;
		
		Semaphore edgeListSem;
		LinkedList<LinkedList<PseudoEdge>> edgeLists;

		Semaphore remainingPointsSem;
		LinkedList<LinkedList<Point2D>> remainingPoints;
		
		Semaphore rowAndNearestOptionsSem;
		LinkedList<RowAndNearest> rowAndNearestOptions;
		
		private class PseudoEdge {
			private Point2D source;
			private Point2D target;
			private double weight;
			
			public PseudoEdge(Point2D _source, Point2D _target, double _weight){
				source = _source;
				target = _target;
				weight = _weight;
			}
			
			public void addToGraphContainer(){
				graph.addEdge(source, target, weight, parent);
			}
		}
		
		public GraphAnnotator(LinkedList<Line2D> _blockingLines, LinkedList<LinkedList<Point2D>> _allPoints,
				GraphContainer _graph, PathConstructor _parent, double _connectionDistance) {
			blockingLines = _blockingLines;
			graph = _graph;
			parent = _parent;
			connectionDistance = _connectionDistance;
			
			graphSem = new Semaphore(1);
			edgeListSem = new Semaphore(1);
			remainingPointsSem = new Semaphore(1);
			rowAndNearestOptionsSem = new Semaphore(1);
			edgeLists = new LinkedList<LinkedList<PseudoEdge>>();
			remainingPoints = new LinkedList<LinkedList<Point2D>>(_allPoints);
			rowAndNearestOptions = new LinkedList<RowAndNearest>();
		}

		private class RowAndNearest{
			public LinkedList<Point2D> centerRow;
			public LinkedHashMap<PeekableIterator<Point2D>,LinkedList<Point2D>> relavantPoints;
			
			public RowAndNearest(LinkedList<Point2D> _nextRow, LinkedList<LinkedList<Point2D>> possibleNeighbors){
				centerRow = _nextRow;
				relavantPoints = getNearest(possibleNeighbors);
			}
			
			private LinkedHashMap<PeekableIterator<Point2D>,LinkedList<Point2D>> getNearest(LinkedList<LinkedList<Point2D>> possibleNeighbors){
				LinkedHashMap<PeekableIterator<Point2D>,LinkedList<Point2D>> relavantPoints = new LinkedHashMap<PeekableIterator<Point2D>,LinkedList<Point2D>>();
				for(LinkedList<Point2D> currentList : possibleNeighbors ){
					//Distance on Y axis is sufficient for including in additional comparisons, otherwise the entire row can be discarded
					if(!currentList.isEmpty()) {
						if(Math.abs(currentList.peekFirst().getY() - centerRow.peekFirst().getY()) < connectionDistance){
							relavantPoints.put(new PeekableIterator<Point2D>(currentList), new LinkedList<Point2D>());
						} else if (!relavantPoints.isEmpty()) {
							return relavantPoints;
						}
					}
				}
				return relavantPoints;
			}
		}
		
		private RowAndNearest getNextRow(){
			RowAndNearest nextRow = null;
			try {
				remainingPointsSem.acquire();
				while(!remainingPoints.isEmpty() && nextRow == null){
					LinkedList<Point2D> nextRowList = remainingPoints.removeFirst();
					if(nextRowList.isEmpty()) nextRow = null;
					else nextRow = new RowAndNearest(nextRowList,remainingPoints);
				}
				remainingPointsSem.release();
			} catch (InterruptedException e) {
				System.err.println("No one should be interruppting this thread. GraphAnnotator.getNextRow()");
				System.err.flush();
				System.exit(1);
			}
			return nextRow;
		}
				
		@Override
		public void run() {
			annotateGraph();
		}
		
		public void annotateGraph(){
			//1. Determine which set of points on which we are operating
			RowAndNearest currentRow = getNextRow();
			while(currentRow != null){		//Keep going until we cannot get another row
				
				//2. Determine All Points that could possibly interact with these points and get iterators for them
				
				//3. Determine a subset of all lines that could possibly interact with these point pairs
				//Create a rectangle representing range of reach. This could include some invalid lines, but is cheap and easy
				double xCoord = currentRow.centerRow.peekFirst().getX() - connectionDistance;
				double yCoord = currentRow.centerRow.peekFirst().getY() + connectionDistance;
				double width = (currentRow.centerRow.peekLast().getX() - currentRow.centerRow.peekFirst().getX()) + 2*connectionDistance;
				double height = 2*connectionDistance;
				Rectangle2D bounds = new Rectangle2D.Double(xCoord, yCoord, width, height);
				LinkedList<Line2D> blockingLinesSubset = new LinkedList<Line2D>();
				for(Line2D testLine : blockingLines) if(bounds.intersectsLine(testLine)) blockingLinesSubset.add(testLine);
				
				//List for adding edges
				LinkedList<PseudoEdge> pseudoEdges = new LinkedList<PseudoEdge>();
				
				//Main Processing
				for(Point2D currentPoint : currentRow.centerRow){
					Iterator<Entry<PeekableIterator<Point2D>, LinkedList<Point2D>>> iterListPairIterator = currentRow.relavantPoints.entrySet().iterator();
					while(iterListPairIterator.hasNext()){
						Entry<PeekableIterator<Point2D>, LinkedList<Point2D>> iterListPair = iterListPairIterator.next();
						PeekableIterator<Point2D> currentIter = iterListPair.getKey();
						LinkedList<Point2D> currentList = iterListPair.getValue();
						//4. Create a set of sublists for each row, using the iterators to add to them, and removing out of reach points
						//Add in range points
						while(currentIter.hasNext() && //Add points while they are available, are in range, or are to the left of the current point
								(currentIter.peek().distance(currentPoint) < connectionDistance || currentIter.peek().getX() < currentPoint.getX())){
							currentList.add(currentIter.next());
						}
						//Remove out of range points
						while(currentList.size() > 0 && //Remove points while there are points to remove, and they are our of range and to the left of the current point
								(currentList.getLast().distance(currentPoint) > connectionDistance && currentList.getLast().getX() < currentPoint.getX())){
							currentList.removeLast();
						}
						//5. For each point in the sublist, if a path to the current node is not blocked, add an edge to the current PseudoEdge List
						//At this point all edges in the current list are in range of the current point. Add connections if they are not blocked
						for(Point2D inRangePoint : currentList){
							Line2D line = new Line2D.Double(currentPoint, inRangePoint);
							if(!BasicShapeConstructor.intersectAnyLine(line,blockingLinesSubset)){
								pseudoEdges.add(new PseudoEdge(currentPoint, inRangePoint, parent.getWeight(currentPoint, inRangePoint)));
							}
						}
					}
				}
				
				//6. Add the local PseudoEdge List to the master PseudoEdge List
				addPseudoEdges(pseudoEdges);
				//7. If we can get a handle on the graph, that means that no one is adding edges to it, so start doing so.
				if(graphSem.tryAcquire()){
					updateGraph();
					graphSem.release();
				}
				//8. Determine if there are more rows to be processed
				currentRow = getNextRow();
			}	//end while(currentRow != null)
			return;
		}
		
		private void updateGraph() {
			LinkedList<PseudoEdge> currentPseudoEdges = getNextPseudoEdges();
			while(currentPseudoEdges != null){
				while(currentPseudoEdges.size() > 0){
					currentPseudoEdges.removeFirst().addToGraphContainer();
				}
				currentPseudoEdges = getNextPseudoEdges();
			}
		}

		private LinkedList<PseudoEdge> getNextPseudoEdges() {
			try {
				edgeListSem.acquire();
			} catch (InterruptedException e) {
				System.err.println("No one should be interruppting this thread. GraphAnnotator.addPseudoEdges()");
				System.err.flush();
				System.exit(1);
			}
			LinkedList<PseudoEdge> pseudoEdges = null;
			if(edgeLists.size() > 0) pseudoEdges = edgeLists.removeFirst();
			edgeListSem.release();
			return pseudoEdges;
		}

		private void addPseudoEdges(LinkedList<PseudoEdge> pseudoEdges) {
			try {
				edgeListSem.acquire();
			} catch (InterruptedException e) {
				System.err.println("No one should be interruppting this thread. GraphAnnotator.addPseudoEdges()");
				System.err.flush();
				System.exit(1);
			}
			edgeLists.add(pseudoEdges);
			edgeListSem.release();
		}
		
	}
	
	@Override
	public abstract Group blockingArea(Constructor c, Group constructed);

	@Override
	public abstract Group construct(Area routeableArea, Group currentMap);

	protected double getWeight(Point2D p0, Point2D p1){
		throw new UnsupportedOperationException("This method must be overridden by a superclass for use.");
	}
}
