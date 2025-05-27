package org.example;
import com.google.gson.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.server.ApartmentClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {
    private static String getListingsHTML(String url) throws IOException, InterruptedException {
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


    public static void apartments(OffsetDateTime lastPollingTime) throws IOException, InterruptedException {
        DaftUrlBuilder urlBuilder = new DaftUrlBuilder(1, 1);
        List<Apartment> apartments = new ArrayList<>();
        ApartmentClient mongoClient = new ApartmentClient();
        while (urlBuilder.hasNext()) {
            String url = urlBuilder.next();
            String html = getListingsHTML(url);
            Document document = Jsoup.parse(html);
            Element script = document.select("script#__NEXT_DATA__[type=application/json][crossorigin=anonymous]").first();
            if (script != null) {
                String jsonString = script.html();
                try {
                    JsonElement jsonElement = JsonParser.parseString(jsonString);
                    JsonArray listingsArray = getListingsArray(jsonElement);
                    List<ListingProcessor> processors = new ArrayList<>();
                    for (int i = 0; i < listingsArray.size(); i++) {
                        ListingProcessor processor = new ListingProcessor(listingsArray.get(i), apartments, mongoClient, lastPollingTime);
                        processors.add(processor);
                    }
                    for (ListingProcessor processor : processors) {
                        try {
                            processor.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (com.google.gson.JsonParseException e) {
                    System.err.println("Error parsing JSON: " + e.getMessage());
                }

            } else {
                System.out.println("Script with specified characteristics not found.");
            }
        }
    }
}