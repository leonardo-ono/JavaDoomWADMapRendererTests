package doom.infra;

import doom.wad.Directory;
import doom.wad.Picture;
import doom.wad.Thing;
import doom.wad.WADLoader;
import java.awt.Graphics2D;

/**
 * Enemy class.
 * 
 * First tests.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Enemy {
    
    public double x;
    public double y;
    public double angle;
    public double heightFloor = 0;
    public double radius = 16;
    private final Vec2 direction;

    private final Vec2 v0 = new Vec2();
    private final Vec2 v1 = new Vec2();
    private final Vec2 v2 = new Vec2();
    private final Vec2 v3 = new Vec2();

    private final Picture[] pics = new Picture[8];
    
    public Enemy(Thing enemyThing) {
        this.x = enemyThing.positionX;
        this.y = enemyThing.positionY;
        this.angle = Math.toRadians(enemyThing.angle);
        direction = new Vec2();
        
        Directory picLump1 = WADLoader.getLumpByName("SPOSA1");
        Directory picLump2 = WADLoader.getLumpByName("SPOSA2A8");
        Directory picLump3 = WADLoader.getLumpByName("SPOSA3A7");
        Directory picLump4 = WADLoader.getLumpByName("SPOSA4A6");
        Directory picLump5 = WADLoader.getLumpByName("SPOSA5");
        Directory picLump6 = WADLoader.getLumpByName("SPOSA4A6");
        Directory picLump7 = WADLoader.getLumpByName("SPOSA3A7");
        Directory picLump8 = WADLoader.getLumpByName("SPOSA2A8");
        pics[0] = new Picture(picLump1);
        pics[1] = new Picture(picLump2);
        pics[2] = new Picture(picLump3);
        pics[3] = new Picture(picLump4);
        pics[4] = new Picture(picLump5);
        pics[5] = new Picture(picLump6);
        pics[6] = new Picture(picLump7);
        pics[7] = new Picture(picLump8);
        pics[5].flip();
        pics[6].flip();
        pics[7].flip();
        for (int i = 0; i < 8; i++) {
            pics[i].fixRGB();
        }
    }
    
    public void update() {
        heightFloor = WADLoader.getMapFloorHeight(x, y);
    }
    
    public void draw(Graphics2D g, Player player) {
        v0.set(x - player.x, y - player.y);

        double cyTop = heightFloor - player.heightWalk - 56; 
        double cyBottom = heightFloor - player.heightWalk; 
        
        // convert to camera space
        v2.set(player.getDirection());
        double za = v2.dot(v0);
        v2.set(-v2.y, v2.x);
        double xa = v2.dot(v0);

        double xLeft = xa - 20;
        double xRight = xa + 20;
        
        // behind player
        if (za <= 0.1) {
            return;
        }

        double plane = 400;
        int scrXA = (int) (plane * xa / -za) + 400;
        int scrXLeft = (int) (plane * xLeft / -za) + 400;
        int scrXRight = (int) (plane * xRight / -za) + 400;
        int scrYATop = (int) (plane * cyTop / -za) + 300;
        int scrYABottom = (int) (plane * cyBottom / -za) + 300;
        
        int rw = scrXLeft - scrXRight;
        int rh = scrYATop - scrYABottom;
        int rx = scrXRight;
        int ry = scrYABottom - rh;
        
        int picIndex = calculate360ViewSpriteIdOffset(player);
        g.drawImage(pics[picIndex].getImage(), rx, ry, rw, rh, null);
        
        //g.setColor(Color.RED);
        //g.drawRect(rx, ry, rw, rh);
        
    }

    private static final double PI_DIV_BY_4 = Math.PI / 4;
    private static final double PI_DIV_BY_8 = Math.PI / 8;

    public int calculate360ViewSpriteIdOffset(Player player) {
        double dx = x - player.x;
        double dy = y - player.y;
        double playerEnemyAngle = Math.atan2(dy, dx);
        double enemyAngle = angle + Math.PI;
        double dif = (playerEnemyAngle - enemyAngle
                            + PI_DIV_BY_8) % (2 * Math.PI);

        if (dif < 0) {
            dif = 2 * Math.PI + dif;
        }
        int billboardIndex = (int) (dif / PI_DIV_BY_4);
        return billboardIndex;        
    }
        
        
}
