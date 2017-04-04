package Graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arun on 23/02/2017.
 */
public class ExitPoint {
    private Link link;
    private List<Vehicle> received = new ArrayList<>();
    public ExitPoint(Link link){
        this.link = link;
    }
    public void received(Vehicle vehicle, double time){
        vehicle.setJourneyTime(time-vehicle.getStartTime());
        received.add(vehicle);
    }
    public List<Vehicle> getReceived(){
        return received;
    }
}
