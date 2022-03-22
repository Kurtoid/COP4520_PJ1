package graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class Graph {
    public ArrayList<Node> nodes;
    public String name;
    public String comment;
    public ArrayList<Integer> optimal_tour;
    public HashMap<Integer, Integer> id_to_index;

    public Graph() {
        nodes = new ArrayList<>();
        id_to_index = new HashMap<>();
        optimal_tour = null;
    }

    static public Graph from_tsplib(String filename) {
        try {
            Graph graph = new Graph();
            int dimension = 0;
            String edge_weight_type = null;
            // open the file
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            // read until EOF
            while (line != null) {
                line = line.trim();
                String wsp_reg = "\\s+";
                String[] parts = line.split(wsp_reg);
                // there must be two values: name and val
                if (parts.length != 2) {
                    continue;
                }
                String name = parts[0].trim();
                name = name.toUpperCase();
                String val = parts[1].trim();

                switch (name) {
                    case "NAME":
                        graph.name = val;
                        break;
                    case "COMMENT":
                        graph.comment = val;
                        break;
                    case "DIMENSION":
                        dimension = Integer.parseInt(val);
                        break;
                    case "EDGE_WEIGHT_TYPE":
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
                        if (!edge_weight_type.equals("EUC_2D")) {
                            System.err.println("Error: edge_weight_type not supported");
                            return null;
                        }
                        for (int i = 0; i < dimension; i++) {
                            line = reader.readLine();
                            line = line.trim();
                            parts = line.split(wsp_reg);
                            if (parts.length != 3) {
                                continue;
                            }
                            int id = Integer.parseInt(parts[0]);
                            double x = Double.parseDouble(parts[1]);
                            double y = Double.parseDouble(parts[2]);
                            Node node = new Node(id, x, y);
                            graph.nodes.add(node);
                            graph.id_to_index.put(id, i);
                        }
                        break;
                }
            }
            reader.close();
            return graph;
        } catch (Exception e) {
            return null;
        }
    }

    // static ArrayList<Integer> read_tour(String filename) {
    // try {
    // ArrayList<Integer> tour = new ArrayList<>();
    // BufferedReader reader = new BufferedReader(new FileReader(filename));
    // String line = reader.readLine();
    // while (line != null) {
    // line = line.trim();
    // String wsp_reg = "\\s+";
    // String[] parts = line.split(wsp_reg);
    // if (parts.length != 2) {
    // continue;
    // }
    // String name = parts[0].trim();
    // if (name.equals("TOUR_SECTION")) {
    // while(line.eq
    // }
    // }
    // reader.close();
    // return tour;
    // } catch (Exception e) {
    // return null;
    // }
    // }
}
