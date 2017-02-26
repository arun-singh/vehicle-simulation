import Graph.Link;
import Graph.Statistics;
import Graph.Vehicle;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIn.isIn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Arun on 24/02/2017.
 */
public class TestStatistics {

    @Test
    public void testTotalVehiclesOutput(){
        LinkedHashMap<Integer, Link> linkMap = new LinkedHashMap<>();
        for(int i = 0; i<10000;i++){
            Link link = new Link(i);
            link.getOutputQueue().received(new Vehicle(i), i);
            linkMap.put(i,link);
        }

        int out = Statistics.totalVehiclesOutput(linkMap);
        assertThat(out, is(10000));
    }

    @Test
    public void testTotalVehiclesInput(){
        LinkedHashMap<Integer, Link> linkMap = new LinkedHashMap<>();
        for(int i = 0; i<10000;i++){
            Link link = new Link(i, 10);
            link.getInputQueue().push(new Vehicle(i), i);
            linkMap.put(i,link);
        }

        int out = Statistics.totalVehiclesInput(linkMap);
        assertThat(out, is(10000));
    }
}
