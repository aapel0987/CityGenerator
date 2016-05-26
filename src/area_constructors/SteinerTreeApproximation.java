package area_constructors;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.ClosestFirstIterator;

public final class SteinerTreeApproximation {

	/*
	 * This code is strongly based on the code found here: https://github.com/ls1intum/jReto/blob/master/Source/src/de/tum/in/www1/jReto/routing/algorithm/MinimumSteinerTreeApproximation.java
	 * on date: May 13th, 2016
	 * This code has been extensively modified from the original, and preserves the original graph edges through the creation of
	 * the Steiner Tree. Function names have been preserved. In addition the return type was changed to a jgrapht native type.
	 */
	
	private static class MetricClosureEdge<T_Edge> extends DefaultWeightedEdge {

		private final LinkedList<T_Edge> path = new LinkedList<T_Edge>();
				
		public MetricClosureEdge(List<T_Edge> _path) {
			super();
			path.addAll(_path);
		}
		
		public MetricClosureEdge(T_Edge _path) {
			super();
			addEdge(_path);
		}
		
		public MetricClosureEdge(MetricClosureEdge<T_Edge> metricEdge) {
			this(metricEdge.path);
		}
		
		public void addEdge(T_Edge edge){
			path.add(edge);
		}

		public void addEdges(MetricClosureEdge<T_Edge> metricEdge){
			addEdges(metricEdge.path);
		}
		
		public void addEdges(Collection<T_Edge> edges){
			path.addAll(edges);
		}
		
		public LinkedList<T_Edge> getEdges(){
			return new LinkedList<T_Edge>(path);
		}
		
		public String toString() {
			return this.path.toString() + " // "+this.getWeight();
		}
	}
	
	public static <T_Vertex, T_Edge> DirectedWeightedMultigraph<T_Vertex, T_Edge> approximateSteinerTree(DirectedWeightedPseudograph<T_Vertex, T_Edge> graph, Set<T_Vertex> vertices){
		return approximateSteinerTree(graph,vertices,null);
	}
	
	public static <T_Vertex, T_Edge> DirectedWeightedMultigraph<T_Vertex, T_Edge> approximateSteinerTree(DirectedWeightedPseudograph<T_Vertex, T_Edge> graph, Collection<T_Vertex> vertices, T_Vertex startVertex){
		// 1. Construct the metric closure for the given set of vertices.
		DirectedWeightedPseudograph<T_Vertex, MetricClosureEdge<T_Edge>> metricClosure = createMetricClosure(graph, vertices);
		
		// 2. Compute the edges that compose an aborescence of the metric closure (aka. directed minimum spanning tree).
		DirectedMultigraph<T_Vertex, MetricClosureEdge<T_Edge>> arborescenceEdges = minimumArborescenceEdges(metricClosure, startVertex);

		// 3. Now construct the steiner tree from the original graph and the selected edges
		DirectedWeightedMultigraph<T_Vertex, T_Edge> steinerTreeGraphApproximation = constructSteinerTree(graph,arborescenceEdges);
		
		return steinerTreeGraphApproximation;
	}

	private static <T_Vertex, T_Edge> DirectedWeightedMultigraph<T_Vertex, T_Edge> constructSteinerTree(
			DirectedWeightedPseudograph<T_Vertex, T_Edge> graph,
			DirectedMultigraph<T_Vertex, MetricClosureEdge<T_Edge>> arborescenceEdges) {
		
		DirectedWeightedMultigraph<T_Vertex, T_Edge> steinerTreeGraphApproximation = new DirectedWeightedMultigraph<T_Vertex, T_Edge>((Class) null);
		
		for(MetricClosureEdge<T_Edge> metricEdge : arborescenceEdges.edgeSet()){
			for(T_Edge edge : metricEdge.getEdges()){
				T_Vertex sourceVertex = graph.getEdgeSource(edge);
				T_Vertex targetVertex = graph.getEdgeTarget(edge);
				double edgeWeight = graph.getEdgeWeight(edge);
				steinerTreeGraphApproximation.addVertex(sourceVertex);
				steinerTreeGraphApproximation.addVertex(targetVertex);
				steinerTreeGraphApproximation.addEdge(sourceVertex, targetVertex, edge);
				steinerTreeGraphApproximation.setEdgeWeight(edge, edgeWeight);
			}
		}
		return steinerTreeGraphApproximation;
	}

	private static <T_Vertex, T_Edge> DirectedMultigraph<T_Vertex, MetricClosureEdge<T_Edge>> minimumArborescenceEdges(
			DirectedWeightedPseudograph<T_Vertex, MetricClosureEdge<T_Edge>> metricClosure, T_Vertex startVertex) {
		if(metricClosure.vertexSet().isEmpty()) throw new IllegalArgumentException("metricClosure must contain at least 2 verticies!");
		System.out.println("metricClosure verticies:");
		for(T_Vertex vertex : metricClosure.vertexSet()) System.out.println(vertex.toString());
		System.out.flush();
		if(startVertex != null) return minimumSpanningForest(metricClosure,startVertex);
		//Search for the lowest weight tree
		DirectedMultigraph<T_Vertex, MetricClosureEdge<T_Edge>> minSpanningTree = null;
		double minSpanningTreeWeight = Double.MAX_VALUE;
		for(T_Vertex vertex : metricClosure.vertexSet()){
			DirectedMultigraph<T_Vertex, MetricClosureEdge<T_Edge>> spanningTree = minimumSpanningForest(metricClosure,vertex);
			double spanningTreeWeight = getWeightOfGraph(spanningTree);
			if(spanningTreeWeight<minSpanningTreeWeight) minSpanningTree = spanningTree;
		}
		
		return minSpanningTree;
	}

