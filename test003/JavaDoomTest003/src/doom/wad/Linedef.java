package doom.wad;

import java.awt.Point;

/**
 * Linedef class.
 * 
 * References:
 * https://github.com/amroibrahim/DIYDoom/tree/master/DIYDOOM/Notes002/notes
 * https://doomwiki.org/wiki/Linedef
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Linedef {

    public final Point startVertex;
    public final Point endVertex;
    public final int flags;
    public final int specialType;
    public final int sectorTag;
    public final Sidedef frontSidedef;        
    public final Sidedef backSidedef;        

    public Linedef(Point startVertex, Point endVertx, int flags, int specialType
                , int sectorTag, Sidedef frontSidedef, Sidedef backSidedef) {
        
        this.startVertex = startVertex;
        this.endVertex = endVertx;
        this.flags = flags;
        this.specialType = specialType;
        this.sectorTag = sectorTag;
        this.frontSidedef = frontSidedef;
        this.backSidedef = backSidedef;
    }
        
}
