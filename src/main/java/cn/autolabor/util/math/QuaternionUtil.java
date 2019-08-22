package cn.autolabor.util.math;

import cn.autolabor.message.common.Quaternion;

import static java.lang.Math.*;

public class QuaternionUtil {

    public static Quaternion fromAngles(double roll, double pitch, double yaw) {
        Quaternion q = new Quaternion();
        final double hr = roll * 0.5;
        final double shr = sin(hr);
        final double chr = cos(hr);
        final double hp = pitch * 0.5;
        final double shp = sin(hp);
        final double chp = cos(hp);
        final double hy = yaw * 0.5;
        final double shy = sin(hy);
        final double chy = cos(hy);
        final double chy_shp = chy * shp;
        final double shy_chp = shy * chp;
        final double chy_chp = chy * chp;
        final double shy_shp = shy * shp;

        q.setX((chy_chp * shr) - (shy_shp * chr)); // cos(yaw/2) * cos(pitch/2) * sin(roll/2) - sin(yaw/2) * sin(pitch/2) * cos(roll/2)
        q.setY((chy_shp * chr) + (shy_chp * shr)); // cos(yaw/2) * sin(pitch/2) * cos(roll/2) + sin(yaw/2) * cos(pitch/2) * sin(roll/2)
        q.setZ((shy_chp * chr) - (chy_shp * shr)); // sin(yaw/2) * cos(pitch/2) * cos(roll/2) - cos(yaw/2) * sin(pitch/2) * sin(roll/2)
        q.setW((chy_chp * chr) + (shy_shp * shr)); // cos(yaw/2) * cos(pitch/2) * cos(roll/2) + sin(yaw/2) * sin(pitch/2) * sin(roll/2)

        return q;
    }

    public static double length2(Quaternion q) {
        double x = q.getX();
        double y = q.getY();
        double z = q.getZ();
        double w = q.getW();
        return x * x + y * y + z * z + w * w;
    }

    public static Quaternion normalize(Quaternion q, boolean newCopy) {
        double len = sqrt(length2(q));
        if (newCopy) {
            return new Quaternion(q.getX() / len, q.getY() / len, q.getZ() / len, q.getW() / len);
        } else {
            q.setX(q.getX() / len);
            q.setY(q.getY() / len);
            q.setZ(q.getZ() / len);
            q.setW(q.getW() / len);
            return q;
        }
    }

    public static Quaternion fromYawRad(double yaw) {
        final double hy = yaw * 0.5;
        return (new Quaternion(0, 0, sin(hy), cos(hy)));
    }


    public static double getYawRad(Quaternion q) {
        Quaternion norQ = normalize(q, true);
        final double x = norQ.getX();
        final double y = norQ.getY();
        final double z = norQ.getZ();
        final double w = norQ.getW();
        return atan2(2 * (w * z + y * x), 1 - 2 * (y * y + z * z));
    }

    public static double getRollRad(Quaternion q) {
        Quaternion norQ = normalize(q, true);
        final double x = norQ.getX();
        final double y = norQ.getY();
        final double z = norQ.getZ();
        final double w = norQ.getW();
        return atan2(2 * (w * x + y * z), 1 - 2 * (x * x + y * y));
    }

    public static double getPitchRad(Quaternion q) {
        Quaternion norQ = normalize(q, true);
        final double x = norQ.getX();
        final double y = norQ.getY();
        final double z = norQ.getZ();
        final double w = norQ.getW();
        return asin(2 * (w * y - z * x));
    }

}
