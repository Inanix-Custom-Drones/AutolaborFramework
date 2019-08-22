package cn.autolabor.core.server.executor;

public class QueueIndexData {

    private ItemType itemType;
    private int index;
    public QueueIndexData(ItemType itemType, int index) {
        this.itemType = itemType;
        this.index = index;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public enum ItemType {TIMEOUT, NON_TIMEOUT, NONE}
}