	private static <T_Vertex, T_Edge> DirectedMultigraph<T_Vertex, T_Edge> minimumSpanningForest(
			DirectedWeightedPseudograph<T_Vertex, T_Edge> graph, T_Vertex startVertex) {
		// Using Prim's algorithm, search the entire graph to use the results for the minimum spanning forest
		ClosestFirstIterator<T_Vertex, T_Edge> iterator = new ClosestFirstIterator<T_Vertex, T_Edge>(graph, startVertex);
		iterator.setCrossComponentTraversal(false);
		//Search the graph to completion
		while (iterator.hasNext()) iterator.next();
		//The null constructor should presumably not be used
		DirectedMultigraph<T_Vertex, T_Edge> minSpanningTree = new DirectedMultigraph<T_Vertex, T_Edge>((Class) null);
		
		for (T_Vertex targetVertex : graph.vertexSet()) {
			T_Edge edge = iterator.getSpanningTreeEdge(targetVertex);
			//If there is a path between the start and the destination
			if (edge != null){
				T_Vertex sourceVertex = graph.getEdgeSource(edge);
				minSpanningTree.addVertex(sourceVertex);
				minSpanningTree.addVertex(targetVertex);
				minSpanningTree.addEdge(sourceVertex, targetVertex, edge);
			}
		}
		
		return minSpanningTree;
	}
	
	private static <T_Vertex, T_Edge> double getWeightOfGraph(AbstractBaseGraph<T_Vertex, T_Edge> graph){
		double weight = 0;
		for(T_Edge edge : graph.edgeSet()) weight += graph.getEdgeWeight(edge);
		return weight;
	}
	
	private static <T_Vertex, T_Edge> DirectedWeightedPseudograph<T_Vertex, MetricClosureEdge<T_Edge>> createMetricClosure(
			DirectedWeightedPseudograph<T_Vertex, T_Edge> graph, Collection<T_Vertex> vertices) {
		//Create the graph to return
		DirectedWeightedPseudograph<T_Vertex, MetricClosureEdge<T_Edge>> metricClosure = new DirectedWeightedPseudograph<T_Vertex, MetricClosureEdge<T_Edge>>(new EdgeFactory<T_Vertex, MetricClosureEdge<T_Edge>>() {
			@Override
			public MetricClosureEdge<T_Edge> createEdge(T_Vertex sourceVertex, T_Vertex targetVertex) {
				throw new UnsupportedOperationException("All graphs of type MetricClosureEdge should have their edges created externally.");
			}
		});
		
		for (T_Edge edge : graph.edgeSet()) {
			T_Vertex start = graph.getEdgeSource(edge);
			T_Vertex end = graph.getEdgeTarget(edge); 
			//If a vertex has an edge, add it to the graph
			metricClosure.addVertex(start);
			metricClosure.addVertex(end);
			MetricClosureEdge<T_Edge> metricEdge = new MetricClosureEdge<T_Edge>(edge);
			metricClosure.addEdge(start, end, metricEdge);
			metricClosure.setEdgeWeight(metricEdge, graph.getEdgeWeight(edge));
		}
		
		//At this point all of the vertices with edges and edges from the original graph are now in the
		//metricClosure graph. Create a list of the vertices to fold into the closure and start compressing them.
		HashSet<T_Vertex> verticesToRemove = new HashSet<T_Vertex>(metricClosure.vertexSet());
		verticesToRemove.removeAll(vertices);
		
		System.out.println("Metric Closure:");
		for(T_Vertex vertex : metricClosure.vertexSet()) System.out.println(vertex.toString());
		System.out.println("Keeping Verticies:");
		for(T_Vertex vertex : vertices) System.out.println(vertex.toString());
		//Now integrate these into the closure
		for(T_Vertex closingVertex : verticesToRemove){
			System.out.println("Removing: " + closingVertex.toString());
			closeVertex(metricClosure,closingVertex);
		}
		
		
		return metricClosure;
	}

	private static <T_Vertex, T_Edge> void closeVertex(DirectedWeightedPseudograph<T_Vertex, MetricClosureEdge<T_Edge>> metricClosure,
			T_Vertex closingVertex) {
		
		LinkedList<MetricClosureEdge<T_Edge>> incomingEdges = new LinkedList<>(metricClosure.incomingEdgesOf(closingVertex));
		LinkedList<MetricClosureEdge<T_Edge>> outgoingEdges = new LinkedList<>(metricClosure.outgoingEdgesOf(closingVertex));
		System.out.println("incomingEdges: " + incomingEdges.size() + "\toutgoingEdges: " + outgoingEdges.size());
		//To fold a vertex into the closure, create an edge from all its sources to all of its targets
		for (MetricClosureEdge<T_Edge> incomingEdge : incomingEdges) {
			for (MetricClosureEdge<T_Edge> outgoingEdge : outgoingEdges) {
				MetricClosureEdge<T_Edge> newEdge = new MetricClosureEdge<T_Edge>(incomingEdge);
				newEdge.addEdges(outgoingEdge);

				metricClosure.addEdge(metricClosure.getEdgeSource(incomingEdge), metricClosure.getEdgeTarget(outgoingEdge), newEdge);
				metricClosure.setEdgeWeight(newEdge, metricClosure.getEdgeWeight(incomingEdge) + metricClosure.getEdgeWeight(outgoingEdge));
			}
		}
		//Now that all edges have been closed, remove the vertex that was just bypassed
		metricClosure.removeVertex(closingVertex);
	}
	
}
