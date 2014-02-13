package hasoftware.comms;

import hasoftware.util.StringUtil;

public class PortFactory {

    public static IPort getPort(String description) {
        if (StringUtil.isNullOrEmpty(description)) {
            throw new IllegalArgumentException("Port description must not be blank");
        }
        // Check for standard windows COM port names
        if (description.startsWith("COM")) {
            return new ComPort(description);
        }
        // Check for standard linux serial port names
        if (description.startsWith("/dev/")) {
            return new ComPort(description);
        }
        // Check for TCP/IP port
        if (description.indexOf(":") != -1) {
            return new TcpIpPort(description);
        }
        throw new IllegalArgumentException("Unknown port description - " + description);
    }
}
