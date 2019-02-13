package eu.miaounyan.isthereanynetwork.model;

public class IsThereAnyNetworkData {

    /* Network */
    private String type;
    private String operator;
    private int signalStrength;

    /* Location */
    private double lat;
    private double lon;

    private String date;

    public IsThereAnyNetworkData(String type, String operator, int signalStrength, double lat, double lon, String date) {
        this.type = type;
        this.operator = operator;
        this.signalStrength = signalStrength;
        this.lat = lat;
        this.lon = lon;
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public String getOperator() {
        return operator;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getDate() {
        return date;
    }
}
