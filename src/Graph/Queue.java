package Graph;

import GUI.LinkController;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

/**
 * Created by Arun on 17/01/2017.
 */
public class Queue extends PriorityQueue<Vehicle> implements QueueTemplate {

    private int capacity;
    private double runningProportion = 0.5;
    private Link link;
    LinkController controller;

    public Queue(){}
    public Queue(int capacity, Link link){
        this.capacity = capacity;
        this.link = link;
    }
    public Queue(int capacity){
        this.capacity = capacity;
    }
    public Queue(int capacity, Link link, LinkController controller) {
        this.capacity = capacity;
        this.link = link;
        this.controller = controller;
    }

    @Override
    public Vehicle pop() {
        return poll();
    }

    @Override
    public void push(Vehicle vehicle) {
        vehicle.getSeen().add(link);
        add(vehicle);
    }

    @Override
    public boolean isFree() {
        return size() < capacity;
    }

    @Override
    public int runningSectionCars(double _t) {
        return this.stream()
                .filter(v -> v.getEarliestExitTime() >= _t)
                .collect(Collectors.toList())
                .size();
    }

    @Override
    public double queueLength(double _t) {
        return this.stream()
                .filter(v -> v.getEarliestExitTime() < _t)
                .mapToDouble(v->v.getLength())
                .sum();
    }

    @Override
    public Vehicle getHead() {
        return peek();
    }

    public void runningProportion(double time, int runningCars){
        double size = size();
        double newProportion = ((double)runningCars)/size;
        if(newProportion != runningProportion)
            setRunningProportion(newProportion);
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getRunningProportion() {
        return runningProportion;
    }

    public void setRunningProportion(double runningProportion) {
        this.runningProportion = runningProportion;
        //controller.onChange(link, runningProportion);
    }
    public Link getLink() {
        return link;
    }
    public LinkController getController() {
        return controller;
    }
    public void setController(LinkController controller) {
        this.controller = controller;
    }
}
