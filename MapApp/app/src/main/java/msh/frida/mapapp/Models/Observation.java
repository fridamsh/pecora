package msh.frida.mapapp.Models;

import org.osmdroid.util.GeoPoint;

/**
 * Created by Frida on 01/11/2017.
 */

public class Observation {

    private GeoPoint location;
    private String typeOfObservation;
    private String details;
    private ObservationPoint parentPoint;
    private GeoPoint parentGeoPoint;

    public Observation(GeoPoint location, GeoPoint parentPoint) {
        this.location = location;
        this.parentGeoPoint = parentPoint;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public ObservationPoint getObservationPoint() {
        return parentPoint;
    }

    public void setObservationPoint(ObservationPoint parentPoint) {
        this.parentPoint = parentPoint;
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
}
