package doom.wad;

import java.awt.Color;
import java.awt.Point;

/**
 * Seg class.
 * 
 * References:
 * https://github.com/amroibrahim/DIYDoom/tree/master/DIYDOOM/Notes012/notes
 * https://doomwiki.org/wiki/Seg
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Seg {

    public final Point startVertex;
    public final Point endVertex;
    public final int angle;
    public final Linedef linedef;
    public final int offset;
    public final Color color 
                = new Color(0xff000000 + (int) (0xaaaaaa * Math.random()));
    
    public final double length;
    
    public final Sidedef frontSidedef;
    public final Sidedef backSidedef;
    public final boolean isPortal;
    
    public final Sector frontSector;
    public final Sector backSector;

    public Seg(Point startingVertex, Point endVertex, int angle
                , Linedef linedef, Sidedef frontSidedef, Sidedef backSidedef
                    , boolean isPortal, int offset, double length
                        , Sector frontSector, Sector backSector) {
        
        this.startVertex = startingVertex;
        this.endVertex = endVertex;
        this.angle = angle;
        this.linedef = linedef;
        this.offset = offset;
        this.length = length;
        this.frontSidedef = frontSidedef;
        this.backSidedef = backSidedef;
        this.isPortal = isPortal;
        this.frontSector = frontSector;
        this.backSector = backSector;        
    }

}
