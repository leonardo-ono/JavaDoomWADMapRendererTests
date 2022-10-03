package doom.wad;

import doom.infra.Player;
import doom.infra.Scaler;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Blockmap class.
 * 
 * References:
 * https://doomwiki.org/wiki/Blockmap
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Blockmap {

    public final Point gridOrigin;
    public final int cols;
    public final int rows;
    public final Linedef[][][] blocks;        

    public Blockmap(Point gridOrigin, int cols, int rows, Linedef[][][] blocks) {
        this.gridOrigin = gridOrigin;
        this.cols = cols;
        this.rows = rows;
        this.blocks = blocks;
    }

    public Linedef[] getBlock(int x, int y) {
        int row = (y - gridOrigin.y) / 128;
        int col = (x - gridOrigin.x) / 128; 
        return blocks[row][col];
    }
    
    private final Point dstTmp = new Point();
    private final Set<Linedef> linedefsTmp = new HashSet<>();
    private final Point p0 = new Point();
    private final Point p1 = new Point();
    
    public void drawGrid(Graphics2D g, Color gridColor, Color linedefColor, Player player) {
        // draw linedefs
        retrieveCollidingWalls(linedefsTmp, player);
        g.setColor(linedefColor);
        for (Linedef linedef : linedefsTmp) {
            Scaler.get(linedef.startVertex, p0);
            Scaler.get(linedef.endVertex, p1);
            g.drawLine(p0.x, p0.y, p1.x, p1.y);
        }                  
        
        // draw grid
        g.setColor(gridColor);
        final int size = 128;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int rx = gridOrigin.x + col * size;
                int ry = gridOrigin.y + row * size;
                int scaledSize = (int) (size * Scaler.SCALE);
                Scaler.get(rx, ry, dstTmp);
                g.drawRect(dstTmp.x, dstTmp.y, scaledSize, scaledSize);
            }
        }
    }

    private void retrieveCollidingWalls(Set<Linedef> linedefs, Player player) {
        final double radius = player.radius;
        double[] offsets = { -radius, -radius
                           , -radius,  radius
                           ,  radius,  radius
                           ,  radius, -radius };
        
        linedefs.clear();
        for (int o = 0; o < offsets.length; o += 2) {
            int bx = (int) (player.x + offsets[o + 0]);
            int by = (int) (player.y + offsets[o + 1]);
            Linedef[] blocksTmp = WADLoader.blockmap.getBlock(bx, by);
            linedefs.addAll(Arrays.asList(blocksTmp));
        }
    }
    
}
