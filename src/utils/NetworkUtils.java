package utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkUtils {
    public static String getLocalIPAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }
}
