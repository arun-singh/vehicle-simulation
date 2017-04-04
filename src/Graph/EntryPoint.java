package Graph;

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
        for(Vehicle veh: waiting){
            if(link.isFree()) {
                double speed = link.speedDensity(time);
                double _eet = time + (link.getLength() / speed);
                veh.setEarliestExitTime(_eet);
                veh.setLinkCounter(1);
                veh.setNextLink(veh.getRoute().get(1));
                veh.setStartTime(time);
                link.getQueue().push(veh);
                waiting.remove(veh);
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
