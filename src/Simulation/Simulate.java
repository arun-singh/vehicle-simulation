package Simulation;

import Graph.*;
import Graph.Queue;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Arun on 27/01/2017.
 */
public class Simulate {

    private final int SIMULATION_STEPS = 30;
    private final int ONE_STEP = 1;
    private static int vehicleCounter = 0;
    public int shockwavesGenerated = 0;
    private Grid grid;
    Random ran = new Random();
    boolean randomise = true;
    int totalVehicles = 5000;
    int roadLengthMax = 500;
    int roadLengthMin = 50;
    int minLookBack = 1;
    int maxLookBack = 10;
    int minLanes = 1;
    int maxLane = 2;
    int minCap = 1;
    int maxCap = 10;
    int minCarLength = 4;
    int maxCarLength = 6;

    public Simulate(Grid grid) {
        this.grid = grid;
    }
    public Simulate(){}

    public Statistics run(){
        Grid grid = new Grid();
        shockwavesGenerated = 0;
        generateLinks(grid.getNodes(), grid.getNodePairs(), grid.getLinkMap());
        Link.createServers(grid.getLinkMap());
        List<Link> inputLinks = Statistics.getInputLinks(grid.getLinkMap());

        Vehicle[] vehicles = new Vehicle[totalVehicles];
        generateVehicles(vehicles, inputLinks);

        int step = 0;
        int vehiclesLeft = Integer.MAX_VALUE;
        while (vehiclesLeft!=0) {

            // For all links calculate density
            for (Map.Entry<Integer, Link> entry : grid.getLinkMap().entrySet()) {
                entry.getValue().setRunningDensity(entry.getValue().runningDensity(step));
            }

            // For all links process turns
            for (Map.Entry<Integer, Link> entry : grid.getLinkMap().entrySet()) {
                handleIntersections(entry.getValue(), step);
            }

            // Push waiting vehicles
            List<InputQueue> waiting = QUtil.getWaitingVehicles(grid.getLinkMap());
            for(InputQueue queue: waiting)
                queue.pushWaiting(step);

            vehiclesLeft = totalVehicles - Statistics.totalVehiclesOutput(grid.getLinkMap());
            step++;
        }
        return new Statistics(step, shockwavesGenerated, vehicles, grid.getLinkMap());
    }

    private Vehicle[] generateVehicles(Vehicle[] vehicles, List<Link> inputLinks) {
        for(int i =0; i<totalVehicles;i++){
            int length = randomise ? ran.nextInt(maxCarLength - minCarLength + 1) + minCarLength : maxCarLength;
            Vehicle vehicle = new Vehicle(i);
            vehicle.setLength(length);

            int randomStartingLink = ran.nextInt((inputLinks.size()-1) + 1);
            Link next = inputLinks.get(randomStartingLink);
            vehicle.getRoute().add(next);
            int serverSize = next.getServers().size();

            while(serverSize!=0){
                int serverIndex = ran.nextInt((serverSize-1) + 1);
                next = next.getServers().get(serverIndex).getOutgoing();
                vehicle.getRoute().add(next);
                serverSize = next.getServers().size();
            }
            vehicles[i] = vehicle;
        }

        for(int i = 0; i<totalVehicles;i++){
            vehicles[i].getRoute().get(0).getInputQueue().push(vehicles[i], 0.0);
        }
        return vehicles;
    }

    private void generateLinks(Node[] nodes, List<Integer[]> nodePairs, HashMap<Integer, Link> linkMap) {
        for(int i = 0; i<nodePairs.size(); i++) {
            for(int j = 0 ; j < 2 ; j++){
                int length = randomise ? ran.nextInt(roadLengthMax - roadLengthMin + 1) + roadLengthMin : roadLengthMax;
                int capacity = length/4;
                int id = (i*2)+j;

                Integer[] pair = nodePairs.get(i);
                Node start = nodes[pair[j==0?0:1]-1];
                Node end = nodes[pair[j==0?1:0]-1];

                Link link = new Link(id, capacity, start, end);
                link.setLength(length);
                int lookBack = randomise ? ran.nextInt(maxLookBack - minLookBack + 1) + minLookBack : maxLookBack;
                link.setLookBackLimit(lookBack);
                int lanes =  randomise ? ran.nextInt(maxLane - minLanes + 1) + minLanes : maxLane;
                link.setLanes(lanes);

                link.setkMin(0);
                link.setkMax(5);
                link.setvMin(1);
                link.setvFree(5);

                linkMap.put(id, link);
            }
        }
    }

