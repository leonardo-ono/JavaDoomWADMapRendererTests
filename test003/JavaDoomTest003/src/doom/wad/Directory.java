package doom.wad;

import java.nio.ByteBuffer;

/**
 * WAD Directory (Lump) class.
 * 
 * References:
 * https://github.com/amroibrahim/DIYDoom/tree/master/DIYDOOM/Notes001/notes
 * https://www.gamers.org/dhs/helpdocs/dmsp1666.html
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Directory {

    public final long offset; // from the start of file
    public final long length; // size of lump in bytes
    public final String name; // name of the lump (8 bytes)
    public final ByteBuffer data; // data

    public Directory(long offset, long length, String name, ByteBuffer data) {
        this.offset = offset;
        this.length = length;
        this.name = name;
        this.data = data;
    }    

    @Override
    public String toString() {
        return "Directory{" + "offset=" + offset + ", length=" + length 
                                + ", name=" + name + ", data=" + data + '}';
    }
        
}
