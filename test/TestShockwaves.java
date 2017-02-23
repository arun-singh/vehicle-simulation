import Graph.*;
import Simulation.Simulate;
import org.junit.Test;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIn.isIn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Created by Arun on 14/02/2017.
 */
public class TestShockwaves {

    final int minCarLength = 4;
    final int maxCarLength = 6;

    @Test
    public void randomShockwaveTest(){
        System.out.println(Simulate.shockwaveSpeed(20, 10, 5, 10));
    }

    @Test
    public void testProcessShockwave(){
        int QUEUE_SIZE = 10;
        Random ran = new Random();
        Simulate sim = new Simulate();
        Link linkOne = new Link(1);
        Link linkTwo = new Link(2);

        Queue queueOne = new Queue(QUEUE_SIZE, linkOne, null);
        Queue queueTwo = new Queue(QUEUE_SIZE, linkTwo, null);
        linkOne.setQueue(queueOne);
        linkTwo.setQueue(queueTwo);

        for(int i = 1; i <= QUEUE_SIZE; i++){
            int length =  ran.nextInt(maxCarLength - minCarLength + 1) + minCarLength;
            queueOne.push(new Vehicle(i, i*2, length));
            int lengthTwo = ran.nextInt(maxCarLength - minCarLength + 1) + minCarLength;
            queueTwo.push(new Vehicle(i, i, lengthTwo));
        }

        Queue beforeShockwave = QUtil.copy(queueOne);
        List<Double> _eetBeforeShockwave = beforeShockwave.stream()
                .mapToDouble(v -> v.getEarliestExitTime())
                .mapToObj(i->i)
                .collect(Collectors.toList());

        sim.processShockwave(linkOne.getQueue().getHead(), linkOne, 10, linkTwo);

        Queue afterShockwave = QUtil.copy(queueOne);
        List<Double> _eetAfterSchockwave = afterShockwave.stream()
                .mapToDouble(v -> v.getEarliestExitTime())
                .mapToObj(i->i)
                .collect(Collectors.toList());

        for(int j = 0; j < QUEUE_SIZE; j++){
            if(j==0) {
                assertThat(_eetBeforeShockwave.get(j), equalTo(_eetAfterSchockwave.get(j)));
                continue;
            }
            assertThat(_eetAfterSchockwave.get(j), greaterThan(_eetBeforeShockwave.get(j)));
        }
    }

    @Test
    public void testProcessVehicle(){
        int QUEUE_SIZE = 10;
        Random ran = new Random();
        Simulate sim = new Simulate();
        Link linkOne = new Link(1);
        Link linkTwo = new Link(2);

        Queue queueOne = new Queue(QUEUE_SIZE, linkOne, null);
        Queue queueTwo = new Queue(QUEUE_SIZE, linkTwo, null);
        linkOne.setQueue(queueOne);
        linkTwo.setQueue(queueTwo);
        linkOne.getServers().add(new QueueServer(linkOne, linkTwo, QueueServer.Type.NORMAL));

        for(int i = 1; i <= QUEUE_SIZE; i++){
            int length =  ran.nextInt(maxCarLength - minCarLength + 1) + minCarLength;
            queueOne.push(new Vehicle(i, i*3, length));
        }

        List<Vehicle> outgoing = QUtil.queuedVehicles(linkOne.getQueue(), 40);
        for(int i = 0; i<QUEUE_SIZE; i++){
            sim.processVehicle(outgoing.get(i), linkOne, linkOne.getServers().get(0));

            assertThat(outgoing.get(i), not(isIn(linkOne.getQueue())));
            assertThat(outgoing.get(i), isIn(linkTwo.getQueue()));

            assertThat(linkOne.getQueue(), hasSize(QUEUE_SIZE-(i+1)));
            assertThat(linkTwo.getQueue(), hasSize(i+1));
        }
    }

    @Test
    public void testPorcessPocketDelay(){
        int QUEUE_SIZE = 10;
        Random ran = new Random();

        Simulate sim = new Simulate();
        Link linkOne = new Link(1);
        Link linkTwo = new Link(2);
        Queue queueOne = new Queue(QUEUE_SIZE, linkOne, null);
        Queue queueTwo = new Queue(QUEUE_SIZE, linkTwo, null);
        linkOne.setQueue(queueOne);
        linkTwo.setQueue(queueTwo);
        QueueServer server = new QueueServer(linkOne, linkTwo, QueueServer.Type.NORMAL);
        linkOne.getServers().add(server);

        for(int i = 1; i <= QUEUE_SIZE; i++){
            int length =  ran.nextInt(maxCarLength - minCarLength + 1) + minCarLength;
            queueOne.push(new Vehicle(i, i*3, length));
        }

        int delay = 10;
        server.setPocketDelayedUntil(delay);
        boolean delayed = sim.processPocketDelay(server, linkOne, 0);
        assertThat(delayed, is(false));
        assertThat(server.getPocketDelayedUntil(), is(delay-1.0));

        server.setPocketDelayedUntil(1.0);
        boolean unblocked = sim.processPocketDelay(server, linkOne, 10);
        assertThat(unblocked, is(true));
        assertThat(server.getPocketDelayedUntil(), is(0.0));
    }

}
