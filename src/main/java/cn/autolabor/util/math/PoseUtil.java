package cn.autolabor.util.math;

import cn.autolabor.message.common.Vector3;

import java.util.List;

import static java.lang.Math.atan2;
import static java.lang.Math.sqrt;

public class PoseUtil {

    public static double norm(Vector3 p) {
        final double x = p.getX();
        final double y = p.getY();
        final double z = p.getZ();
        return sqrt(x * x + y * y + z * z);
    }

    public static Vector3 minus(Vector3 from, Vector3 to) {
        return new Vector3(from.getX() - to.getX(), from.getY() - to.getY(), from.getZ() - from.getZ());
    }

    public static double diffSumSquare(Vector3 from, Vector3 to) {
        final double diffX = from.getX() - to.getX();
        final double diffY = from.getY() - to.getY();
        final double diffZ = from.getZ() - to.getZ();
        return diffX * diffX + diffY * diffY + diffZ * diffZ;
    }

    public static double pointDistance(Vector3 from, Vector3 to) {
        return sqrt(diffSumSquare(from, to));
    }

    public static double getYawRad(Vector3 from, Vector3 to) {
        return getYawRad(minus(from, to));
    }

    public static double getYawRad(Vector3 p) {
        return atan2(p.getY(), p.getX());
    }

    public static int getNearestIndex(Vector3 from, List<Vector3> points) {
        int index = -1;
        double tmp, dis = Double.MAX_VALUE;
        for (int i = 0; i < points.size(); i++) {
            tmp = diffSumSquare(points.get(i), from);
            if (dis < tmp) {
                index = i;
                dis = tmp;
            }
        }
        return index;
    }
}
