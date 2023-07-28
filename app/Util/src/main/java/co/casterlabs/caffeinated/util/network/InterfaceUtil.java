package co.casterlabs.caffeinated.util.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class InterfaceUtil {

    public static Set<String> getLocalIpAddresses() {
        Set<String> result = new HashSet<>();

        result.add("0:0:0:0:0:0:0:1");
        result.add("127.0.0.1");

        if (System.getenv().containsKey("COMPUTERNAME")) {
            result.add(System.getenv("COMPUTERNAME"));
        } else if (System.getenv().containsKey("HOSTNAME")) {
            result.add(System.getenv("HOSTNAME"));
        }

        try {
            for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress addr : Collections.list(iface.getInetAddresses())) {
                    result.add(addr.getHostAddress());
                }
            }
        } catch (SocketException e) {
            FastLogger.logStatic(LogLevel.DEBUG, "Could not enumerate network interfaces:\n%s", e);
        }

        return result;
    }

}
