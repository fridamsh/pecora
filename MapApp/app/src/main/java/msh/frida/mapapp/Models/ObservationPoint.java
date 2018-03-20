package msh.frida.mapapp.Models;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Frida on 01/11/2017.
 */

public class ObservationPoint {

    private static final AtomicInteger count = new AtomicInteger(0);

    private GeoPoint locationPoint;
    private List<Observation> observationList;
    private final int pointId;
    private int sheepCount = 0;
    private long timeOfObservationPoint;

    public ObservationPoint(GeoPoint location) {
        this.pointId = count.incrementAndGet();
        this.locationPoint = location;
        observationList = new ArrayList<>();
    }

    public void resetId() {
        count.set(0);
    }

    public int getPointId() {
        return pointId;
    }

    public GeoPoint getLocation() {
        return locationPoint;
    }

    public long getTimeOfObservationPoint() {
        return timeOfObservationPoint;
    }

    public void setTimeOfObservationPoint(long timeOfObservationPoint) {
        this.timeOfObservationPoint = timeOfObservationPoint;
    }

    public List<Observation> getObservationList() {
        return observationList;
    }

    public void setObservationList(List<Observation> observationList) {
        this.observationList = observationList;
    }

    public int getSheepCount() {
        return sheepCount;
    }

    public void increaseSheepCount(int sheepCount) {
        this.sheepCount += sheepCount;
    }
}
