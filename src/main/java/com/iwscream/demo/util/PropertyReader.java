package com.iwscream.demo.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class PropertyReader {

    public String get(String filename, String propertyKey){
        try {
            InputStream in = this.getClass().getResourceAsStream(filename);
            Properties properties = new Properties();
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            properties.load(inputStreamReader);

            return properties.getProperty(propertyKey);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
