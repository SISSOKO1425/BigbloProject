package com.chaine_commandement.utils;

import com.chaine_commandement.dto.Edge;
import com.chaine_commandement.dto.GraphNode;
import com.chaine_commandement.dto.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphGenerator {
    private static final int NODE_WIDTH = 250;
    private static final int BASE_HEIGHT = 100;
    private static final int PERSON_HEIGHT = 80;
    private static final int SPACING_X = 300;
    private static final int SPACING_Y = 200;

    public static Map<String, List<?>> generateGraph(Node node, Integer parentId, int x, int y, int level) {
        List<GraphNode> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        int nodeHeight = BASE_HEIGHT + node.getPersonnes().size() * PERSON_HEIGHT;

        GraphNode newNode = new GraphNode(node.getId(), x, y, node.getNom(), node.getRM(), node.getPersonnes());
        nodes.add(newNode);

        if (parentId != null) {
            edges.add(new Edge(parentId, node.getId()));
        }

        int numChildren = node.getSousFonctions().size();
        int centerX = x;
        int baseY = y + nodeHeight + SPACING_Y;

        for (int i = 0; i < numChildren; i++) {
            Node child = node.getSousFonctions().get(i);
            int childX = (numChildren == 1) ? centerX : (centerX + (i - (numChildren - 1) / 2) * SPACING_X);

            Map<String, List<?>> result = generateGraph(child, node.getId(), childX, baseY, level + 1);
            nodes.addAll((List<GraphNode>) result.get("nodes"));
            edges.addAll((List<Edge>) result.get("edges"));
        }

        Map<String, List<?>> graph = new HashMap<>();
        graph.put("nodes", nodes);
        graph.put("edges", edges);
        return graph;
    }
}
