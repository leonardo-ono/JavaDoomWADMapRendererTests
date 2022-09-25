package doom.wad;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * https://doomwiki.org/wiki/Picture_format
 * Patch https://github.com/amroibrahim/DIYDoom/tree/master/DIYDOOM/Notes019/notes
 * 
  * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Picture {

    private BufferedImage image;
    private int[] imageData;
    
    //0x00-0x01	Unsigned short	Patch (image) Width
    //0x02-0x03	Unsigned short	Patch (image) Height
    //0x04-0x05	signed short	Left Offset (X Offset)
    //0x06-0x07	signed short	Top Offset (Y Offset)
    //0x08-0x0C*	4 * Width	Column/Post Data pointer offset
    private int width;
    private int height;
    private int leftOffset;
    private int topOffset;
    private int[] postPointers;
    
    public Picture(Directory picLump) {
        createImage(picLump);
    }

    private void createImage(Directory picLump) {
        picLump.data.position(0);
        
        width = picLump.data.getShort() & 0xffff;
        height = picLump.data.getShort() & 0xffff;
        leftOffset = picLump.data.getShort();
        topOffset = picLump.data.getShort();
        postPointers = new int[width];
        for (int i = 0; i < postPointers.length; i++) {
            postPointers[i] = picLump.data.getInt();
        }
        
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        imageData 
            = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        for (int i = 0; i < postPointers.length; i++) {
            int postPointer = postPointers[i];
            picLump.data.position(postPointer);
            boolean keepDrawingPost = true;
            while (keepDrawingPost) {
                int yOffset = picLump.data.get() & 0xff;
                if (yOffset == 0xff) {
                    keepDrawingPost = false;
                }
                else {
                    int length = picLump.data.get() & 0xff;
                    int padding = picLump.data.get() & 0xff;
                    for (int y = yOffset; y < yOffset + length; y++) {
                        int colorIndex = picLump.data.get() & 0xff;
                        //int color = Palette.colors[colorIndex];
                        image.setRGB(i, y, 0xff000000 + colorIndex);
                    }
                    int padding2 = picLump.data.get() & 0xff;
                }
            }
        }
    }
    
    public BufferedImage getImage() {
        return image;
    }

    public int[] getImageData() {
        return imageData;
    }
     
}
