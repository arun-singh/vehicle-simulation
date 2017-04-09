package GUI;

import Graph.MapUtil;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.openstreetmap.gui.jmapviewer.Coordinate;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by Arun on 18/03/2017.
 */
public class CoordPane extends GridPane {

    Label _maxLatLabel = new Label("Max Lat");
    Label _minLatLabel = new Label("Min Lat");
    Label _maxLonLabel = new Label("Max Lon");
    Label _minLonLabel = new Label("Min Lon");

    public static TextField _maxLatText = new TextField();
    public static TextField _minLatText = new TextField();
    public static TextField _maxLonText = new TextField();
    public static TextField _minLonText = new TextField();

    public CoordPane(){
        super();
        setWidth(Display.WIDTH);

        HBox maxLat = new HBox(1);
        maxLat.getChildren().add(0, _maxLatLabel);
        maxLat.getChildren().add(1, _maxLatText);
        _maxLatText.setText("52.422039");
        HBox minLat = new HBox(1);
        minLat.getChildren().add(0, _minLatLabel);
        minLat.getChildren().add(1, _minLatText);
        _minLatText.setText("52.404448");
        HBox maxLon = new HBox(1);
        maxLon.getChildren().add(0, _maxLonLabel);
        maxLon.getChildren().add(1, _maxLonText);
        _maxLonText.setText("-1.774139");
        HBox minLon = new HBox(1);
        minLon.getChildren().add(0, _minLonLabel);
        minLon.getChildren().add(1, _minLonText);
        _minLonText.setText("-1.813321");

        add(maxLat, 0, 0);
        add(minLat, 1, 0);
        add(maxLon, 2, 0);
        add(minLon, 3, 0);
    }

    public static boolean checkAndSetCoordinates(Coordinate bottomLeft, Coordinate bottomRight, Coordinate topLeft, Coordinate topRight){

        double maxLat = MapUtil.maxLat(bottomLeft, bottomRight, topLeft, topRight);
        double maxLon = MapUtil.maxLon(bottomLeft, bottomRight, topLeft, topRight);
        double minLat = MapUtil.minLat(bottomLeft, bottomRight, topLeft, topRight);
        double minLon = MapUtil.minLon(bottomLeft, bottomRight, topLeft, topRight);

        if(maxLat>Display.maxLatDatabase || maxLon>Display.maxLonDatabase || minLat<Display.minLatDatabase || minLon<Display.minLonDatabase){
            notWithinBoundsError();
            return false;
        }
        _maxLatText.setText(Double.toString(round(maxLat,7)));
        _maxLonText.setText(Double.toString(round(maxLon,7)));
        _minLatText.setText(Double.toString(round(minLat,7)));
        _minLonText.setText(Double.toString(round(minLon,7)));

        Display.recordBox.setSelected(false);
        return true;
    }


    public static void notWithinBoundsError(){
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("One or more ponts outside of simulation area");
            alert.setContentText("Draw box within yellow markers");
            alert.showAndWait();
        });
    }

    /**
     * Author: http://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
     * @param value
     * @param places
     * @return
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
