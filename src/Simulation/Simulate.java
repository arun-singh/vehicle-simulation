package Simulation;

import Graph.*;
import Graph.Queue;
import Statistics.Statistics;

import java.util.*;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Arun on 27/01/2017.
 */
public class Simulate {

    private final int ONE_STEP = 1;
    private static int vehicleCounter = 0;
    private int shockwavesGenerated = 0;
    private Grid grid;
    private Random ran = new Random();
    boolean randomise = false;
    private Statistics stats;

    private int totalVehicles = 1000;

    int minCarLength = 4;
    int maxCarLength = 6;

    public Simulate(Grid grid) {
        this.grid = grid;
    }

    public Simulate() {
    }

    public Simulate(int totalVehicles, double[] coords) {
        this.totalVehicles = totalVehicles;
        this.grid = new Grid(coords[0], coords[1], coords[2], coords[3]);
    }

    public Statistics run() {
        shockwavesGenerated = 0;
        List<Link> inputLinks = MapUtil.getInputLinks(grid.getLinkMap(), grid.getAverageConnectivity());
        LinkedHashMap<Integer, Integer> vehiclesMap = new LinkedHashMap<>();
        LinkedHashMap<Integer, Integer> shockMap = new LinkedHashMap<>();

        Vehicle[] vehicles = new Vehicle[totalVehicles];
        System.out.println("Generating vehicles and routes");
        generateVehicles(vehicles, inputLinks);
        System.out.println("Finished generating vehicles and routes");


        int step = 0;
        int vehiclesLeft = -1;
        while (vehiclesLeft != 0) {
            /// For all links calculate density
//            for (Map.Entry<Integer, Link> entry : grid.getLinkMap().entrySet()) {
//                entry.getValue().setRunningDensity(entry.getValue().runningDensity(step));
//            }

            /// For all links process turns
            for (Map.Entry<Integer, Link> entry : grid.getLinkMap().entrySet()) {
                handleIntersections(entry.getValue(), step);
            }

            // Push waiting vehicles
            List<EntryPoint> waiting = QUtil.getWaitingVehicles(grid.getLinkMap());
            for (EntryPoint queue : waiting)
                queue.pushWaiting(step);

            vehiclesLeft = totalVehicles - Statistics.totalVehiclesOutput(grid.getLinkMap());

            System.out.println(vehiclesLeft);
            vehiclesMap.put(step, vehiclesLeft);
            shockMap.put(step, shockwavesGenerated);
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Statistics.diagnostics(grid.getLinkMap());
            step++;
        }
        stats = new Statistics(vehiclesMap, shockwavesGenerated, vehicles, grid.getLinkMap(), totalVehicles, shockMap);
        System.out.println(shockwavesGenerated);
        GUI.Map.getInstance().getMap().removeAllMapPolygons();
        return stats;
    }

    private Vehicle[] generateVehicles(Vehicle[] vehicles, List<Link> inputLinks) {
        for (int i = 0; i < totalVehicles; i++) {
            int length = randomise ? ran.nextInt(maxCarLength - minCarLength + 1) + minCarLength : maxCarLength;
            Vehicle vehicle = new Vehicle(i);
            vehicle.setLength(length);

            List<Link> route = Grid.generateRoute(inputLinks); //grid.oneRouteDemo(grid.getLinkMap());
            vehicle.setRoute(route);

            List<Link> list = vehicle.getRoute();
            Set<Link> set = new HashSet<>(list);
            if (set.size() < list.size()) {
                System.out.println("Dup links!!! in route!!");
            }
            vehicles[i] = vehicle;
        }

        for (int i = 0; i < totalVehicles; i++) {
            vehicles[i].getRoute().get(0).getEntryPoint().push(vehicles[i], 0.0);
        }
        return vehicles;
    }



    private void handleIntersections(Link link, double time) {
        if (link.getQueue().size() == 0)
            return;
        if (link.getServers().size() == 0)
            return;

        for (Server server : link.getServers()) {
            boolean delayProcessed = processPocketDelay(server, link, time);
            if (delayProcessed) continue;

            boolean isOutgoingBlocked = calculateDelay(server, time);
            if (!isOutgoingBlocked) continue;

            // Outgoing link free
            processOutgoingVehicles(link, server, time);
        }
    }

