package tsp_demo2.algorithms.helpers;

import java.util.ArrayDeque;
import java.util.ArrayList;

import tsp_demo2.graph.Edge;
import tsp_demo2.graph.Graph;
import tsp_demo2.graph.Node;

public class Prims {
    // while there are unvisited nodes, find the nearest unvisited node
    // add it to the tour
    public static Graph find(Graph g) {
        Graph new_graph = new Graph();
        ArrayDeque<Edge> edges = new ArrayDeque<>();
        for (int i = 1; i <= g.dimension; i++) {
            Node orig_node = g.getNode(i);
            Node new_node = Node.copy_without_edges(orig_node);
            new_graph.addNode(new_node);
        }
        ArrayList<Integer> visited = new ArrayList<>();

        Node current_node = g.getNode(1);
        visited.add(current_node.id);
        edges.addAll(current_node.get_edges());
        while (visited.size() < g.dimension) {
            Edge next_edge = null;
            Node next_node = null;
            do {
                next_edge = edges.remove();
                next_node = g.getNode(next_edge.to);
            } while (visited.contains(next_node.id));
            visited.add(next_node.id);
            edges.addAll(next_node.get_edges());
            next_node.add_edge(next_edge);
        }
        new_graph.make_undirected();
        return new_graph;
    }
}
