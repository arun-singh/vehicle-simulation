package Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Arun on 17/01/2017.
 */
public class Vehicle implements Comparable{
    private double length;
    private Link currentLink;
    private Link nextLink;
    private int ID;
    private double earliestExitTime;
    private List<Link> route = new ArrayList<>();
    private List<Link> seen = new ArrayList<>();
    private int linkCounter = 0;
    private boolean onLastLink = false;
    private double startTime, journeyTime;

    public Vehicle(int ID){
        this.ID = ID;
    }

    public Vehicle(int ID, double earliestExitTime, double length){
        this.ID = ID;
        this.earliestExitTime = earliestExitTime;
        this.length = length;
    }

    public void updateRoute(){
        if(linkCounter != route.size()-1) {
            linkCounter = linkCounter+1;
            setNextLink(route.get(linkCounter));
        }else {
            setOnLastLink(true);
           // System.out.println("Vehicle " + ID + " on last link: " + route.get(route.size() - 1).getId());
        }
    }

    public static Vehicle copy(Vehicle old){
        Vehicle toReturn = new Vehicle(old.getID(), old.getEarliestExitTime(), old.getLength());
        toReturn.setRoute(old.getRoute());
        toReturn.setNextLink(old.getNextLink());
        toReturn.setLinkCounter(old.getLinkCounter());
        return toReturn;
    }

    public static double estimatedSimTime(Vehicle[] vehicles){
        double time = 0.0;
        for(Vehicle veh: vehicles){
            time+=veh.estimatedJourneyTime();
        }
        return time;
    }

    public double estimatedJourneyTime(){
        double et=  route.stream()
                .mapToDouble(l->l.getLength()/l.getvFree())
                .sum();
        return et;
    }

    public double getEarliestExitTime() {
        return earliestExitTime;
    }
    public void setEarliestExitTime(double earliestExitTime) {
        this.earliestExitTime = earliestExitTime;
    }
    public double getLength() {return length;}
    public void setLength(double length) {
        this.length = length;
    }
    public Link getNextLink() {
        return nextLink;
    }
    public void setNextLink(Link nextLink) {
        this.nextLink = nextLink;
    }
    public List<Link> getRoute() {return route;}
    public void setRoute(List<Link> route) {this.route = route;}
    public Link getCurrentLink() {
        return currentLink;
    }
    public void setCurrentLink(Link currentLink) {
        this.currentLink = currentLink;
    }
    public int getID(){return ID;};
    public void setLinkCounter(int linkCounter){ this.linkCounter = linkCounter;}
    public int getLinkCounter(){return linkCounter;}

    @Override
    public int compareTo(Object o) {
        Vehicle toCompare = (Vehicle)o;
        if(this.getEarliestExitTime() < toCompare.getEarliestExitTime())
            return -1;
        if(this.getEarliestExitTime() > toCompare.getEarliestExitTime())
            return 1;
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle vehicle = (Vehicle) o;
        return ID == vehicle.ID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID);
    }


    public boolean isOnLastLink() {
        return onLastLink;
    }

    public void setOnLastLink(boolean onLastLink) {
        this.onLastLink = onLastLink;
    }

    public double getJourneyTime() {
        return journeyTime;
    }

    public void setJourneyTime(double journeyTime) {
        this.journeyTime = journeyTime;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public List<Link> getSeen() {
        return seen;
    }
}
