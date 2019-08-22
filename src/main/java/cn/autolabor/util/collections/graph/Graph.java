package cn.autolabor.util.collections.graph;

import cn.autolabor.util.Strings;
import cn.autolabor.util.Sugar;
import cn.autolabor.util.collections.DuplexMap;
import cn.autolabor.util.collections.multivaluemap.LinkedMultiValueMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Graph {

    private AtomicInteger indexBuilder = new AtomicInteger(0);
    private ReentrantLock lock = new ReentrantLock();
    protected DuplexMap<Integer, VertexContainer> vertexMap = new DuplexMap<>();

    public void merge(Graph graph) {
        lock.lock();
        graph.lock.lock();
        try {
            for (VertexContainer container : graph.vertexMap.values()) {
                VertexContainer sourceContainer = addVertex(container.getUserObject());
                LinkedMultiValueMap<Integer, Edge> edgeInfos = container.getEdgeInfos();
                for (Map.Entry<Integer, Set<Edge>> entry : edgeInfos.multiEntrySet()) {
                    VertexContainer targetContainer = addVertex(graph.getVertexUserObject(entry.getKey()));
                    add(sourceContainer, targetContainer, entry.getValue());
                }
            }
        } finally {
            graph.lock.unlock();
            lock.unlock();
        }
    }

    public Vertex getVertexUserObject(Integer index) {
        if (vertexMap.contains(index)) {
            return vertexMap.get(index).getUserObject();
        }
        return null;
    }

    public VertexContainer addVertex(Vertex vertex) {
        lock.lock();
        try {
            VertexContainer container = vertexMap.getValueByValue(VertexContainer.createTmp(vertex));
            if (container == null) {
                container = createVertex(vertex);
            }
            return container;
        } finally {
            lock.unlock();
        }
    }

    public void add(VertexContainer source, VertexContainer target, Edge edge) {
        lock.lock();
        try {
            if (source.getIndex() == null) {
                throw Sugar.makeThrow("vertexContainer %s does not have index information", source.toString());
            }

            if (target.getIndex() == null) {
                throw Sugar.makeThrow("vertexContainer %s does not have index information", target.toString());
            }

            unsafeAdd(source, target, edge);
        } finally {
            lock.unlock();
        }
    }

    public void add(VertexContainer source, VertexContainer target, Collection<Edge> edges) {
        lock.lock();
        try {
            if (source.getIndex() == null) {
                throw Sugar.makeThrow("vertexContainer %s does not have index information", source.toString());
            }

            if (target.getIndex() == null) {
                throw Sugar.makeThrow("vertexContainer %s does not have index information", target.toString());
            }

            edges.forEach(edge -> unsafeAdd(source, target, edge));
        } finally {
            lock.unlock();
        }
    }

    protected void unsafeAdd(VertexContainer source, VertexContainer target, Edge edge) {
        source.addEdge(target.getIndex(), edge);
    }

    public void add(Vertex from, Vertex to, Edge edge) {
        lock.lock();
        try {
            VertexContainer source = vertexMap.getValueByValue(VertexContainer.createTmp(from));
            if (source == null) {
                source = createVertex(from);
            }
            VertexContainer target = vertexMap.getValueByValue(VertexContainer.createTmp(to));
            if (target == null) {
                target = createVertex(to);
            }
            this.add(source, target, edge);
        } finally {
            lock.unlock();
        }
    }

    public void removeVertex(Vertex vertex) {
        lock.lock();
        try {
            Integer key = vertexMap.getbyValue(VertexContainer.createTmp(vertex));
            if (key != null) {
                vertexMap.removeByKey(key);
                for (VertexContainer container : vertexMap.values()) {
                    container.removeEdge(key);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void removeEdge(Edge edge) {
        lock.lock();
        try {
            for (VertexContainer container : vertexMap.values()) {
                container.removeEdge(edge);
            }
        } finally {
            lock.unlock();
        }
    }

    public void removeEdge(Vertex from, Vertex to, Edge edge) {
        lock.lock();
        try {
            VertexContainer source = vertexMap.getValueByValue(VertexContainer.createTmp(from));
            if (source == null) {
                return;
            }
            VertexContainer target = vertexMap.getValueByValue(VertexContainer.createTmp(to));
            if (target == null) {
                return;
            }
            source.removeEdge(target.getIndex(), edge);
        } finally {
            lock.unlock();
        }
    }

    public void removeEdge(Vertex from, Vertex to) {
        lock.lock();
        try {
            VertexContainer source = vertexMap.getValueByValue(VertexContainer.createTmp(from));
            if (source == null) {
                return;
            }
            VertexContainer target = vertexMap.getValueByValue(VertexContainer.createTmp(to));
            if (target == null) {
                return;
            }
            source.removeEdge(target.getIndex());
        } finally {
            lock.unlock();
        }
    }

    public void beginEdit() {
        lock.lock();
    }

    public void endEdit() {
        lock.unlock();
    }

    @Override
    public String toString() {
        return Strings.mapToString(vertexMap, 5);
    }

    private VertexContainer createVertex(Vertex vertexInfo) {
        VertexContainer container = new VertexContainer(indexBuilder.getAndAdd(1), vertexInfo);
        vertexMap.put(container.getIndex(), container);
        return container;
    }
}
