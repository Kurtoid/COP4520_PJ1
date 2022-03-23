package tsp_demo2;
import java.util.ArrayList;

import algorithms.GreedyNearest;
import graph.Graph;

public class App {

    public static void main(String[] args) throws Exception {
        // print current resource path
        System.out.println(System.getProperty("user.dir"));
        Graph g = Graph.from_tsplib("solved_graphs/a280.tsp");
        g.optimal_tour = Graph.read_tour("solved_graphs/a280.opt.tour");
        System.out.printf("Optimal tour length: %f\n", g.get_tour_length(g.optimal_tour));
        // System.out.println(g.optimal_tour);
        ArrayList<Integer> tour = GreedyNearest.find(g);
        System.out.printf("Greedy tour length: %f\n", g.get_tour_length(tour));
        // System.out.println(tour);

    }
}
