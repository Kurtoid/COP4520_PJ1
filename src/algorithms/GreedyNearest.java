package algorithms;

import java.util.ArrayList;

import graph.Graph;

public class GreedyNearest {
    public static ArrayList<Integer> find(Graph g) {
        ArrayList<Integer> tour = new ArrayList<>();
        ArrayList<Integer> unvisited = new ArrayList<>();
        // node ids are 1-indexed!
        for (int i = 1; i <= g.dimension; i++) {
            unvisited.add(i);
        }
        int current_node = 1;
        tour.add(current_node);
        unvisited.remove(new Integer(current_node));
        while (unvisited.size() > 0) {
            int nearest_node = -1;
            double nearest_dist = Double.MAX_VALUE;
            for (int i = 0; i < unvisited.size(); i++) {
                int node = unvisited.get(i);
                double dist = g.getNode(current_node).get_edge(g.getNode(node)).weight;
                if (dist < nearest_dist) {
                    nearest_node = node;
                    nearest_dist = dist;
                }
            }
            tour.add(nearest_node);
            unvisited.remove(new Integer(nearest_node));
            current_node = nearest_node;
        }
        return tour;

    }
}
