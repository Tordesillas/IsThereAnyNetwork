package eu.miaounyan.isthereanynetwork.utils;

import java.util.Date;

import eu.miaounyan.isthereanynetwork.service.DateFormatter;
import eu.miaounyan.isthereanynetwork.service.location.GPSTracker;
import eu.miaounyan.isthereanynetwork.service.telephony.Network;

public final class DataUtilities {

    private static DateFormatter dateFormatter = new DateFormatter();

    private DataUtilities() {
    }

    public static boolean checkDataConsistency(GPSTracker gpsTracker, Network network) {
        return (-180 <= gpsTracker.getLatitude() && gpsTracker.getLatitude() <= 180) &&
                (-180 <= gpsTracker.getLongitude() && gpsTracker.getLongitude() <= 180) &&
                (gpsTracker.getLatitude() != 0 && gpsTracker.getLongitude() != 0) &&
                (-150 <= network.getSignalStrength() && network.getSignalStrength() < -40) &&
                (!"Unknown".equals(network.getType()));
    }

    public static String getCurrentTimeDate() {
        Date now = new Date();
        return dateFormatter.toISO8601String(now);
    }
}
