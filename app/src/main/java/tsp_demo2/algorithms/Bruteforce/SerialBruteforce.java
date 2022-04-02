package tsp_demo2.algorithms.Bruteforce;

import java.util.ArrayList;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import tsp_demo2.PathResult;
import tsp_demo2.graph.Graph;

public class SerialBruteforce {
    public static PathResult find(Graph g) {
        g.make_undirected();
        int dimension = g.dimension;
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph = g.to_JGraphT();
        g = null;
        long start = System.currentTimeMillis();
        ArrayList<Integer> current_permutation = new ArrayList<>();
        ArrayList<Integer> best_tour = null;
        double best_tour_length = Double.MAX_VALUE;
        for (int i = 0; i < dimension; i++) {
            current_permutation.add(i + 1);
        }
        while (true) {
            double tour_length = 0.0;
            for (int i = 0; i < dimension - 1; i++) {
                int node1 = current_permutation.get(i);
                int node2 = current_permutation.get(i + 1);
                DefaultWeightedEdge edge = graph.getEdge(node1, node2);
                tour_length += graph.getEdgeWeight(edge);
            }
            int node1 = current_permutation.get(dimension - 1);
            int node2 = current_permutation.get(0);
            DefaultWeightedEdge edge = graph.getEdge(node1, node2);
            tour_length += graph.getEdgeWeight(edge);
            if (tour_length < best_tour_length) {
                best_tour_length = tour_length;
                best_tour = (ArrayList<Integer>) current_permutation.clone();
                // add the return home
                best_tour.add(best_tour.get(0));
            }
            current_permutation = nextPermutation(current_permutation);
            if (current_permutation == null) {
                break;
            }
        }
        long elapsed = System.currentTimeMillis() - start;

        return new PathResult(best_tour, elapsed);
    }

    public static ArrayList<Integer> nextPermutation(ArrayList<Integer> permutation) {
        // find the largest index k such that a[k] < a[k + 1]
        int k = -1;
        for (int i = 0; i < permutation.size() - 1; i++) {
            if (permutation.get(i) < permutation.get(i + 1)) {
                k = i;
            }
        }
        // if no such index exists, the permutation is the last permutation
        if (k == -1) {
            return null;
        }
        // find the largest index l such that a[k] < a[l]
        int l = -1;
        for (int i = 0; i < permutation.size(); i++) {
            if (permutation.get(k) < permutation.get(i)) {
                l = i;
            }
        }
        // swap a[k] and a[l]
        int temp = permutation.get(k);
        permutation.set(k, permutation.get(l));
        permutation.set(l, temp);
        // reverse the sequence from a[k + 1] up to and including the final element
        for (int i = k + 1, j = permutation.size() - 1; i < j; i++, j--) {
            temp = permutation.get(i);
            permutation.set(i, permutation.get(j));
            permutation.set(j, temp);
        }
        return permutation;
    }
}
