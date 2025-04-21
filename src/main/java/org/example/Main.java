package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.*;
import com.google.maps.routing.v2.Waypoint;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Main {
    private static String getListingsHTML() throws IOException, InterruptedException {
        String url = "https://www.daft.ie/property-for-rent/ireland/apartments?rentalPrice_to=3500&numBeds_from=2"; // Replace with the URL of the page you want to parse

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw e;
        }
        return "";
    }
    private static JsonArray getListingsArray(JsonElement root) {
        JsonObject propsObject = root.getAsJsonObject().getAsJsonObject("props");
        JsonObject pagePropsObject = propsObject.getAsJsonObject("pageProps");
        return pagePropsObject.getAsJsonArray("listings");
    }

    private static float[] extractCoordinates(JsonElement root, int listingNumber){
        JsonArray listingsArray = getListingsArray(root);
        JsonObject listingsObject = listingsArray.get(listingNumber).getAsJsonObject().getAsJsonObject("listing");
        JsonObject pointObject = listingsObject.getAsJsonObject("point");
        JsonArray coordinatesArray = pointObject.getAsJsonArray("coordinates");
        float longitude = coordinatesArray.get(0).getAsFloat();
        float latitude = coordinatesArray.get(1).getAsFloat();

        return new float[] {longitude, latitude};

    }

    private static String extractSEOFriendlyPath(JsonElement root, int listingNumber) {
        JsonArray listingsArray = getListingsArray(root);
        JsonObject listingsObject = listingsArray.get(listingNumber).getAsJsonObject().getAsJsonObject("listing");
        return listingsObject.getAsJsonPrimitive("seoFriendlyPath").getAsString();
    }

    private static Number extractListingID(JsonElement root, int listingNumber) {
        JsonArray listingsArray = getListingsArray(root);
        JsonObject listingsObject = listingsArray.get(listingNumber).getAsJsonObject().getAsJsonObject("listing");
        return listingsObject.getAsJsonPrimitive("id").getAsNumber();
    }

    private static String makeUrl(JsonElement root, int listingNumber) {
        return (String.format("https://www.daft.ie/%s",extractSEOFriendlyPath(root, listingNumber)));
    }


    public static void main(final String[] Stringargs) throws IOException, InterruptedException {
                String html = getListingsHTML();

                // 1. Use Jsoup to parse the HTML
                Document document = Jsoup.parse(html);

                // 2. Select the script element using a CSS selector
                Element script = document.select("script#__NEXT_DATA__[type=application/json][crossorigin=anonymous]").first();

                // 3. Check if the script element was found
                if (script != null) {
                    // 4. Extract the script's content (which is a JSON string)
                    String jsonString = script.html();

                    try {
                        JsonElement jsonElement = JsonParser.parseString(jsonString);
                        List<Apartment> apartments = new ArrayList<Apartment>();
                        for (int i = 0; i < 20 ; i++){
                            float[] coordinates = extractCoordinates(jsonElement,i);
                            float longitude = coordinates[0];
                            float latitude = coordinates[1];
                            Apartment apartment = new Apartment(latitude,longitude,makeUrl(jsonElement,i));
                            HashMap<String, Long> itineraries = RoutesCalculator.calculateTravelTime(apartment, RoutesCalculator.getWillyFries());
                            if (itineraries != null){
                                apartment.setItineraries(itineraries);
                                apartments.add(apartment);
                            }

                        }
                        RoutesRanker ranker = new RoutesRanker(apartments);
                        List<Apartment> walkableRoutes = ranker.getWalkableRoutes();
                        List<Apartment> lightRailRoutes = ranker.getLightRailRoutes();
                        List<Apartment> busRoutes = ranker.getBusRoutes();
                        System.out.println("Found " + walkableRoutes.size() + "apartments within walking distance (20 mins):\n");
                        for (Apartment apartment : walkableRoutes){
                            System.out.println(apartment.serialize());
                        }
                        System.out.println("Found " + lightRailRoutes.size() + "reachable by Luas/Dart (30 mins commute max):\n");
                        for (Apartment apartment : lightRailRoutes){
                            System.out.println(apartment.serialize());
                        }
                        System.out.println("Found " + busRoutes.size() + "reachable by bus (30 mins commute max):\n");
                        for (Apartment apartment : busRoutes){
                            System.out.println(apartment.serialize());
                        }
                    } catch (com.google.gson.JsonParseException e) {
                        System.err.println("Error parsing JSON: " + e.getMessage());
                    }

                } else {
                    System.out.println("Script with specified characteristics not found.");
                }




    }
}