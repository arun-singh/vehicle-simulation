package Statistics;

import Graph.Link;
import Graph.Vehicle;
import Simulation.Simulate;
import com.sun.org.glassfish.external.statistics.Statistic;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Arun on 24/02/2017.
 */
public class Statistics {


    private int shockwavesGenerated;
    private Vehicle[] vehicles;
    private HashMap<Integer, Link> linkMap;
    private int cars;
    HashMap<Integer, Integer> vehiclesMap;
    HashMap<Integer, Integer> shockMap;

    public Statistics(HashMap<Integer, Integer> vehiclesMap, int shockwavesGenerated,  Vehicle[] vehicles, HashMap<Integer, Link> linkMap, int cars,
                      HashMap<Integer, Integer> shockMap){
        this.vehiclesMap = vehiclesMap;
        this.shockwavesGenerated = shockwavesGenerated;
        this.vehicles = vehicles;
        this.linkMap = linkMap;
        this.cars = cars;
        this.shockMap = shockMap;
       // report(finishStep, shockwavesGenerated,  vehicles, linkMap);
    }

    public Statistics(){}

    public static void report(int finishStep, int shockwavesGenerated, Vehicle[] vehicles, HashMap<Integer, Link> linkMap){
        System.out.print("Finish time: " + finishStep + " ---- ");
        System.out.print("Shockwaves generated: " + shockwavesGenerated + " ---- ");
        System.out.print("Grid length: " + totalGridLength(linkMap) + " ---- ");

        boolean longer = actualAverageJourneyTime(vehicles) - estimatedAverageJourneyTime(vehicles)  > 0 ? true : false;
        System.out.println("Actual greater than estimate average journey: " +  longer);
    }

    public static List<List<Statistics>> increaseCars(int carsBegin, int carsEnd, int increment, int runs, double[] coords){
        List<List<Statistics>> stats = new ArrayList<>();
        int cars = carsBegin;
        while(cars <= carsEnd){
            System.out.println((cars/((double)carsEnd))+"%");
            List<Statistics> perCar = new ArrayList<>();
            for(int i = 0; i < runs; i++){
                Simulate simulate = new Simulate(cars, coords);
                perCar.add(simulate.run());
            }
            stats.add(perCar);
            cars+=increment;
        }
        return stats;
    }

    public static VBox drawCarStats(List<List<Statistics>> results, String xLabel, String yLabel){

        LineChart chart = createChart(xLabel, yLabel);
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        chart.getData().add(series);
        VBox box = new VBox();
        box.getChildren().add(chart);

        for(List<Statistics> stat : results){
            int cars = stat.get(0).getCars();
            double averageFinish = stat.stream().mapToDouble(s->s.getFinishStep()).sum() / stat.size();
            series.getData().add(new XYChart.Data<>(cars, averageFinish));
        }

        return box;
    }

    public static VBox drawShockwaves(List<List<Statistics>> results, String xLabel, String yLabel){
        LineChart chart = createChart(xLabel, yLabel);
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        chart.getData().add(series);
        VBox box = new VBox();
        box.getChildren().add(chart);

        for(List<Statistics> stat : results){
            int cars = stat.get(0).getCars();
            double averageFinish = stat.stream().mapToDouble(s->s.getShockwavesGenerated()).sum() / stat.size();
            series.getData().add(new XYChart.Data<>(cars, averageFinish));
        }
        return box;
    }

    public static VBox drawShockwaveSingleJourney(List<List<Statistics>> results, String xLabel, String yLabel){
        LineChart chart = createChart(xLabel, yLabel);
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        chart.getData().add(series);
        VBox box = new VBox();
        box.getChildren().add(chart);

        List<Statistics> ls = results.get(results.size()-1);
        Statistics s = ls.get(0);
        HashMap<Integer, Integer> swm = s.shockMap;

        for(Map.Entry<Integer, Integer> entry : swm.entrySet()){
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        return box;
    }

    public static VBox vehiclesRemaining(List<List<Statistics>> results, String xLabel, String yLabel){
        LineChart chart = createChart(xLabel, yLabel);
        VBox box = new VBox();
        box.getChildren().add(chart);

        for(List<Statistics> stat : results){
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            Statistics stats = stat.get(0);
            for(Map.Entry<Integer, Integer> entry : stats.vehiclesMap.entrySet()){
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            chart.getData().add(series);
        }
        return box;
    }

    public static LineChart<Number, Number> createChart(String xLabel, String yLabel){
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xLabel);
        yAxis.setLabel(yLabel);

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        xAxis.setAutoRanging(true);
        yAxis.setAutoRanging(true);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        chart.setLegendVisible(false);

        return chart;
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

    public static void diagnostics(HashMap<Integer, Link> linkMap){
        List<Link> occupied = linkMap.entrySet().stream()
                                                .filter(l->l.getValue().getQueue().size()>0)
                                                .map(Map.Entry::getValue)
                                                .collect(Collectors.toList());

        occupied.sort(new Comparator<Link>() {
            @Override
            public int compare(Link o1, Link o2) {
                int o1Size = o1.getQueue().size();
                int o2size = o2.getQueue().size();
                if(o1Size==o2size)
                    return 0;
                if(o1Size>o2size)
                    return 1;
                return -1;
            }
        });

        for(Link link : occupied){
            int id = link.getId();
            int vehicleCount = link.getQueue().size();
            int[] nextLinkIds = link.getQueue().stream().mapToInt(v->v.getNextLink().getId()).toArray();
            int cap = link.getQueue().getCapacity();
            int lookback = link.getLookBackLimit();
            double latS = link.getSource().getLatitude();
            double lonS = link.getSource().getLongitude();
            double latT = link.getTarget().getLatitude();
            double lonT = link.getTarget().getLongitude();
            String coord = latS + ", " + lonS + ":" + latT + ", " + lonT;

            System.out.print(String.format("%-20s-- %s%d" , "Link id: " + id, "Vehicle count: ", vehicleCount));
            System.out.print("  Lookback: " + lookback + " -- Cap: " + cap + "-- Next links: ");
            for(Integer i: nextLinkIds){
                System.out.print(String.format("  %-5s -- " , i));
            }
            System.out.println(coord);
        }
        System.out.println("-----------------------------------------------------");
    }

    public int getFinishStep() {
        return vehiclesMap.size()-1;
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

    public int getCars() {
        return cars;
    }

    public static void main(String[] args){
        //Statistics.increaseCars(100, 10100, 1000, 10);
        //double a = Math.log(1.1);
       // System.out.println(a);
    }
}
