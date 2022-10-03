package doom.infra;

import java.awt.Point;

/**
 * Player class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Scaler {
    
    public static final double SCALE = 1.0;
    
    public static void get(double srcX, double srcY, Point dst) {
        dst.x = (int) (srcX * SCALE);
        dst.y = (int) (srcY * -SCALE);
    }

    public static void get(Point src, Point dst) {
        get(src.x, src.y, dst);
    }

    public static void get(Point.Double src, Point dst) {
        get(src.x, src.y, dst);
    }
    
}
