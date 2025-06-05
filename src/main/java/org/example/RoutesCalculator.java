package org.example;

import com.google.api.gax.core.NoCredentialsProvider;
import com.google.maps.routing.v2.*;
import com.google.protobuf.Duration;
import com.google.type.LatLng;
import org.jetbrains.annotations.Nullable;
import org.server.SecretManager;

import java.io.IOException;
import java.util.*;

public class RoutesCalculator {

    private static final Waypoint willyFries = buildWaypoint(53.3438192,-6.2413203);
    private static final Waypoint Smithfield = buildWaypoint(53.347097, -6.2803257);
    public static Waypoint getWillyFries() {
        return willyFries;
    };
    public static Waypoint getSmithfield() {
        return Smithfield;
    };

    public static void main(String[] arguments) {
        //53.3338524,-6.2310308
        //Smithfield: [53.347097,-6.2803257]
        //Abbey St: 53.3485942,-6.2610689

        //Oconnel: 53.348848,-6.262455
        //Broadstone: 53.3540352,-6.2746791
        //Location willyFries = Location.newBuilder().setLatLng(LatLng.newBuilder().setLatitude(53.3438192).setLongitude(-6.2413203).build()).build();
        Waypoint waypoint1 = getSmithfield();
        Waypoint waypoint2 = getWillyFries();
        System.out.println(calculateTravelTime(waypoint1, waypoint2));

    }
    private static String getApiKey() {
        try {
            return new SecretManager().getSecret("MAPS_KEY");
        } catch (Exception e) {
            System.err.println("Failed to retrieve API key from Secrets Manager: " + e.getMessage());
            return System.getenv("MAPS_KEY");
        }
    }
    private static RoutesClient buildRoutesClient() throws IOException {
        return RoutesClient.create(RoutesSettings.newBuilder()
                .setHeaderProvider(() -> {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("X-Goog-FieldMask", "routes.duration,routes.legs.steps.transit_details");
                    headers.put("x-goog-api-key", getApiKey());
                    return headers;
                })
                .setCredentialsProvider(NoCredentialsProvider.create()) // Disable ADC
                .build());

    }

    public static Waypoint buildWaypoint(double lat, double lng) {
        return Waypoint.newBuilder().setLocation(
                Location.newBuilder().setLatLng(
                        LatLng.newBuilder()
                                .setLongitude(lng)
                                .setLatitude(lat)
                                .build()))
                .build();
    }

    public static Waypoint buildWaypoint(ArrayList<Double> coordinates) {
        return Waypoint.newBuilder().setLocation(
                        Location.newBuilder().setLatLng(
                                LatLng.newBuilder()
                                        .setLongitude(coordinates.get(0))
                                        .setLatitude(coordinates.get(1))
                                        .build()))
                .build();
    }

    public static Waypoint buildWaypoint(double[] coordinates) {
        return Waypoint.newBuilder().setLocation(
                        Location.newBuilder().setLatLng(
                                LatLng.newBuilder()
                                        .setLongitude(coordinates[0])
                                        .setLatitude(coordinates[1])
                                        .build()))
                .build();
    }

