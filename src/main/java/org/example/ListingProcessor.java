package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mongodb.client.result.InsertOneResult;
import org.server.ApartmentClient;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ListingProcessor extends Thread{
    private final JsonElement listing;
    private final List<Apartment> apartments;
    private final ApartmentClient mongoClient;
    private OffsetDateTime lastPollingTime = null;
    public ListingProcessor(JsonElement listing, List<Apartment> apartments, ApartmentClient mongoClient) {
        this.listing = listing;
        this.apartments = apartments;
        this.mongoClient = mongoClient;
        this.start();
    }

    public ListingProcessor(JsonElement listing, List<Apartment> apartments, ApartmentClient mongoClient, OffsetDateTime lastPollingTime) {
        this.listing = listing;
        this.apartments = apartments;
        this.mongoClient = mongoClient;
        this.lastPollingTime = lastPollingTime;
        this.start();
    }
    private float[] extractCoordinates() {
        JsonObject listingsObject = this.listing.getAsJsonObject().getAsJsonObject().getAsJsonObject("listing");
        JsonObject pointObject = listingsObject.getAsJsonObject("point");
        JsonArray coordinatesArray = pointObject.getAsJsonArray("coordinates");
        float longitude = coordinatesArray.get(0).getAsFloat();
        float latitude = coordinatesArray.get(1).getAsFloat();

        return new float[]{longitude, latitude};

    }
    private float extractCompanyProbability(){
        JsonObject listingsObject = this.listing.getAsJsonObject().getAsJsonObject().getAsJsonObject("listing");
        JsonObject sellerObject = listingsObject.getAsJsonObject("seller");
        String sellerType = sellerObject.getAsJsonPrimitive("sellerType").getAsString();
        if (sellerType.equals("BRANDED_AGENT")){
            return 0.75f;
        }
        if (sellerType.equals("UNBRANDED_AGENT")){
            return 0.50f;
        }
        return 0f;


    }

    private Set<Integer> extractPrices(){
        JsonObject listingsObject = this.listing.getAsJsonObject().getAsJsonObject().getAsJsonObject("listing");
        String abbreviatedPriceString = listingsObject.getAsJsonPrimitive("abbreviatedPrice").getAsString();
        Set<Integer> priceList = new java.util.HashSet<>();
        Integer startPrice =  Integer.parseInt(abbreviatedPriceString.replaceAll("\\D",""));
        priceList.add(startPrice);
        if (listingsObject.has("prs")) {
            JsonObject prsObject = listingsObject.getAsJsonObject("prs");
            if (prsObject.has("subUnits")){
                JsonArray subUnitsArray = prsObject.getAsJsonArray("subUnits");
                for (JsonElement subUnit : subUnitsArray) {
                    JsonObject subUnitObject = subUnit.getAsJsonObject();
                    String priceString = subUnitObject.getAsJsonPrimitive("price").getAsString();
                    priceList.add(Integer.parseInt(priceString.replaceAll("\\D", "")));
                }
            }

        }
        return priceList;
    }

    private String extractTitle() {
        JsonObject listingsObject = this.listing.getAsJsonObject().getAsJsonObject().getAsJsonObject("listing");
        return listingsObject.getAsJsonPrimitive("title").getAsString();
    }

    private long extractPostedAt() {
        JsonObject listingsObject = this.listing.getAsJsonObject().getAsJsonObject().getAsJsonObject("listing");
        return listingsObject.getAsJsonPrimitive("publishDate").getAsLong();
    }

    private List<String> extractImages() {
        JsonObject listingsObject = this.listing.getAsJsonObject().getAsJsonObject("listing");
        JsonObject mediaObject = listingsObject.getAsJsonObject("media");
        JsonArray imagesArray = mediaObject.getAsJsonArray("images");
        List<String> images = new ArrayList<>();
        for (JsonElement image : imagesArray) {
            JsonPrimitive imageElement = image.getAsJsonObject().getAsJsonPrimitive("size720x480");
            images.add(imageElement.getAsString());
        }
        return images;
    }


    private String extractSEOFriendlyPath() {
        JsonObject listingsObject = this.listing.getAsJsonObject().getAsJsonObject("listing");
        return listingsObject.getAsJsonPrimitive("seoFriendlyPath").getAsString();
    }

    private String makeUrl() {
        return (String.format("https://www.daft.ie/%s", extractSEOFriendlyPath()));
    }
    public void run(){
        String listingUrl = makeUrl();
        long postedAt = extractPostedAt();
        if (lastPollingTime != null && postedAt < lastPollingTime.toInstant().toEpochMilli()) {
            System.out.println("Listing is older than last polling time, skipping...");
            return;
        }
        if (!mongoClient.isUrlIndexed(listingUrl)){
            float[] coordinates = extractCoordinates();
            Set<Integer> prices = extractPrices();
            String title = extractTitle();
            float longitude = coordinates[0];
            float latitude = coordinates[1];
            float companyProbability = extractCompanyProbability();
            try {
                Apartment apartment  = new Apartment(title,latitude, longitude, postedAt, listingUrl, prices, companyProbability);
                HashMap<String, Long> itineraries = RoutesCalculator.calculateTravelTime(apartment, RoutesCalculator.getWillyFries());
                if (itineraries != null) {
                    apartment.setItineraries(itineraries);
                    apartments.add(apartment);
                    InsertOneResult result = mongoClient.safeInsertApartment(apartment);
                    mongoClient.insertPictures(result.getInsertedId(), extractImages());
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