    public void handleIntersections(Link link, double time){
        if(link.getQueue().size() == 0)
            return;
        if(link.getServers().size() == 0)
            return;

        for (QueueServer server : link.getServers()) {

            boolean delayProcessed = processPocketDelay(server, link, time);
            if(delayProcessed) continue;

            boolean isOutgoingBlocked = calculateDelay(server, time);
            if(isOutgoingBlocked) continue;

            // Outgoing link free
            processOutgoingVehicles(link, server, time);
        }
    }

    public boolean processPocketDelay(QueueServer server, Link link, double time){
        double pocketDelay = server.getPocketDelayedUntil();
        if(pocketDelay>0) {
            server.setPocketDelayedUntil(pocketDelay - ONE_STEP);
            if(server.getPocketDelayedUntil() == 0.0){ // Become unblocked so shock-wave formed
                int lookback = link.getLookBackLimit();
                List<Vehicle> queued = QUtil.queuedVehicles(link.getQueue(), time);
                List<Vehicle> forOutgoing = QUtil.getServerComforedVehicles(queued, server.getOutgoing(), lookback);
                for(Vehicle ve : forOutgoing){
                    processShockwave(ve, link, time, server.getOutgoing());
                }
                return true;
            }
            return true;
        }else{
            return false;
        }
    }

    public boolean calculateDelay(QueueServer server, double time){

        boolean isFree = server.getOutgoing().isFree();
        if(!isFree){

            double shockSpeed = 2.0; //TODO: Use correct densities and flows
            double delayedUntil = server.getOutgoing().getLength() / shockSpeed;
            server.setPocketDelayedUntil(delayedUntil);
            return true;
        }
        return false;
    }


    public void processVehicle(Vehicle ve, Link current, QueueServer server, double time){
        if(ve.isOnLastLink()){
            int size = ve.getRoute().size();
            ve.getRoute().get(size-1).getOutputQueue().received(ve, time);
        }else {
            server.getOutgoing().getQueue().push(ve);
        }
        current.getQueue().remove(ve);
    }

    public void processOutgoingVehicles(Link current, QueueServer server, double time){
        int lookback = current.getLookBackLimit();
        List<Vehicle> queued = QUtil.queuedVehicles(current.getQueue(), time);
        List<Vehicle> forOutgoing = QUtil.getServerComforedVehicles(queued, server.getOutgoing(), lookback);
        for (Vehicle ve : forOutgoing) {
            double speed = server.getOutgoing().speedDensity(time);
            double _eet = time + (server.getOutgoing().getLength() / speed) + server.getDelay();
            ve.setEarliestExitTime(_eet);
            ve.updateRoute();
            processVehicle(ve, current, server, time);
        }
    }

    public void processShockwave(Vehicle start, Link current, double time, Link outgoing){
        //TODO: Use correct densities and flows
        //NOTE: Currently, vehicles in running section affected by shock-wave - distance in front could be made more
        //      accurate by working out distance travelled (not in mesoscopic scope)
        if(current.getQueue().size()==0)
            return;
        double shockSpeed = 2.0;
        double speedAtCap = 3.0;
        List<Vehicle> toUpdate = current.getQueue().size() == 1 ? new ArrayList<Vehicle>(){{add(start);}}
                                                                : QUtil.getVehiclesBehind(current.getQueue(), start);
        shockwavesGenerated++;
        double latestExitTime = toUpdate.get(0).getEarliestExitTime();
        Queue beforeUpdates = QUtil.copy(current.getQueue());
        for (Vehicle ve: toUpdate){
            if(ve.getEarliestExitTime() > latestExitTime) break;
            double distanceInFront = QUtil.distanceInFront(beforeUpdates, ve);
            double timeUntilShockwaveImpacts =  distanceInFront / shockSpeed;
            double timeToStopLine = distanceInFront / speedAtCap;
            latestExitTime = time + timeUntilShockwaveImpacts + timeToStopLine;
            ve.setEarliestExitTime(latestExitTime);
        }
    }

    public static boolean pushNewVehicle(Link link, double time, List<Link> route) {
        if (!link.isFree())
            return false;

        double speed = link.speedDensity(time);
        double _eet = time + (link.getLength() / speed);

        Vehicle vehicle = new Vehicle(++vehicleCounter);
        vehicle.setRoute(route);
        vehicle.setLength(4.0);
        vehicle.setEarliestExitTime(_eet);
        vehicle.updateRoute();
        link.getQueue().push(vehicle);
        return true;
    }

    public static double shockwaveSpeed(double Q1, double Q2, double K1, double K2){
        double flow = Q1 + Q2;
        double density = K1 + K2;
        double diff = flow / density;
        return diff;
    }

    public static void main(String[] args){
        Simulate simulate = new Simulate();
        List<Statistics> stats = new ArrayList<>();
        for(int i =0; i<100;i++) {
            stats.add(simulate.run());
        }
    }
}
