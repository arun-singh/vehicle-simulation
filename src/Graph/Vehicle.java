package Graph;

import Graph.Link;

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
    private List<Link> route;
    private int linkCounter = 0;

    public Vehicle(int ID){
        this.ID = ID;
    }

    public void updateRoute(){
        if(linkCounter != route.size()) {
            setNextLink(route.get(linkCounter++));
        }else
            System.out.println("Vehicle " + ID + " on last link: " + route.get(route.size()-1).getId());
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
}
