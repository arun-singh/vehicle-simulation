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
    private double vMin, vFree, kMin, kMax;
    private LinkPolyline polyline;
    private int lookBackLimit;
    private List<QueueServer> servers = new ArrayList<>();
    private int id;
    private int delay = 0;
    private List<Link> adjacencyList;
    private InputQueue inputQueue = new InputQueue(this);
    private OutputQueue outputQueue = new OutputQueue(this);
    private boolean accessable;
    private int connectivity;
    private List<Coordinate> coords = new ArrayList<>();

    public Link(int id, Node source, Node target){
        this.id = id;
        this.source = source;
        this.target = target;
        polyline = new LinkPolyline(this, generateInitialPath());
    }

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
        //System.out.println(k);
        if(k < kMin)
            return vFree;
        if(k > kMax)
            return vMin;

        double u = 1 - Math.pow(((k - kMin)/(kMax-kMin)), 2);
        double v = Math.pow(u, 2);
        double x = vFree - vMin;
        double y = x * v;
        double z = vMin + y;

        return z;
    }

    public double runningDensity(double time){
        int carCount = queue.runningSectionCars(time);
        queue.runningProportion(time, carCount);

        double runningLength = getLength() - (queue.queueLength(time)/getLanes());
        double density = runningLength != 0.0 ? ((double)(carCount)) / (runningLength * getLanes()) : 0.0;
        return density;
    }

    public List<Coordinate> generateInitialPath(){
        List<Double[]> requested = RequestMapData.getCoordinates(getSource(), getTarget());
        List<Point> points = new ArrayList<>();
        List<Coordinate> coordinates = new ArrayList<>();
        JMapViewer map = Map.getInstance().getMap();

        for(int i = 0; i < requested.size(); i++) {
            coordinates.add(new Coordinate(requested.get(i)[0], requested.get(i)[1]));
            points.add(map.getMapPosition(requested.get(i)[0], requested.get(i)[1], false));
        }

        Path2D path = new Path2D.Double();
        Point first = points.get(0);
        path.moveTo(first.getX(),first.getY());
        for(Point p : points){
            path.lineTo(p.getX(), p.getY());
        }
        return coordinates;
    }

    public boolean isFree(){
        return getQueue().isFree();
    }

    public static void createServers(HashMap<Integer, Link> linkMap){
        for (java.util.Map.Entry<Integer, Link> entry : linkMap.entrySet()) {
            Node end = entry.getValue().getTarget();
            Node start = entry.getValue().getSource();

            List<Link> adjacent = linkMap.entrySet().stream()
                    .filter(l -> l.getValue().getSource().equals(end) && !(l.getValue().getTarget().equals(start)))
                    .map(java.util.Map.Entry::getValue)
                    .collect(Collectors.toList());
            entry.getValue().setAdjacencyList(adjacent);

            adjacent.stream().forEach(l -> entry.getValue().getServers().add(
                    new QueueServer(entry.getValue(), l, QueueServer.Type.NORMAL)));
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
    public List<QueueServer> getServers() {
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

    public InputQueue getInputQueue() {
        return inputQueue;
    }

    public OutputQueue getOutputQueue() {
        return outputQueue;
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
}