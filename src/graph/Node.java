package graph;

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
    private ArrayList<Edge> edges;

    private HashMap<Integer, Edge> edge_map;

    // not guaranteed to exis
    public double x;
    public double y;

    public Edge get_edge(Node node2) {
        return edge_map.get(node2.id);
    }

    public void add_edge(Edge edge) {
        edges.add(edge);
        edge_map.put(edge.to, edge);
    }

}
