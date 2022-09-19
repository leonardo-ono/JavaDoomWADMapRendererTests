package doom.wad;

import doom.infra.Player;
import doom.infra.Vec2;
import doom.infra.Visplane;
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

    public void drawFloorTest(int[] offscreen3DData, Player player, Visplane visplane) {
        double planeDistance = 400;
        int[] floorTextureData = floorFlat.getImageData();
        Vec2 playerDir = player.getDirection();
        for (int y = visplane.minY; y < visplane.maxY; y++) {

            if (y < 301 || y >= 600) continue;

            double z = planeDistance * (-floorHeight + player.height) * FLOOR_Y_INV[y];

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
            for (int screenX = visplane.left[y]; screenX <= visplane.right[y]; screenX++) {
                int textureX = (int) worldX & 0b000111111;
                int textureY = (int) worldY & 0b000111111;
                int c = floorTextureData[textureY * 64 + textureX];
                offscreen3DData[y * 800 + screenX] = c;
                worldX += dx;
                worldY += dy;
            }
        }
    }

    public void drawCeilingTest(int[] offscreen3DData, Player player, Visplane visplane) {
        if (ceilingPic != null) {
            int[] ceilingTextureData = ceilingPic.getImageData();
            int ceilingTextureWidth = ceilingPic.getImage().getWidth();
            int ceilingTextureHeight = ceilingPic.getImage().getHeight();
            double dx = ceilingTextureWidth / 800.0;
            double dy = ceilingTextureHeight / 300.0;
            for (int y = visplane.maxY; y >= visplane.minY; y--) {
                //if (y < 0 || y >= 299) continue;
                double texture = ((int) (y * dy) << 8) + visplane.left[y] * dx;
                for (int screenX = visplane.left[y]; screenX <= visplane.right[y]; screenX++) {
                    int c = ceilingTextureData[(int) texture];
                    offscreen3DData[y * 800 + screenX] = c;
                    texture += dx;
                }
            }            
        }
        else {
            int[] ceilingTextureData = ceilingFlat.getImageData();
            double planeDistance = 400;
            Vec2 playerDir = player.getDirection();
            for (int y = visplane.maxY; y >= visplane.minY; y--) {

                if (y < 0 || y >= 299) continue;

                double z = -planeDistance * (-ceilingHeight + player.height) * CEIL_Y_INV[y];

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
                for (int screenX = visplane.left[y]; screenX <= visplane.right[y]; screenX++) {
                    int textureX = (int) worldX & 0b000111111;
                    int textureY = (int) worldY & 0b000111111;
                    int c = ceilingTextureData[textureY * 64 + textureX];
                    offscreen3DData[y * 800 + screenX] = c;
                    worldX += dx;
                    worldY += dy;
                }
            }
        }
    }
    
}
