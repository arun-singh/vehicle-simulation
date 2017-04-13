package Graph;

import Simulation.Simulate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arun on 17/01/2017.
 */
public class EntryPoint {
    private Link link;
    private int vehiclesPushed = 0;
    private List<Vehicle> waiting = new ArrayList<>();
    public EntryPoint(Link link){
        this.link = link;
    }

    public boolean push(Vehicle vehicle, double time){
        if (!link.isFree()){
            waiting.add(vehicle);
            return false;
        }
        double speed = link.speedDensity(time);
        double _eet = time + (link.getLength() / speed);
        vehicle.setEarliestExitTime(_eet);
        vehicle.setLinkCounter(1);
        if(vehicle.getRoute().size()>1)
             vehicle.setNextLink(vehicle.getRoute().get(1));
        link.getQueue().push(vehicle);
        ++vehiclesPushed;
        return true;
    }

    public boolean pushWaiting(double time){
        for(int i = 0; i < Simulate.INCREMENTAL_PUSH_SIZE ; i++){
            if(link.isFree()) {
                double speed = link.speedDensity(time);
                double _eet = time + (link.getLength() / speed);
                waiting.get(i).setEarliestExitTime(_eet);
                waiting.get(i).setLinkCounter(1);
                waiting.get(i).setNextLink(waiting.get(i).getRoute().get(1));
                waiting.get(i).setStartTime(time);
                link.getQueue().push(waiting.get(i));
                waiting.remove(waiting.get(i));
                ++vehiclesPushed;
                return true;
            }
        }
        return false;
    }

    public int getVehiclesPushed() {
        return vehiclesPushed;
    }

    public List<Vehicle> getWaiting(){
        return waiting;
    }
}
