package doom.wad;

/**
 * Thing class.
 * 
 * References:
 * https://github.com/amroibrahim/DIYDoom/tree/master/DIYDOOM/Notes005/notes
 * https://doomwiki.org/wiki/Thing
 * 
 * https://doomwiki.org/wiki/Thing_types
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Thing {

    public final int positionX;
    public final int positionY;
    public final int angle;
    public final int type;
    public final int flags;

    public Thing(int positionX, int positionY, int angle, int type, int flags) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.angle = angle;
        this.type = type;
        this.flags = flags;
    }
        
}
