package doom.wad;

import java.awt.Point;
import java.awt.geom.Line2D;

/**
 * Linedef class.
 * 
 * References:
 * https://github.com/amroibrahim/DIYDoom/tree/master/DIYDOOM/Notes002/notes
 * https://doomwiki.org/wiki/Linedef
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Linedef extends Line2D.Double {

    public final Point startVertex;
    public final Point endVertex;
    public final int flags;
    public final int specialType;
    public final int sectorTag;
    public final Sidedef frontSidedef;        
    public final Sidedef backSidedef;        
    public final double length;
    
    public Linedef(Point startVertex, Point endVertex, int flags
                    , int specialType, int sectorTag, Sidedef frontSidedef
                        , Sidedef backSidedef, double length) {
        
        super(startVertex, endVertex);
        
        this.startVertex = startVertex;
        this.endVertex = endVertex;
        this.flags = flags;
        this.specialType = specialType;
        this.sectorTag = sectorTag;
        this.frontSidedef = frontSidedef;
        this.backSidedef = backSidedef;
        this.length = length;
    }
    
}
