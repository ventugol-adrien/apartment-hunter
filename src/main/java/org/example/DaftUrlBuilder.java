package org.example;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Iterator;

public class DaftUrlBuilder implements Iterator<String> {
    private final String url;
    private final int totalPages;
    private int pageNumber = 0;

    public DaftUrlBuilder(int washingMachine, int dishwasher, int dryer) throws IOException, InterruptedException {
        String baseUrl = "https://www.daft.ie/property-for-rent/dublin-city/apartments?rentalPrice_to=4000&numBeds_from=2&numBaths_from=2&sort=publishDateDesc";
        if (washingMachine == 1) {
            baseUrl += "&facilities=washing-machine";
        }
        if (dishwasher == 1) {
            baseUrl += "&facilities=dishwasher";
        }
        if (dryer == 1) {
            baseUrl += "&facilities=dryer";
        }
        this.url = baseUrl;
        this.totalPages = getTotalPages(this.url);
        System.out.println("Total pages: " + this.totalPages);
    }

    private static int getTotalPages(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
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
                    return extractTotalPages(jsonElement);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw e;
        }
        throw new InvalidObjectException("No total pages found.");
    }
    private static int extractTotalPages(JsonElement root) {
        JsonObject pagingObject = getPagingObject(root);
        return pagingObject.getAsJsonPrimitive("totalPages").getAsInt();
    }
    private static JsonObject getPagingObject(JsonElement root) {
        JsonObject propsObject = root.getAsJsonObject().getAsJsonObject("props");
        JsonObject pagePropsObject = propsObject.getAsJsonObject("pageProps");
        return pagePropsObject.getAsJsonObject("paging");
    }


    public String getUrl() {
        return this.url;
    }

    @Override
    public boolean hasNext() {
        return this.pageNumber < this.totalPages ;
    }

    @Override
    public String next() {
        int from = this.pageNumber * 20;
        if (!this.hasNext()) {
            throw new IllegalStateException("No more pages to iterate");
        }
        if (from > 0){
            this.pageNumber++;
            return this.getUrl() +"&pageSize=20"+ "&page=" + this.pageNumber;
        }
        this.pageNumber++;
        return this.getUrl();
    }
}

