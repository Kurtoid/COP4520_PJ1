package graph;

import java.util.ArrayList;

public class Node {
    public Node(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public int id;
    public ArrayList<Edge> edges;

    // not guaranteed to exis
    public int x;
    public int y;
}
