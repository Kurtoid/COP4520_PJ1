package tsp_demo2.algorithms.helpers;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;

import org.jgrapht.alg.interfaces.MatchingAlgorithm.Matching;
import org.jgrapht.alg.matching.blossom.v5.KolmogorovWeightedPerfectMatching;
import org.jgrapht.alg.matching.blossom.v5.ObjectiveSense;

import tsp_demo2.graph.Edge;
import tsp_demo2.graph.Graph;
import tsp_demo2.graph.Node;

public class Blossom {
    public static ArrayList<Edge> find(Graph g) {
        // convert graph to a JGraphT graph
        // run blossom on it
        // convert it back to our graph representation
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> jgrapht_graph = g.to_JGraphT();
        // run blossom
        KolmogorovWeightedPerfectMatching<Integer, DefaultWeightedEdge> blossom = new KolmogorovWeightedPerfectMatching<>(
                jgrapht_graph,
                ObjectiveSense.MINIMIZE);
        Matching<Integer, DefaultWeightedEdge> matching = blossom.getMatching();
        // convert back to our graph representation
        ArrayList<Edge> edges = new ArrayList<>();
        for (DefaultWeightedEdge e : matching.getEdges()) {
            edges.add(new Edge(
                    jgrapht_graph.getEdgeSource(e),
                    jgrapht_graph.getEdgeTarget(e),
                    jgrapht_graph.getEdgeWeight(e)));
        }

        return edges;
    }
}
