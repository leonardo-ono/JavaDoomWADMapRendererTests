package doom;

import doom.Test.LineDef;
import doom.Test.Node;
import doom.Test.Sector;
import doom.Test.Seg;
import doom.Test.SideDef;
import doom.Test.Subsector;
import static doom.Test.lineDefs;
import static doom.Test.segs;
import static doom.Test.vertexes;
import java.io.File;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author Leo
 */
public class MapViewer extends JPanel {
    
    public static final double MAP_SCALE = 0.15;
    
    private Player player;
    
    private BufferedImage offscreenTopView;
    private Graphics2D offscreenTopViewG2D;
    private BufferedImage offscreen3D;
    private Graphics2D offscreen3DG2D;
    
    public void start() {
        setPreferredSize(new Dimension(800, 600));

//        String wadFile = System.getProperty("user.dir").trim();
//        String fileSeparator = File.pathSeparator;
//        if (wadFile.endsWith(fileSeparator)) {
//            wadFile += fileSeparator;
//        }
//        wadFile += "DOOM1.WAD";

        String wadFile = "D:/dosbox-0.74-3/hd/doomshr/DOOM1.WAD";
        Test.loadWad(wadFile);
        
        // map E1M1
        Test.extractThings(Test.bb, 7);
        Test.extractLineDefs(Test.bb, 8);
        Test.extractSideDefs(Test.bb, 9);
        Test.extractVertexes(Test.bb, 10);
        Test.extractSegs(Test.bb, 11);
        Test.extractSubsectors(Test.bb, 12);
        Test.extractNodes(Test.bb, 13);
        Test.extractSectors(Test.bb, 14);
        
        player = new Player(Test.things.get(0));
        
        offscreenTopView = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        offscreenTopViewG2D = offscreenTopView.createGraphics();

        offscreen3D = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        offscreen3DG2D = offscreen3D.createGraphics();
        
        addKeyListener(new Input());
    }
    
    private void update() {
        player.update();
    }
    
    int drawIndex;
    
    private Color clearColor = new Color(0, 0, 0, 0);
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        update();
        
        Graphics2D g2d = (Graphics2D) g;
        
        offscreenTopViewG2D.setBackground(clearColor);
        offscreenTopViewG2D.clearRect(0, 0, 800, 600);
        offscreen3DG2D.setBackground(clearColor);
        offscreen3DG2D.clearRect(0, 0, 800, 600);
        
        AffineTransform at = offscreenTopViewG2D.getTransform();
        //g2d.translate(150, -150);
        offscreenTopViewG2D.translate(200, -200);
        offscreenTopViewG2D.scale(1, -1);
        
        offscreenTopViewG2D.drawLine(-400, 0, 400, 0);
        offscreenTopViewG2D.drawLine(0, -300, 0, 300);
        
        //drawLineDefs(offscreenTopViewG2D);
        drawVertexes(offscreenTopViewG2D);
        //drawNodes(offscreenTopViewG2D);
        //drawSegs(offscreenTopViewG2D);
        
        player.draw(offscreenTopViewG2D);
        
        
        drawIndex = 0;
        Node rootNode = Test.nodes.get(Test.nodes.size() - 1);
        traverseBspNodesFarthestToClosest(offscreenTopViewG2D, offscreen3DG2D, rootNode, 0);
        
        offscreenTopViewG2D.setTransform(at);
        
        g2d.drawImage(offscreen3D, 0, 0, null);
        g2d.drawImage(offscreenTopView, 0, 0, null);
        
        try {
            Thread.sleep(1000 / 120);
        } catch (InterruptedException ex) { }
        
