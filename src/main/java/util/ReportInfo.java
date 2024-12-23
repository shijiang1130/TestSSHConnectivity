package utils;

public class ReportInfo {
    private boolean SSHStatus = false;
    private boolean SudoStatus = false;

    public ReportInfo() {
    }

    public boolean setSSHStatus() {
        return this.SSHStatus = true;
    }

    public boolean setSudoStatus() {
        return this.SudoStatus = true;
    }

    public boolean getSSHStatus() {
        return this.SSHStatus;
    }
    
    public boolean getSudoStatus() {
        return this.SudoStatus;
    }

}
