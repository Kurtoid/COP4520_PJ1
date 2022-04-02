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
    static final int NUM_RUNS = 8;
    public static void main(String[] args) throws Exception {
        // print current resource path
        System.out.println(System.getProperty("user.dir"));
        Graph g = Graph.from_tsplib("custom_graphs/lin10.tsp");
        // Graph g = Graph.from_tsplib("solved_graphs/berlin52.tsp");
        // g.optimal_tour = Graph.read_tour("solved_graphs/tsp225.opt.tour");
        System.out.printf("Optimal tour length: %f\n",
                g.get_tour_length(g.optimal_tour));
        // System.out.println(g.optimal_tour);

        RunMetrics[] metrics = new RunMetrics[NUM_RUNS];
        for (int i = 0; i < NUM_RUNS; i++) {
            PathResult tour = GreedyNearest.find(g);
            metrics[i] = new RunMetrics(tour.time, g.get_tour_length(tour.path));
        }
        RunMetrics avg_metrics = RunMetrics.getAvg(metrics);
        System.out.println(avg_metrics.report("GreedyNearest"));

        metrics = new RunMetrics[NUM_RUNS];
        for (int i = 0; i < NUM_RUNS; i++) {
            PathResult christofides = Christofides.find(g);
            metrics[i] = new RunMetrics(christofides.time, g.get_tour_length(christofides.path));
        }
        avg_metrics = RunMetrics.getAvg(metrics);
        System.out.println(avg_metrics.report("Christofides"));

        metrics = new RunMetrics[NUM_RUNS];
        for (int i = 0; i < NUM_RUNS; i++) {
            PathResult ant_colony = SerialAntColony.find(g, 4, 5);
            metrics[i] = new RunMetrics(ant_colony.time, g.get_tour_length(ant_colony.path));
        }
        avg_metrics = RunMetrics.getAvg(metrics);
        System.out.println(avg_metrics.report("AntColony-Serial"));

        metrics = new RunMetrics[NUM_RUNS];
        for (int i = 0; i < NUM_RUNS; i++) {
            PathResult ant_colony = ParallelAntColony.find(g, 4, 5);
            metrics[i] = new RunMetrics(ant_colony.time, g.get_tour_length(ant_colony.path));
        }
        avg_metrics = RunMetrics.getAvg(metrics);
        System.out.println(avg_metrics.report("AntColony-Parallel"));

        if (g.dimension < 12) {
            metrics = new RunMetrics[NUM_RUNS];
            for (int i = 0; i < NUM_RUNS; i++) {
                PathResult bruteforce = SerialBruteforce.find(g);
                metrics[i] = new RunMetrics(bruteforce.time, g.get_tour_length(bruteforce.path));
            }
            avg_metrics = RunMetrics.getAvg(metrics);
            System.out.println(avg_metrics.report("Bruteforce-Serial"));

            metrics = new RunMetrics[NUM_RUNS];
            for (int i = 0; i < NUM_RUNS; i++) {
                PathResult bruteforce = ParallelBruteforce.find(g, 4);
                metrics[i] = new RunMetrics(bruteforce.time, g.get_tour_length(bruteforce.path));
            }
            avg_metrics = RunMetrics.getAvg(metrics);
            System.out.println(avg_metrics.report("Bruteforce-Parallel"));
        }
    }

    static class RunMetrics {
        double dist;
        double time;

        public RunMetrics(double time, double tour_length) {
            this.time = time;
            this.dist = tour_length;
        }

        static double getAvgTime(List<RunMetrics> metrics) {
            double sum = 0;
            for (RunMetrics m : metrics) {
                sum += m.time;
            }
            return sum / metrics.size();
        }

        static double getAvgDist(List<RunMetrics> metrics) {
            double sum = 0;
            for (RunMetrics m : metrics) {
                sum += m.dist;
            }
            return sum / metrics.size();
        }

        static RunMetrics getAvg(RunMetrics[] metrics) {
            double sum_time = 0;
            double sum_dist = 0;
            for (RunMetrics m : metrics) {
                sum_time += m.time;
                sum_dist += m.dist;
            }
            return new RunMetrics(sum_time / metrics.length, sum_dist / metrics.length);
        }

        String report(String run_name) {
            return String.format("%s: %.2f, %.2fms", run_name, dist, time);
        }
    }
}
