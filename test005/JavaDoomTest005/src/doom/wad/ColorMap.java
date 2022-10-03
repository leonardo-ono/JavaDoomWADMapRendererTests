package doom.wad;

import doom.infra.Util;

/**
 * ColorMap (Lump) class.
 * 
 * Reference:
 * https://doomwiki.org/wiki/COLORMAP
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class ColorMap {

    public static int[][] colorsMap;
    
    public static final double DISTANCE_FACTOR = 0.05;
    
    public static void extractColorMap(Directory colorMapLump) {
        colorMapLump.data.position(0);
        
        colorsMap = new int[34][256];
        for (int light = 0; light < 34; light++) {
            for (int colorIndex = 0; colorIndex < 256; colorIndex++) {
                int c = colorMapLump.data.get() & 0xff;
                colorsMap[light][colorIndex] = Palette.colors[c];
            }
        }
    }
    
    private static final int[] OSCILLATION = new int[256];
    
    static {
        for (int i = 0; i < 256; i++) {
            OSCILLATION[i] = (int) (13 + 13 * Math.sin(2 * Math.PI * (i / 255.0)));
        }
    }
    
    private static final int START_Z = 5;
    
    public static int[] getWallColorMap(double currentZ, Seg seg) {
        Sector sector = seg.frontSector;
        int colorMapIndex = (int) ((currentZ - START_Z) * DISTANCE_FACTOR);
        int shadow = ((((seg.angle + 8192) & 0x7fff) - 16384) & 0x7fff) / 3200; //>> 12;
        if (colorMapIndex > 32 - (seg.frontSector.lightLevel >> 3)) colorMapIndex = 32 - (seg.frontSector.lightLevel >> 3);
        colorMapIndex += shadow;
        switch (sector.specialType) {
            case 1, 17 -> { // random blink
                if (sector.random[(Util.getFrameCount() & 0xff0) >> 4]) {
                    colorMapIndex += 10;
                }
            }
            case 2, 12 -> { // blink 0.5 sec
                if ((Util.getFrameCount() % 120) < 60) {
                    colorMapIndex += 10;
                }
            }
            case 3, 13 -> { // blink 1.0 sec
                if ((Util.getFrameCount() % 240) < 120) {
                    colorMapIndex += 10;
                }
            }
            case 8 -> { // oscillates
                colorMapIndex += OSCILLATION[Util.getFrameCount() & 0xff];
            }
        }
        if (colorMapIndex > 31) colorMapIndex = 31;
        if (colorMapIndex < 0) colorMapIndex = 0;
        return colorsMap[colorMapIndex];
    }

    public static int[] getFloorCeilingColorMap(double currentZ, Sector sector) {
        int colorMapIndex = (int) ((currentZ - START_Z) * DISTANCE_FACTOR);
        if (colorMapIndex > 32 - (sector.lightLevel >> 3)) colorMapIndex = 32 - (sector.lightLevel >> 3);
        switch (sector.specialType) {
            case 1, 17 -> { // random blink
                if (sector.random[(Util.getFrameCount() & 0xff0) >> 4]) {
                    colorMapIndex += 10;
                }
            }
            case 2, 12 -> { // blink 0.5 sec
                if ((Util.getFrameCount() % 120) < 60) {
                    colorMapIndex += 10;
                }
            }
            case 3, 13 -> { // blink 1.0 sec
                if ((Util.getFrameCount() % 240) < 120) {
                    colorMapIndex += 10;
                }
            }
            case 8 -> { // oscillates
                colorMapIndex += OSCILLATION[Util.getFrameCount() & 0xff];
            }
        }
        if (colorMapIndex > 31) colorMapIndex = 31;
        if (colorMapIndex < 0) colorMapIndex = 0;
        
        //if (sector.specialType != 0 && sector.specialType != 1) {
        //    System.out.println("");
        //}
        
        return ColorMap.colorsMap[colorMapIndex];        
    }
        
}
