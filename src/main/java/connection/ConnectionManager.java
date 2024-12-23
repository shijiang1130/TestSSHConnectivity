package connection;

import java.net.Socket;
import java.net.InetSocketAddress;
import java.io.IOException;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import utils.HostInfo;

public class ConnectionManager {
    private static final int SSH_PORT = 22;
    private static final int RDP_PORT = 3389;
    private static final int TIMEOUT = 5000; // 5 seconds

    public static boolean pingHost(String ip) {
        // Try common ports in order: SSH (22), RDP (3389), HTTP (80), HTTPS (443)
        int[] portsToTry = {22, 3389};
        
        for (int port : portsToTry) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(ip, port), 3000);
                System.out.println("Successfully connected to " + ip + " on port " + port);
                return true;
            } catch (IOException e) {
                System.out.println("Port " + port + " is not accessible on " + ip);
            }
        }
        
        System.out.println("No common ports are accessible on " + ip);
        return false;
    }

    public static Session getSession(HostInfo hostInfo) throws JSchException {
        JSch jsch = new JSch();
        Session session = null;

        try {
            // Debugging output
            System.out.println("Connect to host: " + hostInfo.getHostname() + hostInfo.getJumpUser() + hostInfo.getipAddr() + hostInfo.getJumpPassword());

            session = jsch.getSession(hostInfo.getJumpUser(), hostInfo.getipAddr(), 22);
            session.setPassword(hostInfo.getJumpPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("PreferredAuthentications","password,keyboard-interactive");

            // Debugging output before connecting
            System.out.println("Session configuration set. Attempting to connect...");
            session.connect();
            System.out.println("Connection established successfully.");

        } catch (JSchException e) {
            // Log the error with details
            System.err.println("Failed to connect to " + hostInfo.getHostname() + ": " + e.getMessage());
            throw e; // Rethrow the exception after logging
        }

        return session;
    }
} 