    private boolean processPocketDelay(Server server, Link link, double time) {
        double pocketDelay = server.getPocketDelayedUntil();
        if (pocketDelay > 0) {
            server.setPocketDelayedUntil(pocketDelay - ONE_STEP);
            if (server.getPocketDelayedUntil() <= 0.0) { // Become unblocked so shock-wave formed
                int lookback = link.getLookBackLimit();
                List<Vehicle> queued = QUtil.queuedVehicles(link.getQueue(), time);
                List<Vehicle> forOutgoing = QUtil.getServerComforedVehicles(queued, server.getOutgoing(), lookback);
                if (forOutgoing.size() > 0)
                    processShockwave(forOutgoing, link, time, server.getOutgoing());
                return true;
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean calculateDelay(Server server, double time) {
        boolean isFree = server.getOutgoing().isFree();
        int cap = server.getOutgoing().getQueue().getCapacity();
        if (!isFree) {
            server.setEntriesDeniedCount(server.getEntriesDeniedCount() + 1);
            double shockSpeed = 2.0; //TODO: Use correct densities and flows
            double delayedUntil = server.getOutgoing().getLength() / shockSpeed;
            server.setPocketDelayedUntil(delayedUntil);
            return false;
        }
        return true;
    }

    private void processVehicle(Vehicle ve, Link current, Server server, double time) {
        if (ve.isOnLastLink()) {
            int size = ve.getRoute().size();
            ve.getRoute().get(size - 1).getExitPoint().received(ve, time);
        } else {
            server.getOutgoing().getQueue().push(ve);
        }
        current.getQueue().remove(ve);
    }

    private void processOutgoingVehicles(Link current, Server server, double time) {
        int lookback = current.getLookBackLimit();
        List<Vehicle> queued = QUtil.queuedVehicles(current.getQueue(), time);
        int freeSpaces = server.getOutgoing().getQueue().getCapacity() - server.getOutgoing().getQueue().size();
        int limit = freeSpaces < lookback ? freeSpaces : lookback;
        List<Vehicle> forOutgoing = QUtil.getServerComforedVehicles(queued, server.getOutgoing(), limit);
        for (Vehicle ve : forOutgoing) {
            double speed = server.getOutgoing().speedDensity(time);
            double _eet = time + (server.getOutgoing().getLength() / speed) + server.getDelay();
            ve.setEarliestExitTime(_eet);
            ve.updateRoute();
            processVehicle(ve, current, server, time);
        }
    }

    private void processShockwave(List<Vehicle> vehicles, Link current, double time, Link outgoing) {
        if (current.getQueue().size() == 0)
            return;
        double shockSpeed = 2.0;
        double speedAtCap = 1.0;

        shockwavesGenerated++;
        double latestExitTime = Double.MAX_VALUE;
        Queue beforeUpdates = QUtil.copy(current.getQueue());
        for (Vehicle ve : vehicles) {
            if (ve.getEarliestExitTime() > latestExitTime) break;
            double distanceInFront = QUtil.distanceInFront(beforeUpdates, ve);
            double timeUntilShockwaveImpacts = distanceInFront / shockSpeed;
            double timeToStopLine = distanceInFront / speedAtCap;
            latestExitTime = time + timeUntilShockwaveImpacts + timeToStopLine;
            ve.setEarliestExitTime(latestExitTime);
        }
    }

    private static double shockwaveSpeed(double Q1, double Q2, double K1, double K2) {
        double flow = Q1 + Q2;
        double density = K1 + K2;
        double diff = flow / density;
        return diff;
    }

    public void isGridLocked(){
        List<Link> remaining = grid.getLinkMap().entrySet().stream()
                .filter(l->l.getValue().getQueue().size()>0)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        for(Link link : remaining){
            List<Link> grid = new ArrayList<>();

            Link next = link.getQueue().getHead().getNextLink();
            grid.add(next);
            while(!next.isFree()){
                next = next.getQueue().getHead().getNextLink();
                if(grid.get(0).equals(next)){
                    System.out.println("Gridlocked");
                    return;
                }else{
                    grid.add(next);
                }
            }
        }
    }

    public Statistics getStats() {
        return stats;
    }
}
