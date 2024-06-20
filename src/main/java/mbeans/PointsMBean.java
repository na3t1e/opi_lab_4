package mbeans;

public interface PointsMBean {
    int getTotalPoints();
    int getMissedPoints();
    void incrementMissedPoints();
    void incrementTotalPoints();
    void infoMissedPoints();
    void resetPoints();
}
