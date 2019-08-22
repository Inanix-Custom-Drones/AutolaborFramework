package cn.autolabor.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Files {

    public static String getContents(String fileName) {
        File file = new File(fileName);
        return getContents(file);
    }

    public static String getContents(File file) {
        Long fileLength = file.length();
        byte[] fileContent = new byte[fileLength.intValue()];
        FileInputStream in;
        try {
            in = new FileInputStream(file);
            in.read(fileContent);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(fileContent);
    }

    public static void main(String[] args) {
        String filePath = ClassLoader.getSystemResource(".").getPath();
        System.out.println(filePath);
    }
}
