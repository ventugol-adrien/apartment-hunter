package org.example;

import com.google.maps.routing.v2.Waypoint;

import java.util.HashMap;

public class Apartment {
    private final float latitude;
    private final float longitude;
    private HashMap<String,Long> itineraries;
    private final String url;
    private  Waypoint location;

    public Apartment(float latitude, float longitude, HashMap<String,Long> itineraries, String url) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.itineraries = itineraries;
        this.url = url;
        this.location = RoutesCalculator.buildWaypoint(latitude, longitude);

    }

    public Apartment(float latitude, float longitude, String url) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.url = url;
        this.location = RoutesCalculator.buildWaypoint(latitude, longitude);

    }

    public Waypoint getLocation() {
        return this.location;
    }
    public String getGoogleMapsUrl() {
        return ("Google maps link: \n" + String.format("https://www.google.com/maps/place/%.6f,%.6f", this.latitude, this.longitude));
    }

    public String getUrl() {
        return this.url;
    }
    public void setItineraries(HashMap<String,Long> itineraries) {
        this.itineraries = itineraries;
    }
    public HashMap<String,Long> getRoutes() {
        return this.itineraries;
    }
    public String serialize() {
        return (String.format("{gmaps: %s, itineraries: %s, url: %s}", this.getGoogleMapsUrl(),this.itineraries,this.url));
    }
}
