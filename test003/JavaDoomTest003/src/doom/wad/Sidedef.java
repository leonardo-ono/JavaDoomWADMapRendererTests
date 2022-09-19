/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package doom.wad;

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

    public Sidedef(int offsetX, int offsetY
                    , String upperTextureName, String lowerTextureName
                        , String middleTextureName, Sector sector) {
        
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.upperTextureName = upperTextureName;
        this.lowerTextureName = lowerTextureName;
        this.middleTextureName = middleTextureName;
        this.sector = sector;
    }
        
}
