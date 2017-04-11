package Statistics;

import Graph.Link;
import Graph.Vehicle;
import Simulation.Simulate;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Created by Arun on 24/02/2017.
 */
public class Statistics {


    private int shockwavesGenerated;
    private Vehicle[] vehicles;
    private LinkedHashMap<Integer, Link> linkMap;
    private int cars;
    LinkedHashMap<Integer, Integer> vehiclesMap;
    LinkedHashMap <Integer, Integer> shockMap;

    public Statistics(LinkedHashMap<Integer, Integer> vehiclesMap, int shockwavesGenerated,  Vehicle[] vehicles, LinkedHashMap<Integer, Link> linkMap, int cars,
                      LinkedHashMap<Integer, Integer> shockMap){
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

    public static List<List<Statistics>> increaseCars2(int carsBegin, int carsEnd, int increment, int runs, double[] coords){
        List<List<Statistics>> stats = new ArrayList<>();
        ExecutorService pool = Executors.newFixedThreadPool(runs);
        int cars = carsBegin;
        while(cars <= carsEnd){
            // System.out.println((cars/((double)carsEnd))+"%");
            List<Statistics> perCar = new ArrayList<>();
            Set<Future<Statistics>> set = new HashSet<Future<Statistics>>();
            for (int i = 0; i < runs; i++) {
                Callable<Statistics> simulate = new Simulate(cars, coords);
                Future<Statistics> future = pool.submit(simulate);
                set.add(future);
            }

            for (Future<Statistics> future : set) {
                try {
                    perCar.add(future.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
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
            XYChart.Series<Number, Number> error = new XYChart.Series<>();
            chart.getData().add(error);

            int cars = stat.get(0).getCars();
            double averageFinish = stat.stream().mapToDouble(s->s.getFinishStep()).sum() / stat.size();
            double n = stat.size();

            double temp = 0;
            for(Statistics s : stat){
                temp+= (s.getFinishStep()-averageFinish) * (s.getFinishStep()-averageFinish);
            }
            double var = temp/stat.size();
            double stdv = Math.sqrt(var);
            double stde = stdv/(Math.sqrt(n));

            series.getData().add(new XYChart.Data<>(cars, averageFinish));
            error.getData().add(new XYChart.Data<>(cars, averageFinish+stde));
            error.getData().add(new XYChart.Data<>(cars, averageFinish-stde));

            Node line = series.getNode().lookup(".chart-series-line");
            line.setStyle("-fx-stroke: " + "blue");

            Node lineTwo = error.getNode().lookup(".chart-series-line");
            lineTwo.setStyle("-fx-stroke: " + "red;" + " -fx-stroke-dash-array: " +  "0.1 5.0;");
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
            XYChart.Series<Number, Number> error = new XYChart.Series<>();
            chart.getData().add(error);
            int cars = stat.get(0).getCars();
            double averageFinish = stat.stream().mapToDouble(s->s.getShockwavesGenerated()).sum() / stat.size();
            double n = stat.size();

            double temp = 0;
            for(Statistics s : stat){
                temp+= (s.getFinishStep()-averageFinish) * (s.getFinishStep()-averageFinish);
            }
            double var = temp/stat.size();
            double stdv = Math.sqrt(var);
            double stde = stdv/(Math.sqrt(n));

            series.getData().add(new XYChart.Data<>(cars, averageFinish));
            error.getData().add(new XYChart.Data<>(cars, averageFinish+stde));
            error.getData().add(new XYChart.Data<>(cars, averageFinish-stde));

            Node line = series.getNode().lookup(".chart-series-line");
            line.setStyle("-fx-stroke: " + "blue");

            Node lineTwo = error.getNode().lookup(".chart-series-line");
            lineTwo.setStyle("-fx-stroke: " + "red;" + " -fx-stroke-dash-array: " +  "0.1 5.0;");
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
        List<Integer> keyList = new ArrayList<>(swm.keySet());
        for(int i = 1; i < keyList.size(); i++) {
            Integer step1 = keyList.get(i);
            int shock1 = swm.get(step1);

            Integer step2 = keyList.get(i-1);
            int shock2 = swm.get(step2);

            series.getData().add(new XYChart.Data<>(step2, (shock1-shock2)/2));
        }
        return box;
    }

    public static VBox drawVehiclesRemaining(List<List<Statistics>> results, String xLabel, String yLabel){
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

    private static LineChart<Number, Number> createChart(String xLabel, String yLabel){
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

    public int getCars() {
        return cars;
    }

}
