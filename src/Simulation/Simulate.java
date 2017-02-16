package Simulation;

import Graph.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Arun on 27/01/2017.
 */
public class Simulate {

    private final int SIMULATION_STEPS = 30;
    private static int vehicleCounter = 0;
    private Grid grid;

    public Simulate(Grid grid) {
        this.grid = grid;
    }

    public void run() {
        for (int step = 0; step < SIMULATION_STEPS; step++) {
            //TODO: Smarter method to instantiate vehicles needed
            //TODO: Need to assign routes
            if (step < 10) {
                List<Link> dummyRoute = new ArrayList<Link>(){{
                    add(grid.getLinkMap().get(1));
                }};
                pushNewVehicle(grid.getLinkMap().get(0), step, dummyRoute);
            }

            // For all links calculate density
            for (Map.Entry<Integer, Link> entry : grid.getLinkMap().entrySet()) {
                entry.getValue().setRunningDensity(entry.getValue().runningDensity(step));
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // For all links process turns - allowing more than one vehicle per step?
            for (Map.Entry<Integer, Link> entry : grid.getLinkMap().entrySet()) {
                handleIntersections(entry.getValue(), step);
            }
        }


    }

    public static void handleIntersections(Link link, double time){
        if(link.getQueue().size() == 0)
            return;
        if(link.getServers().size() == 0)
            return;
        for (QueueServer server : link.getServers()) {
            int lookback = link.getLookBackLimit();
            while (server.getOutgoing().isFree() && lookback>0 && link.getQueue().size()>0) {
                if(link.getQueue().getHead().getNextLink().equals(server.getOutgoing())) {
                    Vehicle ve = link.getQueue().pop();
                    double speed = server.getOutgoing().speedDensity(server.getOutgoing().getRunningDensity());
                    double _eet = time + (server.getOutgoing().getLength() / speed);
                    ve.setEarliestExitTime(_eet);
                    ve.updateRoute();
                    server.getOutgoing().getQueue().push(ve);
                    link.setRunningDensity(link.runningDensity(time));
                }
                lookback--;
            }
        }
    }

    public static boolean pushNewVehicle(Link link, double time, List<Link> route) {
        if (!link.isFree())
            return false;

        double speed = link.speedDensity(link.getRunningDensity());
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
        return diff < 0 ? diff : diff * -1;
    }
}
