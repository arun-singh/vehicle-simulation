package GUI;

import Graph.Link;
import Graph.Node;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openstreetmap.gui.jmapviewer.Coordinate;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arun on 24/01/2017.
 */
public interface RequestMapData {
    public static List<Double[]> getCoordinates(Node upStream, Node downStream){

        double upStreamLat = upStream.getLatitude();
        double upStreamLong = upStream.getLongitude();
        double downStreamLat = downStream.getLatitude();
        double downStreamLong = downStream.getLongitude();

        String formattedCoords = upStreamLat+","+upStreamLong+":"+downStreamLat+","+downStreamLong;
        List<Double[]> linkCoordinates = new ArrayList<>();

        try {
            URL url = new URL("https://api.tomtom.com/routing/1/calculateRoute/" + formattedCoords
                    + "/json?key=qnyrrpsyzeueqxavgq8nyteb&travelMode=car");

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoOutput(true);
            InputStream input = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(input));
            StringBuilder sb = new StringBuilder();

            String response = "";
            while((response=br.readLine())!=null)
                sb.append(response+"\n");

            JSONObject jObject = new JSONObject(sb.toString());
            JSONArray routes = (JSONArray) jObject.get("routes");

            if(routes != null){
                JSONObject map = routes.getJSONObject(0);
                JSONArray legs = map.getJSONArray("legs");
                JSONArray coords = legs.getJSONObject(0).getJSONArray("points");

                for(int i = 0; i < coords.length(); i++){
                    JSONObject coord = coords.getJSONObject(i);
                    linkCoordinates.add(new Double[]{coord.getDouble("latitude"),  coord.getDouble("longitude")});
                }

            }

        } catch(MalformedURLException e){
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return linkCoordinates;
    }

    public double linkLength(Link link);
    public int getLanes(Link link);
}
