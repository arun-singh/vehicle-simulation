import Simulation.Simulate;
import org.junit.Test;

/**
 * Created by Arun on 14/02/2017.
 */
public class TestShockwaves {

    @Test
    public void randomShockwaveTest(){
        System.out.println(Simulate.shockwaveSpeed(20, 10, 5, 10));
    }

}
