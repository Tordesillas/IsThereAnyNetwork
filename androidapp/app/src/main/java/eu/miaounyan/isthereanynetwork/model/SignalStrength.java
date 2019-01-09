package eu.miaounyan.isthereanynetwork.model;

public enum SignalStrength {
    BAD(0x40FF0000),
    BAD_AVERAGE(0x40FFFF00),
    AVERAGE(0x40FFFF00),
    AVERAGE_GOOD(0x407FFF00),
    GOOD(0x4000FF00);

    private final int color;

    SignalStrength(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
