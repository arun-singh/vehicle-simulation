package Graph;

import javax.xml.validation.ValidatorHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Arun on 24/02/2017.
 */
public class Statistics {


    private int finishStep, shockwavesGenerated;
    private Vehicle[] vehicles;
    private HashMap<Integer, Link> linkMap;

    public Statistics(int finishStep, int shockwavesGenerated,  Vehicle[] vehicles, HashMap<Integer, Link> linkMap){
        this.finishStep = finishStep;
        this.shockwavesGenerated = shockwavesGenerated;
        this.vehicles = vehicles;
        this.linkMap = linkMap;
        report(finishStep, shockwavesGenerated,  vehicles, linkMap);
    }

    public Statistics(){

    }

    public static void report(int finishStep, int shockwavesGenerated, Vehicle[] vehicles, HashMap<Integer, Link> linkMap){
        System.out.print("Finish time: " + finishStep + " ---- ");
        System.out.print("Shockwaves generated: " + shockwavesGenerated + " ---- ");
        System.out.print("Grid length: " + totalGridLength(linkMap) + " ---- ");
        boolean longer = actualAverageJourneyTime(vehicles) - estimatedAverageJourneyTime(vehicles)  > 0 ? true : false;
        System.out.println("Actual greater than estimate average journey: " +  longer);
    }


    public static int totalVehiclesInput(HashMap<Integer, Link> linkMap){
        return linkMap.entrySet().stream()
                .mapToInt(l->l.getValue().getInputQueue().getVehiclesPushed())
                .sum();
    }

    public static int totalVehiclesOutput(HashMap<Integer, Link> linkMap){
        return linkMap.entrySet().stream()
                .mapToInt(l->l.getValue().getOutputQueue().getReceived().size())
                .sum();
    }

    public static List<Link> getInputLinks(HashMap<Integer, Link> linkMap){
        return linkMap.entrySet().stream()
                .filter(l->l.getValue().getServers().size()>0)
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

    public int getFinishStep() {
        return finishStep;
    }

    public void setFinishStep(int finishStep) {
        this.finishStep = finishStep;
    }

    public int getShockwavesGenerated() {
        return shockwavesGenerated;
    }

    public void setShockwavesGenerated(int shockwavesGenerated) {
        this.shockwavesGenerated = shockwavesGenerated;
    }

    public Vehicle[] getVehicles() {
        return vehicles;
    }

    public void setVehicles(Vehicle[] vehicles) {
        this.vehicles = vehicles;
    }

    public HashMap<Integer, Link> getLinkMap() {
        return linkMap;
    }

    public void setLinkMap(HashMap<Integer, Link> linkMap) {
        this.linkMap = linkMap;
    }
}
