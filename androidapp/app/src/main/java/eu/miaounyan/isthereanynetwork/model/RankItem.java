package eu.miaounyan.isthereanynetwork.model;

public class RankItem {
    private String operatorName;
    private double signalStrength;

    public RankItem(String operatorName, double signalStrength) {
        this.operatorName = operatorName;
        this.signalStrength = signalStrength;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public double getSignalStrength() {
        return signalStrength;
    }
}
