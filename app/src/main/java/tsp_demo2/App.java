package tsp_demo2;
import java.util.ArrayList;
import java.util.List;

import tsp_demo2.algorithms.Christofides;
import tsp_demo2.algorithms.GreedyNearest;
import tsp_demo2.algorithms.AntColony.ParallelAntColony;
import tsp_demo2.algorithms.AntColony.SerialAntColony;
import tsp_demo2.algorithms.Bruteforce.ParallelBruteforce;
import tsp_demo2.algorithms.Bruteforce.SerialBruteforce;
import tsp_demo2.graph.Graph;

public class App {

    public static void main(String[] args) throws Exception {
        // print current resource path
        System.out.println(System.getProperty("user.dir"));
        // Graph g = Graph.from_tsplib("custom_graphs/lin10.tsp");
        Graph g = Graph.from_tsplib("solved_graphs/berlin52.tsp");
        // g.optimal_tour = Graph.read_tour("solved_graphs/tsp225.opt.tour");
        System.out.printf("Optimal tour length: %f\n",
                g.get_tour_length(g.optimal_tour));
        // System.out.println(g.optimal_tour);

        PathResult tour = GreedyNearest.find(g);
        System.out.println("GreedyNearest: " + tour.time + "ms");
        System.out.printf("Greedy tour length: %f\n", g.get_tour_length(tour.path));

        PathResult christofides = Christofides.find(g);
        System.out.println("Christofides (JGraphT): " + (christofides.time) + "ms");
        System.out.printf("Christofides tour length: %f\n",
                g.get_tour_length(christofides.path));

        PathResult parallelAntColony = ParallelAntColony.find(g, 5, 5);
        System.out.println("AntColony (Parallel): " + (parallelAntColony.time) + "ms");
        System.out.printf("AntColony tour length: %f\n", g.get_tour_length(parallelAntColony.path));

        // turns we don't need a lot of ants or iterations to get a good result
        PathResult ant_col_tour = SerialAntColony.find(g, 5, 5);
        System.out.println("AntColony: " + (ant_col_tour.time) + "ms");
        System.out.printf("Ant colony tour length: %f\n", g.get_tour_length(ant_col_tour.path));

        if (g.dimension < 12) {
            PathResult tsp_tour = SerialBruteforce.find(g);
            System.out.println("Bruteforce: " + (tsp_tour.time) + "ms");
            System.out.printf("Bruteforce tour length: %f\n", g.get_tour_length(tsp_tour.path));

            PathResult tsp_tour2 = ParallelBruteforce.find(g, 4);
            System.out.println("Bruteforce (Parallel): " + (tsp_tour2.time) + "ms");
            System.out.printf("Bruteforce tour length: %f\n", g.get_tour_length(tsp_tour2.path));
        }
    }
}
