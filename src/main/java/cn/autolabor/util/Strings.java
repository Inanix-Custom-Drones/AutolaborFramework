package cn.autolabor.util;

import cn.autolabor.core.server.statistics.TaskEventVertex;
import cn.autolabor.util.json.Json;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Strings {

    private static final char[] hexChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final char[] binChar = {'0', '1'};
    private static String[] chars = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    private static Pattern isInt = Pattern.compile("^-?[0-9]\\d*$");
    private static Pattern isDouble = Pattern.compile("^([-+])?\\d+(\\.\\d+)?$");

    private static Pattern linePattern = Pattern.compile("_(\\w)");
    private static Pattern humpPattern = Pattern.compile("[A-Z]");


    /***
     *  判断字符序列是否为空
     *
     * @param cs
     * @return
     */
    public static boolean isBlank(CharSequence cs) {
        if (null == cs)
            return true;
        int length = cs.length();
        for (int i = 0; i < length; i++) {
            if (!(Character.isWhitespace(cs.charAt(i))))
                return false;
        }
        return true;
    }

    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    /***
     *  生成短唯一字符串序列 8个字符
     *
     * @return
     */
    public static String getShortUUID() {
        StringBuilder stringBuilder = new StringBuilder();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 8; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            stringBuilder.append(chars[x % 0x3E]);
        }
        return stringBuilder.toString();
    }

    /***
     *  生成重复字符串
     *
     * @param str
     * @param number
     * @return
     */
    public static String repeat(String str, int number) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < number; i++) {
            stringBuilder.append(str);
        }
        return stringBuilder.toString();
    }

    public static boolean isInteger(String str) {
        return isInt.matcher(str).matches();
    }

    public static boolean isDouble(String str) {
        return isDouble.matcher(str).matches();
    }

    public static boolean isNumber(String str) {
        return isInteger(str) || isDouble(str);
    }

    @SuppressWarnings("unchecked")
    public static Object fromString(String str, Class clazz) {
        if (clazz.equals(String.class)) {
            return str;
        } else if (clazz.equals(Integer.class) || clazz.equals(int.class)) {
            return Integer.parseInt(str);
        } else if (clazz.equals(Double.class) || clazz.equals(double.class)) {
            return Double.parseDouble(str);
        } else if (clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
            return Boolean.parseBoolean(str);
        } else if (clazz.isAssignableFrom(List.class)) {
            return Json.fromJson(str).getListRaw();
        } else if (clazz.isAssignableFrom(Map.class)) {
            return Json.fromJson(str).getMapRaw();
        } else {
            throw Sugar.makeThrow("%s can't convert to %s", str, clazz.getName());
        }
    }

    public static String bytesToHexString(byte... bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(hexChar[b >>> 4 & 0xf]);
            sb.append(hexChar[b & 0xf]);
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String formatByteToHexString(byte[] bytes, String prefix, String suffix, int foldLength) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            sb.append(prefix);
            sb.append(hexChar[b >>> 4 & 0xf]);
            sb.append(hexChar[b & 0xf]);
            sb.append(suffix);
            if (foldLength > 0 && ((i + 1) % foldLength == 0)) {
                sb.append("\n");
            }

        }
        return sb.toString();
    }

    public static String bytesToBinString(byte... bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            for (int i = 7; i >= 0; i--) {
                sb.append(binChar[b >>> i & 0x01]);
            }
            sb.append(" ");
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public static String mapToString(Map map) {
        StringBuilder sb = new StringBuilder();
        map.forEach((key, value) -> {
            sb.append(String.format("%-80s --> %s\n", key, value));
        });
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public static String mapToString(Map map, int distance) {
        String disStr = Integer.toString(distance);
        StringBuilder sb = new StringBuilder();
        map.forEach((Object key, Object value) -> {
            sb.append(String.format("%-" + disStr + "s --> %s\n", key, value));
        });
        return sb.toString();
    }

    public static String objectToString(Object data) {
        return objectToString(data, 0);
    }

    private static String objectToString(Object data, int indent) {
        String indentStr = Strings.repeat("\t", indent);
        StringBuilder sb = new StringBuilder();

        if (data instanceof Object[]) {
            Object[] objects = (Object[]) data;
            sb.append(indentStr).append("[").append("\n");
            for (int i = 0; i < objects.length; i++) {
                sb.append(objectToString(objects[i], indent + 1));
                if (i != objects.length - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
            sb.append(indentStr).append("]");
        } else {
            sb.append(indentStr).append(data.toString());
        }
        return sb.toString();
    }

    public static String lastClassName(String className) {
        int index = className.lastIndexOf(".");
        if (index > 0) {
            return className.substring(index + 1);
        } else {
            throw Sugar.makeThrow("Class name format error");
        }
    }

    public static String join(List<String> list, boolean reverse) {
        StringBuilder sb = new StringBuilder();
        if (reverse) {
            for (int i = list.size() - 1; i >= 0; i--) {
                sb.append(list.get(i));
            }
        } else {
            list.forEach(sb::append);
        }
        return sb.toString();
    }

    /**
     * 蛇形转驼峰
     *
     * @param str 待转换字符串
     * @return 转换结果
     */
    public static String line2Hump(String str) {
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return upperFirst(sb.toString());
    }

    /**
     * 驼峰转蛇形
     *
     * @param str 待转换字符串
     * @return 转换结果
     */
    public static String hump2Line(String str) {
        str = lowerFirst(str);
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 将字符串首字母大写
     *
     * @param s 字符串
     * @return 首字母大写后的新字符串
     */
    public static String upperFirst(CharSequence s) {
        if (null == s)
            return null;
        int len = s.length();
        if (len == 0)
            return "";
        char c = s.charAt(0);
        if (Character.isUpperCase(c))
            return s.toString();
        return new StringBuilder(len).append(Character.toUpperCase(c)).append(s.subSequence(1, len)).toString();
    }

    /**
     * 将字符串首字母小写
     *
     * @param s 字符串
     * @return 首字母小写后的新字符串
     */
    public static String lowerFirst(CharSequence s) {
        if (null == s)
            return null;
        int len = s.length();
        if (len == 0)
            return "";
        char c = s.charAt(0);
        if (Character.isLowerCase(c))
            return s.toString();
        return new StringBuilder(len).append(Character.toLowerCase(c)).append(s.subSequence(1, len)).toString();
    }

    /**
     * "cn.fh.lightning" -> "cn/fh/lightning"
     */
    public static String dotToSplash(String name) {
        return name.replaceAll("\\.", "/");
    }

    /**
     * "cn/fh/lightning" -> "cn.fh.lightning"
     */
    public static String splashToDot(String name) {
        return name.replaceAll("/", ".");
    }

    /**
     * "Apple.class" -> "Apple"
     */
    public static String trimExtension(String name) {
        int pos = name.lastIndexOf('.');
        if (-1 != pos) {
            return name.substring(0, pos);
        }

        return name;
    }

    /**
     * /application/home -> /home
     */
    public static String trimURI(String uri) {
        String trimmed = uri.substring(1);
        int splashIndex = trimmed.indexOf('/');
        return trimmed.substring(splashIndex);
    }

    public static int countSubString(String totalStr, String subStr) {
        int count = 0;
        int index = -1;
        while (true) {
            index = totalStr.indexOf(subStr);
            if (index < 0) {
                break;
            }
            count = count + 1;
            totalStr = totalStr.substring(index + subStr.length(), totalStr.length());
        }
        return count;
    }

    public static String timeString(String format){
        java.util.Date d=new java.util.Date();
        SimpleDateFormat s= new SimpleDateFormat(format);
        return s.format(d);
    }


    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder("asdwa::awdwa::awd");
        System.out.println(countSubString(sb.toString(), "::"));

    }


}
