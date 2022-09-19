package doom.infra;

import doom.wad.Thing;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;

/**
 * Player class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Player {

    public double x;
    public double y;
    public double angle;
    public double height = 32;
    private Vec2 direction;

    public double textureOffsetX;
    public double textureOffsetY;

    public Player(Thing playerThing) {
        this.x = playerThing.positionX;
        this.y = playerThing.positionY;
        this.angle = Math.toRadians(playerThing.angle);
        direction = new Vec2();
    }

    public void update(double delta) {
        final double angSpeed = 2.5;
        boolean strafe = Input.isKeyPressed(KeyEvent.VK_CONTROL);
        if (!strafe && Input.isKeyPressed(KeyEvent.VK_LEFT)) {
            angle += angSpeed * delta;
        }
        else if (!strafe && Input.isKeyPressed(KeyEvent.VK_RIGHT)) {
            angle -= angSpeed * delta;
        }
    }
    
    public void fixedUpdate() {
        final double speed = 2.5;

        boolean strafe = Input.isKeyPressed(KeyEvent.VK_CONTROL);
        
        if (strafe && Input.isKeyPressed(KeyEvent.VK_LEFT)) {
            movePlayer(2 * speed, Math.toRadians(90));
        }
        else if (strafe && Input.isKeyPressed(KeyEvent.VK_RIGHT)) {
            movePlayer(2 * speed, Math.toRadians(-90));
        }
        
        
        if (Input.isKeyPressed(KeyEvent.VK_UP)) {
            movePlayer(speed, 0);
        }
        else if (Input.isKeyPressed(KeyEvent.VK_DOWN)) {
            movePlayer(-speed, 0);
        }

        double dx = Math.cos(angle);
        double dy = Math.sin(angle);
        direction.set(dx, dy);
        

        // texture offset
        if (Input.isKeyPressed(KeyEvent.VK_A)) {
            textureOffsetX--;
        }
        else if (Input.isKeyPressed(KeyEvent.VK_D)) {
            textureOffsetX++;
        }
        
        if (Input.isKeyPressed(KeyEvent.VK_W)) {
            textureOffsetY--;
        }
        else if (Input.isKeyPressed(KeyEvent.VK_S)) {
            textureOffsetY++;
        }        
        
        textureOffsetX += 0.5;
        textureOffsetY += 0.5;
    }
    

    private void movePlayer(double speed, double strafe) {
        double vx = speed * Math.cos(angle + strafe);
        double vy = speed * Math.sin(angle + strafe);
        x += vx;
        y += vy;
    }
    
    private final Point pointTmp = new Point();
    private final Point pointTmp2 = new Point();
            
    public void draw(Graphics2D g) {
        g.setColor(new Color((int) (Integer.MAX_VALUE * Math.random())));
        
        Scaler.get(x, y, pointTmp);
        g.fillOval(pointTmp.x - 2, pointTmp.y - 2, 4, 4);
        
        Scaler.get(10 * direction.x, 10 * direction.y, pointTmp2);
        g.drawLine(pointTmp.x, pointTmp.y, pointTmp.x + pointTmp2.x, pointTmp.y + pointTmp2.y);
    }

    public Vec2 getDirection() {
        return direction;
    }

}
