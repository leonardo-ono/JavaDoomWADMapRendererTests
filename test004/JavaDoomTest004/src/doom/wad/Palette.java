package doom.wad;

/**
 * https://github.com/amroibrahim/DIYDoom/tree/master/DIYDOOM/Notes018/notes
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Palette {

    public static int[] colors;
    
    public static void extractPalette(Directory paletteLump) {
        paletteLump.data.position(0);
        
        colors = new int[256];
        for (int i = 0; i < 256; i++) {
            int r = paletteLump.data.get() & 0xff;
            int g = paletteLump.data.get() & 0xff;
            int b = paletteLump.data.get() & 0xff;
            int a = 255;
            colors[i] = b + (g << 8) + (r << 16) + (a << 24); 
        }
    }
    
}
