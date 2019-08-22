package cn.autolabor.util.collections.graph;

import cn.autolabor.util.collections.multivaluemap.LinkedMultiValueMap;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiFunction;

public class VertexContainer {

    private Integer index;
    private Vertex userObject;
    private LinkedMultiValueMap<Integer, Edge> edgeInfos = new LinkedMultiValueMap<>();

    public static VertexContainer createTmp(Vertex userObject) {
        return new VertexContainer(userObject);
    }

    public VertexContainer(Vertex userObject) {
        this.userObject = userObject;
    }

    public VertexContainer(int index, Vertex userObject) {
        this.index = index;
        this.userObject = userObject;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public void addEdge(Integer toIndex, Edge edgeInfo) {
        edgeInfos.put(toIndex, edgeInfo);
    }

    public void mergeEdge(Integer toIndex, Edge edgeInfo, BiFunction<Set<Edge>, Edge, Edge> mergeFun) {
        edgeInfos.put(toIndex, mergeFun.apply(edgeInfos.getValues(toIndex), edgeInfo));
    }

    public void addEdges(Integer toIndex, Collection<Edge> edges) {
        for (Edge edge : edges) {
            addEdge(toIndex, edge);
        }
    }

    public void removeEdge(Integer toIndex, Edge edgeInfo) {
        edgeInfos.removeValue(toIndex, edgeInfo);
    }

    public void removeEdge(Integer toIndex) {
        edgeInfos.removeValues(toIndex);
    }

    public void removeEdge(Edge edgeInfo) {
        edgeInfos.removeValue(edgeInfo);
    }

    public Vertex getUserObject() {
        return userObject;
    }

    public void setUserObject(Vertex userObject) {
        this.userObject = userObject;
    }

    public LinkedMultiValueMap<Integer, Edge> getEdgeInfos() {
        return edgeInfos;
    }

    public void setEdgeInfos(LinkedMultiValueMap<Integer, Edge> edgeInfos) {
        this.edgeInfos = edgeInfos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        VertexContainer that = (VertexContainer) o;

        return userObject.equals(that.userObject);
    }

    @Override
    public int hashCode() {
        return userObject.hashCode();
    }

    @Override
    public String toString() {
        return "VertexContainer{" + "index=" + index + ", userObject=" + userObject + ", edgeInfos=" + edgeInfos + '}';
    }
}
