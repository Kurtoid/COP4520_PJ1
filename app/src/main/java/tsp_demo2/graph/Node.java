package tsp_demo2.graph;

import java.util.ArrayList;
import java.util.HashMap;

public class Node {
    public Node(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;

        this.edges = new ArrayList<>();
        this.edge_map = new HashMap<>();
    }

    public int id;
    protected ArrayList<Edge> edges;

    protected HashMap<Integer, Edge> edge_map;

    // not guaranteed to exis
    public double x;
    public double y;

    public Edge get_edge(Node node2) {
        return edge_map.get(node2.id);
    }

    public void add_edge(Edge edge) {
        if (edges.contains(edge)) {
            return;
        }
        edges.add(edge);
        edge_map.put(edge.to, edge);
    }

    public ArrayList<Edge> get_edges() {
        return edges;
    }

    public static Node copy_without_edges(Node orig_node) {
        Node new_node = new Node(orig_node.id, orig_node.x, orig_node.y);
        return new_node;
    }

    public int get_degree() {
        // get unique edges
        return edge_map.size();
    }

    public boolean equals(Node node) {
        return this.id == node.id;
    }

    public int hashCode() {
        return Integer.hashCode(id);
    }
}
