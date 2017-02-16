import Graph.*;
import org.junit.Test;
import org.junit.Assert;
import org.junit.BeforeClass;


/**
 * Created by Arun on 06/02/2017.
 */
public class TestQueueOperations {

    static Queue queueOne;
    static Vehicle v1, v2, v3, v4, v5;

    @BeforeClass
    public static void setup(){
        queueOne = new Queue();

        v1 = new Vehicle(3);
        v1.setLength(4);

        v2 = new Vehicle(2);
        v2.setLength(4);

        v3 = new Vehicle(3);
        v3.setLength(4);

        v4 = new Vehicle(4);
        v4.setLength(4);
    }

    @Test
    public void testHeadPush(){
        queueOne.push(v1);
        Assert.assertTrue(queueOne.size() == 1);
        Assert.assertTrue(queueOne.getHead().equals(v1));
    }

    @Test
    public void testHeadPop(){
        queueOne.push(v1);
        queueOne.pop();
        Assert.assertTrue(queueOne.size()==0);
    }

    @Test
    public void testSorting(){
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
}
