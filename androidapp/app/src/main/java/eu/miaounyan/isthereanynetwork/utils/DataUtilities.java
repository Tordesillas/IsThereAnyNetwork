package eu.miaounyan.isthereanynetwork.utils;

import java.util.Date;

import eu.miaounyan.isthereanynetwork.service.DateFormatter;
import eu.miaounyan.isthereanynetwork.service.location.GPSTracker;
import eu.miaounyan.isthereanynetwork.service.telephony.Network;

public final class DataUtilities {

    private static DateFormatter dateFormatter = new DateFormatter();

    private DataUtilities() {
    }

    public static String getCurrentTimeDate() {
        Date now = new Date();
        return dateFormatter.toISO8601String(now);
    }
}
