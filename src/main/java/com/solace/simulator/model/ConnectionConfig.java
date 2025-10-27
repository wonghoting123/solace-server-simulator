package com.solace.simulator.model;

import java.util.Map;

public class ConnectionConfig {
    private String host;
    private String vpnName;
    private String username;
    private String password;
    private String port;

    public ConnectionConfig() {
    }

    public ConnectionConfig(String host, String vpnName, String username, String password, String port) {
        this.host = host;
        this.vpnName = vpnName;
        this.username = username;
        this.password = password;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getVpnName() {
        return vpnName;
    }

    public void setVpnName(String vpnName) {
        this.vpnName = vpnName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
