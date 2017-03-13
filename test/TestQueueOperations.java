import Graph.*;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import org.hamcrest.collection.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import org.junit.Assert;
import java.util.List;


/**
 * Created by Arun on 06/02/2017.
 */
public class TestQueueOperations {

    @Test
    public void testHeadPush(){
        Queue queueOne = new Queue();
        Vehicle v1 = new Vehicle(3);
        v1.setLength(4);

        queueOne.push(v1);
        Assert.assertTrue(queueOne.size() == 1);
        Assert.assertTrue(queueOne.getHead().equals(v1));
    }

    @Test
    public void testHeadPop(){
        Queue queueOne = new Queue();
        Vehicle v1 = new Vehicle(3);
        v1.setLength(4);

        queueOne.push(v1);
        queueOne.pop();
        Assert.assertTrue(queueOne.size()==0);
    }

    @Test
    public void testSorting(){
        Queue queueOne = new Queue();
        Vehicle v1 = new Vehicle(3);
        v1.setLength(4);

        Vehicle v2 = new Vehicle(2);
        v2.setLength(4);

        Vehicle v3 = new Vehicle(3);
        v3.setLength(4);

        Vehicle v4 = new Vehicle(4);
        v4.setLength(4);

        v1.setEarliestExitTime(3);
        v2.setEarliestExitTime(2);
        v3.setEarliestExitTime(4);
        v4.setEarliestExitTime(1);

        queueOne.push(v1);
        queueOne.push(v2);
        queueOne.push(v3);
        Assert.assertTrue(queueOne.getHead().equals(v2));

        queueOne.push(v4);
        Assert.assertTrue(queueOne.getHead().equals(v4));
    }

    @Test
    public void testPushAndPop(){
        Queue queueOne = new Queue();
        Vehicle v1 = new Vehicle(3);
        v1.setLength(4);

        Vehicle v2 = new Vehicle(2);
        v2.setLength(4);

        Vehicle v3 = new Vehicle(3);
        v3.setLength(4);

        Vehicle v4 = new Vehicle(4);
        v4.setLength(4);

        v1.setEarliestExitTime(3);
        v2.setEarliestExitTime(2);
        v3.setEarliestExitTime(4);
        v4.setEarliestExitTime(1);

        queueOne.push(v1);
        queueOne.push(v2);
        queueOne.push(v3);

        Vehicle popped = queueOne.pop();
        Assert.assertTrue(popped.equals(v2));
        Assert.assertTrue(queueOne.getHead().equals(v1));
    }

    @Test
    public void testQueuedVehicles(){
        Queue queue = new Queue();
        Vehicle v1 = new Vehicle(3);
        v1.setLength(4);
        Vehicle v2 = new Vehicle(2);
        v2.setLength(4);
        Vehicle v3 = new Vehicle(3);
        v3.setLength(4);
        Vehicle v4 = new Vehicle(4);
        v4.setLength(4);

        // EMPTY test
        List<Vehicle> zeroVehicles = QUtil.queuedVehicles(queue, 10);
        assertThat(zeroVehicles, IsEmptyCollection.empty());

        v1.setEarliestExitTime(3);
        v2.setEarliestExitTime(2);
        v3.setEarliestExitTime(4);
        v4.setEarliestExitTime(1);
        queue.push(v1);
        queue.push(v2);
        queue.push(v3);
        queue.push(v4);

        double time = 10;
        List<Vehicle> allVehicles = QUtil.queuedVehicles(queue, time);
        assertThat(allVehicles.size(), is(4));
        assertThat(allVehicles, contains(v4, v2, v1, v3));

        v4.setEarliestExitTime(time+1);
        List<Vehicle> allButOneVehicles = QUtil.queuedVehicles(queue, time);
        assertThat(allButOneVehicles.size(), is(3));
        assertThat(allButOneVehicles, contains(v2, v1, v3));
    }

