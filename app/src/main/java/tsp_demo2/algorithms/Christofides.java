package tsp_demo2.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import tsp_demo2.algorithms.helpers.Blossom;
import tsp_demo2.algorithms.helpers.Prims;
import tsp_demo2.graph.Edge;
import tsp_demo2.graph.Graph;
import tsp_demo2.graph.Node;

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
        System.out.println(mst.toString());

        // 2. find O = set of odd degree vertices
        ArrayList<Integer> odd_degree_nodes = new ArrayList<>();
        for (int i = 1; i <= g.dimension; i++) {
            // int degree = mst.getNode(i).get_degree();
            // System.out.println("Node " + i + " has degree " + degree);
            if (mst.getNode(i).get_degree() % 2 == 1) {
                odd_degree_nodes.add(i);
            }
        }

        // by the handshaking lemma, O has an even number of vertices
        assert odd_degree_nodes.size() % 2 == 0;
        // System.out.println("O = " + odd_degree_nodes.toString());

        // create subgraph G from O
        // using only nodes from O, but with all edges
        Graph subgraph = new Graph();
        for (Integer node_id : odd_degree_nodes) {
            Node node = Node.copy_without_edges(g.getNode(node_id));
            subgraph.addNode(node);
        }
        // remove invalid edges
        subgraph.make_undirected();
        System.out.println("subgraph: " + subgraph.toString());

        // 3. find minumum weight perfect matching M given by vertices in O
        // using Blossom algorithm
        ArrayList<Edge> matched_edges = Blossom.find(subgraph);

        // 4. combine edges of M and T to form H
        // add the edges from matching to the mst
        for (Edge edge : matched_edges) {
            mst.addEdge(edge);
            // add the reverse edge
            Edge reverse_edge = new Edge(edge.from, edge.to, edge.weight);
            mst.addEdge(reverse_edge);
        }

        // 5. form a circut from H
        ArrayList<Integer> tour = euler_tour(mst);
        System.out.println("tour: " + tour.size());

        // 6. remove repeated vertices
        ArrayList<Integer> filtered_tour = new ArrayList<>();
        for (Integer i : tour) {
            if (!filtered_tour.contains(i)) {
                filtered_tour.add(i);
            }
        }
        System.out.println("filtered tour: " + filtered_tour.size());
        return filtered_tour;
    }

    static ArrayList<Integer> euler_tour(Graph g) {
        // Hierholzer’s Algorithm - linear time Eulerian Tour
        // create two stacks - one for a temporary path, and one for the final tour
        // temporary path:
        ArrayList<Integer> cpath = new ArrayList<>();
        // final tour:
        ArrayList<Integer> epath = new ArrayList<>();

        Set<Edge> visited = new HashSet<>();
        Node start = g.getNode(1);
        cpath.add(start.id);
        visited.add(start.get_edge(start));
        while (cpath.size() > 0) {
            int u = cpath.get(cpath.size() - 1);
            Node node = g.getNode(u);
            // are all edges in the node visited?
            boolean all_visited = true;
            for (Edge edge : node.get_edges()) {
                if (!visited.contains(edge)) {
                    all_visited = false;
                    break;
                }
            }
            if (all_visited) {
                // remove the node from the temporary path
                cpath.remove(cpath.size() - 1);
                // add the node to the final tour
                epath.add(u);
            } else {
                // select an edge that has not been visited
                Edge edge = null;
                for (Edge e : node.get_edges()) {
                    if (!visited.contains(e)) {
                        edge = e;
                        break;
                    }
                }
                // add the edge to the temporary path
                cpath.add(edge.to);
                // mark the edge as visited
                visited.add(edge);
            }
        }
        return epath;
    }

}