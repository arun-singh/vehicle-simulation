//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package GUI;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;
import java.util.stream.Collectors;

import Graph.Link;
import Graph.MapUtil;
import Graph.Node;
import org.openstreetmap.gui.jmapviewer.*;

public class MapController extends JMapController implements MouseListener, MouseMotionListener, MouseWheelListener {
    private static final int MOUSE_BUTTONS_MASK = 7168;
    private static final int MAC_MOUSE_BUTTON3_MASK = 1152;
    private Point lastDragPoint;
    private boolean isMoving = false;
    private boolean movementEnabled = true;
    private int movementMouseButton = 3;
    private int movementMouseButtonMask = 4096;
    private boolean wheelZoomEnabled = true;
    private boolean doubleClickZoomEnabled = true;
    private Point origin = null;

    public MapController(JMapViewer map) {
        super(map);
    }

    public void mouseDragged(MouseEvent e) {
        if (this.movementEnabled && this.isMoving) {
            if ((e.getModifiersEx() & 7168) == this.movementMouseButtonMask || isPlatformOsx() && e.getModifiersEx() == 1152) {
                Point p = e.getPoint();
                if (this.lastDragPoint != null && !Display.recordBox.isSelected()) {
                    int diffx = this.lastDragPoint.x - p.x;
                    int diffy = this.lastDragPoint.y - p.y;
                    this.map.moveMap(diffx, diffy);
                }
                this.lastDragPoint = p;
            }
        }
    }

//    public void mouseClicked(MouseEvent e) {
//        if(this.doubleClickZoomEnabled && e.getClickCount() == 2 && e.getButton() == 1) {
//            this.map.zoomIn(e.getPoint());
//        }
//
//    }

    public void mouseClicked(MouseEvent e) {
        Coordinate c = map.getPosition(e.getPoint());
       // System.out.println(e.getPoint().getX() + ", " + e.getPoint().getY());
        MapMarkerDot source = nearest(c);
        Node nsource = new Node(source.getLat(), source.getLon());
        System.out.println("-----------------------------------------------");
        System.out.println(source.getLat() + ", " + source.getLon());

        List<Link> targetList = Map.getInstance().getGrid()
                .getLinkMap()
                .entrySet()
                .stream()
                .filter(m->m.getValue().getSource().equals(nsource))
                .map(l->l.getValue())
                .collect(Collectors.toList());

        for(Link link : targetList){
            System.out.println(link.getId());

        }

//        if(targetList.size()==0)
//            System.out.println("No Targets");
//        else{
//            System.out.println("-----------------------------------------------");
//            System.out.println("Source has " + targetList.size() + " targets");
//            Color sourceColour = source.getBackColor().equals(Color.GREEN) ? Color.YELLOW : Color.GREEN;
//            source.setBackColor(sourceColour);
//
//            for(int i = 0 ; i < targetList.size(); i++){
//                double targetLat = targetList.get(i).getTarget().getLatitude();
//                double targetLon = targetList.get(i).getTarget().getLongitude();
//                System.out.println(targetLat + ", " + targetLon);
//                for (int j = 0; j < super.map.getMapMarkerList().size(); j++) {
//                    if(super.map.getMapMarkerList().get(j).getLat() == targetLat && super.map.getMapMarkerList().get(j).getLon()==targetLon){
//                        List<MapMarker> l = super.map.getMapMarkerList();
//                        MapMarkerDot ret= (MapMarkerDot) super.map.getMapMarkerList().get(j);
//                        Color targetColour = source.getBackColor().equals(Color.GREEN) ? Color.RED : Color.YELLOW;
//                        ret.setBackColor(targetColour);
//                        map.repaint();
//                        map.revalidate();
//                        break;
//                    }
//                }
//            }
//        }
    }

    public void mousePressed(MouseEvent e) {
        if(e.getButton() == this.movementMouseButton || isPlatformOsx() && e.getModifiersEx() == 1152) {
            origin = e.getPoint();
            this.lastDragPoint = null;
            this.isMoving = true;
        }

    }

    public void mouseReleased(MouseEvent e) {
        if(e.getButton() == this.movementMouseButton || isPlatformOsx() && e.getButton() == 1) {
            if(Display.recordBox.isSelected()){
                calculateBox(origin, new Point(e.getX(), e.getY()));
                origin = null;
            }
            this.lastDragPoint = null;
            this.isMoving = false;
        }

    }

