package GUI;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

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
        setWidth(Main.WIDTH);

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

}
