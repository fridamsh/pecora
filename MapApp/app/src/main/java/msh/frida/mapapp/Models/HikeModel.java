package msh.frida.mapapp.Models;

import android.os.Parcel;
import android.os.Parcelable;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Frida on 11/10/2017.
 */

public class HikeModel implements Parcelable, Serializable {

    private int id;
    private String title;
    private String name;
    private int numberOfParticipants;
    private String weatherState;
    private String description;
    private long dateStart;
    private long dateEnd;
    private String mapFileName;
    private List<ObservationPoint> observationPoints;
    //private String observationPointsString;
    private transient Polyline track;
    private List<GeoPoint> trackPoints;
    //private String trackPointsString;

    // Empty constructor
    public HikeModel() {}

    public HikeModel(String title, String name, String weatherState) {
        this.title = title;
        this.name = name;
        this.weatherState = weatherState;
    }

    // Constructor
    public HikeModel(int id, String title, String name, int numberOfParticipants, String weatherState, String description,
                     long dateStart, long dateEnd, String mapFile, List<ObservationPoint> observationPoints, List<GeoPoint> trackPoints) {
        this.id = id;
        this.title = title;
        this.name = name;
        this.numberOfParticipants = numberOfParticipants;
        this.weatherState = weatherState;
        this.description = description;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.mapFileName = mapFile;
        this.observationPoints = observationPoints;
        this.trackPoints = trackPoints;
    }

    // Constructor
    public HikeModel(String title, String name, int numberOfParticipants, String weatherState, String description, long dateStart,
                     long dateEnd, String mapFile, List<ObservationPoint> observationPoints, List<GeoPoint> trackPoints) {
        this.title = title;
        this.name = name;
        this.numberOfParticipants = numberOfParticipants;
        this.weatherState = weatherState;
        this.description = description;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.mapFileName = mapFile;
        this.observationPoints = observationPoints;
        this.trackPoints = trackPoints;
    }

    // Constructor that takes a Parcel and gives you an object populated with it's values
    public HikeModel(Parcel in) {
        title = in.readString();
        name = in.readString();
        numberOfParticipants = in.readInt();
        weatherState = in.readString();
        description = in.readString();
        mapFileName = in.readString();
        dateStart = in.readLong();
        dateEnd = in.readLong();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberOfParticipants() {
        return numberOfParticipants;
    }

    public void setNumberOfParticipants(int numberOfParticipants) {
        this.numberOfParticipants = numberOfParticipants;
    }

    public String getWeatherState() {
        return weatherState;
    }

    public void setWeatherState(String weatherState) {
        this.weatherState = weatherState;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getDateStart() {
        return dateStart;
    }

    public void setDateStart(long dateStart) {
        this.dateStart = dateStart;
    }

    public long getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(long dateEnd) {
        this.dateEnd = dateEnd;
    }

    public String getMapFileName() {
        return mapFileName;
    }

    public void setMapFileName(String mapFileName) {
        this.mapFileName = mapFileName;
    }

    public List<ObservationPoint> getObservationPoints() {
        return observationPoints;
    }

    public void setObservationPoints(List<ObservationPoint> observationPoints) {
        this.observationPoints = observationPoints;
    }

    public Polyline getTrack() {
        return track;
    }

    public void setTrack(Polyline track) {
        this.track = track;
    }

    public List<GeoPoint> getTrackPoints() {
        return trackPoints;
    }

    public void setTrackPoints(List<GeoPoint> trackPoints) {
        this.trackPoints = trackPoints;
    }

    // ---------- Parcelable stuff ----------
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(name);
        dest.writeInt(numberOfParticipants);
        dest.writeString(weatherState);
        dest.writeString(description);
        dest.writeString(mapFileName);
        dest.writeLong(dateStart);
        dest.writeLong(dateEnd);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<HikeModel> CREATOR = new Parcelable.Creator<HikeModel>() {
        public HikeModel createFromParcel(Parcel in) {
            return new HikeModel(in);
        }

        public HikeModel[] newArray(int size) {
            return new HikeModel[size];
        }
    };
}
