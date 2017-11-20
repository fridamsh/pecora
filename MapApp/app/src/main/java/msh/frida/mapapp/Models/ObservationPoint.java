package msh.frida.mapapp.Models;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frida on 01/11/2017.
 */

public class ObservationPoint {


    private GeoPoint location;
    private long timeOfObservation;
    private List<Observation> observationList;
    private int sheepCount = 0;

    public ObservationPoint(GeoPoint location) {
        this.location = location;
        observationList = new ArrayList<>();
    }

    public GeoPoint getLocation() {
        return location;
    }

    public long getTimeOfObservation() {
        return timeOfObservation;
    }

    public void setTimeOfObservation(long timeOfObservation) {
        this.timeOfObservation = timeOfObservation;
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
