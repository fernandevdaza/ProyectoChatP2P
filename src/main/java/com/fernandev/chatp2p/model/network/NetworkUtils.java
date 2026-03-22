package com.fernandev.chatp2p.model.network;

import java.net.*;
import java.util.Enumeration;

public class NetworkUtils {

    public static String getWifiLanIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }

                String name = networkInterface.getName() != null
                        ? networkInterface.getName().toLowerCase()
                        : "";

                String displayName = networkInterface.getDisplayName() != null
                        ? networkInterface.getDisplayName().toLowerCase()
                        : "";

                boolean isWifi =
                        name.startsWith("wlan") ||
                                name.startsWith("wlp") ||
                                name.contains("wifi") ||
                                name.contains("wi-fi") ||
                                displayName.contains("wifi") ||
                                displayName.contains("wi-fi") ||
                                displayName.contains("wireless");

                if (!isWifi) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();

                    if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}