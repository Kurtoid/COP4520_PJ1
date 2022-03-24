package tsp_demo2.algorithms.helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.TreeSet;

import tsp_demo2.graph.Edge;
import tsp_demo2.graph.Graph;
import tsp_demo2.graph.Node;

public class Prims {
    // while there are unvisited nodes, find the nearest unvisited node
    // add it to the tour
    public static Graph find(Graph g) {
        Graph mst = new Graph();
        // add all the nodes, without edges, to the MST
        for (int i = 1; i <= g.dimension; i++) {
            Node orig_node = g.getNode(i);
            Node new_node = Node.copy_without_edges(orig_node);
            mst.addNode(new_node);
        }
        HashSet<Node> visited = new HashSet<>();
        TreeSet<Edge> edges = new TreeSet<>();

        Node first_node = g.getNode(1);
        visited.add(first_node);
        edges.addAll(first_node.get_edges());
        while (visited.size() < g.dimension) {
            Edge nearest_edge = null;
            Iterator<Edge> it = edges.iterator();
            while (it.hasNext()) {
                Edge edge = it.next();
                Node node = mst.getNode(edge.to);
                if (!visited.contains(node)) {
                    nearest_edge = edge;
                    break;
                }
            }
            if (nearest_edge == null) {
                System.out.println("ERROR: no nearest edge found!");
                return null;
            }
            Node orig_node = g.getNode(nearest_edge.to);
            Node new_node = mst.getNode(nearest_edge.to);
            visited.add(new_node);
            edges.addAll(orig_node.get_edges());
            Edge new_edge = new Edge(nearest_edge.from, nearest_edge.to, nearest_edge.weight);
            mst.addEdge(new_edge);
            // add the reverse edge
            Edge reverse_edge = new Edge(nearest_edge.to, nearest_edge.from, nearest_edge.weight);
            mst.addEdge(reverse_edge);
        }
        return mst;
    }
}
