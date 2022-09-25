package doom.wad;

import doom.infra.Util;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * https://doomwiki.org/wiki/Flat
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class AnimatedFlat extends Flat {
    
    private BufferedImage[] images;
    private int[][] imageDatas;
    private int imageIndex;
    public AnimatedFlat(Directory ... flatLumps) {
        images = new BufferedImage[flatLumps.length];
        imageDatas = new int[flatLumps.length][];
        int imageIndexTmp = 0;
        for (Directory flatLump : flatLumps) {
            images[imageIndexTmp] = createImage(flatLump);
            imageDatas[imageIndexTmp] = ((DataBufferInt) images[imageIndexTmp].getRaster().getDataBuffer()).getData();
            imageIndexTmp++;
        }
        //createImage(flatLump);
    }

    @Override
    public BufferedImage getImage() {
        return images[Util.getFloorFrame()];
    }

    @Override
    public int[] getImageData() {
        return imageDatas[Util.getFloorFrame()];
    }
    
}
