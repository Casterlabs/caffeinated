package co.casterlabs.caffeinated.util.network;

import java.util.HashSet;
import java.util.Set;

public class InterfaceUtil {

    public static Set<String> getLocalIpAddresses() {
        Set<String> result = new HashSet<>();
        result.add("localhost");
//        Consumer<String> add = (ip) -> {
//            if (ip.contains(":")) {
//                result.add('[' + ip + ']');
//            } else {
//                result.add(ip);
//            }
//        };
//
//        add.accept("0:0:0:0:0:0:0:1");
//        add.accept("127.0.0.1");
//
////        if (System.getenv().containsKey("COMPUTERNAME")) {
////            add.accept(System.getenv("COMPUTERNAME"));
////        } else if (System.getenv().containsKey("HOSTNAME")) {
////            add.accept(System.getenv("HOSTNAME"));
////        }
//
////        try {
////            for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
////                for (InetAddress addr : Collections.list(iface.getInetAddresses())) {
////                    add.accept(addr.getHostAddress());
////                }
////            }
////        } catch (SocketException e) {
////            FastLogger.logStatic(LogLevel.DEBUG, "Could not enumerate network interfaces:\n%s", e);
////        }
//
        return result;
    }

}
