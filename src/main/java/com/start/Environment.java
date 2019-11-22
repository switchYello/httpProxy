package com.start;

import com.utils.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * 所有配置
 */
public class Environment {

    private static Integer localPort;
    private static String remoteHost;
    private static Integer remotePort;

    static {
        try (InputStream resourceAsStream = ResourceManager.gerResourceForFile("param.properties")) {
            Objects.requireNonNull(resourceAsStream, "未发现配置文件: param.properties");
            Properties properties = new Properties();
            properties.load(resourceAsStream);
            loadData(properties);
            check();
        } catch (IOException e) {
            throw new RuntimeException("读取配置文件异常", e);
        }
    }

    private static void loadData(Properties properties) {
        remoteHost = properties.getProperty("remoteHost");
        String remotePortProperties = properties.getProperty("remotePort");
        if (remotePortProperties != null) {
            remotePort = Integer.valueOf(remotePortProperties);
        }
        String localPortProperties = properties.getProperty("localPort");
        if (localPortProperties != null) {
            localPort = Integer.valueOf(localPortProperties);
        }
    }

    private static void check() {
        Objects.requireNonNull(localPort, "未知localPort");
        Objects.requireNonNull(remotePort, "未知remotePort");
        Objects.requireNonNull(remoteHost, "未知remoteHost");
    }

    public static Integer getLocalPort() {
        return localPort;
    }

    public static String getRemoteHost() {
        return remoteHost;
    }

    public static Integer getRemotePort() {
        return remotePort;
    }
}
