package doom.infra;

import doom.wad.Linedef;
import doom.wad.Thing;
import doom.wad.WADLoader;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Player class.
 * 
 * References:
 * https://doomwiki.org/wiki/Player
 * https://www.doomworld.com/forum/topic/87199-the-doom-movement-bible/
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Player {

    public double x;
    public double y;
    public double vx;
    public double vy;
    public double vz;
    public double va;
    private final double linearAcceleration = 0.1;
    private final double angularAcceleration = 30.0;
    public double angle;
    
    public double heightFloor = 0;
    public double height = 0;
    public double heightWalk = 0;
    
    public double radius = 16;
    private final Vec2 direction;

    public Player(Thing playerThing) {
        this.x = playerThing.positionX;
        this.y = playerThing.positionY;
        this.angle = Math.toRadians(playerThing.angle);
        direction = new Vec2();
    }

    public void update(double delta) {
        boolean strafe = Input.isKeyPressed(KeyEvent.VK_CONTROL);
        if (!strafe && Input.isKeyPressed(KeyEvent.VK_LEFT)) {
            va += angularAcceleration * delta;
        }
        else if (!strafe && Input.isKeyPressed(KeyEvent.VK_RIGHT)) {
            va -= angularAcceleration * delta;
        }
        if (va > 30.0) va = 30.0;
        if (va < -30.0) va = -30.0;
        angle += va * delta;
        va = va * (1.0 - 8.0 * delta);
    }
    
    public void fixedUpdate() {

        double targetHeight2 = 48 + heightFloor;
        if (height < targetHeight2) {
            height += 0.1 * (targetHeight2 - height);
            vz = 0;
        }
        else {
            vz -= 0.1;
            if (vz < -5.0) {
                vz = -5.0;
            }
            height += vz;
        }
    
        heightWalk = height + 5 * Math.sin(0.05 * y) + 5 * Math.cos(0.05 * x) - 5;
        
        // ---
        
        boolean strafe = Input.isKeyPressed(KeyEvent.VK_CONTROL);
        
        if (strafe && Input.isKeyPressed(KeyEvent.VK_LEFT)) {
            movePlayer(linearAcceleration, Math.toRadians(90));
        }
        else if (strafe && Input.isKeyPressed(KeyEvent.VK_RIGHT)) {
            movePlayer(linearAcceleration, Math.toRadians(-90));
        }
        
        
        if (Input.isKeyPressed(KeyEvent.VK_UP)) {
            movePlayer(linearAcceleration, 0);
        }
        else if (Input.isKeyPressed(KeyEvent.VK_DOWN)) {
            movePlayer(-linearAcceleration, 0);
        }

        double dx = Math.cos(angle);
        double dy = Math.sin(angle);
        direction.set(dx, dy);
        
        updateMovement();
    }

    
    private final Point pointTmp = new Point();
    private final Point pointTmp2 = new Point();
            
    public void draw(Graphics2D g) {
        g.setColor(new Color((int) (Integer.MAX_VALUE * Math.random())));
        
        Scaler.get(x, y, pointTmp);
        g.fillOval(pointTmp.x - 2, pointTmp.y - 2, 4, 4);
        
        g.drawString("h:" + (int) heightFloor, pointTmp.x, pointTmp.y);
        
        int scaledRadius = (int) (radius * Scaler.SCALE);
        int diameter = (int) (2 * radius * Scaler.SCALE);
        g.drawOval(pointTmp.x - scaledRadius, pointTmp.y - scaledRadius, diameter, diameter);
        
        Scaler.get(10 * direction.x, 10 * direction.y, pointTmp2);
        g.drawLine(pointTmp.x, pointTmp.y, pointTmp.x + pointTmp2.x, pointTmp.y + pointTmp2.y);
    }

    public Vec2 getDirection() {
        return direction;
    }

    private final List<Linedef> collidingWalls = new ArrayList<>();

    private void movePlayer(double acceleration, double strafe) {
        double ax = acceleration * Math.cos(angle + strafe);
        double ay = acceleration * Math.sin(angle + strafe);
        vx += ax;
        vy += ay;
        if (vx > 4) vx = 4;
        if (vx < -4) vx = -4;
        if (vy > 4) vy = 4;
        if (vy < -4) vy = -4;
    }
    
    private final Vec2 collisionNormal = new Vec2();
    
    private void updateMovement() {
        x += vx;
        y += vy;
        vx *= 0.95;
        vy *= 0.95;

        collidingWalls.clear();
        retrieveCollidingWalls(collidingWalls);
        
        boolean result = false;
        boolean collides = true;
        
        collisionNormal.set(0, 0);
        
        //outer:
        //while (collides) {
            collides = false;
            for (Linedef linedef : collidingWalls) {
                
                // TODO E1M1 stars linedef.startVertex.updateNormal(this) return normal length = 0
                boolean check = false;
                boolean isPortal = linedef.frontSidedef != null && linedef.backSidedef != null;
                if (!isPortal) {
                    check = true;
                }
                else {
                    double ffh = linedef.frontSidedef.sector.floorHeight - heightFloor;
                    double bfh = linedef.backSidedef.sector.floorHeight - heightFloor;
                    if (ffh > 24 || bfh > 24) {
                        check = true;
                    }
                }
            
                double d = linedef.ptSegDist(x, y);
                if (d <= radius + 0.01 && check) { // 0.01 avoid unwanted collision
                    
                    double dx = linedef.getX2() - linedef.getX1();
                    double dy = linedef.getY2() - linedef.getY1();
                    double length = Math.sqrt(dx * dx + dy * dy);
                    dx /= length;
                    dy /= length;
                    
                    double px = x - linedef.getX1();
                    double py = y - linedef.getY1();
                    
                    double dot = dx * px + dy * py;
                    if (dot < 0) {
                        double n2x = x - linedef.getX1();
                        double n2y = y - linedef.getY1();
                        double nLength = Math.hypot(n2x, n2y);
                        n2x /= nLength;
                        n2y /= nLength;
                        double nx = n2x;
                        double ny = n2y;
                        collisionNormal.x += nx;
                        collisionNormal.y += ny;
                        //linedef.startVertex.updateNormal(this);
                        //double nx = linedef.startVertex.getNormal().x;
                        //double ny = linedef.startVertex.getNormal().y;
                        double ax = nx * (radius - nLength + 0.01);
                        double ay = ny * (radius - nLength + 0.01);
                        x += ax;
                        y += ay;
                        //collisionNormal.add(linedef.startVertex.getNormal());
                    }
                    else if (dot > length) {
                        double n2x = x - linedef.getX2();
                        double n2y = y - linedef.getY2();
                        double nLength = Math.hypot(n2x, n2y);
                        n2x /= nLength;
                        n2y /= nLength;
                        double nx = n2x;
                        double ny = n2y;
                        collisionNormal.x += nx;
                        collisionNormal.y += ny;
                        //linedef.endVertex.updateNormal(this);
                        //double nx = linedef.endVertex.getNormal().x;
                        //double ny = linedef.endVertex.getNormal().y;
                        double ax = nx * (radius - nLength + 0.01);
                        double ay = ny * (radius - nLength + 0.01);
                        x += ax;
                        y += ay;
                        //collisionNormal.add(linedef.endVertex.getNormal());
                    }
                    else {
                        double nx = -dy;
                        double ny = dx;

                        //double speed2 = nx * vx + ny * vy;

                        //double nvx = vx - speed2 * nx;
                        //double nvy = vy - speed2 * ny;

                        //vx -= nvx;
                        //vy -= nvy;

                        double ax = nx * (radius - d + 0.01);
                        double ay = ny * (radius - d + 0.01);
                        x -= ax;
                        y -= ay;
                        
                        collisionNormal.x = nx;
                        collisionNormal.y = ny;
                    }
                    
                    collides = true;
                    result = result || collides;
                }
                
            }
        //}
        
        if (result) {
            collisionNormal.normalize();
            double nx = collisionNormal.x;
            double ny = collisionNormal.y;
            
            double speed2 = nx * vx + ny * vy;

            double nvx = vx - speed2 * nx;
            double nvy = vy - speed2 * ny;

            vx = nvx;
            vy = nvy;
        }
    }

    private Linedef nonCollidingLinedef;

    private void retrieveCollidingWalls(List<Linedef> collidingWalls) {
        double[] offsets = { -radius, -radius
                           , -radius,  radius
                           ,  radius,  radius
                           ,  radius, -radius };
        
        double greaterHeightTmp = -Double.MAX_VALUE;
        
        for (int o = 0; o < offsets.length; o += 2) {
            int bx = (int) (x + offsets[o + 0]);
            int by = (int) (y + offsets[o + 1]);
            Linedef[] blocks = WADLoader.blockmap.getBlock(bx, by);
            for (Linedef linedef : blocks) {
                boolean collides = linedef.ptSegDist(x, y) <= radius;
                if (!collidingWalls.contains(linedef) && collides) {
                    
                    boolean isPortal = linedef.frontSidedef != null && linedef.backSidedef != null;
                    if (!isPortal) {
                        collidingWalls.add(linedef);
                    }
                    else {
                        double ffh = linedef.frontSidedef.sector.floorHeight - heightFloor;
                        double bfh = linedef.backSidedef.sector.floorHeight - heightFloor;
                        //int heightDif = Math.abs(ffh - bfh);
                        //if (heightDif > 24) {
                        if (ffh > 24 || bfh > 24) {
                            collidingWalls.add(linedef);
                        }
                        else {
                            if (linedef.frontSidedef != null && linedef.frontSidedef.sector.floorHeight > greaterHeightTmp) {
                                greaterHeightTmp = linedef.frontSidedef.sector.floorHeight;
                                nonCollidingLinedef = linedef;
                            }
                            if (linedef.backSidedef != null && linedef.backSidedef.sector.floorHeight > greaterHeightTmp) {
                                greaterHeightTmp = linedef.backSidedef.sector.floorHeight;
                                nonCollidingLinedef = linedef;
                            }                            
                        }
                    }
                }
            }        
        }

        if (greaterHeightTmp != -Double.MAX_VALUE) {
            heightFloor = greaterHeightTmp;
        }
        else if (nonCollidingLinedef != null) {
//            if (nonCollidingLinedef.relativeCCW(x, y) > 0) {
            double lx = nonCollidingLinedef.x2 - nonCollidingLinedef.x1;
            double ly = nonCollidingLinedef.y2 - nonCollidingLinedef.y1;
            double px = nonCollidingLinedef.x2 - x;
            double py = nonCollidingLinedef.y2 - y;
            if (py * lx > px * ly) {
                heightFloor = nonCollidingLinedef.frontSidedef.sector.floorHeight;
            }
            else {
                heightFloor = nonCollidingLinedef.backSidedef.sector.floorHeight;
            }
            nonCollidingLinedef = null;
        }
        
    }

}
