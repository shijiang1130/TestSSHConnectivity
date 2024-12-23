package report;

import java.io.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import utils.HostInfo;

public class ReportWriter {
    private static final String REPORT_FILE = "connection_report.html";
    private LocalDateTime startTime;
    private int linuxSuccessCount = 0;
    private int linuxFailureCount = 0;
    private boolean pingSuccess;
    private boolean sshSuccess;
    private boolean sudoSuccess;

    public void initializeReport() throws IOException {
        startTime = LocalDateTime.now();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(REPORT_FILE))) {
            writer.write("<!DOCTYPE html><html><head>");
            writer.write("<style>");
            writer.write(".success { color: blue; }");
            writer.write(".error { color: red; }");
            writer.write("body { font-family: Arial, sans-serif; }");
            writer.write("table { border-collapse: collapse; width: 100%; margin-top: 20px; }");
            writer.write("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            writer.write("th { background-color: #f2f2f2; }");
            writer.write("tr:nth-child(even) { background-color: #f9f9f9; }");
            writer.write("</style></head><body>");
            writer.write("<h1>Connection Report</h1>");
            writer.write("<p>Test started at: " + startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>");
            
            // Linux hosts table
            writer.write("<h2>Linux Hosts</h2>");
            writer.write("<table>");
            writer.write("<tr><th>No</th><th>Host</th><th>Resolved IP</th><th>Ping Status</th><th>SSH Status</th><th>Sudo Status</th><th>Details</th><th>Duration</th><th>Test Start</th></tr>");
        }
    }

    public void writeToReport(int sequence, String host, String resolvedIp, boolean pingSuccess, boolean sshSuccess, boolean sudoSuccess, long duration, LocalDateTime testStart) throws IOException {
        this.pingSuccess = pingSuccess;
        this.sshSuccess = sshSuccess;
        this.sudoSuccess = sudoSuccess;

        // Increment failure count if any test failed
        if (!pingSuccess) incrementLinuxFailure();
        if (!sshSuccess) incrementLinuxFailure();
        if (!sudoSuccess) incrementLinuxFailure();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(REPORT_FILE, true))) {
            writer.write("<tr>");
            writer.write("<td>" + sequence + "</td>");
            writer.write("<td>" + host + "</td>");
            writer.write("<td>" + (resolvedIp != null ? resolvedIp : "Failed") + "</td>");
            writer.write("<td class='" + (pingSuccess ? "success" : "error") + "'>" + 
                        (pingSuccess ? "Success" : "Failed") + "</td>");
            writer.write("<td class='" + (sshSuccess ? "success" : "error") + "'>" + 
                        (sshSuccess ? "Success" : "Failed") + "</td>");
            writer.write("<td class='" + (sudoSuccess ? "success" : "error") + "'>" + 
                        (sudoSuccess ? "Success" : "Failed") + "</td>");
            
            // Log failed tests
            if (!pingSuccess || !sshSuccess || !sudoSuccess) {
                writer.write("<td>Failed Tests: ");
                if (!pingSuccess) writer.write("Ping ");
                if (!sshSuccess) writer.write("SSH ");
                if (!sudoSuccess) writer.write("Sudo ");
                writer.write("</td>");
            } else {
                writer.write("<td>All Tests Successful</td>");
            }
            
            writer.write("<td>" + duration + " ms</td>");
            writer.write("<td>" + testStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</td>");
            
            writer.write("</tr>");
        }
    }

      public void writeSummary() throws IOException {
        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, endTime);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(REPORT_FILE, true))) {
            writer.write("</table>");
            writer.write("<h2>Connection Summary</h2>");
            writer.write("<table>");
            writer.write("<tr><th>Metric</th><th>Value</th></tr>");
            writer.write("<tr><td>Test completed at</td><td>" + 
                        endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</td></tr>");
            writer.write("<tr><td>Total duration</td><td>" + 
                        duration.getSeconds() + "." + 
                        String.format("%03d", duration.toMillis() % 1000) + " seconds</td></tr>");
            
            // New summary metrics
            writer.write("<tr><td>Total Linux hosts</td><td>" + 
                        (linuxSuccessCount + linuxFailureCount) + "</td></tr>");
            writer.write("<tr><td>Successful Linux connections</td><td class='success'>" + 
                        linuxSuccessCount + "</td></tr>");
            writer.write("<tr><td>Failed Linux connections</td><td class='error'>" + 
                        linuxFailureCount + "</td></tr>");
            writer.write("</table>");
        }
    }

    public void finalizeReport() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(REPORT_FILE, true))) {
            writer.write("</body></html>");
        }
    }

    public void incrementLinuxSuccess() {
        linuxSuccessCount++;
    }

    public void incrementLinuxFailure() {
        linuxFailureCount++;
    }
} 
