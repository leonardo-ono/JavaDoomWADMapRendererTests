package doom.wad;

import doom.infra.Player;
import doom.infra.Vec2;
import java.awt.Color;

/**
 * Sector class.
 * 
 * References:
 * https://github.com/amroibrahim/DIYDoom/tree/master/DIYDOOM/Notes009/notes
 * https://doomwiki.org/wiki/Sector
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Sector {

    public final int floorHeight; // int16_t
    public final int ceilingHeight; // int16_t
    public final String floorTextureName; // int8_t[8]
    public final String ceilingTextureName; // int8_t[8]
    public final int lightLevel; // int16_t
    public final int specialType; // int16_t
    public final int tagNumber; // int16_t
    public final Color color;
    public final Flat floorFlat;
    public final Flat ceilingFlat;
    public final Picture ceilingPic;
    
    public final boolean[] random = new boolean[256];
    
    public Sector(int floorHeight, int ceilingHeight
                    , String floorTextureName, String ceilingTextureName
                        , int lightLevel, int specialType, int tagNumber
                            , Flat floorFlat, Flat ceilingFlat
                                , Picture ceilingPic) {
        
        this.floorHeight = floorHeight;
        this.ceilingHeight = ceilingHeight;
        this.floorTextureName = floorTextureName;
        this.ceilingTextureName = ceilingTextureName;
        this.lightLevel = lightLevel;
        this.specialType = specialType;
        this.tagNumber = tagNumber;
        this.floorFlat = floorFlat;
        this.ceilingFlat = ceilingFlat;
        this.ceilingPic = ceilingPic;
        int c = (int) (0x55 * Math.random());
        color = new Color(c, c, c);
        
        // random blink
        if (specialType == 1) {
            for (int i = 0; i < 256; i++) {
                random[i] = Math.random() < 0.5;
            }
        }
    }

    private static final double TAN_45_DEG = Math.tan(Math.toRadians(45));
    private static final double[] FLOOR_Y_INV = new double[600];
    private static final double[] CEIL_Y_INV = new double[600];
    private static final double CANVAS_HEIGHT_INV = 1.0 / 800.0;

    static {
        for (int y = 301; y < 600; y++) {
            FLOOR_Y_INV[y] = 1.0 / (y - 300);
        }
        for (int y = 299; y >= 0; y--) {
            CEIL_Y_INV[y] = 1.0 / (300 - y);
        }
    }

    public void drawFloorTest(int[] offscreen3DData, Player player, Visplane visplane, Sector sector) {
        double planeDistance = 400;
        int[] floorTextureData = floorFlat.getImageData();
        Vec2 playerDir = player.getDirection();
        int minY = visplane.minY < 301 ? 301 : visplane.minY;
        int maxY = visplane.maxY > 599 ? 599 : visplane.maxY;
        for (int y = minY; y < maxY; y++) {

            //if (y < 301 || y >= 600) continue;

            double z = planeDistance * (-floorHeight + (int) player.height) * FLOOR_Y_INV[y];
            int[] colorMap = ColorMap.getFloorCeilingColorMap(z, sector);
            double px = playerDir.x * z + player.x;
            double py = playerDir.y * z + player.y;

            double lateralLength = TAN_45_DEG * z;

            double leftX = -playerDir.y * lateralLength + px;
            double leftY = playerDir.x * lateralLength + py;
            double rightX = playerDir.y * lateralLength + px;
            double rightY = -playerDir.x * lateralLength + py;

            double dx = (rightX - leftX) * CANVAS_HEIGHT_INV;
            double dy = (rightY - leftY) * CANVAS_HEIGHT_INV;
            double worldX = leftX + dx * visplane.left[y];
            double worldY = leftY + dy * visplane.left[y];
            int pixelIndex = y * 800 + visplane.left[y];
            for (int screenX = visplane.left[y]; screenX <= visplane.right[y]; screenX++) {
                int textureX = (int) worldX & 0b000111111;
                int textureY = (int) worldY & 0b000111111;
                int c = floorTextureData[textureY * 64 + textureX];
                offscreen3DData[pixelIndex++] = colorMap[c];
                worldX += dx;
                worldY += dy;
            }
        }
    }

    public void drawCeilingTest(int[] offscreen3DData, Player player, Visplane visplane, Sector sector) {
        int[] palette = Palette.colors;
        if (ceilingPic != null) {
            int[] ceilingTextureData = ceilingPic.getImageData();
            int ceilingTextureWidth = ceilingPic.getImage().getWidth();
            int ceilingTextureHeight = ceilingPic.getImage().getHeight();
            double normPlayerAngle = player.angle % (2 * Math.PI);
            if (normPlayerAngle < 0) {
                normPlayerAngle += 2 * Math.PI;
            }
            int textureOffsetX = (int) (ceilingTextureWidth * (normPlayerAngle / (Math.PI * 0.5)));
            double dx = ceilingTextureWidth / 800.0;
            double dy = ceilingTextureHeight / 400.0;
            for (int y = visplane.maxY; y >= visplane.minY; y--) {
                //if (y < 0 || y >= 299) continue;
                int pixelIndex = y * 800 + visplane.left[y];
                int textureY = (int) (y * dy) << 8;
                double textureX = dx * visplane.left[y] - textureOffsetX;
                for (int screenX = visplane.left[y]; screenX <= visplane.right[y]; screenX++) {
                    int c = ceilingTextureData[textureY + ((int) textureX & 0xff)];
                    offscreen3DData[pixelIndex++] = palette[c & 0xff];
                    textureX += dx;
                }
            }            
        }
        else {
            int[] ceilingTextureData = ceilingFlat.getImageData();
            double planeDistance = 400;
            Vec2 playerDir = player.getDirection();
            int maxY = visplane.maxY >= 299 ? 299 : visplane.maxY;
            int minY = visplane.minY < 0 ? 0 : visplane.minY;
            for (int y = maxY; y >= minY; y--) {

                //if (y < 0 || y >= 299) continue;

                double z = -planeDistance * (-ceilingHeight + (int) player.height) * CEIL_Y_INV[y];
                int[] colorMap = ColorMap.getFloorCeilingColorMap(z, sector);
                double px = playerDir.x * z + player.x;
                double py = playerDir.y * z + player.y;

                double lateralLength = TAN_45_DEG * z;

                double leftX = -playerDir.y * lateralLength + px;
                double leftY = playerDir.x * lateralLength + py;
                double rightX = playerDir.y * lateralLength + px;
                double rightY = -playerDir.x * lateralLength + py;

                double dx = (rightX - leftX) * CANVAS_HEIGHT_INV;
                double dy = (rightY - leftY) * CANVAS_HEIGHT_INV;
                double worldX = leftX + dx * visplane.left[y];
                double worldY = leftY + dy * visplane.left[y];
                int pixelIndex = y * 800 + visplane.left[y];
                for (int screenX = visplane.left[y]; screenX <= visplane.right[y]; screenX++) {
                    int textureX = (int) worldX & 0b000111111;
                    int textureY = (int) worldY & 0b000111111;
                    int c = ceilingTextureData[(textureY << 6) + textureX];
                    offscreen3DData[pixelIndex++] = colorMap[c];
                    worldX += dx;
                    worldY += dy;
                }
            }
        }
    }
    
}
