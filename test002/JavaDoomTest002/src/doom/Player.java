package doom;

import static doom.MapViewer.MAP_SCALE;
import doom.Test.Thing;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/**
 *
 * @author Leo
 */
public class Player {

    public double x;
    public double y;
    public double angle;
    public double height = 32;
    private Vec2 directionVec2;

    public Player(Thing playerThing) {
        x = (short) playerThing.xPosition;
        y = (short) playerThing.yPosition;
        angle = Math.toRadians(playerThing.angle);
        directionVec2 = new Vec2();
    }
    
    public void update() {
        final double angSpeed = 0.05;
        final double speed = 3.0;

        boolean strafe = Input.isKeyPressed(KeyEvent.VK_CONTROL);
        
        if (strafe && Input.isKeyPressed(KeyEvent.VK_LEFT)) {
            movePlayer(2 * speed, Math.toRadians(90));
        }
        else if (strafe && Input.isKeyPressed(KeyEvent.VK_RIGHT)) {
            movePlayer(2 * speed, Math.toRadians(-90));
        }
        
        if (!strafe && Input.isKeyPressed(KeyEvent.VK_LEFT)) {
            angle += angSpeed;
        }
        else if (!strafe && Input.isKeyPressed(KeyEvent.VK_RIGHT)) {
            angle -= angSpeed;
        }
        
        if (Input.isKeyPressed(KeyEvent.VK_UP)) {
            movePlayer(speed, 0);
        }
        else if (Input.isKeyPressed(KeyEvent.VK_DOWN)) {
            movePlayer(-speed, 0);
        }
    }
    

    private void movePlayer(double speed, double strafe) {
        double vx = speed * Math.cos(angle + strafe);
        double vy = speed * Math.sin(angle + strafe);
        x += vx;
        y += vy;
    }
    
    public void draw(Graphics2D g) {
        g.setColor(new Color((int) (Integer.MAX_VALUE * Math.random())));
        
        int px = (int) (MAP_SCALE * x);
        int py = (int) (MAP_SCALE * y);
        g.fillOval(px - 2, py - 2, 4, 4);
        
        int dx = (int) (10 * Math.cos(angle));
        int dy = (int) (10 * Math.sin(angle));
        g.drawLine(px, py, px + dx, py + dy);
    }

    public Vec2 getDirection() {
        double dx = Math.cos(angle);
        double dy = Math.sin(angle);
        directionVec2.set(dx, dy);
        return directionVec2;
    }
    
}
