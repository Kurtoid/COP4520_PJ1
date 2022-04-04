package tsp_demo2.algorithms.AntColony;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import tsp_demo2.PathResult;
import tsp_demo2.algorithms.GreedyNearest;
import tsp_demo2.graph.Graph;

public class SerialAntColony {
    // weight of pheromone importance
    static final double alpha = 1;
    // weight of distance importance
    static final double beta = 5;
    // pheromone evaporation rate
    static final double evaporation = 0.9;
    // pheromone deposit rate
    static final double Q = 500;
    // chance of choosing a random path instead of the best path
    static final double random_chance = 0.01;

    public static PathResult find(Graph g, int n_ants, int iterations) {
        g.make_undirected();
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph = g.to_JGraphT();
        int dimension = g.dimension;
        long start = System.currentTimeMillis();
        ArrayList<Integer> tour = new ArrayList<>();
        double shortest_tour_length = Double.MAX_VALUE;
        // create a second graph to store the pheromone scores
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> pheromone_graph = empty_jgraph_from_dimension(dimension);
        update_trails_from_greedy(g, dimension, pheromone_graph);
        // don't use g
        g = null;
        int steps_since_last_improvement = 0;

        for (int i = 0; i < iterations; i++) {
            // each ant performs a random tour
            ArrayList<ArrayList<Integer>> tours = new ArrayList<>();
            ArrayList<Double> tour_lengths = new ArrayList<>();
            for (int j = 0; j < n_ants; j++) {
                ArrayList<Integer> this_tour = new ArrayList<>();
                double this_tour_length = 0.0;
                // pick a random starting node (from 1 to dimension)
                int start_node = (int) (Math.random() * dimension) + 1;
                // start at that node
                int current_node = start_node;
                // while we haven't visited all nodes
                while (this_tour.size() < dimension) {
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
                        if (this_tour.contains(node)) {
                            continue;
                        }
                        double dist = graph.getEdgeWeight(edge);
                        DefaultWeightedEdge pheromone_edge = pheromone_graph.getEdge(current_node, node);
                        double ph = pheromone_graph.getEdgeWeight(pheromone_edge);
                        double score = edge_score(ph, dist);

                        candidate_edges.add(edge);
                        candidate_scores.add(score);
                    }
                    DefaultWeightedEdge chosen_edge = get_best_edge(candidate_edges, candidate_scores);

                    // get the vertex in edge that isn't current_node
                    int lowest_score_index = graph.getEdgeTarget(chosen_edge);
                    if (lowest_score_index == current_node) {
                        lowest_score_index = graph.getEdgeSource(chosen_edge);
                    }
                    // add the lowest score node to the tour
                    this_tour.add(lowest_score_index);
                    // update the current node
                    current_node = lowest_score_index;

                    // update distance traveled
                    double chosen_dist = graph.getEdgeWeight(chosen_edge);
                    this_tour_length += chosen_dist;
                }
                tours.add(this_tour);
                tour_lengths.add(this_tour_length);
            }
            for (int j = 0; j < n_ants; j++) {
                ArrayList<Integer> this_tour = tours.get(j);
                double this_tour_length = tour_lengths.get(j);
                for (DefaultWeightedEdge e : pheromone_graph.edgeSet()) {
                    double ph = pheromone_graph.getEdgeWeight(e);
                    ph *= evaporation;
                    pheromone_graph.setEdgeWeight(e, ph);
                }
                // deposit along the path taken
                double contrib = Q / this_tour_length;
                contrib /= n_ants;
                for (int k = 0; k < this_tour.size() - 1; k++) {
                    int node1 = this_tour.get(k);
                    int node2 = this_tour.get(k + 1);
                    DefaultWeightedEdge edge = pheromone_graph.getEdge(node1, node2);
                    double ph = pheromone_graph.getEdgeWeight(edge);
                    ph += contrib;
                    pheromone_graph.setEdgeWeight(edge, ph);
                }

                // last node to start node
                int node1 = this_tour.get(this_tour.size() - 1);
                int node2 = this_tour.get(0);
                DefaultWeightedEdge edge = pheromone_graph.getEdge(node1, node2);
                double ph = pheromone_graph.getEdgeWeight(edge);
                ph += contrib;
                pheromone_graph.setEdgeWeight(edge, ph);

                // is this tour shorter than the shortest tour?
                if (this_tour_length < shortest_tour_length) {
                    shortest_tour_length = this_tour_length;
                    tour = this_tour;
                    steps_since_last_improvement = 0;
                }
            }
            steps_since_last_improvement++;
            // System.out.println("Iteration " + i + ": " + shortest_tour_length);
            if (steps_since_last_improvement > 5) {
                break;
            }
        }
        long elapsed = System.currentTimeMillis() - start;
        PathResult result = new PathResult(tour, elapsed);
        return result;
    }

    public static SimpleWeightedGraph<Integer, DefaultWeightedEdge> empty_jgraph_from_dimension(int dimension) {
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> pheromone_graph = new SimpleWeightedGraph<>(
                DefaultWeightedEdge.class);
        for (int i = 0; i < dimension; i++) {
            pheromone_graph.addVertex(i + 1);
        }
        // fully connected graph - only need to add edges in one direction since it's
        // undirected
        for (int i = 0; i < dimension; i++) {
            for (int j = i + 1; j < dimension; j++) {
                DefaultWeightedEdge e = pheromone_graph.addEdge(i + 1, j + 1);
                pheromone_graph.setEdgeWeight(e, Q);
            }
        }
        return pheromone_graph;
    }

    public static void update_trails_from_greedy(Graph g, int dimension,
            SimpleWeightedGraph<Integer, DefaultWeightedEdge> pheromone_graph) {
        {
            // initialize the pheromone score with a nearest neighbor tour
            ArrayList<Integer> nearest_neighbors = GreedyNearest.find(g).path;
            double nearest_tour_length = g.get_tour_length(nearest_neighbors);
            double contrib = Q / nearest_tour_length;
            for (int i = 0; i < dimension; i++) {
                int node = nearest_neighbors.get(i);
                int next_node = nearest_neighbors.get((i + 1) % dimension);
                DefaultWeightedEdge e = pheromone_graph.getEdge(node, next_node);
                pheromone_graph.setEdgeWeight(e, pheromone_graph.getEdgeWeight(e) + contrib);
            }
        }
    }

    public static DefaultWeightedEdge get_best_edge(List<DefaultWeightedEdge> candidate_edges,
            List<Double> candidate_scores) {
        Random r = new Random();
        if (r.nextDouble() < random_chance) {
            // use a random edge
            int index = (int) (Math.random() * candidate_edges.size());
            return candidate_edges.get(index);
        }
        // create an array of probabilities from calculated scores
        double[] probabilities = new double[candidate_scores.size()];
        double sum = 0.0;
        for (int i = 0; i < candidate_scores.size(); i++) {
            double score = candidate_scores.get(i);
            sum += score;
        }
        for (int i = 0; i < candidate_scores.size(); i++) {
            double score = candidate_scores.get(i);
            probabilities[i] = score / sum;
        }
        // // make sure probabilities add up to 1
        // double sum_probabilities = 0.0;
        // for (int i = 0; i < probabilities.length; i++) {
        // sum_probabilities += probabilities[i];
        // }
        // if (Math.abs(sum_probabilities - 1.0) > 0.00001) {
        // System.out.println("probabilities don't add up to 1");
        // System.out.println("sum: " + sum_probabilities);
        // System.out.println("probabilities: " + Arrays.toString(probabilities));
        // }

        // pick a random number between 0 and 1
        double rand = Math.random();
        // find the index of the first element in probabilities that is greater than
        // rand
        sum = 0;
        for (int i = 0; i < probabilities.length; i++) {
            sum += probabilities[i];
            if (sum > rand) {
                return candidate_edges.get(i);
            }
        }
        return candidate_edges.get(candidate_edges.size() - 1);

        // // normalize array from 0 to 1
        // double max = candidate_scores.get(0);
        // double min = candidate_scores.get(0);
        // for (double d : candidate_scores) {
        // if (d > max) {
        // max = d;
        // }
        // if (d < min) {
        // min = d;
        // }
        // }
        // for (int k = 0; k < candidate_scores.size(); k++) {
        // double d = candidate_scores.get(k);
        // candidate_scores.set(k, 100 * ((d - min) / (max - min)));
        // }
        // // normalize the scores by softmax (logistic)
        // double sum = 0.0;
        // for (double score : candidate_scores) {
        // sum += Math.exp(score);
        // }
        // for (int k = 0; k < candidate_scores.size(); k++) {
        // double score = candidate_scores.get(k);
        // double new_score = Math.exp(score) / sum;
        // // System.out.println("score: " + score + " new_score: " + new_score);
        // candidate_scores.set(k, new_score);
        // }
        // Map<DefaultWeightedEdge, Double> normalized_scores = new HashMap<>();
        // for (int k = 0; k < candidate_scores.size(); k++) {
        // double score = candidate_scores.get(k);
        // if (Double.isNaN(score)) {
        // System.out.println("score is NaN");
        // continue;
        // }
        // normalized_scores.put(candidate_edges.get(k), score);
        // }
        // DiscreteProbabilityCollectionSampler<DefaultWeightedEdge> sampler = new
        // DiscreteProbabilityCollectionSampler<DefaultWeightedEdge>(
        // RandomSource.MT.create(), normalized_scores);
        // DefaultWeightedEdge chosen_edge = sampler.sample();
        // return chosen_edge;
    }

    static double dist_weight = 0.5;

    // score: higher is better
    static double edge_score(double ph, double dist) {
        return Math.pow(ph, alpha) * Math.pow(1 / dist, beta);
    }
}
