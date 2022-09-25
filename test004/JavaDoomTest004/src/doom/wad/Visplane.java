package doom.wad;

import java.awt.Color;
import java.awt.Graphics2D;

// This implementation is little different from original source
// since the horizontal spans are generated at the same time 
// as vertical columns are added.
// Ref.: https://fabiensanglard.net/doomIphone/doomClassicRenderer.php
public class Visplane {

    public int minY;
    public int maxY;
    public final int[] left;
    public final int[] right;

    private int previousTop;
    private int previousBottom;
    private int previousX;

    private interface ColumnHandler {

        public void add(int addX, int addMinY, int addMaxY);

    }

    private final ColumnHandler firstColumnHandler;
    private final ColumnHandler intermediateColumnHandler;
    
    private ColumnHandler columnHandler;

    public Visplane(int screenHeight) {
        left = new int[screenHeight];
        right = new int[screenHeight];

        intermediateColumnHandler = (addX, addMinY, addMaxY) -> {
            previousX = addX;
            final int xMinusOne = addX - 1;
            final int topY0 = previousTop;
            final int topY1 = addMinY;
            final int difTop = topY1 - topY0;
            previousTop = addMinY;
            if (addMinY < this.minY) this.minY = addMinY;

            final int bottomY0 = previousBottom;
            final int bottomY1 = addMaxY;
            final int difBottom = bottomY1 - bottomY0;
            previousBottom = addMaxY;
            if (addMaxY > this.maxY) this.maxY = addMaxY;

            if (difTop < 0) { // up
                for (int y = topY0 - 1; y >= topY1; y--) {
                    left[y] = addX;
                }
            }
            else { // topSignal >= 0 down or keep
                for (int y = topY0; y <= topY1; y++) {
                    right[y] = xMinusOne;
                }
            }

            if (difBottom > 0) { // down
                for (int y = bottomY0 + 1; y <= bottomY1; y++) {
                    left[y] = addX;
                }
            }
            else { // bottomSignal <= 0 // up
                for (int y = bottomY0; y >= bottomY1; y--) {
                    right[y] = xMinusOne;
                }
            }            
        };

        firstColumnHandler = (addX, addMinY, addMaxY) -> {
            previousX = addX;
            this.minY = addMinY;
            this.maxY = addMaxY;
            previousTop = addMinY;
            previousBottom = addMaxY;
            for (int y = addMinY; y <= addMaxY; y++) {
                left[y] = addX;
            }
            columnHandler = intermediateColumnHandler;
        };
    }

    public int getPreviousX() {
        return previousX;
    }

    public void reset() {
        columnHandler = firstColumnHandler;
        previousX = -1;
    }

    public void addColumn(int x, int minY, int maxY) {
        columnHandler.add(x, minY, maxY);
    }

    public void finish() {
        for (int y = previousTop; y <= previousBottom; y++) {
            right[y] = previousX + 1;
        }
    }

    public void draw(Graphics2D g, int size, int tx, int ty, Color color) {
        g.setColor(color);
        for (int y = minY; y <= maxY; y++) {
//            int rx = tx + size * left[y];
//            int ry = ty + size * y;
//            g.drawRect(rx, ry, size, size);
//
//            rx = tx + size * right[y];
//            ry = ty + size * y;
//            g.drawRect(rx, ry, size, size);
            g.drawLine(left[y], y, right[y], y);
        }
    }

}
