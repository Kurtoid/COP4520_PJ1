package tsp_demo2.graph;

public class Edge implements Comparable<Edge> {
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

    @Override
    public int compareTo(Edge o) {
        return Double.compare(this.weight, o.weight);
    }

    public boolean equals(Edge e) {
        // return this.to == e.to && this.from == e.from;
        // handle undirected equality
        return (this.to == e.to && this.from == e.from) || (this.to == e.from && this.from == e.to);
    }

    // hash to and from
    public int hashCode() {
        int to = this.to;
        int from = this.from;
        // handle undirected equality
        if (to > from) {
            int temp = to;
            to = from;
            from = temp;
        }
        return Integer.hashCode(to) * 31 + Integer.hashCode(from);
    }
}
