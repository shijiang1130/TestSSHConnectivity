package connection;

import report.ReportWriter;
import utils.HostInfo;
import utils.ReportInfo;
import java.time.Duration;
import java.time.LocalDateTime;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.ChannelExec;
import net.sf.expectit.*;
import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.regexp;


public class SSHConnection implements ConnectionStrategy {
    private HostInfo hostInfo;
    private ReportInfo reportInfo;
    private String resolvedIp;
    private boolean performSSHCommands;
    
    // Define the missing variables
    private String errorDetails = ""; // Initialize as empty string
    private boolean isLinux = true; // Assuming default is true
    private Duration duration; // This will need to be set appropriately
    private static final boolean PERFORM_SSH_COMMANDS = true; // Assuming this is a constant

    public SSHConnection(HostInfo hostInfo, String resolvedIp, boolean performSSHCommands) {
        this.hostInfo = hostInfo;
        this.resolvedIp = resolvedIp;
        this.performSSHCommands = performSSHCommands;
    }

    public ReportInfo performSudoTest() {
        System.out.println("Performing sudo test on " + hostInfo.getHostname());
        Session session = null;
        reportInfo = new ReportInfo();
        boolean sudoSuccess = false; // Track Sudo success
        try {
                session = ConnectionManager.getSession(hostInfo);
                reportInfo.setSSHStatus();
                ChannelShell channel = (ChannelShell) session.openChannel("shell");
                channel.setPtyType("vt100");
                channel.setPty(true);
                channel.connect();

                Expect expect = new ExpectBuilder()
                        .withOutput(channel.getOutputStream())
                        .withInputs(channel.getInputStream(), channel.getExtInputStream())
                //      .withEchoOutput(System.out)
                        .withEchoInput(System.err)
                        //        .withInputFilters(removeColors(), removeNonPrintable())
                        .withExceptionOnFailure()
                        .build();
                try {
                        expect.expect(regexp("$"));
                        expect.sendLine("su root");
                        expect.expect(regexp("Password|[\u4e00-\u9fa5]"));
                        expect.sendLine(hostInfo.getRootPassword());
                        expect.expect(regexp("#"));
                        sudoSuccess = true; // Set to true if sudo command executed successfully
                        reportInfo.setSudoStatus();
                //      String ipAddress = expect.expect(regexp("Trying (.*)\\.\\.\\.")).group(1);
                //      System.out.println("Captured IP: " + ipAddress);
                        expect.sendLine("exit");
                        expect.expect(regexp("$"));
                        expect.sendLine("exit");
                        
                    } finally {
                                            expect.close();
                                            channel.disconnect();
                                            session.disconnect();
                    }

            
        } catch (Exception e) {
            System.err.println("Error during sudo test: " + e.getMessage());
        } finally {
            // Report Sudo status
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
        return reportInfo;
    }

    // Helper method to read output from InputStream
    private String readOutput(InputStream in) throws IOException {
        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        return output.toString();
    }

    @Override
    public void connect(ReportWriter reportWriter, String host, String resolvedIp, boolean performSSHCommands, LocalDateTime testStart) {
        // Implement the connection logic here
        // For example, you might want to perform the SSH connection and log the results using reportWriter
        try {
            // Your existing connection logic
            Session session = ConnectionManager.getSession(hostInfo);
            
            // Perform SSH commands if required
            if (performSSHCommands) {
                // Execute commands and log results
            }
            
            // Log success or failure using reportWriter
            reportWriter.incrementLinuxSuccess(); // or reportWriter.incrementLinuxFailure();
        } catch (Exception e) {
            // Handle exceptions and log failures
            reportWriter.incrementLinuxFailure();
            System.err.println("Error during SSH connection: " + e.getMessage());
        }
    }
} 
