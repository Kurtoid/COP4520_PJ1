package graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class Graph {
    private ArrayList<Node> nodes;
    public String name;
    public String comment;
    public int dimension;
    public ArrayList<Integer> optimal_tour;
    private HashMap<Integer, Integer> id_to_index;

    public Graph() {
        nodes = new ArrayList<>();
        id_to_index = new HashMap<>();
        optimal_tour = null;
    }

    public Node getNode(int id) {
        return nodes.get(id_to_index.get(id));
    }

    public void addNode(Node node) {
        nodes.add(node);
        id_to_index.put(node.id, nodes.size() - 1);
    }

    static public Graph from_tsplib(String filename) {
        try {
            Graph graph = new Graph();
            String edge_weight_type = null;
            // open the file
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = null;
            // read until EOF
            do {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                // split by ":", and any surrounding whitespace
                String colon_reg = "\\s*:\\s*";
                String[] parts = line.split(colon_reg);

                String name = parts[0].trim();
                name = name.toUpperCase();
                String val;
                if (parts.length > 1) {
                    val = parts[1].trim();
                } else {
                    val = "";
                }
                switch (name) {
                    case "NAME":
                        System.out.println("name: " + val);
                        graph.name = val;
                        break;
                    case "COMMENT":
                        System.out.println("comment: " + val);
                        graph.comment = val;
                        break;
                    case "DIMENSION":
                        System.out.println("dimension: " + val);
                        graph.dimension = Integer.parseInt(val);
                        break;
                    case "EDGE_WEIGHT_TYPE":
                        System.out.println("edge_weight_type: " + val);
                        edge_weight_type = val;
                        if (edge_weight_type.equals("EXPLICIT") || edge_weight_type.equals("GEO")) {
                            System.err.println("Error: edge_weight_type not supported");
                            return null;
                        }
                        if (!edge_weight_type.equals("EUC_2D")) {
                            System.err.println("Error: edge_weight_type not supported");
                            return null;
                        }
                        break;
                    case "NODE_COORD_SECTION":
                        System.out.println("node_coord_section");
                        if (!edge_weight_type.equals("EUC_2D")) {
                            System.err.println("Error: edge_weight_type not supported");
                            return null;
                        }
                        for (int i = 0; i < graph.dimension; i++) {
                            line = reader.readLine();
                            line = line.trim();
                            parts = line.split("\\s+");
                            if (parts.length != 3) {
                                continue;
                            }
                            int id = Integer.parseInt(parts[0]);
                            double x = Double.parseDouble(parts[1]);
                            double y = Double.parseDouble(parts[2]);
                            Node node = new Node(id, x, y);
                            graph.addNode(node);
                            System.out.println("node: " + node);
                        }
                        break;
                }
            } while (line != null);
            reader.close();
            assert (graph.nodes.size() == graph.dimension);
            // add edges - the graph is undirected and fully connected
            for (int i = 0; i < graph.dimension; i++) {
                Node node = graph.nodes.get(i);
                for (int j = 0; j < graph.dimension; j++) {
                    if (i == j) {
                        continue;
                    }
                    Node node2 = graph.nodes.get(j);
                    double dist = Math.sqrt(Math.pow(node.x - node2.x, 2) + Math.pow(node.y - node2.y, 2));
                    Edge edge = new Edge(node2.id, node.id, dist);
                    node.add_edge(edge);
                    edge = new Edge(node.id, node2.id, dist);
                    node2.add_edge(edge);
                }
            }
            return graph;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            // print backtrace
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<Integer> read_tour(String filename) {
        try {
            ArrayList<Integer> tour = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            do {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                String wsp_reg = "\\s+";
                String[] parts = line.split(wsp_reg);
                String name = parts[0].trim();
                if (name.equals("TOUR_SECTION")) {
                    // read the tour
                    // read a new line
                    line = reader.readLine();
                    while (line != null) {
                        line = line.trim();
                        // there _should_ be one per line, but
                        // there might be multiple items per line
                        parts = line.split(wsp_reg);
                        for (int i = 0; i < parts.length; i++) {
                            String val = parts[i].trim();
                            if (val.length() == 0) {
                                line = reader.readLine();
                                continue;
                            }
                            int id = Integer.parseInt(val);
                            if (id == -1) {
                                break;
                            }
                            tour.add(id);
                        }
                        line = reader.readLine();
                        }
                }
            } while (line != null);
            reader.close();
            return tour;
        } catch (Exception e) {
            return null;
        }
    }

    public double get_tour_length(ArrayList<Integer> tour) {
        if (tour == null) {
            if (optimal_tour == null) {
                // TODO: throw an exception
                return -1;
            }
            tour = optimal_tour;
        }
        double length = 0;
        for (int i = 0; i < tour.size() - 1; i++) {
            int id1 = tour.get(i);
            int id2 = tour.get(i + 1);
            Node node1 = getNode(id1);
            Node node2 = getNode(id2);
            Edge edge = node1.get_edge(node2);
            length += edge.weight;
        }
        return length;
    }
}
