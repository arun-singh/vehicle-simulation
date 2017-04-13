package Simulation;

import GUI.Display;
import Graph.*;
import Graph.Queue;
import Statistics.Statistics;

import java.util.*;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Created by Arun on 27/01/2017.
 */
public class Simulate implements Callable<Statistics>{

    private final int ONE_STEP = 1;
    public static boolean running = false;
    private int shockwavesGenerated = 0;
    private Grid grid;
    private Random ran = new Random();
    boolean randomise = false;
    private Statistics stats;
    public static final int INCREMENTAL_PUSH_SIZE = 1000;

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
        running = true;
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
        long start = System.currentTimeMillis();
        while (vehiclesLeft != 0) {
            /// For all links process turns
            for (Map.Entry<Integer, Link> entry : grid.getLinkMap().entrySet()) {
                handleIntersections(entry.getValue(), step);
            }

            // Push waiting vehicles
            List<EntryPoint> waiting = QUtil.getWaitingVehicles(grid.getLinkMap());
            for (EntryPoint queue : waiting)
                queue.pushWaiting(step);

            vehiclesLeft = totalVehicles - GridUtil.totalVehiclesOutput(grid.getLinkMap());

            System.out.println(vehiclesLeft);
            vehiclesMap.put(step, vehiclesLeft);
            shockMap.put(step, shockwavesGenerated);
            try {
                Thread.sleep(Display.stats.isSelected() ? 0 : 150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //isGridLocked();
           // Statistics.diagnostics(grid.getLinkMap());
            step++;
        }
        long end = System.currentTimeMillis();
        long duration = (end - start);
        System.out.println("Duration: " + duration);
        stats = new Statistics(vehiclesMap, shockwavesGenerated, vehicles, grid.getLinkMap(), totalVehicles, shockMap);
        System.out.println(shockwavesGenerated);
        running = false;
        return stats;
    }

    private Vehicle[] generateVehicles(Vehicle[] vehicles, List<Link> inputLinks) {
        for (int i = 0; i < totalVehicles; i++) {
            int length = randomise ? ran.nextInt(maxCarLength - minCarLength + 1) + minCarLength : maxCarLength;
            Vehicle vehicle = new Vehicle(i);
            vehicle.setLength(length);

            //int routeNumber = grid.getRoutes().size();
            //int randomRoute = ran.nextInt((routeNumber-1) + 1);
            List<Link> route = Grid.generateRoute(inputLinks);
            vehicle.setRoute(route);

            List<Link> list = vehicle.getRoute();
            Set<Link> set = new HashSet<>(list);
            if (set.size() < list.size()) {
                System.out.println("Dup links!!! in route!!");
            }
            vehicles[i] = vehicle;
        }

        int initialPush = totalVehicles >= INCREMENTAL_PUSH_SIZE ? INCREMENTAL_PUSH_SIZE : totalVehicles;
        int waiting = initialPush == INCREMENTAL_PUSH_SIZE ? totalVehicles - INCREMENTAL_PUSH_SIZE : 0;

        for (int i = 0; i < initialPush; i++) {
            vehicles[i].getRoute().get(0).getEntryPoint().push(vehicles[i], 0.0);
        }
        if(waiting>0){
            for(int j = initialPush; j < totalVehicles; j++){
                vehicles[j].getRoute().get(0).getEntryPoint().getWaiting().add(vehicles[j]);
            }
        }

        return vehicles;
    }

    public void handleIntersections(Link link, double time) {
        if (link.getQueue().size() == 0)
            return;
        if (link.getServers().size() == 0)
            return;

        for (Server server : link.getServers()) {
            boolean delayed = processPocketDelay(server, link, time);
            if (delayed) continue;

            boolean outgoingBlocked = calculateDelay(server, time);
            if (outgoingBlocked) continue;

            // Outgoing link free
            processOutgoingVehicles(link, server, time);
        }
    }

    public boolean processPocketDelay(Server server, Link link, double time) {
        double pocketDelay = server.getPocketDelayedUntil();
        if (pocketDelay > 0) {
            server.setPocketDelayedUntil(pocketDelay - ONE_STEP);
            if (server.getPocketDelayedUntil() <= 0.0) { // Become unblocked so shock-wave reached queue front
                int lookback = link.getLookBackLimit();
                List<Vehicle> queued = QUtil.queuedVehicles(link.getQueue(), time);
                List<Vehicle> forOutgoing = QUtil.getServerComforedVehicles(queued, server.getOutgoing(), lookback);
                if (forOutgoing.size() > 0) {
                    if (server.getOutgoing().getQueue().size() == 0)
                        return false;
                    else
                        processShockwave(forOutgoing, link, time, server.getOutgoing());
                }
                return true;
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean calculateDelay(Server server, double time) {
        boolean isFree = server.getOutgoing().isFree();
        if (!isFree) {
            shockwavesGenerated++;
            server.setEntriesDeniedCount(server.getEntriesDeniedCount() + 1);
            List<Server> outgoingServers = server.getOutgoing().getServers();
            double shockSpeed = 2.0; //default
            if(outgoingServers.size()>1){ //May need to average shock speeds
                Set<Link> relevantLinks = server.getOutgoing().getQueue().stream()
                        .filter(v->v.getNextLink().getQueue().size()>0)
                        .map(v->v.getNextLink())
                        .collect(Collectors.toSet());

                if(relevantLinks.size()!=0) {
                    // Sum all shock speeds
                    double shockSpeedSum = relevantLinks.stream()
                            .mapToDouble(l -> Link.shockwaveSpeed(server.getOutgoing(), l, time))
                            .sum();
                    shockSpeed = shockSpeedSum / relevantLinks.size(); //average
                }
            }else{
                // Only one link to compare
                if(outgoingServers.size() > 0 && outgoingServers.get(0).getOutgoing().getQueue().size()>0)
                    shockSpeed = Link.shockwaveSpeed(server.getOutgoing(), outgoingServers.get(0).getOutgoing(), time);
            }
            //System.out.println(shockSpeed);
            double delayedUntil = server.getOutgoing().getLength() / shockSpeed;
            server.setPocketDelayedUntil(delayedUntil);
            return true;
        }
        return false;
    }

    public void processVehicle(Vehicle ve, Link current, Server server, double time) {
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

    public void processShockwave(List<Vehicle> vehicles, Link current, double time, Link outgoing) {
        if (current.getQueue().size() == 0)
            return;
        double shockSpeed = Link.shockwaveSpeed(current, outgoing, time);
        double speedAtCap = current.speedDensity2(current.getkMax());

        double latestExitTime = Integer.MAX_VALUE;
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



    public void isGridLocked(){
        List<Link> remaining = grid.getLinkMap().entrySet().stream()
                .filter(l->l.getValue().getQueue().size()>0)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        for(Link link : remaining){
            List<Link> grid = new ArrayList<>();

            Link next = link.getQueue().getHead().getNextLink();
            grid.add(next);
            while(next!=null){
                Queue q = next.getQueue();
                Vehicle v = q.getHead();
                next = v.getNextLink();
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

    @Override
    public Statistics call() throws Exception {
        return run();
    }
}
