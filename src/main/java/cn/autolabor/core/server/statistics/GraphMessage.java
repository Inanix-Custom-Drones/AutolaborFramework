package cn.autolabor.core.server.statistics;

import cn.autolabor.util.Strings;
import cn.autolabor.util.autobuf.SerializableMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphMessage implements SerializableMessage {

    List<NodeMessage> nodes = new ArrayList<>();
    Map<PairIndex, EdgeMessage> edges = new HashMap<>();

    public GraphMessage() {}


    public boolean addNode(NodeMessage nodeMessage) {
        if (nodes.contains(nodeMessage)) {
            return false;
        } else {
            nodes.add(nodeMessage);
            return true;
        }
    }

    public boolean addEdge(Integer from, Integer to, EdgeMessage edgeMessage) {
        PairIndex pairIndex = new PairIndex(from, to);
        if (!edges.containsKey(pairIndex)) {
            edges.put(pairIndex, edgeMessage);
            return true;
        } else {
            return false;
        }
    }

    public List<NodeMessage> getNodes() {
        return nodes;
    }

    public Map<PairIndex, EdgeMessage> getEdges() {
        return edges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        GraphMessage that = (GraphMessage) o;

        if (!nodes.equals(that.nodes))
            return false;
        return edges.equals(that.edges);
    }

    @Override
    public int hashCode() {
        int result = nodes.hashCode();
        result = 31 * result + edges.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodes.size(); i++) {
            sb.append(String.format("%-5d", i)).append(nodes.get(i)).append("\n");
        }
        sb.append("\n");
        sb.append(nodes.toString()).append("\n");
        sb.append(Strings.mapToString(edges, 20)).append("\n");
        return sb.toString();
    }
}
