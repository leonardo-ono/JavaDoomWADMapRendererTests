package doom.wad;

import java.nio.ByteBuffer;

/**
 * https://doom.fandom.com/wiki/PNAMES
 * https://github.com/amroibrahim/DIYDoom/tree/master/DIYDOOM/Notes020/notes
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class PNames {

    public static String[] names;
    
    public static void load(ByteBuffer data) {
        data.position(0);
        
        long namesSize = data.getInt() & 0xffffffff;
        
        names = new String[(int) namesSize];
        
        for (long i = 0; i < namesSize; i++) {
            byte[] nameBytes = new byte[8];
            data.get(nameBytes);
            names[(int) i] = WADLoader.convertBytesArrayToString(nameBytes);
        }
    }
    
}
