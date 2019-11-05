package com.utils;

import com.sun.org.apache.bcel.internal.util.ClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class PropertiesUtil {
    private static Logger log = LoggerFactory.getLogger(PropertiesUtil.class);

    private static Map<String, String> map = new HashMap<>();
    private static List<String> truses = new LinkedList<>();

    static {
        Properties prop = new Properties();
        try {
            log.info("start load param with file");
            FileInputStream in = new FileInputStream("param.properties");
            prop.load(in);
            load(prop);
        } catch (IOException e) {
            log.info("file not found");
            InputStream in = ClassLoader.getSystemResourceAsStream("param.properties");
            log.info("start load param with stream");
            try {
                prop.load(in);
                load(prop);
            } catch (IOException e1) {
                log.info("resource not found");
            }
        }
    }

    private static void load(Properties prop) {
        for (Map.Entry<Object, Object> e : prop.entrySet()) {
            map.put(e.getKey().toString(), e.getValue().toString());
            log.info("load [ " + e.getKey() + " : " + e.getValue() + " ]");
        }
        String strProp = getStrProp("thrust.host");
        String[] split = strProp.split(",");
        truses.addAll(Arrays.asList(split));
    }


    public static Integer getIntProp(String key) {
        String o = map.get(key);
        return Integer.valueOf(o);
    }

    public static String getStrProp(String key) {
        String o = map.get(key);
        return o;
    }

    public static boolean getBooleanProp(String key) {
        String s = map.get(key);
        return Boolean.valueOf(s);
    }

    public static List<String> getThrustHosts() {
        return truses;
    }

}
