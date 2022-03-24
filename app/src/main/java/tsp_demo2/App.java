package tsp_demo2;
import java.util.ArrayList;
import java.util.List;

import tsp_demo2.algorithms.Christofides;
import tsp_demo2.algorithms.GreedyNearest;
import tsp_demo2.graph.Graph;

public class App {

    public static void main(String[] args) throws Exception {
        // print current resource path
        System.out.println(System.getProperty("user.dir"));
        Graph g = Graph.from_tsplib("solved_graphs/eil76.tsp");
        g.optimal_tour = Graph.read_tour("solved_graphs/eil76.opt.tour");
        System.out.printf("Optimal tour length: %f\n", g.get_tour_length(g.optimal_tour));
        // System.out.println(g.optimal_tour);
        List<Integer> tour = GreedyNearest.find(g);
        System.out.printf("Greedy tour length: %f\n", g.get_tour_length(tour));
        List<Integer> christofides = Christofides.find(g);
        System.out.printf("Christofides tour length: %f\n", g.get_tour_length(christofides));

    }
}
