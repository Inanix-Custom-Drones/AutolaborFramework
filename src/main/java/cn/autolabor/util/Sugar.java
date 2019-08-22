package cn.autolabor.util;


import cn.autolabor.util.reflect.Reflects;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.*;

public class Sugar {

    /**
     * 根据格式化字符串，生成运行时异常
     *
     * @param format 格式
     * @param args   参数
     * @return 运行时异常
     */
    public static RuntimeException makeThrow(String format, Object... args) {
        return new RuntimeException(String.format(format, args));
    }

    /***
     * 判断变量是否为空
     *
     * @param obj
     */
    public static void checkNull(Object obj) {
        if (null == obj) {
            throw new NullPointerException();
        }
    }

    /***
     * 判断两个类是否具有继承关系
     *
     * @param subClass    子类
     * @param superClass  父类
     * @return
     */
    public static boolean checkInherit(Class<?> subClass, Class<?> superClass) {
        return superClass.isAssignableFrom(subClass);
    }

    public static void showAllNetworkInterface() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface interf = interfaces.nextElement();
                System.out.println(String.format("name : %-10s isUp : %-6s isLoopback : %-6s supportMulticast : %-6s p2p : %-6s", interf.getName(), interf.isUp(), interf.isLoopback(), interf.supportsMulticast(), interf.isPointToPoint()));

                Enumeration<InetAddress> addrList = interf.getInetAddresses();
                if (!addrList.hasMoreElements()) {
                    System.out.println("\t(No addresses for this interface)");
                }
                while (addrList.hasMoreElements()) {
                    InetAddress address = addrList.nextElement();
                    System.out.print("\tAddress " + ((address instanceof Inet4Address ? "(v4)" : (address instanceof Inet6Address ? "(v6)" : "(?)"))));
                    System.out.println(": " + address.getHostAddress());
                }
                System.out.println();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }


    public static NetworkInterface getDefaultNetworkInterface() {
        List<NetworkInterface> supportedIface = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isUp() && iface.supportsMulticast()) {
                    supportedIface.add(iface);
                }
            }

            if (supportedIface.size() > 0) {

                for (NetworkInterface siface : supportedIface) {
                    //无线
                    if (siface.getName().startsWith("wl") || siface.getName().startsWith("ww")) {
                        return siface;
                    }
                }

                for (NetworkInterface siface : supportedIface) {
                    //有线
                    if (siface.getName().startsWith("eth") || siface.getName().startsWith("en")) {
                        return siface;
                    }
                }


                return supportedIface.get(0);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static NetworkInterface tryGetNetworkInterface(String networkInterfaceName) {
        NetworkInterface networkInterface = null;
        try {
            if (Strings.isNotBlank(networkInterfaceName)) {
                networkInterface = NetworkInterface.getByName(networkInterfaceName);
            }

            if (networkInterface == null || (!(networkInterface.isUp() && networkInterface.supportsMulticast()))) {
                networkInterface = Sugar.getDefaultNetworkInterface();
            }
            return networkInterface;
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InetAddress getIpv4Address(NetworkInterface iface) {
        if (iface != null) {
            Enumeration<InetAddress> enumeration = iface.getInetAddresses();
            while (enumeration.hasMoreElements()) {
                InetAddress address = enumeration.nextElement();
                if (address != null && address instanceof Inet4Address) {
                    return address;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static Map createMapByType(Class mapClass) {
        if (Reflects.isMap(mapClass)) {
            try {
                if (mapClass.isInterface()) {
                    return new HashMap();
                } else {
                    Constructor constructor = mapClass.getConstructor();
                    return (Map) constructor.newInstance();
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                throw Sugar.makeThrow("Unable to create map");
            }
        } else {
            throw Sugar.makeThrow("Type is not a map");
        }
    }

    @SuppressWarnings("unchecked")
    public static List createListByType(Class mapClass) {
        if (Reflects.isList(mapClass)) {
            try {
                if (mapClass.isInterface()) {
                    return new ArrayList();
                } else {
                    Constructor constructor = mapClass.getConstructor();
                    return (List) constructor.newInstance();
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                throw Sugar.makeThrow("Unable to create list");
            }
        } else {
            throw Sugar.makeThrow("Type is not a list");
        }
    }

    public static void printStack() {
        StackTraceElement[] stackInfo = Thread.currentThread().getStackTrace();
        StringBuilder sb = new StringBuilder();
        Arrays.stream(stackInfo).forEach(stackTraceElement -> sb.append(stackTraceElement.toString()).append("\n"));
        System.out.println(sb.toString());
    }

    public static void main(String[] args) {
        System.out.println(createListByType(List.class).getClass());
    }
}
