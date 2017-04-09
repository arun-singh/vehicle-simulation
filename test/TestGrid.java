import Graph.*;
import Simulation.Simulate;
import Statistics.Statistics;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import static org.hamcrest.collection.IsIn.isIn;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Arun on 10/02/2017.
 */
public class TestGrid {

    @Test
    public void testSetup(){
        int totalNodes = 8;
        int totalVehicles = 10000;
        int roadLengthMax = 500;
        int roadLengthMin = 50;
        int minLookBack = 1;
        int maxLookBack = 10;
        int minLanes = 1;
        int maxLane = 2;
        int minCap = 1;
        int maxCap = 10;
        int minCarLength = 4;
        int maxCarLength = 6;

        Simulate sim = new Simulate();
        Random ran = new Random();
        LinkedHashMap<Integer, Link> linkMap = new LinkedHashMap<>();
        Node[] nodes = new Node[totalNodes];
        for(int i = 0; i<totalNodes;i++){
            nodes[i] = new Node(i+1);
        }
        List<Integer[]> nodePairs = new ArrayList<>();
        nodePairs.add(new Integer[]{1, 2});
        nodePairs.add(new Integer[]{2, 3});
        nodePairs.add(new Integer[]{2, 4});
        nodePairs.add(new Integer[]{2, 6});
        nodePairs.add(new Integer[]{6, 5});
        nodePairs.add(new Integer[]{6, 7});
        nodePairs.add(new Integer[]{6, 8});

        for(int i = 0; i<nodePairs.size(); i++) {
            for(int j = 0 ; j < 2 ; j++){
                int length = ran.nextInt(roadLengthMax - roadLengthMin + 1) + roadLengthMin;
                int capacity = length/4;
                int id = (i*2)+j;

                Integer[] pair = nodePairs.get(i);
                Node start = nodes[pair[j==0?0:1]-1];
                Node end = nodes[pair[j==0?1:0]-1];

                Link link = new Link(id, capacity, start, end);
                link.setLength(length);
                int lookBack = ran.nextInt(maxLookBack - minLookBack + 1) + minLookBack;
                link.setLookBackLimit(lookBack);
                int lanes = ran.nextInt(maxLane - minLanes + 1) + minLanes;
                link.setLanes(lanes);

                link.setkMin(0);
                link.setkMax(5);
                link.setvMin(1);
                link.setvFree(5);

                linkMap.put(id, link);
            }
        }

        Link.createServers(linkMap);
        List<Link> inputLinks = Statistics.getInputLinks(linkMap);

        Vehicle[] vehicles = new Vehicle[totalVehicles];
        for(int i =0; i<totalVehicles;i++){
            int length = ran.nextInt(maxCarLength - minCarLength + 1) + minCarLength;
            Vehicle vehicle = new Vehicle(i);
            vehicle.setLength(length);

            int randomStartingLink = ran.nextInt((inputLinks.size()-1) - 0 + 1);
            Link next = inputLinks.get(randomStartingLink);
            vehicle.getRoute().add(next);
            int serverSize = next.getServers().size();

            while(serverSize!=0){
                int serverIndex = ran.nextInt((serverSize-1) - 0 + 1);
                next = next.getServers().get(serverIndex).getOutgoing();
                vehicle.getRoute().add(next);
                serverSize = next.getServers().size();
            }
            vehicles[i] = vehicle;
        }

        for(int i = 0; i<totalVehicles;i++){
            vehicles[i].getRoute().get(0).getEntryPoint().push(vehicles[i], 0.0);
        }

        //linkMap.entrySet().stream().forEach(link->link.getValue().getServers().forEach(server->server.setPocketDelayedUntil(5)));

        int step = 0;
        int waitingPushed = 0;
        int vehiclesLeft = Integer.MAX_VALUE;
        while (vehiclesLeft!=0) {

            // For all links calculate density
            for (Map.Entry<Integer, Link> entry : linkMap.entrySet()) {
                entry.getValue().setRunningDensity(entry.getValue().runningDensity(step));
            }

            // For all links process turns
            for (Map.Entry<Integer, Link> entry : linkMap.entrySet()) {
                sim.handleIntersections(entry.getValue(), step);
            }

            // Push waiting vehicles
            List<EntryPoint> waiting = linkMap.entrySet().stream()
                                                        .filter(l->l.getValue().getEntryPoint().getWaiting().size()>0)
                                                        .map(Map.Entry::getValue)
                                                        .map(l->l.getEntryPoint())
                                                        .collect(Collectors.toList());

            for(EntryPoint queue: waiting){
                queue.pushWaiting(step);
            }

            vehiclesLeft = totalVehicles - Statistics.totalVehiclesOutput(linkMap);
            System.out.println(vehiclesLeft);
            step++;
        }

        System.out.println("Step is: " + step + " Estimated average is: " + Statistics.estimatedAverageJourneyTime(vehicles) + " Actual average is: " + Statistics.actualAverageJourneyTime(vehicles));
        System.out.println(Statistics.totalVehiclesInput(linkMap));
        System.out.println("Waiting pushed: " + waitingPushed);
       // System.out.println("ShockWaves: " + sim.shockwavesGenerated);
       // assertThat(vehicle.getRoute().get(2).getOutputQueue().getReceived().size(), is(1));
    }
}