        repaint();
    }
    
    private void drawVertexes(Graphics2D g) {
        g.setColor(Color.RED);
        for (int i = 0; i < vertexes.size(); i++) {
            Point p = vertexes.get(i);
            int px = (int) (MAP_SCALE * (short) p.x);
            int py = (int) (MAP_SCALE * (short) p.y);
            g.fillOval(px - 2, py - 2, 4, 4);
        }                  
    }
    
    private void drawLineDefs(Graphics2D g) {
        g.setColor(Color.BLUE);
        for (int i = 0; i < lineDefs.size(); i++) {
            LineDef lineDef = lineDefs.get(i);
            Point v1 = vertexes.get(lineDef.startVertex);
            Point v2 = vertexes.get(lineDef.endVertx);
            int p1x = (int) (MAP_SCALE * (short) v1.x);
            int p1y = (int) (MAP_SCALE * (short) v1.y);
            int p2x = (int) (MAP_SCALE * (short) v2.x);
            int p2y = (int) (MAP_SCALE * (short) v2.y);
            g.drawLine(p1x, p1y, p2x, p2y);
        }                  
    }
    
    private void drawSegs(Graphics2D g) {
        for (int i = 0; i < Test.segs.size(); i++) {
            Seg seg = segs.get(i);
            Point v1 = vertexes.get(seg.startingVertexNumber);
            Point v2 = vertexes.get(seg.endingVertexNumber);
            int p1x = (int) (MAP_SCALE * (short) v1.x);
            int p1y = (int) (MAP_SCALE * (short) v1.y);
            int p2x = (int) (MAP_SCALE * (short) v2.x);
            int p2y = (int) (MAP_SCALE * (short) v2.y);
            g.setColor(new Color((int) (Integer.MAX_VALUE * Math.random())));
            g.drawLine(p1x, p1y, p2x, p2y);
        }                  
    }
    
    private void drawNodes(Graphics2D g) {
        Node node = Test.nodes.get(Test.nodes.size() - 1);
        //for (Node node : Test.nodes) {
        boolean finished = false;

        while (!finished) {
            int childNodeId = node.getChildSide(player.x, player.y);
            System.out.println("childNodeId=" + childNodeId);

            if (childNodeId == 1) {
                System.out.println("left child id:" + node.LeftChildID);
                System.out.println("right child id:" + node.RightChildID);
                if ((short) node.LeftChildID < 0) {
                    int lbx1 = (int) (MAP_SCALE * (short) node.LeftBoxLeft);
                    int lby1 = (int) (MAP_SCALE * (short) node.LeftBoxTop);
                    int lbx2 = (int) (MAP_SCALE * (short) node.LeftBoxRight);
                    int lby2 = (int) (MAP_SCALE * (short) node.LeftBoxBottom);
                    g.setColor(Color.GREEN);
                    g.drawRect(Math.min(lbx1, lbx2), Math.min(lby1, lby2), Math.abs(lbx2 - lbx1), Math.abs(lby2 - lby1));

                    drawSubsector(g, node.LeftChildID & 0x7fff, 10);
                    finished = true;
                }
                else {
                    node = Test.nodes.get(node.LeftChildID);
                }
            }
            else if (childNodeId == 0) {
                if ((short) node.RightChildID < 0) {
                    int rbx1 = (int) (MAP_SCALE * (short) node.RightBoxLeft);
                    int rby1 = (int) (MAP_SCALE * (short) node.RightBoxTop);
                    int rbx2 = (int) (MAP_SCALE * (short) node.RightBoxRight);
                    int rby2 = (int) (MAP_SCALE * (short) node.RightBoxBottom);
                    g.setColor(Color.MAGENTA);
                    g.drawRect(Math.min(rbx1, rbx2), Math.min(rby1, rby2), Math.abs(rbx2 - rbx1), Math.abs(rby2 - rby1));

                    drawSubsector(g, node.RightChildID & 0x7fff, 10);

                    finished = true;
                }
                else {
                    node = Test.nodes.get(node.RightChildID);
                }
            }

            int x1 = (int) (MAP_SCALE * (short) node.XPartition);
            int y1 = (int) (MAP_SCALE * (short) node.YPartition);
            int x2 = x1 + (int) (MAP_SCALE * (short) node.ChangeXPartition); 
            int y2 = y1 + (int) (MAP_SCALE * (short) node.ChangeYPartition); 
            g.setColor(Color.YELLOW);
            g.drawLine(x1, y1, x2, y2);
        }
            
        //}
        
    }
    
    private void traverseBspNodesFarthestToClosest(Graphics2D gTopView, Graphics2D g3DView, Node node, int level) {
        int childNodeSide = node.getChildSide(player.x, player.y);        
        
        int closestChildId = 0;
        int farthestChildId = 0;
        if (childNodeSide == 0) {
            closestChildId = node.RightChildID;
            farthestChildId = node.LeftChildID;
        }
        else {
            closestChildId = node.LeftChildID;
            farthestChildId = node.RightChildID;
        }
        
        if ((short) farthestChildId < 0) {        
            drawSubsector3DWall(gTopView, g3DView, farthestChildId & 0x7fff, level + 1);
        }
        else {
            Node childNode = Test.nodes.get(farthestChildId);
            traverseBspNodesFarthestToClosest(gTopView, g3DView, childNode, level + 1);
        }

        if ((short) closestChildId < 0) {        
            drawSubsector3DWall(gTopView, g3DView, closestChildId & 0x7fff, level + 1);
        }
        else {
            Node childNode = Test.nodes.get(closestChildId);
            traverseBspNodesFarthestToClosest(gTopView, g3DView, childNode, level + 1);
        }
    }

    private void drawSubsector3DWall(Graphics2D gTopView, Graphics2D g3DView, int subsectorIndex, int level) {
        //Color color = new Color((int) (Integer.MAX_VALUE * Math.random()));
        Color color = new Color((int) (0x1234 * drawIndex) + 0xff000000);

        drawIndex++;
        //if (drawIndex++ > 200) {
        //    return;
        //}

        Subsector subsector = Test.subsectors.get(subsectorIndex);
        for (int i = subsector.firstSegNumber; i < subsector.firstSegNumber + subsector.segCount; i++) {
            drawSeg3DWall(gTopView, g3DView, i, color);
        }
    }
    
    private final Vec2 vaTmp = new Vec2();
    private final Vec2 vbTmp = new Vec2();
    private final Polygon polygon = new Polygon();
    
    private void drawSeg3DWall(Graphics2D gTopView, Graphics2D g3DView, int segIndex, Color color) {
        Seg seg = segs.get(segIndex);
        
        LineDef lineDef = Test.lineDefs.get(seg.lineDefNumber);
        
        // portal ?
        //if (lineDef.backSidedef == 0xffff) {
        //    return;
        //}
        
        SideDef frontSideDef = Test.sideDefs.get(lineDef.frontSidedef);
        //SideDef backSideDef = Test.sideDefs.get(lineDef.backSidedef);
                
        Sector sector = Test.sectors.get(frontSideDef.sectorNumber);
        // System.out.println("sector " + sector);
                
        Point v1 = vertexes.get(seg.startingVertexNumber);
        Point v2 = vertexes.get(seg.endingVertexNumber);
        int p1x = (int) (MAP_SCALE * (short) v1.x);
        int p1y = (int) (MAP_SCALE * (short) v1.y);
        int p2x = (int) (MAP_SCALE * (short) v2.x);
        int p2y = (int) (MAP_SCALE * (short) v2.y);
        gTopView.setColor(color);
        gTopView.drawLine(p1x, p1y, p2x, p2y);
        
        p1x = (short) v1.x;
        p1y = (short) v1.y;
        p2x = (short) v2.x;
        p2y = (short) v2.y;
        int ceil = (short) sector.ceilingHeight;
        int floor = (short) sector.floorHeight;
        
        ceil -= player.height;
        floor -= player.height;
        
        vaTmp.set(p1x - player.x, p1y - player.y);
        vbTmp.set(p2x - player.x, p2y - player.y);
        
        // 3d
        Vec2 playerDir = player.getDirection();
        double za = playerDir.dot(vaTmp);
        double zb = playerDir.dot(vbTmp);
        double pdx = playerDir.x;
        double pdy = playerDir.y;
        playerDir.set(-pdy, pdx);
        double xa = playerDir.dot(vaTmp);
        double xb = playerDir.dot(vbTmp);
        
        if (za <= 0.1 || zb <= 0.1) {
            return;
        }
            
        double plane = 200;
        double scrXA = plane * (xa / -za);
        double scrXB = plane * (xb / -zb);
        double scrYACeil = plane * (ceil / -za);
        double scrYAFloor = plane * (floor / -za);
        double scrYBCeil = plane * (ceil / -zb);
        double scrYBFloor = plane * (floor / -zb);
        
        polygon.reset();
        polygon.addPoint((int) (scrXA + 400), (int) (scrYAFloor + 300));
        polygon.addPoint((int) (scrXA + 400), (int) (scrYACeil + 300));
        polygon.addPoint((int) (scrXB + 400), (int) (scrYBCeil + 300));
        polygon.addPoint((int) (scrXB + 400), (int) (scrYBFloor + 300));
        g3DView.setColor(color);
        //g3DView.drawRect((int) (scrXA + 400), 200, (int) (scrXB + 400), (int) (200 + );
        if (lineDef.backSidedef == 0xffff) {
            g3DView.fill(polygon);
        }
        else {
            g3DView.draw(polygon);
        }
        
        //g3DView.drawLine(0, 0, 800, 600);
    }
    
    private void drawSubsector(Graphics2D g, int subsectorIndex, int level) {
        //Color color = new Color((int) (Integer.MAX_VALUE * Math.random()));
        Color color = new Color((int) (0x1234 * drawIndex) + 0xff000000);

        if (drawIndex++ > 1) {
            return;
        }

        Subsector subsector = Test.subsectors.get(subsectorIndex);
        //for (int i = subsector.firstSegNumber; i < subsector.firstSegNumber + subsector.segCount; i++) {
        for (int i = subsector.firstSegNumber; i <= subsector.firstSegNumber; i++) {
            drawSeg(g, i, color);
        }
    }

    private void drawSeg(Graphics2D g, int segIndex, Color color) {
        Seg seg = segs.get(segIndex);
        
        LineDef lineDef = Test.lineDefs.get(seg.lineDefNumber);
        
        SideDef frontSideDef = Test.sideDefs.get(lineDef.frontSidedef);
        //SideDef backSideDef = Test.sideDefs.get(lineDef.backSidedef);
                
        Sector sector = Test.sectors.get(lineDef.sectorTag);
        // System.out.println("sector " + sector);
                
        Point v1 = vertexes.get(seg.startingVertexNumber);
        Point v2 = vertexes.get(seg.endingVertexNumber);
        int p1x = (int) (MAP_SCALE * (short) v1.x);
        int p1y = (int) (MAP_SCALE * (short) v1.y);
        int p2x = (int) (MAP_SCALE * (short) v2.x);
        int p2y = (int) (MAP_SCALE * (short) v2.y);
        g.setColor(color);
        g.drawLine(p1x, p1y, p2x, p2y);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MapViewer ddaTest = new MapViewer();
            ddaTest.start();
            JFrame frame = new JFrame();
            frame.setTitle("DOOM WAD Map Viewer Test #1");
            
            frame.getContentPane().add(ddaTest);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            ddaTest.requestFocus();
        });
    }
    
}
