package tsp_demo2.algorithms.AntColony;

import java.util.ArrayList;
import java.util.List;
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
        long start = System.currentTimeMillis();

        SimpleWeightedGraph<Integer, DefaultWeightedEdge> pheromone_graph = SerialAntColony
                .pheromone_jgraph(dimension);
        SerialAntColony.update_trails_from_greedy(g, dimension, pheromone_graph);
        g = null;
        ArrayList<Integer> best_tour = null;
        double best_tour_length = Double.MAX_VALUE;
        int steps_since_last_improvement = 0;
        for (int i = 0; i < num_iterations; i++) {
            ArrayList<AntThread> ants = new ArrayList<>();
            for (int j = 0; j < num_ants; j++) {
                AntThread a = new AntThread();
                a.graph = (SimpleWeightedGraph<Integer, DefaultWeightedEdge>) graph;
                a.ph_score = pheromone_graph;
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
            // decay
            for (DefaultWeightedEdge e : pheromone_graph.edgeSet()) {
                pheromone_graph.setEdgeWeight(e, pheromone_graph.getEdgeWeight(e) * SerialAntColony.evaporation);
            }
            for (int j = 0; j < num_ants; j++) {
                ArrayList<Integer> tour = ants.get(j).tour;
                double tour_length = ants.get(j).tour_length;
                double contrib = SerialAntColony.Q / tour_length;
                contrib /= num_ants;
                for (int k = 0; k < tour.size(); k++) {
                    int from = tour.get(k);
                    int to = tour.get((k + 1) % tour.size());
                    DefaultWeightedEdge e = graph.getEdge(from, to);
                    pheromone_graph.setEdgeWeight(e, pheromone_graph.getEdgeWeight(e) + contrib);
                }
                // is this the best tour?
                if (tour_length < best_tour_length) {
                    best_tour_length = tour_length;
                    best_tour = tour;
                    steps_since_last_improvement = 0;
                }
            }
            steps_since_last_improvement++;
            if (steps_since_last_improvement > 5) {
                break;
            }
        }
        long elapsed = System.currentTimeMillis() - start;
        return new PathResult(best_tour, elapsed);

    }

    /**
     * represents a single ant. each run causes the ant to perform a single tour
     */
    static class AntThread extends Thread {
        public SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph;
        public SimpleWeightedGraph<Integer, DefaultWeightedEdge> ph_score;
        public ArrayList<Integer> tour;

        public double tour_length;

        public void run() {
            int current_node = (int) (Math.random() * graph.vertexSet().size()) + 1;
            tour = new ArrayList<>();
            tour.add(current_node);
            int dimension = graph.vertexSet().size();
            while (tour.size() < dimension) {
                // get the edges from the current node
                // warning: edges may be in opposite order!
                Set<DefaultWeightedEdge> edges = graph.outgoingEdgesOf(current_node);
                // if there is only one edge, we're done

                // get the edge with the lowest score
                List<DefaultWeightedEdge> candidate_edges = new ArrayList<>();
                List<Double> candidate_scores = new ArrayList<>();
                for (DefaultWeightedEdge edge : edges) {
                    int node = graph.getEdgeTarget(edge);
                    if (node == current_node) {
                        node = graph.getEdgeSource(edge);
                    }
                    if (tour.contains(node)) {
                        continue;
                    }
                    double dist = graph.getEdgeWeight(edge);
                    DefaultWeightedEdge pheromone_edge = ph_score.getEdge(current_node, node);
                    double ph = ph_score.getEdgeWeight(pheromone_edge);
                    double score = SerialAntColony.edge_score(ph, dist);

                    candidate_edges.add(edge);
                    candidate_scores.add(score);
                }
                DefaultWeightedEdge chosen_edge = SerialAntColony.get_best_edge(candidate_edges, candidate_scores);

                // get the vertex in edge that isn't current_node
                int lowest_score_index = graph.getEdgeTarget(chosen_edge);
                if (lowest_score_index == current_node) {
                    lowest_score_index = graph.getEdgeSource(chosen_edge);
                }
                // add the lowest score node to the tour
                tour.add(lowest_score_index);
                // update the current node
                current_node = lowest_score_index;

                // update distance traveled
                double chosen_dist = graph.getEdgeWeight(chosen_edge);
                tour_length += chosen_dist;
            }
            // add the edge from the last node to the first node
            tour_length += graph.getEdgeWeight(graph.getEdge(tour.get(tour.size() - 1), tour.get(0)));

        }

    }
}
