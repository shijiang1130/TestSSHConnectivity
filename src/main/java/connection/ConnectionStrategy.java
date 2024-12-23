package connection;

import report.ReportWriter;
import java.time.LocalDateTime;

public interface ConnectionStrategy {
    void connect(ReportWriter reportWriter, String host, String resolvedIp, boolean pingSuccess, LocalDateTime hostStartTime);
} 
