package msh.frida.mapapp.Models;

import org.osmdroid.util.GeoPoint;

public class Observation {

    private GeoPoint location;
    private String typeOfObservation;
    private String details;
    private int sheepCount = 0;
    private int id;

    public Observation(GeoPoint location, int id) {
        this.location = location;
        this.id = id;
    }

    public GeoPoint getLocation() {
        return location;
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
        return id;
    }
}
