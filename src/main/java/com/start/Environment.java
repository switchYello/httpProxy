package com.start;

import com.utils.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * 所有配置
 */
public class Environment {

    private static final Logger log = LoggerFactory.getLogger(Environment.class);

    private Integer localPort;
    private String remoteHost;
    private Integer remotePort;

    public Environment(Properties properties) {
        loadData(properties);
        check();
    }

    public Environment(String fileName) {
        try (InputStream resourceAsStream = ResourceManager.gerResourceForFile(fileName)) {
            Objects.requireNonNull(resourceAsStream, "未发现配置文件:" + fileName);
            Properties properties = new Properties();
            properties.load(resourceAsStream);
            loadData(properties);
            check();
        } catch (IOException e) {
            throw new RuntimeException("读取配置文件异常", e);
        }
    }

    private void loadData(Properties properties) {
        remoteHost = properties.getProperty("remoteHost");
        String remotePort = properties.getProperty("remotePort");
        if (remotePort != null) {
            this.remotePort = Integer.valueOf(remotePort);
        }
        String localPort = properties.getProperty("localPort");
        if (localPort != null) {
            this.localPort = Integer.valueOf(localPort);
        }
    }

    private void check() {
        Objects.requireNonNull(localPort, "未知localPort");
        Objects.requireNonNull(remotePort, "未知remotePort");
        Objects.requireNonNull(remoteHost, "未知remoteHost");
    }


    public Integer getLocalPort() {
        return localPort;
    }

    public Environment setLocalPort(Integer localPort) {
        this.localPort = localPort;
        return this;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public Environment setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
        return this;
    }

    public Integer getRemotePort() {
        return remotePort;
    }

    public Environment setRemotePort(Integer remotePort) {
        this.remotePort = remotePort;
        return this;
    }

}
