package eu.miaounyan.isthereanynetwork.model;

public enum SignalStrength {
    LOW(0x40ff3d00),
    MEDIUM(0x40fbc02d),
    HIGH(0x4076FF03);

    private final int color;

    SignalStrength(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
