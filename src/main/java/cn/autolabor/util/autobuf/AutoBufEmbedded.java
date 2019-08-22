package cn.autolabor.util.autobuf;

import cn.autolabor.message.navigation.Msg2DOdometry;
import cn.autolabor.message.navigation.Msg2DPose;
import cn.autolabor.message.navigation.Msg2DTwist;
import cn.autolabor.util.Sugar;
import cn.autolabor.util.Unsafes;
import cn.autolabor.util.autobuf.annotation.IgnoreField;
import cn.autolabor.util.reflect.TypeNode;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class AutoBufEmbedded extends AutoBufObject {

    private String id;

    AutoBufEmbedded(String key, String id) {
        super(key, SchemaItem.DecodeType.EMBEDDED);
        this.data = new LinkedHashMap<String, AutoBufObject>();
        this.id = id;
    }

    AutoBufEmbedded(String key, SerializableMessage msg) {
        super(key, SchemaItem.DecodeType.EMBEDDED);
        this.data = new LinkedHashMap<>();
        this.id = msg.getClass().getName();
        fillMap(msg);
    }

    public static void main(String[] args) {
        Msg2DOdometry odom = new Msg2DOdometry();
        odom.setPose(new Msg2DPose(1, 2, 3));
        odom.setTwist(new Msg2DTwist(4, 5, 6));

        AutoBufEmbedded embedded = (AutoBufEmbedded) AutoBufBuilder.createFromObject(null, odom);
        System.out.println("object : " + embedded);

        Msg2DTwist pose = embedded.getRaw("twist", Msg2DTwist.class);
        System.out.println(pose);
    }

    private void fillMap(SerializableMessage msg) {
        Class msgClass = msg.getClass();
        Field[] fields = msgClass.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            if (f.getAnnotation(IgnoreField.class) != null) {
                continue;
            }
            try {
                Object value = f.get(msg);
                if (value != null) {
                    ((HashMap<String, AutoBufObject>) data).put(f.getName(), createObject(f.getName(), value, getDefaultDecodeTypeFromObject(value)));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public AutoBufEmbedded putRawData(String key, Object obj) {
        SchemaItem.DecodeType valueType = getDefaultDecodeTypeFromObject(obj);
        if (valueType != null) {
            ((HashMap<String, AutoBufObject>) data).put(key, createObject(key, obj, valueType));
            return this;
        } else {
            throw Sugar.makeThrow("Unable to put %s type variable", obj.getClass());
        }
    }

    public AutoBufEmbedded put(String key, AutoBufObject autoBufObject) {
        ((HashMap<String, AutoBufObject>) data).put(key, autoBufObject);
        return this;
    }

    public String getId() {
        return id;
    }

    @Override
    public Object toRawData(TypeNode typeNode) {
        if (typeNode.getType() instanceof Class) {
            Class classType = (Class) typeNode.getType();
            if (Sugar.checkInherit(classType, SerializableMessage.class)) {
                Object msg = null;
                try {
                    msg = Unsafes.allocateInstance(classType);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }

                Map<String, AutoBufObject> map = (Map<String, AutoBufObject>) data;
                for (Map.Entry<String, AutoBufObject> entry : map.entrySet()) {
                    try {
                        Field f = classType.getDeclaredField(entry.getKey());
                        if (f != null) {
                            f.setAccessible(true);
                            f.set(msg, entry.getValue().toRawData(new TypeNode(f.getGenericType())));
                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                return msg;
            }
        }
        throw Sugar.makeThrow("Data type does not match");
    }

    public AutoBufObject get(String key) {
        return ((Map<String, AutoBufObject>) data).get(key);
    }

    public <T> T getRaw(String key, Class<T> tClass) {
        return (T) getRaw(key, new TypeNode(tClass));
    }

    public Object getRaw(String key, TypeNode typeNode) {
        AutoBufObject object = ((Map<String, AutoBufObject>) data).get(key);
        if (object != null) {
            return object.toRawData(typeNode);
        }
        return null;
    }

}
