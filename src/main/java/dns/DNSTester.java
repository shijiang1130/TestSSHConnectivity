package dns;

import network.HostResolver;
import connection.ConnectionManager;

public class DNSTester {
    public static boolean testDNSServer(String dnsServer) {
        try {
            System.out.println("Testing DNS server: " + dnsServer + "...");
            
            // First try to resolve google.com as a basic connectivity test
            System.out.println("Testing DNS resolution with google.com...");
            String googleIp = HostResolver.resolveHostname("google.com", dnsServer);
            if (googleIp == null) {
                System.err.println("ERROR: Could not resolve google.com using DNS server " + dnsServer);
                return false;
            }
            System.out.println("Successfully resolved google.com to " + googleIp);

            // Now try to resolve the DNS server's FQDN
            String dnsServerFQDN = dnsServer + ".lookdata.cn";
            System.out.println("Testing DNS resolution with " + dnsServerFQDN);
            String resolvedIp = HostResolver.resolveHostname(dnsServerFQDN, dnsServer);
            if (resolvedIp == null) {
                System.err.println("ERROR: Could not resolve DNS server FQDN: " + dnsServerFQDN);
                return false;
            }
            
            boolean pingSuccess = ConnectionManager.pingHost(resolvedIp);
            if (!pingSuccess) {
                System.err.println("ERROR: DNS server is not responding to connection attempts");
                return false;
            }
            
            System.out.println("DNS server " + dnsServerFQDN + " (" + resolvedIp + ") is working properly");
            return true;
        } catch (Exception e) {
            System.err.println("ERROR: Failed to test DNS server: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
} 
