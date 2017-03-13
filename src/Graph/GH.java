package Graph;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PointList;

import java.util.Locale;

/**
 * Created by Arun on 12/03/2017.
 */
public class GH {
    public static void request(){
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile("/Users/Arun/Documents/MOJO-Simulation/Resources/Birmingham.osm");
        hopper.setEncodingManager(new EncodingManager("car"));
        hopper.setGraphHopperLocation("../Resources/gh");

        hopper.importOrLoad();


        GHRequest req = new GHRequest(52.4415506, -1.9325445, 52.4422945, -1.9280491).
                setWeighting("fastest").
                setVehicle("car").
                setLocale(Locale.UK);
        GHResponse rsp = hopper.route(req);

        if(rsp.hasErrors()) {
            // handle them!
            // rsp.getErrors()
            System.out.println("errors");
            return;
        }

        PathWrapper path = rsp.getBest();

        PointList pointList = path.getPoints();
        double distance = path.getDistance();
        long timeInMs = path.getTime();

        for(int i = 0; i <pointList.size(); i++){
            double lat = pointList.getLat(i);
            double lon = pointList.getLon(i);
            System.out.println(lat + ", " + lon);
        }


        InstructionList il = path.getInstructions();
//// iterate over every turn instruction
//        for(Instruction instruction : il) {
//            instruction.getDistance();
//   ...
//        }
    }
}
