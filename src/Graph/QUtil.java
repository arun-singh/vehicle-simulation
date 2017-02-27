package Graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Arun on 19/02/2017.
 */
public class QUtil {

    public static Queue copy(Queue toCopy){
        Queue toReturn = new Queue(toCopy.getCapacity());
        toCopy.forEach(v -> toReturn.push(Vehicle.copy(v)));
        return toReturn;
    }

    public static List<Vehicle> queuedVehicles(Queue queue, double _t){
        return queue.stream()
                .filter(v -> v.getEarliestExitTime() < _t)
                .sorted((v1, v2)->{return v1.compareTo(v2);})
                .collect(Collectors.toList());
    }

    public static List<Vehicle> getVehiclesBehind(Queue queue, Vehicle toCompare){
        return queue.stream()
                .filter(v->v.getEarliestExitTime()>toCompare.getEarliestExitTime() && !(v.equals(toCompare)))
                .sorted((v1, v2)->{return v1.compareTo(v2);})
                .collect(Collectors.toList());
    }

    public static Optional<Vehicle> getVehicleInFront(Queue queue, Vehicle toCompare){
        double _eet = toCompare.getEarliestExitTime();
        return queue.stream()
                .filter(v -> v.getEarliestExitTime() < _eet)
                .reduce((v1, v2) -> (_eet - v1.getEarliestExitTime()) < (_eet - v2.getEarliestExitTime()) ? v1 : v2);
    }

    public static double distanceInFront(Queue queue, Vehicle toCompare){
        return queue.stream()
                .filter(v -> v.getEarliestExitTime() < toCompare.getEarliestExitTime())
                .mapToDouble(v->v.getLength())
                .sum();
    }

    public static List<Vehicle> getServerComforedVehicles(List<Vehicle> queued, Link outgoing, int lookback){
        return queued.stream()
                .limit(lookback)
                .filter(v -> v.getNextLink().equals(outgoing))
                .collect(Collectors.toList());
    }

    public static List<InputQueue> getWaitingVehicles(HashMap<Integer, Link> linkMap){
        return linkMap.entrySet().stream()
                .filter(l->l.getValue().getInputQueue().getWaiting().size()>0)
                .map(Map.Entry::getValue)
                .map(l->l.getInputQueue())
                .collect(Collectors.toList());
    }

}
