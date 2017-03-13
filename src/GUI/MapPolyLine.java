package GUI;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.Layer;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.Style;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arun on 11/03/2017.
 */
    public class MapPolyLine extends MapPolygonImpl {
        public MapPolyLine(List<? extends ICoordinate> wayPoints) {
            super(null, null, wayPoints);
        }

        public MapPolyLine(Layer layer1, String string, ArrayList<Coordinate> coordinates, Style style1) {
            super(layer1, string, coordinates, style1);
        }

        @Override
        public void paint(Graphics g, List<Point> wayPoints) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(getColor());
            g2d.setStroke(getStroke());
            Path2D path = buildPath(wayPoints);
            g2d.draw(path);
            g2d.dispose();
        }

        private Path2D buildPath(List<Point> wayPoints) {
            Path2D path = new Path2D.Double();
            if (wayPoints != null && wayPoints.size() > 0) {
                Point firstPoint = wayPoints.get(0);
                path.moveTo(firstPoint.getX(), firstPoint.getY());
                for (Point p : wayPoints) {
                    path.lineTo(p.getX(), p.getY());
                }
            }
            return path;
        }

    }