    private void calculateBox(Point origin, Point end) {
        Coordinate topLeft = null;
        Coordinate bottomRight = null;
        Coordinate topRight = null;
        Coordinate bottomLeft = null;

        int startX = origin.x;
        int startY = origin.y;
        int endX = end.x;
        int endY = end.y;

        if(endX>startX && endY<startY){
            topLeft = map.getPosition(new Point(startX, endY));
            bottomRight = map.getPosition(new Point(endX, startY));
            topRight = map.getPosition(new Point(endX, endY));
            bottomLeft = map.getPosition(new Point(startX, startY));
        }else if(endX<startX && endY<startY){
            topLeft = map.getPosition(new Point(endX, endY));
            bottomRight = map.getPosition(new Point(startX, startY));
            topRight = map.getPosition(new Point(startX, endY));
            bottomLeft = map.getPosition(new Point(endX, startY));
        }else if(endX>startX && endY>startY){
            topLeft = map.getPosition(new Point(startX, startY));
            bottomRight = map.getPosition(new Point(endX, endY));
            topRight = map.getPosition(new Point(endX, startY));
            bottomLeft = map.getPosition(new Point(startX, endY));
        }else if(endX<startX && endY>startY){
            topLeft = map.getPosition(new Point(endX, startY));
            bottomRight = map.getPosition(new Point(startX, endY));
            topRight = map.getPosition(new Point(startX, startY));
            bottomLeft = map.getPosition(new Point(endX, endY));
        }

        MapMarkerDot tL = new MapMarkerDot(topLeft);
        MapMarkerDot tR = new MapMarkerDot(topRight);
        MapMarkerDot bL = new MapMarkerDot(bottomLeft);
        MapMarkerDot bR = new MapMarkerDot(bottomRight);
        map.addMapMarker(tL);
        map.addMapMarker(tR);
        map.addMapMarker(bL);
        map.addMapMarker(bR);

        MapRectangleImpl rec = new MapRectangleImpl(topLeft, bottomRight);
        rec.setBackColor(Color.black);
        rec.setVisible(true);
        rec.setColor(Color.black);
        map.addMapRectangle(rec);
        map.paintImmediately(0, 0, map.getWidth(), map.getHeight());

        CoordPane._maxLatText.setText(MapUtil.maxLat(bottomLeft, bottomRight, topLeft, topRight));
        CoordPane._maxLonText.setText(MapUtil.maxLon(bottomLeft, bottomRight, topLeft, topRight));
        CoordPane._minLatText.setText(MapUtil.minLat(bottomLeft, bottomRight, topLeft, topRight));
        CoordPane._minLonText.setText(MapUtil.minLon(bottomLeft, bottomRight, topLeft, topRight));

        Display.recordBox.setSelected(false);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        if(this.wheelZoomEnabled) {
            this.map.setZoom(this.map.getZoom() - e.getWheelRotation(), e.getPoint());
        }
    }

    public boolean isMovementEnabled() {
        return this.movementEnabled;
    }

    public void setMovementEnabled(boolean movementEnabled) {
        this.movementEnabled = movementEnabled;
    }

    public int getMovementMouseButton() {
        return this.movementMouseButton;
    }

    public void setMovementMouseButton(int movementMouseButton) {
        this.movementMouseButton = movementMouseButton;
        switch(movementMouseButton) {
            case 1:
                this.movementMouseButtonMask = 1024;
                break;
            case 2:
                this.movementMouseButtonMask = 2048;
                break;
            case 3:
                this.movementMouseButtonMask = 4096;
                break;
            default:
                throw new RuntimeException("Unsupported button");
        }

    }

    public boolean isWheelZoomEnabled() {
        return this.wheelZoomEnabled;
    }

    public void setWheelZoomEnabled(boolean wheelZoomEnabled) {
        this.wheelZoomEnabled = wheelZoomEnabled;
    }

    public boolean isDoubleClickZoomEnabled() {
        return this.doubleClickZoomEnabled;
    }

    public void setDoubleClickZoomEnabled(boolean doubleClickZoomEnabled) {
        this.doubleClickZoomEnabled = doubleClickZoomEnabled;
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
//        if(isPlatformOsx()) {
//            if(!this.movementEnabled || !this.isMoving) {
//                return;
//            }
//            if(e.getModifiersEx() == 128) {
//                Point p = e.getPoint();
//                if(this.lastDragPoint != null) {
//                    int diffx = this.lastDragPoint.x - p.x;
//                    int diffy = this.lastDragPoint.y - p.y;
//                    this.map.moveMap(diffx, diffy);
//                }
//                this.lastDragPoint = p;
//            }
//        }
    }

    public static boolean isPlatformOsx() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().startsWith("mac os x");
    }

    public MapMarkerDot nearest(Coordinate c){
        MapMarkerDot mmd = (MapMarkerDot)super.map.getMapMarkerList().stream()
                .reduce((m1, m2)-> MapUtil.getNodesDistance(m1.getCoordinate(), c) < MapUtil.getNodesDistance(m2.getCoordinate(), c) ? m1 : m2 )
                .get();
        return mmd;
    }
}
