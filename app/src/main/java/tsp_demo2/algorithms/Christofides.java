package tsp_demo2.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.checkerframework.checker.units.qual.A;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.cycle.HierholzerEulerianCycle;
import org.jgrapht.alg.tour.ChristofidesThreeHalvesApproxMetricTSP;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.WeightedMultigraph;
import org.jgrapht.util.SupplierUtil;

import tsp_demo2.PathResult;
import tsp_demo2.algorithms.helpers.Blossom;
import tsp_demo2.algorithms.helpers.Prims;
import tsp_demo2.graph.Edge;
import tsp_demo2.graph.Graph;
import tsp_demo2.graph.Node;

public class Christofides {
    public static PathResult find(Graph g) {
        // convert graph to a JGraphT graph
        // run blossom on it
        // convert it back to our graph representation
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> jgrapht_graph = g.to_JGraphT();
        long start = System.currentTimeMillis();
        ChristofidesThreeHalvesApproxMetricTSP<Integer, DefaultWeightedEdge> christofides = new ChristofidesThreeHalvesApproxMetricTSP<>();
        long elapsed = System.currentTimeMillis() - start;
        GraphPath<Integer, DefaultWeightedEdge> tour = christofides.getTour(jgrapht_graph);
        return new PathResult(new ArrayList<Integer>(tour.getVertexList()), elapsed);
    }

    public static ArrayList<Integer> find_not_yet_working(Graph g) {
        // 1. Find the minimum spanning tree
        // 2. find O = set of odd degree vertices
        // 3. find minumum weight perfect matching M given by vertices in O
        // 4. combine edges of M and T to form multigraph H
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
        System.out.println("found " + matched_edges.size() + " edges");
        int num_edges_in_mst = 0;
        for (int i = 1; i <= mst.dimension; i++) {
            num_edges_in_mst += mst.getNode(i).get_edges().size();
        }
        System.out.println("num edges in mst: " + num_edges_in_mst);

        // 4. combine edges of M and T to form H
        // NOTE: this is a multigraph!
        // our current graph structure doesn't support multigraphs, so we'll use
        // WeightedMultigraph

        // it's very important that we don't allow duplicate edges from MST, but allow
        // duplicate edges from matches
        // SimpleWeightedGraph<Integer, DefaultWeightedEdge> mst_jgrapht =
        // mst.to_JGraphT();
        WeightedMultigraph<Integer, DefaultWeightedEdge> H = new WeightedMultigraph<>(
                DefaultWeightedEdge.class);
        H.setEdgeSupplier(SupplierUtil.createDefaultWeightedEdgeSupplier());
        // add nodes
        for (int i = 1; i <= mst.dimension; i++) {
            H.addVertex(i);
        }
        // add edges from mst
        // disallow duplicate edges
        int mst_added_edges = 0;
        for (int i = 1; i <= mst.dimension; i++) {
            for (Edge edge : mst.getNode(i).get_edges()) {
                // make sure we don't add duplicate edges
                if (!H.containsEdge(i, edge.to)) {
                    DefaultWeightedEdge jgrapht_edge = H.addEdge(i, edge.to);
                    H.setEdgeWeight(jgrapht_edge, edge.weight);
                    mst_added_edges++;
                }
            }
        }
        System.out.println("added " + mst_added_edges + " edges from mst");
        // add edges from matched edges
        // allow duplicate edges
        int matched_added_edges = 0;
        for (Edge edge : matched_edges) {
            DefaultWeightedEdge jgrapht_edge = H.addEdge(edge.from, edge.to);
            H.setEdgeWeight(jgrapht_edge, edge.weight);
            matched_added_edges++;
        }
        System.out.println("added " + matched_added_edges + " edges from matched edges");
        // make sure num edges in H == matched_added_edges + mst_added_edges
        assert H.edgeSet().size() == matched_added_edges + mst_added_edges;

        // 5. form a circut from H
        HierholzerEulerianCycle<Integer, DefaultWeightedEdge> cycle = new HierholzerEulerianCycle<>();
        GraphPath<Integer, DefaultWeightedEdge> path = cycle.getEulerianCycle(H);
        // System.out.println("cycle: " + path.toString());

        ArrayList<Integer> tour = new ArrayList<>();
        for (Integer node_id : path.getVertexList()) {
            tour.add(node_id);
        }
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

        // multigraph sanity check: are there edges in the graph with duplicate hashes?
        Set<Edge> all_edges = new HashSet<>();
        for (int i = 1; i <= g.dimension; i++) {
            for (Edge edge : g.getNode(i).get_edges()) {
                if (all_edges.contains(edge)) {
                    System.out.println("ERROR: duplicate edge detected");
                    System.out.println(edge.toString());
                    System.exit(1);
                }
                all_edges.add(edge);
            }
        }
        System.out.println("known edges: " + all_edges.size());

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