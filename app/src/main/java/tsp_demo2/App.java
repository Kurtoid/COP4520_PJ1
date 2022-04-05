package tsp_demo2;

import java.util.Arrays;
import java.util.List;

import tsp_demo2.algorithms.Christofides;
import tsp_demo2.algorithms.GreedyNearest;
import tsp_demo2.algorithms.AntColony.ParallelAntColony;
import tsp_demo2.algorithms.AntColony.SerialAntColony;
import tsp_demo2.algorithms.Bruteforce.ParallelBruteforce;
import tsp_demo2.algorithms.Bruteforce.SerialBruteforce;
import tsp_demo2.graph.Graph;

public class App {
    static final int NUM_RUNS = 3;

    public static void main(String[] args) throws Exception {
        Graph g = Graph.from_tsplib("custom_graphs/lin12.tsp");
        // Graph g = Graph.from_tsplib("solved_graphs/tsp225.tsp");
        // g.optimal_tour = Graph.read_tour("solved_graphs/berlin52.opt.tour");
        // System.out.printf("Optimal tour length: %f\n",
        // g.get_tour_length(g.optimal_tour));

        // runcoretest(g, 8, 48);
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
            metrics[i] = new RunMetrics(christofides.time,
                    g.get_tour_length(christofides.path));
        }
        avg_metrics = RunMetrics.getAvg(metrics);
        System.out.println(avg_metrics.report("Christofides"));

        metrics = new RunMetrics[NUM_RUNS];
        for (int i = 0; i < NUM_RUNS; i++) {
            PathResult ant_colony = SerialAntColony.find(g, g.dimension / 4, 25);
            metrics[i] = new RunMetrics(ant_colony.time,
                    g.get_tour_length(ant_colony.path));
        }
        avg_metrics = RunMetrics.getAvg(metrics);
        System.out.println(avg_metrics.report("AntColony-Serial"));

        metrics = new RunMetrics[NUM_RUNS];
        for (int i = 0; i < NUM_RUNS; i++) {
            PathResult ant_colony = ParallelAntColony.find(g, g.dimension / 4, 25);
            metrics[i] = new RunMetrics(ant_colony.time,
                    g.get_tour_length(ant_colony.path));
        }
        avg_metrics = RunMetrics.getAvg(metrics);
        System.out.println(avg_metrics.report("AntColony-Parallel"));

        if (g.dimension < 10) {
            // TODO: optionally change the run count here
            metrics = new RunMetrics[NUM_RUNS];
            for (int i = 0; i < NUM_RUNS; i++) {
                PathResult bruteforce = SerialBruteforce.find(g);
                metrics[i] = new RunMetrics(bruteforce.time,
                        g.get_tour_length(bruteforce.path));
            }
            avg_metrics = RunMetrics.getAvg(metrics);
            System.out.println(avg_metrics.report("Bruteforce-Serial"));

            metrics = new RunMetrics[NUM_RUNS];
            for (int i = 0; i < NUM_RUNS; i++) {
                PathResult bruteforce = ParallelBruteforce.find(g, 4);
                metrics[i] = new RunMetrics(bruteforce.time,
                        g.get_tour_length(bruteforce.path));
            }
            avg_metrics = RunMetrics.getAvg(metrics);
            System.out.println(avg_metrics.report("Bruteforce-Parallel"));
        }
    }

    private static void runcoretest(Graph g, int start, int stop) {
        int skip = 2;
        RunMetrics[][] metrics = new RunMetrics[(stop - start) / skip][NUM_RUNS];
        for (int i = start; i < stop; i += skip) {
            for (int j = 0; j < NUM_RUNS; j++) {
                PathResult tour = ParallelBruteforce.find(g, i);
                metrics[(i - start) / skip][j] = new RunMetrics(tour.time,
                        g.get_tour_length(tour.path));
                System.out.println("cores: " + i + " time: " + tour.time);
            }
        }
        // average across each run
        double[] times = new double[(stop - start) / skip];
        for (int i = 0; i < metrics.length; i++) {
            times[i] = RunMetrics.getAvg(metrics[i]).time;
        }
        System.out.println(Arrays.toString(times));

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
