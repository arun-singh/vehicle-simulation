package Graph;

import GUI.*;
import GUI.Map;
import org.openstreetmap.gui.jmapviewer.*;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Arun on 17/01/2017.
 */
public class Link{
    private Node target, source;
    private double length;
    private Queue queue;
    private int lanes;
    private double runningLength;
    private double runningDensity = 0.0;
    private double vMin;
    private double vFree;
    private double kMin;
    private double kMax;
    private LinkPolyline polyline;
    private int lookBackLimit;
    private List<Server> servers = new ArrayList<>();
    private int id;
    private int delay = 0;
    private List<Link> adjacencyList;
    private EntryPoint entryPoint = new EntryPoint(this);
    private ExitPoint exitPoint = new ExitPoint(this);
    private boolean accessable;
    private int connectivity;
    private List<Coordinate> coords = new ArrayList<>();

    public Link(int id){this.id = id;}
    public Link(int id, int queueCapacity){
        this.id = id;
        this.queue = new Queue(queueCapacity, this);
    }

    public Link(int id, int queueCapacity, Node source, Node target){
        this.id = id;
        this.queue = new Queue(queueCapacity, this);
        this.source = source;
        this.target = target;
    }

    public double speedDensity(double time){
        double k = runningDensity(time);
        if(k <= kMin)
            return vFree;
        if(k > kMax)
            return vMin;

        double u = 1 - Math.pow(((k - kMin)/(kMax-kMin)), 1);
        double v = Math.pow(u, 1);
        double x = vFree - vMin;
        double y = x * v;
        double z = vMin + y;

        return z;
    }

    public double speedDensity2(double k){
        if(k <= kMin)
            return vFree;
        if(k > kMax)
            return vMin;

        double u = 1 - Math.pow(((k - kMin)/(kMax-kMin)), 1);
        double v = Math.pow(u, 1);
        double x = vFree - vMin;
        double y = x * v;
        double z = vMin + y;

        return z;
    }

    public double speedDensity3(double time){
        int qSize = queue.size();
        double prop = -1;
        if(qSize==0){
            return 11;
        }else{
            prop = (((double)qSize)/queue.getCapacity()) * 100;
        }

        if(prop>0 && prop<=10){
            return 10;
        }else if(prop>10 && prop<=20){
            return 9;
        }else if(prop>20 && prop<=30){
            return 8;
        }else if(prop>30 && prop<=40){
            return 7;
        }else if(prop>40 && prop<=50){
            return 6;
        }else if(prop>50 && prop<=60){
            return 5;
        }else if(prop>60 && prop<=70){
            return 4;
        }else if(prop>70 && prop<=80){
            return 3;
        }else if(prop>80 && prop<=90){
            return 2;
        }else if(prop>90 && prop<=100){
            return 1;
        }
        return 1;
    }

    public double runningDensity(double time){
        int carCount = queue.runningSectionCars(time);
        double runningLength = queue.size() != 0 ? getLength() - (queue.queueLength(time)/getLanes()) : 0;
        double density = runningLength != 0.0 ? ((double)(carCount)) / (runningLength * getLanes()) : 0;
        return density * 100;
    }

    public boolean isFree(){
        return getQueue().isFree();
    }

    public static void createServers(HashMap<Integer, Link> linkMap){
        for (java.util.Map.Entry<Integer, Link> entry : linkMap.entrySet()) {
            Node end = entry.getValue().getTarget();
            Node start = entry.getValue().getSource();

            List<Link> adjacent = linkMap.entrySet().stream()
                    .parallel()
                    .filter(l -> l.getValue().getSource().equals(end) && !(l.getValue().getTarget().equals(start)))
                    .map(java.util.Map.Entry::getValue)
                    .collect(Collectors.toList());
            entry.getValue().setAdjacencyList(adjacent);

            adjacent.stream()
                .forEach(l -> entry.getValue().getServers().add(
                    new Server(entry.getValue(), l, Server.Type.NORMAL)));
        }
    }

    public Queue getQueue() {return queue;}
    public int getLanes() {
        return lanes;
    }
    public double getLength() {
        return length;
    }
    public void setLanes(int lanes) {
        this.lanes = lanes;
    }
    public void setQueue(Queue queue) {
        this.queue = queue;
    }
    public void setLength(double length) {
        this.length = length;
    }
    public Node getTarget() {
        return target;
    }
    public double getRunningLength() { return runningLength; }
    public void setRunningLength(double runningLength) { this.runningLength = runningLength; }
    public double getRunningDensity() {return runningDensity;}
    public void setRunningDensity(double runningDensity) {this.runningDensity = runningDensity;}
    public void setvMin(double vMin) { this.vMin = vMin;}
    public void setvFree(double vFree) {this.vFree = vFree; }
    public void setkMin(double kMin) {this.kMin = kMin;}
    public void setkMax(double kMax) {this.kMax = kMax;}
    public Node getSource() { return source; }
    public LinkPolyline getPolyline() { return polyline; }
    public void setPolyline(List<Coordinate> coords){polyline = new LinkPolyline(this, coords);}
    public int getLookBackLimit() {
        return lookBackLimit;
    }
    public void setLookBackLimit(int lookBackLimit) {
        this.lookBackLimit = lookBackLimit;
    }
    public List<Server> getServers() {
        return servers;
    }
    public int getId() {
        return id;
    }
    public int getDelay() {
        return delay;
    }
    public double getvFree(){return vFree;}

    public void setDelay(int delay) {
        this.delay = delay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return id == link.id &&
                Objects.equals(target, link.target) &&
                Objects.equals(source, link.source);
    }
    @Override
    public int hashCode() {
        return Objects.hash(target, source, id);
    }

    public List<Link> getAdjacencyList() {
        return adjacencyList;
    }

    public void setAdjacencyList(List<Link> adjacencyList) {
        this.adjacencyList = adjacencyList;
    }

    public EntryPoint getEntryPoint() {
        return entryPoint;
    }

    public ExitPoint getExitPoint() {
        return exitPoint;
    }

    public void setAccessable(boolean accessable) {
        this.accessable = accessable;
    }

    public boolean isAccessable() {
        return accessable;
    }

    public int getConnectivity() {
        return connectivity;
    }

    public void setConnectivity(int connectivity) {
        this.connectivity = connectivity;
    }

    public List<Coordinate> getCoords() {
        return coords;
    }

    public void setCoords(List<Coordinate> coords) {
        this.coords = coords;
    }

    public double getvMin() {
        return vMin;
    }

    public double getkMin() {
        return kMin;
    }

    public double getkMax() {
        return kMax;
    }
}