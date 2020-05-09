package com.iwscream.demo.util;

import java.io.*;
import java.util.Properties;

public class PropertyReader {

    public String get(String filename, String propertyKey){
        try {
            Properties properties = new Properties();
            // 使用ClassLoader加载properties配置文件生成对应的输入流
            InputStream in = PropertyReader.class.getClassLoader().getResourceAsStream(filename);
            // 使用properties对象加载输入流
            properties.load(in);
            //获取key对应的value值
            return properties.getProperty(propertyKey);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
