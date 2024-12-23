import utils.HostInfo;
import utils.ReportInfo;
import network.HostResolver;
import connection.ConnectionManager;
import report.ReportWriter;
import dns.DNSTester;
import com.jcraft.jsch.JSchException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import connection.ConnectionStrategy;
import connection.SSHConnection;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class App {
	private static String DNS_SERVER = "ch2dns02p";
	private static final boolean PERFORM_SSH_COMMANDS = true;
	private static final ReportWriter reportWriter = new ReportWriter();
    private static ReportInfo reportInfo = new ReportInfo();

	public static void main(String[] args) throws IOException, JSchException {
		// Test DNS server first
		if (!DNSTester.testDNSServer(DNS_SERVER)) {
			System.err.println("DNS server test failed. Please check the DNS server configuration.");
			System.exit(1);
		}

		// Load hosts from hosts.txt instead of directories
		List<HostInfo> hosts = loadHostsFromFile("hosts.txt");
		System.out.println("Found " + hosts.size() + " hosts in hosts.txt");
		
		reportWriter.initializeReport();
		boolean windowsSectionStarted = false;
		
		// Create a fixed thread pool
		ExecutorService executorService = Executors.newFixedThreadPool(10); // Adjust the number of threads as needed
		List<Future<ReportInfo>> futures = new ArrayList<>();

		AtomicInteger sequence = new AtomicInteger(1); // Use AtomicInteger for sequence
		for (HostInfo hostInfo : hosts) {
			// Submit a task for each host
			futures.add(executorService.submit(() -> {
				String host = hostInfo.getHostname();
				String resolvedIp = null;
				boolean pingSuccess = false;
				LocalDateTime hostStartTime = LocalDateTime.now();
				ReportInfo reportInfo = new ReportInfo(); // Create a new ReportInfo for each task

				try {
					System.out.println("\n=== Processing host: " + host + " ===");
					System.out.println("Start time: " + hostStartTime.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")));
					
					System.out.println("Resolving hostname...");
					String hostFQDN = host + ".htichina.net";
					resolvedIp = HostResolver.resolveHostname(hostFQDN, DNS_SERVER);
					if (resolvedIp == null) {
						throw new IOException("Could not resolve IP address for " + hostFQDN);
					}
					System.out.println("Resolved IP: " + resolvedIp);
					hostInfo.setipAddr(resolvedIp);

					// Step 2: Ping Test
					System.out.println("Performing ping test...");
					pingSuccess = ConnectionManager.pingHost(resolvedIp);
					if (!pingSuccess) {
						throw new IOException("Host is not responding to ping");
					}
					System.out.println("Ping successful");
					
					SSHConnection connectionStrategy = new SSHConnection(hostInfo, resolvedIp, PERFORM_SSH_COMMANDS);
					reportInfo = connectionStrategy.performSudoTest(); // Call the method to perform the test
					Duration duration = Duration.between(hostStartTime, LocalDateTime.now());

					// Calculate duration
					long durationMillis = Duration.between(hostStartTime, LocalDateTime.now()).toMillis();

					// Write to report with sequence number, duration, and test start time
					reportWriter.writeToReport(sequence.getAndIncrement(), host, resolvedIp, pingSuccess, reportInfo.getSSHStatus(), reportInfo.getSudoStatus(), durationMillis, hostStartTime);
				} catch (Exception e) {
					String errorDetails = "Error: " + e.getMessage();
					reportWriter.incrementLinuxFailure();
					
					// Calculate duration
					long durationMillis = Duration.between(hostStartTime, LocalDateTime.now()).toMillis();

					// Write to report with sequence number and duration
					reportWriter.writeToReport(sequence.getAndIncrement(), host, resolvedIp, pingSuccess, reportInfo.getSSHStatus(), reportInfo.getSudoStatus(), durationMillis, hostStartTime);
					
					System.out.println(errorDetails);
				}
				return reportInfo; // Return the report info for this host
			}));
		}

		// Wait for all tasks to complete with a timeout
		for (Future<ReportInfo> future : futures) {
			try {
				future.get(30, TimeUnit.SECONDS); // Set timeout to 30 seconds
			} catch (TimeoutException e) {
				System.err.println("Task timed out: " + e.getMessage());
				// Optionally handle the timeout (e.g., cancel the task)
				future.cancel(true); // Cancel the task if needed
			} catch (Exception e) {
				System.err.println("Error retrieving result: " + e.getMessage());
			}
		}

		// Shutdown the executor service
		executorService.shutdown(); // Initiate an orderly shutdown
		try {
			// Wait for existing tasks to terminate with a timeout
			if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) { // Set timeout to 60 seconds
				executorService.shutdownNow(); // Forcefully shutdown if tasks did not finish
				System.err.println("Executor service did not terminate in the specified time.");
			}
		} catch (InterruptedException e) {
			executorService.shutdownNow(); // Forcefully shutdown if interrupted
			Thread.currentThread().interrupt(); // Preserve interrupt status
			System.err.println("Executor service was interrupted during shutdown.");
		}

		reportWriter.writeSummary();
		reportWriter.finalizeReport();

	}

	private static List<HostInfo> loadHostsFromFile(String filePath) {
		List<HostInfo> hostInfoList = new ArrayList<>();
		try {
			List<String> lines = Files.readAllLines(Paths.get(filePath));
			for (String line : lines) {
				String[] parts = line.split(" ");
				if (parts.length > 1) {
					String host = parts[0];
					String jumpuserPassword = parts[2]; 
					String rootPassword = parts[4];
					hostInfoList.add(new HostInfo(host, jumpuserPassword, rootPassword));
				}
			}
		} catch (IOException e) {
			System.err.println("Error reading hosts from file: " + e.getMessage());
		}
		return hostInfoList;
	}
}
