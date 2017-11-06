package msh.frida.mapapp.Models;

import org.osmdroid.util.GeoPoint;

/**
 * Created by Frida on 01/11/2017.
 */

public class ObservationPoint {


    private GeoPoint location;

    public ObservationPoint(GeoPoint location) {
        this.location = location;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }
}
