package cn.autolabor.core.server.statistics;

import cn.autolabor.util.Strings;
import cn.autolabor.util.Sugar;
import cn.autolabor.util.collections.DuplexMap;
import cn.autolabor.util.collections.graph.Edge;
import cn.autolabor.util.collections.graph.Graph;
import cn.autolabor.util.collections.graph.Vertex;
import cn.autolabor.util.collections.graph.VertexContainer;
import cn.autolabor.util.collections.multivaluemap.LinkedMultiValueMap;

import java.util.Map;
import java.util.Set;

public class TaskRelationGraph extends Graph {

    public TaskRelationGraph() {
    }

    @SuppressWarnings("unchecked")
    public GraphMessage toMessage() {
        beginEdit();
        try {
            GraphMessage message = new GraphMessage();
            DuplexMap<Integer, Integer> graphToMsgIndex = new DuplexMap<>();
            int count = 0;
            for (Map.Entry<Integer, VertexContainer> entry : vertexMap.entrySet()) {
                if (message.addNode(vertexToMessage(entry.getValue().getUserObject()))) {
                    graphToMsgIndex.put(entry.getKey(), count++);
                }
            }
            for (VertexContainer container : vertexMap.values()) {
                LinkedMultiValueMap<Integer, Edge> edgeInfos = container.getEdgeInfos();
                for (Integer toIndex : edgeInfos.keySet()) {
                    Set<Edge> edges = edgeInfos.getValues(toIndex);
                    edges.forEach((edge -> message.addEdge(graphToMsgIndex.get(container.getIndex()), graphToMsgIndex.get(toIndex), edgeToMessage(edge))));
                }
            }
            return message;
        } finally {
            endEdit();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void unsafeAdd(VertexContainer source, VertexContainer target, Edge edge) {
        if (edge.getClass().equals(TaskCallEdge.class)) {
            source.mergeEdge(target.getIndex(), edge, this::mergeTaskCallEdge);
        } else {
            source.addEdge(target.getIndex(), edge);
        }
    }

    private Edge mergeTaskCallEdge(Set<Edge> existEdges, Edge newEdge) {
        if (existEdges != null && Sugar.checkInherit(newEdge.getClass(), TaskCallEdge.class) && existEdges.contains(newEdge)) {
            TaskCallEdge originalEdge = null;
            for (Edge e : existEdges) {
                if (e.equals(newEdge)) {
                    originalEdge = (TaskCallEdge) e;
                }
            }
            if (originalEdge != null) {
                originalEdge.merge((TaskCallEdge) newEdge);
                return originalEdge;
            }
        }
        return newEdge;
    }

    private NodeMessage vertexToMessage(Vertex vertex) {
        switch (Strings.lastClassName(vertex.getClass().getName())) {
            case "TaskEventVertex":
                TaskEventVertex taskEventVertex = (TaskEventVertex) vertex;
                return new NodeMessage("TaskEventVertex", taskEventVertex.getTaskName(), taskEventVertex.getEventName());
            case "ClassFunctionVertex":
                ClassFunctionVertex classFunctionVertex = (ClassFunctionVertex) vertex;
                return new NodeMessage("ClassFunctionVertex", classFunctionVertex.getClassName(), classFunctionVertex.getFunctionName());
            case "UnknowTypeVertex":
                return new NodeMessage("UnknowTypeVertex", null, null);
            default:
                throw Sugar.makeThrow("Node type %s conversion is not supported", vertex.getClass().getName());
        }
    }

    private EdgeMessage edgeToMessage(Edge edge) {
        switch (Strings.lastClassName(edge.getClass().getName())) {
            case "MessageCallEdge":
                MessageCallEdge messageCallEdge = (MessageCallEdge) edge;
                return new EdgeMessage("MessageCallEdge", messageCallEdge.getTopic());
            case "TaskCallEdge":
                TaskCallEdge taskCallEdge = (TaskCallEdge) edge;
                return new EdgeMessage("TaskCallEdge", Long.toString(taskCallEdge.getDelayTime()));
            case "FunctionCallEdge":
                return new EdgeMessage("FunctionCallEdge", null);
            default:
                throw Sugar.makeThrow("Edge type %s conversion is not supported", edge.getClass().getName());
        }
    }

}
