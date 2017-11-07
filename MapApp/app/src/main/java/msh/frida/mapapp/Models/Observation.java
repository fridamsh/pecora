package msh.frida.mapapp.Models;

import org.osmdroid.util.GeoPoint;

/**
 * Created by Frida on 01/11/2017.
 */

public class Observation {

    private GeoPoint location;
    private String typeOfObservation;
    private String details;

    public Observation(GeoPoint location) {
        this.location = location;
    }

    public GeoPoint getLocation() {
        return location;
    }

    /*public void setLocation(GeoPoint location) {
        this.location = location;
    }*/

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
