package GUI;

import Graph.Link;
import Graph.Node;
import org.json.JSONArray;
import org.json.JSONObject;

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
public class RequestMapDataIm implements RequestMapData {


    @Override
    public double linkLength(Link link) {
        return 0;
    }

    @Override
    public int getLanes(Link link) {
        return 0;
    }

    public static void main(String[] args){
        RequestMapDataIm rmd = new RequestMapDataIm();
       // rmd.getCoordinates(new Node(51.411135, -0.868052), new Node(51.411383, -0.866346));
    }
}
