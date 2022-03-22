package graph;

public class Edge {
    public Edge(int to, int from, double weight) {
        this.to = to;
        this.from = from;
        this.weight = weight;
    }

    // this contains the ID of the node this edge points to, not the index
    public int to;
    // for EUC_2D graphs, this is the euclidean distance
    public double weight;
    // this contains the ID of the node this edge points from, not the index
    public int from;
}
