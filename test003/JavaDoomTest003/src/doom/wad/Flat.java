package doom.wad;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * https://doomwiki.org/wiki/Flat
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Flat {

    private BufferedImage image;
    private int[] imageData;

    private final int width = 64;
    private final int height = 64;
    
    public Flat(Directory flatLump) {
        createImage(flatLump);
    }

    private void createImage(Directory lump) {
        lump.data.position(0);
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int colorIndex = lump.data.get() & 0xff;
                int color = Palette.colors[colorIndex];
                image.setRGB(x, y, color);
            }
        }
        imageData 
            = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    }
    
    public BufferedImage getImage() {
        return image;
    }

    public int[] getImageData() {
        return imageData;
    }
    
}
