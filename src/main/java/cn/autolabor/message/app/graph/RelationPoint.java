package cn.autolabor.message.app.graph;

import cn.autolabor.util.autobuf.SerializableMessage;

import java.util.List;

public class RelationPoint implements SerializableMessage {

    private int id;
    private Point point;
    private List<Integer> relation;
    private Integer colorR;
    private Integer colorG;
    private Integer colorB;

    public RelationPoint() {
    }

    public RelationPoint(int id, Point point, List<Integer> relation, Integer colorR, Integer colorG, Integer colorB) {
        this.id = id;
        this.point = point;
        this.relation = relation;
        this.colorR = colorR;
        this.colorG = colorG;
        this.colorB = colorB;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public List<Integer> getRelation() {
        return relation;
    }

    public void setRelation(List<Integer> relation) {
        this.relation = relation;
    }

    public Integer getColorR() {
        return colorR;
    }

    public void setColorR(Integer colorR) {
        this.colorR = colorR;
    }

    public Integer getColorG() {
        return colorG;
    }

    public void setColorG(Integer colorG) {
        this.colorG = colorG;
    }

    public Integer getColorB() {
        return colorB;
    }

    public void setColorB(Integer colorB) {
        this.colorB = colorB;
    }

    @Override
    public String toString() {
        return "RelationPoint{" +
                "id=" + id +
                ", point=" + point +
                ", relation=" + relation +
                ", colorR=" + colorR +
                ", colorG=" + colorG +
                ", colorB=" + colorB +
                '}';
    }
}
