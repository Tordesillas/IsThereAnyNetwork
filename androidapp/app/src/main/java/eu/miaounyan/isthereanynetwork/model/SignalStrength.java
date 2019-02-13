package eu.miaounyan.isthereanynetwork.model;

public enum SignalStrength {
    TRANSPARENT(0x00FFFFFF),
    BAD(0x40FF0000),
    BAD_AVERAGE(0x40FF8000),
    AVERAGE(0x40FFFF00),
    AVERAGE_GOOD(0x4080FF00),
    GOOD(0x4000FF00);

    private final int color;

    SignalStrength(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
