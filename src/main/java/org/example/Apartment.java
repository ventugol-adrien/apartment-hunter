package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.maps.routing.v2.Waypoint;
import org.bson.Document;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

public class Apartment extends Document {
    private final String title;
    private final float latitude;
    private final float longitude;
    private HashMap<String,Long> itineraries;
    private final String url;
    private final Waypoint location;
    private final long postedAt;
    private final Set<Integer> prices;
    private final float companyProbability;

    public Apartment(String title, float latitude, float longitude,long postedAt, String url, Set<Integer> prices, float companyProbability) throws IOException, InterruptedException {
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.url = url;
        this.location = RoutesCalculator.buildWaypoint(latitude, longitude);
        this.postedAt = postedAt;
        this.prices = prices;
        this.companyProbability = companyProbability;

    }
    public String getTitle() {
        return this.title;
    }

    public Waypoint getLocation() {
        return this.location;
    }
    public String getGoogleMapsUrl() {
        return (String.format(Locale.US,"https://www.google.com/maps/place/%.6f,%.6f", this.latitude, this.longitude));
    }
    public long getPostedAt() {
        return this.postedAt;
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
    public JsonObject serialize() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC);
        String formattedDate = formatter.format(Instant.ofEpochMilli(this.postedAt));
        JsonObject jsonObject = new JsonObject();
        JsonObject itinerariesObject = new Gson().toJsonTree(this.itineraries).getAsJsonObject();
        jsonObject.addProperty("title", this.title);
        jsonObject.add("prices", new Gson().toJsonTree(this.prices).getAsJsonArray());
        jsonObject.add("itineraries", itinerariesObject);
        jsonObject.addProperty("gmaps", this.getGoogleMapsUrl());
        jsonObject.addProperty("url", this.url);
        jsonObject.addProperty("postedAt", formattedDate);
        jsonObject.addProperty("companyProbability", this.companyProbability);

        return jsonObject;
    }
}
