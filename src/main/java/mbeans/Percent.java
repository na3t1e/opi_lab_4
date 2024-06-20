package mbeans;

public class Percent implements PercentMBean{
    private final PointsMBean pointCounter;

    public Percent(PointsMBean pointCounter) {
        this.pointCounter = pointCounter;
    }

    @Override
    public double getHitPercent() {
        int totalPoints = pointCounter.getTotalPoints();
        if (totalPoints == 0) {
            return 0;
        }
        return (double) (totalPoints - pointCounter.getMissedPoints()) / totalPoints * 100;
    }

}