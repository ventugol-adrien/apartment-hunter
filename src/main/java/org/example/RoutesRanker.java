package org.example;

import java.util.ArrayList;
import java.util.List;

public class RoutesRanker {
    private final List<Apartment> apartments;
    private final List<Apartment> walkableRoutes;
    private final List<Apartment> lightRailRoutes;
    private final List<Apartment> busRoutes;

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
    public void addToRoutes (List<Apartment> apartments, Apartment apartment) {
        apartments.add(apartment);
    }

    public void computeWalkableRoutes() {
        if (this.apartments.isEmpty()){
            return;
        }
        for (Apartment apartment : apartments) {

            try {
                if (apartment.getRoutes().containsKey("walk") && apartment.getRoutes().get("walk") <= 20) {
                    this.addToRoutes(this.walkableRoutes,apartment);
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
                if ((apartment.getRoutes().containsKey("green") && apartment.getRoutes().get("green") <= 30 ) || (apartment.getRoutes().containsKey("red") && apartment.getRoutes().get("red") <= 30) || (apartment.getRoutes().containsKey("dart") && apartment.getRoutes().get("dart") <= 30)) {
                    this.addToRoutes(this.lightRailRoutes,apartment);
                    //add to MongoDB
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
                if (apartment.getRoutes().containsKey("bus") && apartment.getRoutes().get("bus") <= 30) {
                    this.addToRoutes(this.busRoutes,apartment);
                }
            } catch (NullPointerException e) {
                System.err.println("Route is null.");
            }
        }
    }
    public List<Apartment> getApartments() {
        return this.apartments;
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
