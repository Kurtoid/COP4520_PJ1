import java.util.ArrayList;

import algorithms.helpers.Prims;
import graph.Graph;

public class Christofides {
    public static ArrayList<Integer> find(Graph g) {
        // 1. Find the minimum spanning tree
        // 2. find O = set of odd degree vertices
        // 3. find minumum weight perfect matching M given by vertices in O
        // 4. combine edges of M and T to form H
        // 5. form a circut from H
        // 6. remove repeated vertices

        // 1. Find the minimum spanning tree
        Graph mst = Prims.find(g);

        // 2. find O = set of odd degree vertices
        ArrayList<Integer> odd_degree_nodes = new ArrayList<>();
        for (int i = 1; i <= g.dimension; i++) {
            if (mst.getNode(i).get_degree() % 2 == 1) {
                odd_degree_nodes.add(i);
            }
        }

        // by the handshaking lemma, O has an even number of vertices
        assert odd_degree_nodes.size() % 2 == 0;

        // create subgraph G from O
        // using only nodes from O, but with all edges
        Graph subgraph = new Graph();
        for (int i = 0; i < odd_degree_nodes.size(); i++) {
            subgraph.addNode(mst.getNode(odd_degree_nodes.get(i)));
        }
        // remove invalid edges
        subgraph.make_undirected();

        // 3. find minumum weight perfect matching M given by vertices in O
        // using Blossom algorithm
        // ArrayList<Integer> matching = Blossom.find(subgraph);
        return null;
    }
}