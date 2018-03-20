package msh.frida.mapapp.Models;

import org.osmdroid.util.GeoPoint;

import java.util.concurrent.atomic.AtomicInteger;

public class Observation {

    private static final AtomicInteger count = new AtomicInteger(0);

    private String details;
    private GeoPoint locationObservation;
    private int observationId;
    private int sheepCount = 0;
    private String typeOfObservation;

    public Observation(GeoPoint location) {
        this.locationObservation = location;
        this.observationId = count.incrementAndGet();
    }

    public void resetId() {
        count.set(0);
    }

    public GeoPoint getLocation() {
        return locationObservation;
    }

    public String getTypeOfObservation() {
        return typeOfObservation;
    }

    public void setTypeOfObservation(String typeOfObservation) {
        this.typeOfObservation = typeOfObservation;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public int getSheepCount() {
        return sheepCount;
    }

    public void setSheepCount(int sheepCount) {
        this.sheepCount = sheepCount;
    }

    public void increaseSheepCount(int sheepCount) {
        this.sheepCount += sheepCount;
    }

    public int getId() {
        return observationId;
    }
}
