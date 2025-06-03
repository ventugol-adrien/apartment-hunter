package org.server;

import org.springframework.web.bind.annotation.*;
import org.example.ApartmentFetcher;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
public class Controller {
    private static OffsetDateTime lastPollingTime = OffsetDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault());
    private static final ApartmentClient apartmentClient = new ApartmentClient();
    public Controller() {
    }

    public static void updateLastPollingTime() {
        OffsetDateTime now = OffsetDateTime.now();

        System.out.println("Last polling time updated to: "+ now );
        Controller.lastPollingTime = now;
    }


    @GetMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }
    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/apartments")
    public String getApartments(@RequestParam(value = "walking", defaultValue = "1") int walking, @RequestParam(value = "tram", defaultValue = "0") int tram , @RequestParam(value = "bus", defaultValue = "0") int bus  , @RequestParam(value = "date", defaultValue = "today") String date) throws IOException, InterruptedException {
        System.out.println("Request received at: " + OffsetDateTime.now());
        return apartmentClient.getApartments(walking, tram, bus);

    }
    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/apartments/{apartmentId}/images")
    public List<String> getApartments(@PathVariable String apartmentId ) throws IOException, InterruptedException {
        System.out.println("Request received at: " + OffsetDateTime.now());
        return apartmentClient.getPictures(apartmentId);

    }

    @GetMapping("/last-polling-time")
    public String getLastPollingTime() {
        return lastPollingTime.toString();
    }
    @PatchMapping("/last-polling-time")
    public void postLastPollingTime() {
        updateLastPollingTime();
    }

    @DeleteMapping("/apartments")
    public String deleteApartments() {
        System.out.println("Delete request received at: " + OffsetDateTime.now());
        apartmentClient.dropCollection("apartments");
        return "Apartments deleted";

    }
    @DeleteMapping("/polls")
    public String deletePolls() {
        System.out.println("Delete request received at: " + OffsetDateTime.now());
        apartmentClient.dropCollection("polls");
        return "Polls deleted";

    }

    @PostMapping("/apartments")
        public String createCollection() throws IOException, InterruptedException {
        System.out.println("Post request received at: " + OffsetDateTime.now());
        apartmentClient.createCollection();
        ApartmentFetcher.apartments(lastPollingTime);
        apartmentClient.insertPoll();
        return  "Collection populated successfully.";
        }
    @CrossOrigin(origins = "http://localhost:5173")
    @PutMapping("/apartments")
    public String updateCollection() throws IOException, InterruptedException {
        System.out.println("Put request received at: " + OffsetDateTime.now());
        ApartmentFetcher.apartments(apartmentClient.getLatestPoll());
        apartmentClient.updateLastPollingTime();
        return  "Collection updated successfully.";
    }

}
