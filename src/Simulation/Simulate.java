package Simulation;

import Graph.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Arun on 27/01/2017.
 */
public class Simulate {

    private final int SIMULATION_STEPS = 30;
    private final int ONE_STEP = 1;
    private static int vehicleCounter = 0;
    public int shockwavesGenerated = 0;
    private Grid grid;

    public Simulate(Grid grid) {
        this.grid = grid;
    }
    public Simulate(){}

    public void run(HashMap<Integer, Link> linkMap) {
        for (int step = 0; step < SIMULATION_STEPS; step+=ONE_STEP) {
            //TODO: Smarter method to instantiate vehicles needed
            //TODO: Need to assign routes
//            if (step < 10) {
//                List<Link> dummyRoute = new ArrayList<Link>(){{
//                    add(grid.getLinkMap().get(1));
//                }};
//                pushNewVehicle(grid.getLinkMap().get(0), step, dummyRoute);
//            }

            // For all links calculate density
            for (Map.Entry<Integer, Link> entry : linkMap.entrySet()) {
                entry.getValue().setRunningDensity(entry.getValue().runningDensity(step));
            }

            // For all links process turns - allowing more than one vehicle per step?
            for (Map.Entry<Integer, Link> entry : linkMap.entrySet()) {
                handleIntersections(entry.getValue(), step);
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
            double delayedUntil = time + (server.getOutgoing().getLength() / shockSpeed);
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
        shockwavesGenerated++;
        double shockSpeed = 2.0;
        double speedAtCap = 3.0;
        List<Vehicle> toUpdate = QUtil.getVehiclesBehind(current.getQueue(), start);
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
}
