package Graph;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Arun on 23/02/2017.
 */
public class OutputQueue{
    private Link link;
    private LinkedHashMap<Vehicle, Double> received = new LinkedHashMap<>();
    public OutputQueue(Link link){
        this.link = link;
    }
    public void received(Vehicle vehicle, double time){
        received.put(vehicle, time);
    }
    public HashMap<Vehicle, Double> getReceived(){
        return received;
    }
}
