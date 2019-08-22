package cn.autolabor.util.math;

public class AngleUtil {

    private final static double PI2 = Math.PI * 2;

    // [-PI, PI)
    public static double standardRadians(double rad) {
        if (rad >= Math.PI) {
            return standardRadians(rad - PI2);
        } else if (rad < -Math.PI) {
            return standardRadians(rad + PI2);
        } else {
            return rad;
        }
    }
}
