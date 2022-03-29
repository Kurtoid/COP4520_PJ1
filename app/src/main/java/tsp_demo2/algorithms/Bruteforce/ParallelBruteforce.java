package tsp_demo2.algorithms.Bruteforce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import tsp_demo2.graph.Graph;

public class ParallelBruteforce {
    static public class CachedFactorial {
        final int CACHE_SIZE = 100_000;
        TreeMap<Integer, Integer> cache = new TreeMap<>();

        int factorial(int n) {
            if (n < 0) {
                throw new IllegalArgumentException("n must be non-negative");
            }
            if (n <= 1) {
                return 1;
            }
            if (cache.containsKey(n)) {
                return cache.get(n);
            }
            int result = factorial(n - 1) * n;
            if (cache.size() >= CACHE_SIZE) {
                cache.remove(cache.firstKey());
            }
            cache.put(n, result);
            return result;
        }

        static CachedFactorial instance = new CachedFactorial();

    }

    static class BruteforceThread extends Thread {
        private SimpleWeightedGraph<Integer, DefaultWeightedEdge> g;
        int graph_size;
        ArrayList<Integer> best_tour;
        int max_permutations;
        int start_permutation;
        int min_path_length = Integer.MAX_VALUE;

        @Override
        public void run() {
            try {
                int permutations_done = 0;
                // create a seperate factorial cache for each thread
                CachedFactorial f = new CachedFactorial();
                ArrayList<Integer> current_permutation;
                if (start_permutation != 0) {
                    current_permutation = getNthPermutation(start_permutation, graph_size, f);
                } else {
                    current_permutation = new ArrayList<>();
                    for (int i = 0; i < graph_size; i++) {
                        current_permutation.add(i + 1);
                    }
                }
                // System.out.println("Thread " + this.getId() + " started with permutation " +
                // current_permutation);
                // System.out.println("Will perform " + max_permutations + " permutations");

                while (permutations_done < max_permutations) {
                    current_permutation = SerialBruteforce.nextPermutation(current_permutation);
                    if (current_permutation == null) {
                        // System.out.println(
                        // "Thread " + this.getId() + " finished with permutation " +
                        // current_permutation);
                        break;
                    }
                    permutations_done++;
                    double tour_length = 0.0;
                    for (int i = 0; i < graph_size - 1; i++) {
                        int node1 = current_permutation.get(i);
                        int node2 = current_permutation.get(i + 1);
                        DefaultWeightedEdge edge = g.getEdge(node1, node2);
                        tour_length += g.getEdgeWeight(edge);
                    }
                    if (tour_length < min_path_length) {
                        min_path_length = (int) tour_length;
                        best_tour = (ArrayList<Integer>) current_permutation.clone();
                    }
                }
                // System.out.println("Thread " + this.getId() + " finished with " +
                // permutations_done + " permutations");
            } catch (Exception e) {
                System.out.println("Thread " + this.getId() + " failed with exception " + e);
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<Integer> find(Graph g) {
        g.make_undirected();
        int dimension = g.dimension;
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph = g.to_JGraphT();
        g = null;

        int num_threads = 4;
        int num_permutations = CachedFactorial.instance.factorial(dimension);
        // split the permutations into num_threads parts
        int part_size = num_permutations / num_threads;
        ArrayList<BruteforceThread> threads = new ArrayList<>();
        for (int i = 0; i < num_threads; i++) {
            int start_index = i * part_size;
            int end_index = (i + 1) * part_size;
            if (i == num_threads - 1) {
                end_index = num_permutations;
            }
            BruteforceThread thread = new BruteforceThread();
            thread.g = (SimpleWeightedGraph<Integer, DefaultWeightedEdge>) graph.clone();
            thread.graph_size = dimension;
            thread.max_permutations = end_index - start_index;
            thread.start_permutation = start_index;
            threads.add(thread);
            // System.out.println("Thread " + i + ": " + start_index + " - " + end_index);

        }
        for (BruteforceThread thread : threads) {
            thread.start();
        }
        for (BruteforceThread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ArrayList<Integer> best_tour = null;
        int best_tour_length = Integer.MAX_VALUE;
        for (BruteforceThread thread : threads) {
            if (thread.best_tour != null && thread.min_path_length < best_tour_length) {
                best_tour = thread.best_tour;
                best_tour_length = thread.min_path_length;
            }
        }
        return best_tour;
    }

    // efficiently generate nth permutation of [0, 1, ..., n-1]
    public static ArrayList<Integer> getNthPermutation(int index, int size, CachedFactorial f) {
        if (index < 0 || index >= f.factorial(size)) {
            return null;
        }
        int[] permutation = new int[size];
        for (int i = 0; i < size; i++) {
            permutation[i] = i;
        }
        int[] permutation_index = new int[size];
        for (int i = 0; i < size; i++) {
            permutation_index[i] = i;
        }
        int current_index = index;
        for (int i = 0; i < size; i++) {
            int factorial = f.factorial(size - i - 1);
            int quotient = current_index / factorial;
            permutation[i] = permutation_index[quotient];
            permutation_index[quotient] = permutation_index[size - i - 1];
            current_index = current_index % factorial;
        }
        ArrayList<Integer> permutation_list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            permutation_list.add(permutation[i] + 1);
        }
        return permutation_list;
    }
}
