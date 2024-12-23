package utils;

public class HostInfo {
    private String hostname;
    private String jumpUser = "jumpuser";
    private String jumpPassword;
    private String rootPassword;
    private String ipAddr;

    public HostInfo(String hostname, String jumpPassword, String rootPassword) {
        this.hostname = hostname;
        this.jumpUser = jumpUser;
        this.jumpPassword = jumpPassword;
        this.rootPassword = rootPassword;
    }

    public String getHostname() {
        return hostname;
    }

    public String getJumpUser() {
        return jumpUser;
    }

    public String getJumpPassword() {
        return jumpPassword;
    }

    public String getRootPassword() {
        return rootPassword;
    }

    public String setipAddr(String ipAddr) {
        return this.ipAddr = ipAddr;
    }

    public String getipAddr() {
        return ipAddr;
    }   
}   
