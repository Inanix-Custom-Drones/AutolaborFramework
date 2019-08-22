package cn.autolabor.module.communication;

import cn.autolabor.util.Sugar;
import cn.autolabor.util.autobuf.*;
import cn.autolabor.util.reflect.TypeNode;

public class TCPResponse {

    private TCPRespStatusType status;
    private Object result;

    public TCPResponse(TCPRespStatusType status, Object result) {
        this.status = status;
        this.result = result;
    }

    public TCPResponse() {
    }

    public TCPRespStatusType getStatus() {
        return status;
    }

    public void setStatus(TCPRespStatusType status) {
        this.status = status;
    }

    public Object getResult() {
        return result;
    }

    public <T> T getResult(Class<T> tClass) {
        return (T) getResult(new TypeNode(tClass));
    }

    public Object getResult(TypeNode typeNode) {
        if (Sugar.checkInherit(result.getClass(), AutoBufObject.class)) {
            return getRawData(typeNode);
        } else {
            return result;
        }
    }

    public void setResult(Object result) {
        this.result = result;
    }

    private Object getRawData(TypeNode typeNode) {
        if (result != null) {
            if (result.getClass().equals(AutoBufArray.class)) {
                return ((AutoBufArray) result).toRawData(typeNode);
            } else if (result.getClass().equals(AutoBufList.class)) {
                return ((AutoBufList) result).toRawData(typeNode);
            } else if (result.getClass().equals(AutoBufMap.class)) {
                return ((AutoBufMap) result).toRawData(typeNode);
            } else if (result.getClass().equals(AutoBufEmbedded.class)) {
                return ((AutoBufEmbedded) result).toRawData(typeNode);
            } else {
                return ((AutoBufObject) result).toRawData(typeNode);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "TCPResponse{" +
                "status=" + status +
                ", result=" + result +
                '}';
    }
}
