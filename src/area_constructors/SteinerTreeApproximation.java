package area_constructors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.*;

public final class SteinerTreeApproximation {

	/*
	 * This code is strongly based on the code found here: https://github.com/ls1intum/jReto/blob/master/Source/src/de/tum/in/www1/jReto/routing/algorithm/MinimumSteinerTreeApproximation.java
	 * on date: May 13th, 2016
	 * This code has been extensively modified from the original, and preserves the original graph edges through the creation of
	 * the Steiner Tree.
	 */

	private static class MetricClosureEdge<T_Edge> extends DefaultWeightedEdge {
		private final LinkedList<T_Edge> path = new LinkedList<T_Edge>();
				
		public MetricClosureEdge(List<T_Edge> _path) {
			super();
			path.addAll(_path);
		}
		
		public void addEdge(T_Edge edge){
			path.add(edge);
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
	
	public static <T_Vertex, T_Edge> DirectedMultigraph<T_Vertex, T_Edge> approximateSteinerTree(DirectedWeightedPseudograph<T_Vertex, T_Edge> graph, Set<T_Vertex> vertices){
		return approximateSteinerTree(graph,vertices,null);
	}
	
	public static <T_Vertex, T_Edge> DirectedMultigraph<T_Vertex, T_Edge> approximateSteinerTree(DirectedWeightedPseudograph<T_Vertex, T_Edge> graph, Set<T_Vertex> vertices, T_Vertex startVertex){
		// 1. Construct the metric closure for the given set of vertices.
				DirectedWeightedPseudograph<T_Vertex, MetricClosureEdge<T_Edge>> metricClosure = createMetricClosure(graph, vertices);
				
				// 2. Compute the edges that compose an aborescence of the metric closure (aka. directed minimum spanning tree).
				Set<MetricClosureEdge<T_Edge>> arborescenceEdges = minimumArborescenceEdges(metricClosure, startVertex);

				// 3. Reduce the metric closure by removing all edges not computed before.
				metricClosure.removeAllEdges(inverseEdgeSet(metricClosure, arborescenceEdges));

				// 4. Reconstruct a graph containing all vertices of the original graph.
				DirectedMultigraph<T_Vertex, T_Edge> steinerTreeGraphApproximation = reconstructGraphFromMetricClosure(metricClosure);
				
				return steinerTreeGraphApproximation;
	}

	private static <T_Vertex, T_Edge> DirectedWeightedPseudograph<T_Vertex, MetricClosureEdge<T_Edge>> createMetricClosure(
			DirectedWeightedPseudograph<T_Vertex, T_Edge> graph, Set<T_Vertex> vertices) {
		DirectedWeightedPseudograph<T_Vertex, MetricClosureEdge<T_Edge>> metricClosure = new DirectedWeightedPseudograph<T_Vertex, MetricClosureEdge<T_Edge>>(MetricClosureEdge.class);
		
		for (T_Edge edge : graph.edgeSet()) {
			
		}
		return metricClosure;
	}
}
