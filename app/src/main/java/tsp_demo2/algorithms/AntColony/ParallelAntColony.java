package tsp_demo2.algorithms.AntColony;

import java.util.ArrayList;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import tsp_demo2.PathResult;
import tsp_demo2.graph.Graph;

public class ParallelAntColony {
    public static PathResult find(Graph g, int num_ants, int num_iterations) {
        g.make_undirected();
        int dimension = g.dimension;
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph = g.to_JGraphT();
        g = null;
        long start = System.currentTimeMillis();

        ArrayList<Double> ph_score = new ArrayList<>();
        for (int i = 0; i < dimension; i++) {
            ph_score.add(0.0);
        }
        ArrayList<Integer> best_tour = null;
        double best_tour_length = Double.MAX_VALUE;
        for (int i = 0; i < num_iterations; i++) {
            ArrayList<AntThread> ants = new ArrayList<>();
            for (int j = 0; j < num_ants; j++) {
                AntThread a = new AntThread();
                a.graph = (SimpleWeightedGraph<Integer, DefaultWeightedEdge>) graph;
                a.ph_score = (ArrayList<Double>) ph_score.clone();
                ants.add(a);
            }
            // run all ants, and wait for them to finish
            for (AntThread a : ants) {
                a.start();
            }
            for (AntThread a : ants) {
                try {
                    a.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // get the best run
            for (AntThread a : ants) {
                if (a.tour_length < best_tour_length) {
                    best_tour_length = a.tour_length;
                    best_tour = a.tour;
                }
            }

            // update the pheromone score
            for (int j = 0; j < dimension; j++) {
                int node = best_tour.get(j);
                double ph = ph_score.get(node - 1);
                double new_ph = ph + 1.0 / best_tour_length;
                ph_score.set(node - 1, new_ph);
            }
            // System.out.println("Iteration " + i + ": " + best_tour_length);
        }
        long elapsed = System.currentTimeMillis() - start;
        return new PathResult(best_tour, elapsed);

    }

    /**
     * represents a single ant. each run causes the ant to perform a single tour
     */
    static class AntThread extends Thread {
        public SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph;
        public ArrayList<Double> ph_score;
        public ArrayList<Integer> tour;

        public double tour_length;

        public void run() {
            int current_node = (int) (Math.random() * graph.vertexSet().size()) + 1;
            tour = new ArrayList<>();
            tour.add(current_node);
            int dimension = graph.vertexSet().size();
            while (tour.size() < dimension) {
                Set<DefaultWeightedEdge> edges = graph.outgoingEdgesOf(current_node);
                double lowest_score = Double.MAX_VALUE;
                double chosen_dist = 0.0;
                int lowest_score_index = -1;
                for (DefaultWeightedEdge edge : edges) {
                    int node = graph.getEdgeTarget(edge);
                    if (node == current_node) {
                        node = graph.getEdgeSource(edge);
                    }
                    if (tour.contains(node)) {
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
                tour.add(lowest_score_index);
                // update the current node
                current_node = lowest_score_index;
                // update distance traveled
                tour_length += chosen_dist;
            }
            // add the edge from the last node to the first node
            tour_length += graph.getEdgeWeight(graph.getEdge(tour.get(tour.size() - 1), tour.get(0)));

        }

        static double dist_weight = 0.5;

        static double edge_score(double ph, double dist) {
            return -ph * (1 - dist_weight) + dist * dist_weight;
        }
    }
}
