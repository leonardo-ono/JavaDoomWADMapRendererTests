package doom.wad;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * https://doomwiki.org/wiki/Doom_rendering_engine
 * https://github.com/amroibrahim/DIYDoom
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Textures {
    
    // path layout
    //    0x00-0x01	2	short int	Horizontal offset of the patch relative to the upper-left of the texture
    //    0x02-0x03	2	short int	Vertical offset of the patch relative to the upper-left of the texture
    //    0x04-0x05	2	short int	Patch index as listed in patch names (PNAMES, see below)
    //    0x06-0x07	2	short int	Ignored
    //    0x08-0x09	2	short int	Ignored
    public static class PatchLayout {
        
        public final int horizontalOffset;
        public final int verticalOffset;
        public final int patchIndex;
        
        public PatchLayout(ByteBuffer data) {
            horizontalOffset = data.getShort();
            verticalOffset = data.getShort();
            patchIndex = data.getShort();
            int ignore1 = data.getShort();
            int ignore2 = data.getShort();
        }
        
    }


    // extract textures
    //    0x00-0x07	8	ASCII	Name of texture
    //    0x08-0x0B	4	unsigned int	Ignored
    //    0x0C-0x0D	2	unsigned short	Texture width
    //    0x0E-0x0F	2	unsigned short	Texture height
    //    0x10-0x13	4	unsigned int	Ignored
    //    0x14-0x15	2	unsigned short	Number of patches used by the textures
    //    0x16-0x17	10*	Patch layout	A structure that formats how the patch is drawn on the texture
    public static class Texture {
        
        public BufferedImage image;
        public int[] imageData;
        
        public final String name;
        public final int width;
        public final int height;
        public final int numberOfPatches;
        public final PatchLayout[] patchLayouts; 

        public Texture(ByteBuffer data, int offset) {
            data.position(offset);

            byte[] nameBytes = new byte[8];
            data.get(nameBytes);
            String nameTmp = new String(nameBytes);
            int nullCharIndex = nameTmp.indexOf('\0');
            if (nullCharIndex >= 0) {
                nameTmp = nameTmp.substring(0, nullCharIndex);
            }
            name = nameTmp;
            int ignore1 = data.getInt();
            width = data.getShort() & 0xffff;
            height = data.getShort() & 0xffff;
            int ignore2 = data.getInt();
            numberOfPatches = data.getShort() & 0xffff;
            
            patchLayouts = new PatchLayout[numberOfPatches];
            for (int i = 0; i < numberOfPatches; i++) {
                patchLayouts[i] = new PatchLayout(data);
            }
            
            image = generateTextureImage();
            DataBuffer dbi = image.getRaster().getDataBuffer();
            imageData = ((DataBufferInt) dbi).getData();            
        }
        
        private BufferedImage generateTextureImage() {
            BufferedImage textureImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D imageG2D = textureImage.createGraphics();
            
            for (PatchLayout layout : patchLayouts) {
                int horizOffset = layout.horizontalOffset;
                int vertOffset = layout.verticalOffset;
                String patchName = PNames.names[layout.patchIndex];
                
                Directory lump = WADLoader.getLumpByName(patchName.toUpperCase());
                if (lump == null) {
                    System.out.println("");
                }
                Picture patch = new Picture(lump);
                imageG2D.drawImage(patch.getImage(), horizOffset, vertOffset, null);
            }
            
            return textureImage;
        }

        public void resize(int width, int height) {
            if (width < image.getWidth()) { 
                width = image.getWidth();
            }
            if (height < image.getHeight()) {
                height = image.getHeight();
            }
            BufferedImage imageTmp = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = imageTmp.createGraphics();
            for (int currentY = 0; currentY <= height; currentY += image.getHeight()) {
                for (int currentX = 0; currentX <= width; currentX += image.getWidth()) {
                    g.drawImage(image, currentX, currentY, null);
                }
            }
            image = imageTmp;
            DataBuffer dbi = image.getRaster().getDataBuffer();
            imageData = ((DataBufferInt) dbi).getData();            
        }
        
        public void removeOpacity() {
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int pixelIndex = x + y * image.getWidth();
                    int c = imageData[pixelIndex] & 0xff;
                    imageData[pixelIndex] = c;
                }
            }
        }
        
    }
    
    private static int numberOfTextures;
    private static int[] textureOffsets;
    
    public static Map<String, Texture> textures = new HashMap<>();
    
    public static void load(Directory texturesData) {
        texturesData.data.position(0);
        
        numberOfTextures = texturesData.data.getInt();
        textureOffsets = new int[numberOfTextures];
        
        for (int i = 0; i < numberOfTextures; i++) {
            textureOffsets[i] = texturesData.data.getInt();
        }
        
        for (int i = 0; i < numberOfTextures; i++) {
            int textureOffset = textureOffsets[i];
            Texture texture = new Texture(texturesData.data, textureOffset);
            textures.put(texture.name, texture);
        }
    }
    
}
