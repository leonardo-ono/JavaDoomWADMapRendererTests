package doom.wad;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * https://doomwiki.org/wiki/Flat
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Flat {

    protected BufferedImage image;
    protected int[] imageData; 

    protected final int width = 64;
    protected final int height = 64;

    protected Flat() {
    }
    
    public Flat(Directory flatLump) {
        image = createImage(flatLump);
        imageData 
            = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    }

    protected BufferedImage createImage(Directory lump) {
        lump.data.position(0);
        BufferedImage imageTmp = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int colorIndex = lump.data.get() & 0xff;
                //int color = Palette.colors[colorIndex];
                imageTmp.setRGB(x, y, colorIndex);
            }
        }
        return imageTmp;
    }
    
    public BufferedImage getImage() {
        return image;
    }

    public int[] getImageData() {
        return imageData;
    }
    
}
