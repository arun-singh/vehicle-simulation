package Graph;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Arun on 12/04/2017.
 */
public class GridUtil {

    public static int totalVehiclesInput(HashMap<Integer, Link> linkMap){
        return linkMap.entrySet().stream()
                .mapToInt(l->l.getValue().getEntryPoint().getVehiclesPushed())
                .sum();
    }

    public static int totalVehiclesOutput(HashMap<Integer, Link> linkMap){
        return linkMap.entrySet().stream()
                .mapToInt(l->l.getValue().getExitPoint().getReceived().size())
                .sum();
    }

    public static List<Link> getInputLinks(HashMap<Integer, Link> linkMap){
        return linkMap.entrySet().stream()
                .filter(l->l.getValue().getServers().size()>1 && l.getValue().getConnectivity()>250)
                .map(java.util.Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public static double totalGridLength(HashMap<Integer, Link> linkMap){
        return linkMap.entrySet().stream()
                .mapToDouble(l->l.getValue().getLength())
                .sum();
    }

    public static double estimatedAverageJourneyTime(Vehicle[] vehicles){
        double sum = 0;
        for(Vehicle veh : vehicles){
            sum += veh.estimatedJourneyTime();
        }
        return sum/vehicles.length;
    }

    public static double actualAverageJourneyTime(Vehicle[] vehicles){
        double sum = 0;
        for(Vehicle veh : vehicles){
            sum += veh.getJourneyTime();
        }
        return sum/vehicles.length;
    }
}