    private static ComputeRoutesResponse computeRailRoutes(RoutesClient client , Waypoint origin, Waypoint destination) throws IOException {
        ArrayList<TransitPreferences.TransitTravelMode> modes = new ArrayList<TransitPreferences.TransitTravelMode>();
        modes.add(TransitPreferences.TransitTravelMode.LIGHT_RAIL);
        modes.add(TransitPreferences.TransitTravelMode.RAIL);
        modes.add(TransitPreferences.TransitTravelMode.TRAIN);
        return client.computeRoutes(ComputeRoutesRequest.newBuilder()
                .setOrigin(origin)
                .setDestination(destination)
                .setTravelMode(RouteTravelMode.TRANSIT).setTransitPreferences(TransitPreferences.newBuilder().addAllAllowedTravelModes(modes).build())
                .setComputeAlternativeRoutes(true)
                .build());

    };
    private static ComputeRoutesResponse computeWalkingRoute(RoutesClient client , Waypoint origin, Waypoint destination) throws IOException {
        return client.computeRoutes(ComputeRoutesRequest.newBuilder()
                .setOrigin(origin)
                .setDestination(destination)
                .setTravelMode(RouteTravelMode.WALK)
                .build());

    };
    private static ComputeRoutesResponse computeRailRoutes(RoutesClient client , Waypoint origin, Waypoint destination, boolean busOnly) throws IOException {
        ArrayList<TransitPreferences.TransitTravelMode> modes = new ArrayList<>();
        if (busOnly) {
            modes.add(TransitPreferences.TransitTravelMode.BUS);
        } else {
            modes.add(TransitPreferences.TransitTravelMode.LIGHT_RAIL);
            modes.add(TransitPreferences.TransitTravelMode.RAIL);
            modes.add(TransitPreferences.TransitTravelMode.TRAIN);
        }

        return client.computeRoutes(ComputeRoutesRequest.newBuilder()
                .setOrigin(origin)
                .setDestination(destination)
                .setTravelMode(RouteTravelMode.TRANSIT).setTransitPreferences(TransitPreferences.newBuilder().addAllAllowedTravelModes(modes).build())
                .setComputeAlternativeRoutes(true)
                .build());

    };

    public static HashMap<String,Long> calculateTravelTime(Waypoint origin , Waypoint destination) {
        try {
            RoutesClient client = buildRoutesClient();
            ComputeRoutesResponse response = computeRailRoutes(client, origin , destination);
            ComputeRoutesResponse walkingResponse = computeWalkingRoute(client, origin , destination);
            return getRoutes(response, walkingResponse);

        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
        return null;
    }
    @Nullable
    public static HashMap<String,Long> calculateTravelTime(Apartment origin , Waypoint destination) {
        try {
            RoutesClient client = buildRoutesClient();
            ComputeRoutesResponse response = computeRailRoutes(client, origin.getLocation() , destination);
            ComputeRoutesResponse walkingResponse = computeWalkingRoute(client, origin.getLocation() , destination);
            return getRoutes(response, walkingResponse);

        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
        return null;
    }

    @Nullable
    private static HashMap<String, Long> getRoutes(ComputeRoutesResponse response, ComputeRoutesResponse walkingResponse) {
        HashMap<String,Long> routes = extractTransitModes(response, walkingResponse);
        if (routes != null){
            List<Long> times = new ArrayList<Long>(routes.values());
            if (times.stream().anyMatch(duration-> duration <= 30 )){
                return routes;
            }
        }
        return null;
    }
    @Nullable
    private static HashMap<String,Long> extractTransitModes(ComputeRoutesResponse response, ComputeRoutesResponse walkingResponse) {
        HashMap<String,Long> transitLinesWithDuration = null;
        Route walkingRoute = walkingResponse.getRoutesList().getFirst();
        if (response.getRoutesCount() > 0) {
            transitLinesWithDuration = new HashMap<String,Long>();
            transitLinesWithDuration.put("walk", Math.floorDiv(walkingRoute.getDuration().getSeconds(),60));
            for (Route route : response.getRoutesList()) {
                Duration routeDurationSeconds = route.getDuration(); // Get the route duration in seconds.
                long duration =  Math.floorDiv(routeDurationSeconds.getSeconds(),60);

                for (RouteLeg leg : route.getLegsList()) {
                    for (RouteLegStep step : leg.getStepsList()) {
                        if (step.hasTransitDetails()) {
                            TransitLine line = step.getTransitDetails().getTransitLine();
                            String displayName = line.getNameShort();

                            if (!displayName.isEmpty()) {
                                String name = switch (displayName) {
                                    case "Green" -> "green";
                                    case "Red Line" -> "red";
                                    case "Dart" -> "dart";
                                    default -> "bus";
                                };
                                if (!transitLinesWithDuration.containsKey(name) || Math.floorDiv(routeDurationSeconds.getSeconds(),60) < transitLinesWithDuration.get(name)){
                                    transitLinesWithDuration.put(name, duration);
                                }
                            }
                        }
                    }
                }
            }
        }
        return transitLinesWithDuration;
    }
}