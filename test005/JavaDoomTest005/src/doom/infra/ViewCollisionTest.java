package doom.infra;

import doom.wad.ColorMap;
import doom.wad.Visplane;
import doom.wad.Linedef;
import doom.wad.Node;
import doom.wad.Sector;
import doom.wad.Seg;
import doom.wad.Sidedef;
import doom.wad.Subsector;
import doom.wad.WADLoader;
import static doom.wad.WADLoader.vertexes;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * View class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class ViewCollisionTest extends Canvas implements Runnable {
    
    private BufferedImage offscreen;
    private Graphics2D offscreenG2D;
    private int[] offscreenData;

    private BufferedImage offscreenB;
    private Graphics2D offscreenBG2D;
    private int[] offscreenBData;

    private BufferStrategy bs;
    private Thread gameLoop;
    private boolean running;
    private final Color clearColor = new Color(0, 0, 0, 0);
    
    private Player player;

    private final Point p0 = new Point();
    private final Point p1 = new Point();
    private final Point p2 = new Point();
    private final Point p3 = new Point();
    private final Vec2 v0 = new Vec2();
    private final Vec2 v1 = new Vec2();
    private final Vec2 v2 = new Vec2();
    private final Vec2 v3 = new Vec2();
    private final Polygon polygon = new Polygon();

    private final CBufferNode cbuffer = new CBufferNode(0, 798);
    private final List<Integer> cbufferResult = new ArrayList<>();

    private int[] upperOcclusion = new int[800];
    private int[] lowerOcclusion = new int[800];
    
    private final Visplane ceilVisplane = new Visplane(601);
    private final Visplane floorVisplane = new Visplane(601);
    private final Visplane skyHackVisplane = new Visplane(601);
    
    public void start() {
        setBackground(Color.BLACK);
//        String wadFile = System.getProperty("user.dir").trim();
//        String fileSeparator = File.separator;
//        if (wadFile.endsWith(wadFile)) {
//            wadFile += fileSeparator;
//        }
//        wadFile += "DOOM1.WAD";
//        System.out.println("loading wad: " + wadFile);
        //String wadFile = "D:/work/java/doom/level/DOOM_TEST.WAD";
        String wadFile = "D:/work/java/doom/level/DOOM1.WAD";
        try {
            WADLoader.load(wadFile);
        } catch (Exception ex) {
            Logger.getLogger(ViewCollisionTest.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }

        WADLoader.loadMap("E1M1");
        
        player = new Player(WADLoader.things.get(0));
        
        offscreen = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        offscreenG2D = offscreen.createGraphics();
        offscreenData = ((DataBufferInt) offscreen.getRaster().getDataBuffer()).getData();

        offscreenB = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        offscreenBG2D = offscreenB.createGraphics();
        offscreenBData = ((DataBufferInt) offscreenB.getRaster().getDataBuffer()).getData();
        
        invalidate();
        setIgnoreRepaint(true);
        createBufferStrategy(3);
        bs = getBufferStrategy();
        
        running = true;
        gameLoop = new Thread(this);
        gameLoop.start();
        
        addKeyListener(new Input());
    }
    
    private static final long TIME_PER_UPDATE = 1000000000 / 120;
    private int fps;
    
    @Override
    public void run() {
        long currentTime = System.nanoTime();
        long lastTime = currentTime;
        long delta;
        long unprocessedTime = 0;
        long fpsTime = 0;
        int fpsCount = 0;
        while (running) {
            currentTime = System.nanoTime();
            delta = currentTime - lastTime;
            unprocessedTime += delta;
            lastTime = currentTime;
            while (unprocessedTime >= TIME_PER_UPDATE) {
                unprocessedTime -= TIME_PER_UPDATE;
                fixedUpdate();
            }            
            update(delta * 0.000000001);
            
            do {
                do {
                
                    Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                    //g.setBackground(clearColor);
                    draw(g);
                    g.dispose();
                
                } while (bs.contentsRestored());
                
                bs.show();
                
            } while (bs.contentsLost());            
        
            fpsCount++;
            fpsTime += delta;
            if (fpsTime > 1000000000) {
                fpsTime -= 1000000000;
                fps = fpsCount;
                fpsCount = 0;
            }
            
            //try {
                Thread.yield();
            //    Thread.sleep(0);
            //} catch (InterruptedException ex) {
            //}
        }
    }
    
    private void update(double delta) {
        player.update(delta);
    }
    
    private void fixedUpdate() {
        Util.update();
        
//        // TODO provisory player height
//        final double radius = player.radius;
//        double[] offsets = { -radius, -0
//                           ,  radius,  0
//                           ,  0,  radius
//                           ,  0, -radius };
//        double targetHeight = -Double.MAX_VALUE;
//        for (int i = 0; i < offsets.length; i += 2) {
//            double targetHeightTmp = getCurrentHeight(player.x + offsets[i], player.y + offsets[i + 1]);
//            if (targetHeightTmp > targetHeight) {
//                targetHeight = targetHeightTmp;
//            }
//        }
//        player.heightFloor = targetHeight;
        
        double targetHeight2 = 48 + player.heightFloor;
        if (player.height < targetHeight2) {
            player.height += 0.2 * (targetHeight2 - player.height);
            player.vz = 0;
        }
        else {
            player.vz -= 0.1;
            if (player.vz < -5.0) {
                player.vz = -5.0;
            }
            player.height += player.vz;
        }
    
        player.heightWalk = player.height + 5 * Math.sin(0.05 * player.y) + 5 * Math.cos(0.05 * player.x) - 5;
        
        player.fixedUpdate();
        
    }

    // TODO try to find another way to get the current sector floor height
    //      without traverse all bsp nodes.
    private double getCurrentHeight(double x, double y) {
        Node node = WADLoader.nodes.get(WADLoader.nodes.size() - 1);
        //for (Node node : Test.nodes) {
        boolean finished = false;
        int childId = -1;
        while (!finished) {
            int childNodeId = node.getChildSide(x, y);
            //System.out.println("childNodeId=" + childNodeId);

            if (childNodeId == 1) {
                //System.out.println("left child id:" + node.LeftChildID);
                //System.out.println("right child id:" + node.RightChildID);
                if ((short) node.leftChildID < 0) {
                    childId = node.leftChildID;
                    finished = true;
                }
                else {
                    node = WADLoader.nodes.get(node.leftChildID);
                }
            }
            else if (childNodeId == 0) {
                if ((short) node.rightChildID < 0) {
                    childId = node.rightChildID;
                    finished = true;
                }
                else {
                    node = WADLoader.nodes.get(node.rightChildID);
                }
            }
        }

        Subsector subsector = WADLoader.subsectors.get(childId & 0x7fff);
        Sector sector = subsector.segs.get(0).frontSector;
        return sector.floorHeight;        
    }
    
    private int segDrawOrder = 0;
    
    protected void draw(Graphics2D g2d) {
        g2d.clearRect(0, 0, 800, 600);
        
        offscreenG2D = offscreen.createGraphics();
        
        offscreenG2D.setBackground(clearColor);
        offscreenG2D.clearRect(0, 0, 800, 600);
        
        //offscreenBG2D.setBackground(Color.MAGENTA);
        //offscreenBG2D.clearRect(0, 0, 800, 600);
        
        Scaler.get(player.x, player.y, p0);
        offscreenG2D.translate(400 - p0.x, 300 - p0.y);
        
        drawLineDefs(offscreenG2D);
        drawSegs(offscreenG2D);
        
        WADLoader.blockmap.drawGrid(offscreenG2D, Color.ORANGE, Color.RED, player);
        
        

        cbufferResult.clear();
        cbuffer.reset();
        Arrays.fill(lowerOcclusion, 0);
        Arrays.fill(upperOcclusion, 600);

        //segDrawOrder = 0;
        //Node rootNode = WADLoader.nodes.get(WADLoader.nodes.size() - 1);
        //traverseBspNodesClosestToFarthest(offscreenG2D, rootNode, 0);
        
        //drawVertexes(offscreenG2D);
        player.draw(offscreenG2D);
        
        //offscreenG2D.dispose();
        
        //g2d.drawImage(offscreenB, 0, 0, getWidth(), getHeight(), null);
        g2d.drawImage(offscreen, 0, 0, null);
        
        //g2d.setXORMode(Color.WHITE);
        g2d.setColor(Color.WHITE);
        g2d.drawString("CANVAS SIZE: (" + offscreenB.getWidth() + ", " + offscreenB.getHeight() + ")", 20, 20);
        g2d.drawString("FPS:" + fps, 20, 40);
        //g2d.setPaintMode();
    }
    

    private void traverseBspNodesClosestToFarthest(
                                        Graphics2D g, Node node, int level) {
        
        if (cbuffer.isOccluded()) return;
        // if (segDrawOrder >= 1) return;
        
        int childNodeSide = node.getChildSide(player.x, player.y);        
        
        int closestChildId = 0;
        int farthestChildId = 0;

        if (childNodeSide == 0) {
            closestChildId = node.rightChildID;
            farthestChildId = node.leftChildID;
        }
        else {
            closestChildId = node.leftChildID;
            farthestChildId = node.rightChildID;
        }

        if (closestChildId < 0) {
            int childId = closestChildId & 0x7fff;
            Subsector subsector = WADLoader.subsectors.get(childId);
            drawSubsector(g, subsector);
        }
        else {
            Node childNode = WADLoader.nodes.get(closestChildId);
            traverseBspNodesClosestToFarthest(g, childNode, level + 1);
        }
        
        if (farthestChildId < 0) {        
            int childId = farthestChildId & 0x7fff;
            Subsector subsector = WADLoader.subsectors.get(childId);
            drawSubsector(g, subsector);
        }
        else {
            Node childNode = WADLoader.nodes.get(farthestChildId);
            traverseBspNodesClosestToFarthest(g, childNode, level + 1);
        }
        
        // draw partition
//        Scaler.get(node.partitionX, node.partitionY, p0);
//        Scaler.get(node.partitionX + node.changePartitionX
//                , node.partitionY + node.changePartitionY, p1);
//        
//        g.setColor(Color.YELLOW);
//        g.drawLine(p0.x, p0.y, p1.x, p1.y);

    }
    
    private void drawVertexes(Graphics2D g) {
        g.setColor(Color.RED);
        for (int i = 0; i < WADLoader.vertexes.size(); i++) {
            Scaler.get(vertexes.get(i), p0);
            g.fillOval(p0.x - 2, p0.y - 2, 4, 4);
        }                  
    }
    
    private void drawLineDefs(Graphics2D g) {
        g.setColor(Color.BLUE);
        for (int i = 0; i < WADLoader.linedefs.size(); i++) {
            Linedef linedef = WADLoader.linedefs.get(i);
            Scaler.get(linedef.startVertex, p0);
            Scaler.get(linedef.endVertex, p1);
            g.drawLine(p0.x, p0.y, p1.x, p1.y);
        }                  
    }

    private void drawSegs(Graphics2D g) {
        for (int i = 0; i < WADLoader.segs.size(); i++) {
            Seg seg = WADLoader.segs.get(i);
            g.setColor(Color.DARK_GRAY);
            Scaler.get(seg.startVertex, p0);
            Scaler.get(seg.endVertex, p1);
            g.drawLine(p0.x, p0.y, p1.x, p1.y);
        }                  
    }

    private void drawSubsector(Graphics2D g, Subsector subsector) {
        for (Seg seg : subsector.segs) {
            draw3DWall(g, seg);
        }
    }
    
    private void draw3DWall(Graphics2D g, Seg seg) {
        Sidedef frontSidedef = seg.frontSidedef;
        
        Point pa = seg.startVertex;
        Point pb = seg.endVertex;
        v0.set(pa.x - player.x, pa.y - player.y);
        v1.set(pb.x - player.x, pb.y - player.y);

        // convert to camera space
        v2.set(player.getDirection());
        double za = v2.dot(v0);
        double zb = v2.dot(v1);
        v2.set(-v2.y, v2.x);
        double xa = v2.dot(v0);
        double xb = v2.dot(v1);

        // wall culling (behind camera)
        if (za <= 0.1 && zb <= 0.1) {
            return;
        }
        
        double textureX0 = seg.offset + frontSidedef.offsetX; // player.textureOffsetX;
        double textureX1 = seg.offset + seg.length + frontSidedef.offsetX; //player.textureOffsetX;

        // wall clipping
        if (za <= 0.1) {
            double p = (zb - 0.1) / (zb - za);
            xa = xb + p * (xa - xb);
            textureX0 = textureX1 + p * (textureX0 - textureX1);
            za = 0.1;
        }
        else if (zb <= 0.1) {
            double p = (za - 0.1) / (za - zb);
            xb = xa + p * (xb - xa);
            textureX1 = textureX0 + p * (textureX1 - textureX0);
            zb = 0.1;
        }  

        double plane = 400;
        int scrXA = (int) (plane * xa / -za) + 400;
        int scrXB = (int) (plane * xb / -zb) + 400;

        // backface culling or frustrum culling for left and right plane
        if (scrXA >= scrXB) {
            return;
        }

//        offscreenG2D.setColor(seg.color);
//        Scaler.get(seg.startVertex, p0);
//        Scaler.get(seg.endVertex, p1);
//        offscreenG2D.drawLine(p0.x, p0.y, p1.x, p1.y);
//        int sx = (p1.x + p0.x) / 2;
//        int sy = (p0.y + p1.y) / 2;
//        offscreenG2D.drawString("" + (segDrawOrder++), sx, sy);

        int frontCeil = seg.frontSector.ceilingHeight - (int) player.heightWalk;
        int frontFloor = seg.frontSector.floorHeight - (int) player.heightWalk;
        double scrYAFrontCeil = plane * (frontCeil / -za) + 300;
        double scrYAFrontFloor = plane * (frontFloor / -za) + 300;
        double scrYBFrontCeil = plane * (frontCeil / -zb) + 300;
        double scrYBFrontFloor = plane * (frontFloor / -zb) + 300;

        int backCeil = 0;
        int backFloor = 0;
        double scrYABackCeil = 0;
        double scrYABackFloor = 0;
        double scrYBBackCeil = 0;
        double scrYBBackFloor = 0;
        boolean hasLowerWall = false;
        boolean hasUpperWall = false;
        if (seg.isPortal) {
            backCeil = seg.backSector.ceilingHeight - (int) player.heightWalk;
            backFloor = seg.backSector.floorHeight - (int) player.heightWalk;
            scrYABackCeil = plane * (backCeil / -za) + 300;
            scrYABackFloor = plane * (backFloor / -za) + 300;
            scrYBBackCeil = plane * (backCeil / -zb) + 300;
            scrYBBackFloor = plane * (backFloor / -zb) + 300;
            hasLowerWall = backFloor > frontFloor;
            hasUpperWall = backCeil < frontCeil;
        }
        
        // draw vertical columns
        double dxInv = 1.0 / (scrXB - scrXA);
        double zInvStep = (1 / zb - 1 / za) * dxInv;
        double textureXStep = (textureX1 / zb - textureX0 / za) * dxInv;
        double middleCeilStep = (scrYBFrontCeil - scrYAFrontCeil) * dxInv;
        double middlefloorStep = (scrYBFrontFloor - scrYAFrontFloor) * dxInv;
        double lowerCeilStep = (scrYBBackFloor - scrYABackFloor) * dxInv;
        double lowerfloorStep = (scrYBFrontFloor - scrYAFrontFloor) * dxInv;
        double upperCeilStep = (scrYBFrontCeil - scrYAFrontCeil) * dxInv;
        double upperFloorStep = (scrYBBackCeil - scrYABackCeil) * dxInv;

        cbufferResult.clear();
        if (seg.isPortal) {
            cbuffer.checkSpan(scrXA, scrXB, cbufferResult);
        }
        else {
            cbuffer.addSpan(scrXA, scrXB, cbufferResult);
        }

        if (cbufferResult.isEmpty()) return; // completely occluded

        for (int clip = 0; clip < cbufferResult.size(); clip += 2) {
        
            int clipLeft = cbufferResult.get(clip);
            int clipRight = cbufferResult.get(clip + 1);
            
            double currentMiddleCeil = scrYAFrontCeil;
            double currentMiddleFloor = scrYAFrontFloor;

            double currentLowerCeil = scrYABackFloor;
            double currentLowerFloor = scrYAFrontFloor;
            double currentUpperCeil = scrYAFrontCeil;
            double currentUpperFloor = scrYABackCeil;

            double currentZInv = 1 / za;
            double currentTextureX = textureX0 / za;
            int scrLeft = scrXA;
            int scrRight = scrXB;
            
            // clipping horizontally
            if (scrLeft < clipLeft) {
                int dif = clipLeft - scrXA;
                currentTextureX += dif * textureXStep;
                currentZInv += dif * zInvStep;
                currentMiddleCeil += dif * middleCeilStep;
                currentMiddleFloor += dif * middlefloorStep;
                
                if (hasLowerWall) {
                    currentLowerCeil += dif * lowerCeilStep;
                    currentLowerFloor += dif * lowerfloorStep;
                }
                if (hasUpperWall) {
                    currentUpperCeil += dif * upperCeilStep;
                    currentUpperFloor += dif * upperFloorStep;
                }
                
                scrLeft = clipLeft;
            }
            if (scrRight > clipRight) {
                scrRight = clipRight;
            }
            
            ceilVisplane.reset();
            floorVisplane.reset();
            skyHackVisplane.reset();
            
            for (int x = scrLeft; x <= scrRight; x++) {
                double currentZ = 1.0 / currentZInv;

                int[] colorMap = ColorMap.getWallColorMap(currentZ, seg);
                
                int middleMaxY = (int) currentMiddleFloor;
                int middleMinY = (int) currentMiddleCeil;
                double middleDy = middleMaxY - middleMinY;
                double middleTextureYStep = (frontCeil - frontFloor) / middleDy;
                double middleTextureY = frontSidedef.offsetY; // player.textureOffsetY;
                if (middleMinY < lowerOcclusion[x]) {
                    double dif = lowerOcclusion[x] - middleMinY;
                    middleTextureY = dif * middleTextureYStep + frontSidedef.offsetY; //player.textureOffsetY;
                    middleMinY = lowerOcclusion[x];
                }
                middleMaxY = middleMaxY > upperOcclusion[x] ? upperOcclusion[x] : middleMaxY;
                if (!seg.isPortal && frontSidedef.middleTexture != null) {
                    //System.out.println("angle " + (seg.angle >> 14)); 
                    
                    int textureWidth = frontSidedef.middleTexture.image.getWidth();
                    //int textureHeight = frontSidedef.middleTexture.image.getHeight();
                    int[] wallTextureData = frontSidedef.middleTexture.imageData;
                    int pixelIndex = x + middleMinY * 800;
                    int tx = (int) (currentTextureX * currentZ) & 0x7fffffff;
                    for (int y = middleMinY; y < middleMaxY; y++) {
                        int ty = (int) (middleTextureY); // % textureHeight;
                        offscreenBData[pixelIndex] = colorMap[wallTextureData[ty * textureWidth + tx]];
                        pixelIndex += 800;
                        middleTextureY += middleTextureYStep;
                    }
                }

                //floor
                int ceilMin = (int) Math.max(lowerOcclusion[x], middleMaxY);
                if (ceilMin < upperOcclusion[x]) {
                    offscreenBG2D.setColor(seg.color);

//                    if (floorVisplane.getPreviousX() >= 0 && (x - 1) != floorVisplane.getPreviousX()) {
//                        floorVisplane.finish();
//                        seg.frontSector.drawFloorTest(offscreenBData, player, floorVisplane, seg.frontSector);
//                        //floorVisplane.draw(offscreenBG2D, 1, 0, 0, seg.color);
//                        floorVisplane.reset();
//                    }

                    floorVisplane.addColumn(x, ceilMin, upperOcclusion[x]);
                    //offscreenBG2D.drawLine(x, ceilMin, x, upperOcclusion[x]);
                    upperOcclusion[x] = ceilMin;
                }
                
                if (hasLowerWall) {
                    int lowerMaxY = (int) currentLowerFloor;
                    int lowerMinY = (int) currentLowerCeil;
                    double lowerDy = lowerMaxY - lowerMinY;
                    double lowerTextureYStep = (backFloor - frontFloor) / lowerDy;
                    double lowerTextureY = frontSidedef.offsetY; // player.textureOffsetY;
                    if (lowerMinY < lowerOcclusion[x]) {
                        double dif = lowerOcclusion[x] - lowerMinY;
                        lowerTextureY = dif * lowerTextureYStep + frontSidedef.offsetY; //player.textureOffsetY;
                        lowerMinY = lowerOcclusion[x];
                    }
                    lowerMaxY = lowerMaxY > upperOcclusion[x] ? upperOcclusion[x] : lowerMaxY;

                    if (frontSidedef.lowerTexture != null) {
                        int textureWidth = frontSidedef.lowerTexture.image.getWidth();
                        //int textureHeight = frontSidedef.lowerTexture.image.getHeight();
                        int[] wallTextureData = frontSidedef.lowerTexture.imageData;
                        int pixelIndex = x + lowerMinY * 800;
                        int tx = (int) (currentTextureX * currentZ) & 0x7fffffff;
                        for (int y = lowerMinY; y < lowerMaxY; y++) {
                            int ty = (int) (lowerTextureY); // % textureHeight; //& 0b00111111;
                            offscreenBData[pixelIndex] = colorMap[wallTextureData[ty * textureWidth + tx]];
                            pixelIndex += 800;
                            lowerTextureY += lowerTextureYStep;
                        }
                    }
                    
                    if (lowerMinY < upperOcclusion[x]) {
                        upperOcclusion[x] = lowerMinY;
                    }

                    currentLowerCeil += lowerCeilStep;
                    currentLowerFloor += lowerfloorStep;
                }

                // ceiling
                int ceilMax = (int) Math.min(upperOcclusion[x], middleMinY);
                if (ceilMax > lowerOcclusion[x]) {
                    offscreenBG2D.setColor(seg.color);

//                    if (ceilVisplane.getPreviousX() >= 0 && (x - 1) != ceilVisplane.getPreviousX()) {
//                        ceilVisplane.finish();
//                        ceilVisplane.draw(offscreenBG2D, 1, 0, 0, seg.color);
//                        seg.frontSector.drawCeilingTest(offscreenBData, player, ceilVisplane, seg.frontSector);
//                        ceilVisplane.reset();
//                    }

                    ceilVisplane.addColumn(x, lowerOcclusion[x], ceilMax);
                    //offscreenBG2D.drawLine(x, lowerOcclusion[x], x, ceilMax);
                    lowerOcclusion[x] = middleMinY;
                }

                if (hasUpperWall) {
                    int upperMaxY = (int) currentUpperFloor;
                    int upperMinY = (int) currentUpperCeil;
                    double upperDy = upperMaxY - upperMinY;
                    double upperTextureYStep = (frontCeil - backCeil) / upperDy;
                    double upperTextureY = frontSidedef.offsetY; // player.textureOffsetY;
                    if (upperMinY < lowerOcclusion[x]) {
                        double dif = lowerOcclusion[x] - upperMinY;
                        upperTextureY = dif * upperTextureYStep + frontSidedef.offsetY; //player.textureOffsetY;
                        upperMinY = lowerOcclusion[x];
                    }
                    upperMaxY = upperMaxY > upperOcclusion[x] ? upperOcclusion[x] : upperMaxY;

                    if (frontSidedef.isUseSkyHack() || frontSidedef.upperTexture == null) {
                        if (upperMinY < upperMaxY) {
                            skyHackVisplane.addColumn(x, upperMinY, upperMaxY);
                        }
                    }
                    else {
                        int textureWidth = frontSidedef.upperTexture.image.getWidth();
                        //int textureHeight = frontSidedef.upperTexture.image.getHeight();
                        int[] wallTextureData = frontSidedef.upperTexture.imageData;
                        int pixelIndex = x + upperMinY * 800;
                        int tx = (int) (currentTextureX * currentZ) & 0x7fffffff;
                        for (int y = upperMinY; y < upperMaxY; y++) {
                            int ty = (int) (upperTextureY); // % textureHeight; //& 0b00111111;
                            offscreenBData[pixelIndex] = colorMap[wallTextureData[ty * textureWidth + tx]];
                            pixelIndex += 800;
                            upperTextureY += upperTextureYStep;
                        }
                    }
                    
                    if (upperMaxY > lowerOcclusion[x]) {
                        lowerOcclusion[x] = upperMaxY;
                    }

                    currentUpperCeil += upperCeilStep;
                    currentUpperFloor += upperFloorStep;
                }                    
                
                currentMiddleCeil += middleCeilStep;
                currentMiddleFloor += middlefloorStep;
                currentZInv += zInvStep;
                currentTextureX += textureXStep;            
            }
            
            if (skyHackVisplane.getPreviousX() >= 0) {
                skyHackVisplane.finish();
                seg.frontSector.drawCeilingTest(offscreenBData, player, skyHackVisplane, seg.frontSector);
            }
            
            if (floorVisplane.getPreviousX() >= 0) {
                floorVisplane.finish();
                //floorVisplane.draw(offscreenBG2D, 1, 0, 0, seg.color);
                seg.frontSector.drawFloorTest(offscreenBData, player, floorVisplane, seg.frontSector);
            }
            if (ceilVisplane.getPreviousX() >= 0) {
                ceilVisplane.finish();
                //ceilVisplane.draw(offscreenBG2D, 1, 0, 0, seg.color);
                seg.frontSector.drawCeilingTest(offscreenBData, player, ceilVisplane, seg.frontSector);
            }
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ViewCollisionTest mapViewer = new ViewCollisionTest();
            mapViewer.setPreferredSize(new Dimension(800, 600));

            JFrame frame = new JFrame();
            frame.setTitle("DOOM WAD Map Viewer / Blockmap Collision Test #5");
            frame.getContentPane().add(mapViewer);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            mapViewer.requestFocus();
            mapViewer.start();
        });
    }

}
