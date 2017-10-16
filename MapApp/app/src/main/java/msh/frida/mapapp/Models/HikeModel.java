package msh.frida.mapapp.Models;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Frida on 11/10/2017.
 */

public class HikeModel {

    private String title;
    private String name;
    private int numberOfParticipants;
    private String weatherState;
    private String description;
    private Calendar dateStart;
    private Calendar dateEnd;
    private String mapFile;

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

    public Calendar getDateStart() {
        return dateStart;
    }

    public void setDateStart(Calendar dateStart) {
        this.dateStart = dateStart;
    }

    public Calendar getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Calendar dateEnd) {
        this.dateEnd = dateEnd;
    }

    public String getMapFile() {
        return mapFile;
    }

    public void setMapFile(String mapFile) {
        this.mapFile = mapFile;
    }
}