    @Test
    public void testGetVehiclesBehind(){
        Queue queue = new Queue();
        Vehicle v1 = new Vehicle(1);
        v1.setLength(4);
        Vehicle v2 = new Vehicle(2);
        v2.setLength(4);
        Vehicle v3 = new Vehicle(3);
        v3.setLength(4);
        Vehicle v4 = new Vehicle(4);
        v4.setLength(4);

        v4.setEarliestExitTime(1);
        v2.setEarliestExitTime(2);
        v1.setEarliestExitTime(3);
        v3.setEarliestExitTime(4);

        queue.push(v1);
        queue.push(v2);
        queue.push(v3);
        queue.push(v4);

        List<Vehicle> behindFirst = QUtil.getVehiclesBehind(queue, v4);
        assertThat(behindFirst.size(), is(3));
        assertThat(behindFirst, contains(v2, v1, v3));

        List<Vehicle> behindSecond = QUtil.getVehiclesBehind(queue, v2);
        assertThat(behindSecond.size(), is(2));
        assertThat(behindSecond, contains(v1, v3));

        List<Vehicle> behindThird = QUtil.getVehiclesBehind(queue, v1);
        assertThat(behindThird.size(), is(1));
        assertThat(behindThird, contains(v3));

        List<Vehicle> behindLast = QUtil.getVehiclesBehind(queue, v3);
        assertThat(behindLast, IsEmptyCollection.empty());

        v4.setEarliestExitTime(1);
        v2.setEarliestExitTime(1);
        v1.setEarliestExitTime(1);
        v3.setEarliestExitTime(1);
        List<Vehicle> allSame = QUtil.getVehiclesBehind(queue, v3);
        assertThat(allSame, IsEmptyCollection.empty());
    }

    @Test
    public void testDistanceInFront(){
        Queue queue = new Queue();
        Vehicle v1 = new Vehicle(1);
        v1.setLength(4);
        Vehicle v2 = new Vehicle(2);
        v2.setLength(4);
        Vehicle v3 = new Vehicle(3);
        v3.setLength(4);
        Vehicle v4 = new Vehicle(4);
        v4.setLength(4);

        v4.setEarliestExitTime(1);
        v2.setEarliestExitTime(2);
        v1.setEarliestExitTime(3);
        v3.setEarliestExitTime(4);

        queue.push(v1);
        queue.push(v2);
        queue.push(v3);
        queue.push(v4);

        double inFrontOfFirst = QUtil.distanceInFront(queue, v4);
        assertThat(inFrontOfFirst, is(0.0));

        double inFrontOfSecond = QUtil.distanceInFront(queue, v2);
        assertThat(inFrontOfSecond, is(v4.getLength()));

        double inFrontOfThird = QUtil.distanceInFront(queue, v1);
        assertThat(inFrontOfThird, is(v4.getLength() + v2.getLength()));

        double inFrontOfLast = QUtil.distanceInFront(queue, v3);
        assertThat(inFrontOfLast, is(v4.getLength() + v2.getLength() + v1.getLength()));

        //Sam _eet
        v2.setEarliestExitTime(1);
        double same = QUtil.distanceInFront(queue, v2);
        assertThat(same, is(v4.getLength()));

        v3.setEarliestExitTime(1);
        same = QUtil.distanceInFront(queue, v3);
        assertThat(same, is(v4.getLength() + v2.getLength()));
    }

    @Test
    public void testServerEquality(){
        Queue queue = new Queue();
        Link next = new Link(1);

        Vehicle v1 = new Vehicle(1);
        v1.setLength(4); v1.setNextLink(next);
        Vehicle v2 = new Vehicle(2);
        v2.setLength(4); v2.setNextLink(next);
        Vehicle v3 = new Vehicle(3);
        v3.setLength(4); v3.setNextLink(next);
        Vehicle v4 = new Vehicle(4);
        v4.setLength(4); v4.setNextLink(next);

        v4.setEarliestExitTime(1);
        v2.setEarliestExitTime(2);
        v1.setEarliestExitTime(3);
        v3.setEarliestExitTime(4);

        queue.push(v1);
        queue.push(v2);
        queue.push(v3);
        queue.push(v4);

        List<Vehicle> queued = QUtil.queuedVehicles(queue, 10);
        List<Vehicle> allConform = QUtil.getServerComforedVehicles(queued, next, queued.size());
        assertThat(allConform.size(), is(4));
        assertThat(allConform, contains(v4, v2, v1, v3));

        List<Vehicle> smallerLookback = QUtil.getServerComforedVehicles(queued, next, queued.size()-1);
        assertThat(smallerLookback.size(), is(3));
        assertThat(smallerLookback, contains(v4, v2, v1));

        v4.setNextLink(new Link(2));

        List<Vehicle> checkLimitOrdering = QUtil.getServerComforedVehicles(queued, next, queued.size()-1);
        assertThat(checkLimitOrdering.size(), is(2)); // Stream limited to 3 before filtering
        assertThat(checkLimitOrdering, contains(v2, v1));
    }
}
