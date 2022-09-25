package doom.wad;

import doom.wad.Textures.Texture;

/**
 * Linedef class.
 * 
 * References:
 * https://github.com/amroibrahim/DIYDoom/tree/master/DIYDOOM/Notes012/notes
 * https://doomwiki.org/wiki/Sidedef
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Sidedef {

    public final int offsetX;
    public final int offsetY;
    public final String upperTextureName;
    public final String lowerTextureName;
    public final String middleTextureName;
    public final Sector sector;        

    public final Texture upperTexture;
    public final Texture lowerTexture;
    public final Texture middleTexture;
    
    private boolean useSkyHack;
    
    public Sidedef(int offsetX, int offsetY, String upperTextureName
        , String lowerTextureName, String middleTextureName, Sector sector
        , Texture upperTexture, Texture lowerTexture, Texture middleTexture) {
        
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.upperTextureName = upperTextureName;
        this.lowerTextureName = lowerTextureName;
        this.middleTextureName = middleTextureName;
        this.sector = sector;
        this.upperTexture = upperTexture;
        this.lowerTexture = lowerTexture;
        this.middleTexture = middleTexture;
    }

    public boolean isUseSkyHack() {
        return useSkyHack;
    }

    void setUseSkyHack(boolean useSkyHack) {
        this.useSkyHack = useSkyHack;
    }
        
}
