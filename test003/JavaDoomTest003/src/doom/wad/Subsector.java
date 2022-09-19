package doom.wad;

import java.util.ArrayList;
import java.util.List;

/**
 * Subsector class.
 * 
 * References:
 * https://github.com/amroibrahim/DIYDoom/tree/master/DIYDOOM/Notes009/notes
 * https://doomwiki.org/wiki/Subsector
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Subsector {

    public final int segCount;
    public final int firstSegNumber;
    
    public final List<Seg> segs = new ArrayList<>();

    public Subsector(int segCount, int firstSegNumber) {
        this.segCount = segCount;
        this.firstSegNumber = firstSegNumber;
    }
        
}
