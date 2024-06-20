package mbeans;

import beans.Point;
import beans.ResultTable;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import java.util.List;

public class Points extends NotificationBroadcasterSupport implements PointsMBean {
    private int totalPoints = 0;
    private int missedPoints = 0;
    private int sequenceNumber = 0;
    private ResultTable resultTable;

    public Points(ResultTable resultTable) {
        this.resultTable = resultTable;
        initializeCounts();
    }

    private void initializeCounts() {
        List<Point> results = resultTable.getResults();
        totalPoints = results.size();
    }

    @Override
    public int getTotalPoints() {
        return totalPoints;
    }

    @Override
    public int getMissedPoints() {
        return missedPoints;
    }

    @Override
    public void incrementMissedPoints() {
        missedPoints++;
    }


    @Override
    public void incrementTotalPoints() {
        totalPoints++;
    }

    @Override
    public void infoMissedPoints() {
        sequenceNumber++;
        sendNotification(new Notification("Missed points", this, sequenceNumber, System.currentTimeMillis(), "user input wrong point"));
    }


    @Override
    public void resetPoints() {
        totalPoints = 0;
        missedPoints = 0;
        initializeCounts();
    }
}