package network;

import org.xbill.DNS.*;
import java.net.UnknownHostException;

public class HostResolver {
    private static final int DNS_TIMEOUT = 5000; // 5 seconds

    public static String resolveHostname(String hostname, String dnsServer) {
        try {
            System.out.println("Attempting to resolve hostname: " + hostname);
            System.out.println("Using DNS server: " + dnsServer);
            
            SimpleResolver resolver = new SimpleResolver(dnsServer);
            resolver.setTimeout(DNS_TIMEOUT);
            
            Name name = Name.fromString(hostname + ".");
            Record question = Record.newRecord(name, Type.A, DClass.IN);
            Message query = Message.newQuery(question);
            
            Message response = resolver.send(query);
            Record[] answers = response.getSectionArray(Section.ANSWER);
            
            if (answers.length > 0) {
                for (Record record : answers) {
                    if (record instanceof ARecord) {
                        String resolvedIp = ((ARecord) record).getAddress().getHostAddress();
                        System.out.println("Successfully resolved " + hostname + " to " + resolvedIp);
                        return resolvedIp;
                    }
                }
            }
            
            System.err.println("No A record found for " + hostname);
            return null;
        } catch (TextParseException e) {
            System.err.println("Invalid hostname format: " + hostname);
            e.printStackTrace();
            return null;
        } catch (UnknownHostException e) {
            System.err.println("DNS server not found: " + dnsServer);
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("DNS resolution failed for " + hostname);
            System.err.println("Error details: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
} 
