package org.example;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.maps.routing.v2.Waypoint;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.HashMap;

public class Apartment {
    private final float latitude;
    private final float longitude;
    private HashMap<String,Long> itineraries;
    private final String url;
    private final Waypoint location;
    private final Instant publishedDate;

    public Apartment(float latitude, float longitude, HashMap<String,Long> itineraries, String url) throws IOException, InterruptedException {
        this.latitude = latitude;
        this.longitude = longitude;
        this.itineraries = itineraries;
        this.url = url;
        this.location = RoutesCalculator.buildWaypoint(latitude, longitude);
        this.publishedDate = this.extractInstant();

    }

    public Apartment(float latitude, float longitude, String url) throws IOException, InterruptedException {
        this.latitude = latitude;
        this.longitude = longitude;
        this.url = url;
        this.location = RoutesCalculator.buildWaypoint(latitude, longitude);
        this.publishedDate = this.extractInstant();

    }
    private Instant extractInstant() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.url))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String html = response.body();
                Document document = Jsoup.parse(html);
                Element script = document.select("script#__NEXT_DATA__[type=application/json][crossorigin=anonymous]").first();
                try {
                    assert script != null;
                    JsonElement jsonElement = JsonParser.parseString(script.html());
                    return extractInstant(jsonElement);
                } catch (Exception e) {
                    System.err.println("Error extracting instant from listing, possibly undefined. Using current time instead." + e.getMessage());
                    return Instant.now();


                }

            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return Instant.now();
        }
        return null;
    }
    private static Instant extractInstant(JsonElement root) {

        try {
            JsonObject listingObject = getListingObject(root);
            return Instant.ofEpochSecond(listingObject.getAsJsonPrimitive("publishDate").getAsLong());
        } catch (Exception e) {
            System.err.println(getListingObject(root).toString());
            throw new RuntimeException(e);
        }
    }
    private static JsonObject getListingObject(JsonElement root) {
        JsonObject propsObject = root.getAsJsonObject().getAsJsonObject("props");
        JsonObject pagePropsObject = propsObject.getAsJsonObject("pageProps");
        return pagePropsObject.getAsJsonObject("listing");
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
        return (String.format("{gmaps: %s, itineraries: %s, url: %s, publishedDate: %s}", this.getGoogleMapsUrl(),this.itineraries,this.url, this.publishedDate));
    }
}
