package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoutesRanker {
    private List<Apartment> apartments;
    private List<Apartment> walkableRoutes;
    private List<Apartment> lightRailRoutes;
    private List<Apartment> busRoutes;

    public RoutesRanker(List<Apartment> apartments) {
        this.apartments = apartments;
        this.walkableRoutes = new ArrayList<>();
        this.lightRailRoutes = new ArrayList<>();
        this.busRoutes = new ArrayList<>();


        this.computeWalkableRoutes();
        this.computeRailRoutes();
        this.computeBusRoutes();

        this.lightRailRoutes.removeAll(this.walkableRoutes);

        this.busRoutes.removeAll(this.walkableRoutes);
        this.busRoutes.removeAll(this.lightRailRoutes);
    }

    private void computeWalkableRoutes() {
        if (this.apartments.isEmpty()){
            return;
        }
        for (Apartment apartment : apartments) {

            try {
                if (apartment.getRoutes().containsKey("Walking") && apartment.getRoutes().get("Walking") <= 20) {
                    this.walkableRoutes.add(apartment);
                }
            } catch (NullPointerException e) {
                System.out.println("Route is null.");
            }
        }

    }
    private void computeRailRoutes() {
        if (this.apartments.isEmpty()){
            return;
        }
        for (Apartment apartment : apartments) {
            try {
                if ((apartment.getRoutes().containsKey("Green Line") && apartment.getRoutes().get("Green Line") <= 30 ) || (apartment.getRoutes().containsKey("Red Line") && apartment.getRoutes().get("Red Line") <= 30) || (apartment.getRoutes().containsKey("Dart") && apartment.getRoutes().get("Dart") <= 30)) {
                    this.lightRailRoutes.add(apartment);
                }
            } catch (NullPointerException e) {
                System.err.println("Route is null.");
            }
        }
    }
    private void computeBusRoutes() {
        if (this.apartments.isEmpty()){
            return;
        }
        for (Apartment apartment : apartments) {
            try {
                if (apartment.getRoutes().containsKey("Bus") && apartment.getRoutes().get("Bus") <= 30) {
                    this.busRoutes.add(apartment);
                }
            } catch (NullPointerException e) {
                System.err.println("Route is null.");
            }
        }
    }
    public List<Apartment> getWalkableRoutes() {
        return this.walkableRoutes;
    }
    public List<Apartment> getLightRailRoutes() {
        return this.lightRailRoutes;
    }
    public List<Apartment> getBusRoutes() {
        return busRoutes;
    }
}
