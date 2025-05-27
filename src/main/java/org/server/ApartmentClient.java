package org.server;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import org.bson.BsonDateTime;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;
import org.example.Apartment;
import org.threeten.bp.Instant;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ApartmentClient {
    private final MongoClientSettings settings;
    private HashMap<String, MongoCollection<Document>> collections = new HashMap<>();
    private MongoClient mongoClient;
    public ApartmentClient(){
        String connectionString = String.format("mongodb+srv://admin:%s@cluster0.akpza.mongodb.net/?appName=Cluster0",System.getenv("DB_PWD"));
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        this.settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();
        mongoClient = MongoClients.create(this.settings);
        MongoDatabase database = mongoClient.getDatabase("apartment-locator");
        MongoCollection<Document> apartmentsCollection = database.getCollection("apartments");
        MongoCollection<Document> pollsCollection = database.getCollection("polls");
        MongoCollection<Document> picsCollection = database.getCollection("pics");
        collections.put("apartments", apartmentsCollection);
        collections.put("polls", pollsCollection);
        collections.put("pics", picsCollection);

    }
    private Document createQuery(int walking, int tram, int bus) {
        Document query = new Document();
        List<Document> orConditions = new ArrayList<>();

        if (walking == 1) {
            orConditions.add(new Document("itineraries.walk", new Document("$lte", 20)));
        }
        if (tram == 1) {
            orConditions.add(new Document("itineraries.red", new Document("$lte", 30)));
            orConditions.add(new Document("itineraries.green", new Document("$lte", 30)));
            orConditions.add(new Document("itineraries.dart", new Document("$lte", 30)));
        }
        if (bus == 1) {
            orConditions.add(new Document("itineraries.bus", new Document("$lte", 30)));
        }
        if (!orConditions.isEmpty()) {
            query.put("$or", orConditions);
        }

        return query;
    }

    public String getApartments(int walking, int tram, int bus){
        try {
            Document query = createQuery(walking, tram, bus);
            return collections.get("apartments").find(query)
                    .map(Document::toJson)
                    .into(new ArrayList<>())
                    .toString();
        } catch (MongoException e) {
            throw new MongoException("Failed to query for apartments: ", e);
        }
    }
    private InsertOneResult insertApartment(Apartment apartment) {
        try {
            Document document = Document.parse(apartment.serialize().toString());
            document.put("insertedAt", new BsonDateTime(Instant.now().toEpochMilli()));
            InsertOneResult result = collections.get("apartments").insertOne(document);
            System.out.println("Successfully inserted an appartment with id: " + result.getInsertedId());
            return result;
        } catch (MongoException e) {
            e.printStackTrace();
            throw new MongoException("Failed to insert apartment: " + apartment.getUrl(), e);
        }
    }

    public InsertOneResult safeInsertApartment(Apartment apartment) {
        try {
            if (!isApartmentIndexed(apartment)) {
                return insertApartment(apartment);
            }
            System.out.println("Apartment already indexed.");
            return null;
        } catch (MongoException e) {
            throw new MongoException("Failed to insert apartment: " + apartment.getUrl(), e);
        }
    }

    public InsertOneResult insertPictures(Object id, List<String> pictures) {
        try {
            Document document = new Document();
            document.put("apartment", id);
            document.put("pictures", pictures);
            InsertOneResult result = collections.get("pics").insertOne(document);
            System.out.println("Successfully inserted pictures with id: " + result.getInsertedId());
            return result;
        } catch (MongoException e) {
            throw new MongoException("Failed to insert pictures: ", e);
        }
    }

    public List<String> getPictures(@BsonId String id) {
        try {
            // Convert the id to ObjectId
            // Assuming id is a string representation of ObjectId
            // If id is not a valid ObjectId, this will throw an exception
            ObjectId objectId = new ObjectId(id);
            Document query = new Document("apartment", new BsonObjectId(objectId));
            Document foundApartment = collections.get("pics").find(query).first();
            if (foundApartment != null) {
                System.out.println("Found apartment: " + foundApartment.toJson());
                return (List<String>) foundApartment.get("pictures");
            }
            System.out.println("No apartment found with the given URL.");
            return null;
        } catch (MongoException e) {
            throw new MongoException("Failed to query for apartment with url: " + id, e);
        }
    }

    public void insertPoll(){
        try {
            Document document = new Document();
            document.put("polledAt", new BsonDateTime(Instant.now().toEpochMilli()));
            InsertOneResult result = collections.get("polls").insertOne(document);
            System.out.println("Successfully inserted a poll with id: " + result.getInsertedId());
        } catch (Exception e) {
            throw new MongoException("Failed to insert poll: ", e);
        }
    }

    public OffsetDateTime getLatestPoll(){
        try {
            Document latestDocument = collections.get("polls").find()
                    .sort(Sorts.descending("pollDate"))
                    .limit(1)
                    .first();
            assert latestDocument != null;
            System.out.println("Latest poll: " + latestDocument.toJson());
            return OffsetDateTime.ofInstant(latestDocument.getDate("polledAt").toInstant(), java.time.ZoneOffset.UTC);
        } catch (Exception e) {
            throw new MongoException("Failed to insert poll: ", e);
        }
    }

    public void updateLastPollingTime() {
        try {
            Document query = collections.get("polls").find()
                    .sort(Sorts.descending("pollDate"))
                    .limit(1)
                    .first();
            Document update = new Document("$set", new Document("polledAt", new BsonDateTime(Instant.now().toEpochMilli())));
            assert query != null;
            collections.get("polls").updateOne(query, update);
            System.out.println("Successfully updated the last polling time to current time.");
        } catch (MongoException e) {
            throw new MongoException("Failed to update last polling time: ", e);
        }
    }



    public boolean isApartmentIndexed (Apartment apartment) {
        try {
            Document query = new Document("url", apartment.getUrl());
            Document foundApartment = collections.get("apartments").find(query).first();
            if (foundApartment != null) {
                System.out.println("Found apartment: " + foundApartment.toJson());
                return true;
            }
            System.out.println("No apartment found with the given URL.");
            return false;
        } catch (MongoException e) {
            throw new MongoException("Failed to query for apartment with url: " + apartment.getUrl(), e);

        }
    }

    public boolean isUrlIndexed (String url) {
        try {
            Document query = new Document("url", url);
            Document foundApartment = collections.get("apartments").find(query).first();
            if (foundApartment != null) {
                System.out.println("Found apartment: " + foundApartment.toJson());
                return true;
            }
            System.out.println("No apartment found with the given URL.");
            return false;
        } catch (MongoException e) {
            throw new MongoException("Failed to query for apartment with url: " + url, e);

        }
    }

    public String getWalkingDistance(OffsetDateTime cutoffDateTime) {
        try {
            Document query = new Document("itineraries.walk", new Document("$lte", 20)
                    .append("discoveredAt", new Document("$gt", cutoffDateTime.toString())));
            return collections.get("apartments").find(query)
                    .map(Document::toJson)
                    .into(new ArrayList<>())
                    .toString();

        } catch (MongoException e) {
            throw new MongoException("Failed to query for apartments within 20 mins of William Fry: ", e);
        }
    }

    public String dropCollection(String name){
        try {
            collections.get(name).drop();
            collections.remove(name);
            return "Collection dropped successfully";
        } catch (MongoException e) {
            System.err.println("Error dropping collection: " + e.getMessage());
            return  "Error dropping collection: " + e.getMessage();
        }
    }
    public String createCollection(){
        try {
            MongoDatabase database = mongoClient.getDatabase("apartment-locator");
            database.createCollection("apartments");
            database.createCollection("polls");
            collections.put("apartments", database.getCollection("apartments"));
            collections.put("polls", database.getCollection("polls"));
            return "Collections created successfully";
        } catch (MongoException e) {
            System.err.println("Error creating collection: " + e.getMessage());
            return  "Error creating collection: " + e.getMessage();
        }
    }
}