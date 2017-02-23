import Graph.Link;
import Graph.QueueServer;
import Graph.Queue;
import Simulation.Simulate;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Created by Arun on 01/02/2017.
 */
public class TestIntersectionHandling {

    static List<Link> links;
    static Link l1, l2, l3;

    @BeforeClass
    public static void setup(){
        links = new ArrayList<>();

        l1 = new Link(1);
        l1.setLanes(2);
        l1.setLength(50);
        l1.setLookBackLimit(2);
        l1.setkMin(0);
        l1.setkMax(5);
        l1.setvMin(3);
        l1.setvFree(7);
        Queue queue = new Queue(8, l1, null);
        l1.setQueue(queue);
        links.add(l1);

        l2 = new Link(2);
        l2.setLanes(2);
        l2.setLength(50);
        l2.setLookBackLimit(2);
        l2.setkMin(0);
        l2.setkMax(5);
        l2.setvMin(3);
        l2.setvFree(7);
        Queue queueTwo = new Queue(8, l2, null);
        l2.setQueue(queueTwo);
        links.add(l2);

        l3 = new Link(3);
        l3.setLanes(2);
        l3.setLength(50);
        l3.setLookBackLimit(2);
        l3.setkMin(0);
        l3.setkMax(5);
        l3.setvMin(3);
        l3.setvFree(7);
        Queue queueThree = new Queue(8, l3, null);
        l3.setQueue(queueThree);
        links.add(l3);


    }

    @Test
    public void testSingleIntersection(){
        Simulate sim = new Simulate();
        l1.getServers().add(new QueueServer(l1, l2, QueueServer.Type.NORMAL));

        List<Link> routeOne = new ArrayList<Link>(){{
            add(l2);
        }};

        for(int i = 1; i <= l1.getQueue().getCapacity(); i++)
            Simulate.pushNewVehicle(l1, i, routeOne);

        for(int i = 11; i < 30; i++){
            for(Link link: links) {
                link.setRunningDensity(link.runningDensity(i));
            }
            for(Link link: links) {
                sim.handleIntersections(link, i);
            }
        }

        Assert.assertTrue(l1.getQueue().size() == 0);
        Assert.assertTrue(l2.getQueue().size() == l2.getQueue().getCapacity());
    }

    @Test
    public void testMultipleIntersections(){
        Simulate sim = new Simulate();

        l1.getServers().add(new QueueServer(l1, l2, QueueServer.Type.NORMAL));
        l1.getServers().add(new QueueServer(l1, l3, QueueServer.Type.NORMAL));
        l2.getServers().add(new QueueServer(l2, l3, QueueServer.Type.MERGE));

        List<Link> routeOne = new ArrayList<Link>(){{
            add(l2);
            add(l3);
        }};

        List<Link> routeTwo = new ArrayList<Link>(){{
            add(l3);
        }};

        for(int i = 1; i <= l1.getQueue().getCapacity(); i++)
            sim.pushNewVehicle(l1, i, i <5 ? routeOne : routeTwo);

        for(int i = 11; i < 60; i++){
            //for(Link link: links) {
                //link.setRunningDensity(link.runningDensity(i));
            //}
            for(Link link: links) {
                sim.handleIntersections(link, i);
            }
        }

        Assert.assertTrue(links.get(0).getQueue().size() == 0);
        Assert.assertTrue(links.get(1).getQueue().size() <= links.get(1).getQueue().getCapacity());
        Assert.assertTrue(links.get(2).getQueue().size() == links.get(2).getQueue().getCapacity());
    }


}
