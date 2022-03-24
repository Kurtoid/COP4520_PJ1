package tsp_demo2.algorithms.AntColony;

import java.util.ArrayList;
import java.util.Set;

import javax.swing.DropMode;

import org.checkerframework.checker.units.qual.A;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import tsp_demo2.graph.Graph;

public class SerialAntColony {

    public static ArrayList<Integer> find(Graph g, int n_ants, int iterations) {
        g.make_undirected();
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph = g.to_JGraphT();
        int dimension = g.dimension;
        // don't use g
        g = null;
        ArrayList<Integer> tour = new ArrayList<>();
        double shortest_tour_length = Double.MAX_VALUE;
        ArrayList<Double> ph_score = new ArrayList<>();
        for (int i = 0; i < dimension; i++) {
            ph_score.add(0.0);
        }
        for (int i = 0; i < iterations; i++) {
            // each ant performs a random tour
            for (int j = 0; j < n_ants; j++) {
                ArrayList<Integer> this_tour = new ArrayList<>();
                double distance_traveled = 0.0;
                // pick a random starting node (from 1 to dimension)
                int start_node = (int) (Math.random() * dimension) + 1;
                // start at that node
                int current_node = start_node;
                // while we haven't visited all nodes
                while (this_tour.size() < dimension) {
                    // get the edges from the current node
                    // warning: edges may be in opposite order!
                    Set<DefaultWeightedEdge> edges = graph.outgoingEdgesOf(current_node);
                    // get the edge with the lowest score
                    double lowest_score = Double.MAX_VALUE;
                    double chosen_dist = 0.0;
                    int lowest_score_index = -1;
                    for (DefaultWeightedEdge edge : edges) {
                        int node = graph.getEdgeTarget(edge);
                        if (node == current_node) {
                            node = graph.getEdgeSource(edge);
                        }
                        if (this_tour.contains(node)) {
                            continue;
                        }
                        double dist = graph.getEdgeWeight(edge);
                        double ph = ph_score.get(node - 1);
                        double score = edge_score(ph, dist);

                        if (score < lowest_score) {
                            lowest_score = score;
                            lowest_score_index = node;
                            chosen_dist = dist;
                        }
                    }
                    if (lowest_score_index == -1) {
                        // breakpoint
                        continue;
                    }
                    // add the lowest score node to the tour
                    this_tour.add(lowest_score_index);
                    // update the current node
                    current_node = lowest_score_index;
                    // update distance traveled
                    distance_traveled += chosen_dist;
                }
                // is this tour shorter than the shortest tour?
                if (distance_traveled < shortest_tour_length) {
                    shortest_tour_length = distance_traveled;
                    tour = this_tour;
                }
            }
            // distribute ph across the tour
            for (int j = 0; j < tour.size(); j++) {
                int node = tour.get(j);
                double ph = ph_score.get(node - 1);
                double new_ph = ph + 1.0 / shortest_tour_length;
                ph_score.set(node - 1, new_ph);
            }
            System.out.println("iteration " + i + ": " + shortest_tour_length);
        }
        return tour;
    }

    static double dist_weight = 0.5;

    static double edge_score(double ph, double dist) {
        return -ph * (1 - dist_weight) + dist * dist_weight;
    }
}
