package doom.wad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;

// This is little different from original source
// since, in this implementation, the horizontal spans are generated 
// at the same time as vertical columns are added.
// Reference: https://fabiensanglard.net/doomIphone/doomClassicRenderer.php
// update 28/set/2022:
// - now it works also for concave polygons
// - now it can handle separated polygons
public class Visplane {

    public static final int MAX_FRAGMENTED_SPANS = 16;

    public int minY;
    public int maxY;
    public final int[] horizontalSpanIndex;
    public final int[][] horizontalSpans;

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
        this.horizontalSpans = new int[screenHeight][MAX_FRAGMENTED_SPANS * 2];
        this.horizontalSpanIndex = new int[screenHeight];

        intermediateColumnHandler = (addX, addMinY, addMaxY) -> {
            if (addMinY < this.minY) this.minY = addMinY;
            if (addMaxY > this.maxY) this.maxY = addMaxY;
            final int xMinusOne = addX - 1;

            // handle separated polygons
            if (previousX != xMinusOne) {
                finish();
                previousX = addX;
                previousTop = addMinY;
                previousBottom = addMaxY;
                for (int y = addMinY; y <= addMaxY; y++) {
                    horizontalSpans[y][horizontalSpanIndex[y]++] = addX;
                }
            }

            previousX = addX;
            
            final int topY0 = previousTop;
            final int topY1 = addMinY;
            final int difTop = topY1 - topY0;
            previousTop = addMinY;

            final int bottomY0 = previousBottom;
            final int bottomY1 = addMaxY;
            final int difBottom = bottomY1 - bottomY0;
            previousBottom = addMaxY;

            if (difTop < 0) { // up
                for (int y = topY0 - 1; y >= topY1; y--) {
                    horizontalSpans[y][horizontalSpanIndex[y]++] = addX;
                }
            }
            else if (difTop > 0) { // topSignal >= 0 down or keep
                for (int y = topY0 + 1; y <= topY1; y++) {
                    horizontalSpans[y - 1][horizontalSpanIndex[y - 1]++] = xMinusOne;
                }
            }

            if (difBottom > 0) { // down
                for (int y = bottomY0 + 1; y <= bottomY1; y++) {
                    horizontalSpans[y][horizontalSpanIndex[y]++] = addX;
                }
            }
            else if (difBottom < 0) { // bottomSignal <= 0 // up
                for (int y = bottomY0 - 1; y >= bottomY1; y--) {
                    horizontalSpans[y + 1][horizontalSpanIndex[y + 1]++] = xMinusOne;
                }
            }
        };

        firstColumnHandler = (addX, addMinY, addMaxY) -> {
            previousX = addX;
            Arrays.fill(horizontalSpanIndex, minY, maxY + 1, 0);
            minY = addMinY;
            maxY = addMaxY;
            previousTop = addMinY;
            previousBottom = addMaxY;
            for (int y = addMinY; y <= addMaxY; y++) {
                horizontalSpans[y][horizontalSpanIndex[y]++] = addX;
            }
            columnHandler = intermediateColumnHandler;
        };
    }

    public int getPreviousX() {
        return previousX;
    }

    public void reset() {
        previousX = -1;
        columnHandler = firstColumnHandler;
    }

    public void addColumn(int x, int minY, int maxY) {
        columnHandler.add(x, minY, maxY);
    }

    public void finish() {
        for (int y = previousTop; y <= previousBottom; y++) {
            horizontalSpans[y][horizontalSpanIndex[y]++] = previousX;
        }
    }

    public void draw(Graphics2D g, int size, int tx, int ty, Color color) {
        for (int y = minY; y <= maxY; y++) {
            for (int x = 0; x < horizontalSpanIndex[y]; x += 2) {
                //int x = 0;
                g.setColor(color);

//                int rx = tx + size * horizontalSpans[y][x + 0];
//                int ry = ty + size * y;
//                g.drawRect(rx, ry, size, size);
//
//                rx = tx + size * horizontalSpans[y][x + 1];
//                ry = ty + size * y;
//                g.drawRect(rx, ry, size, size);
//
//                g.setColor(Color.GREEN);
                g.drawLine(horizontalSpans[y][x + 0] * size + size / 2
                                , y * size + size / 2
                                , horizontalSpans[y][x + 1] * size + size / 2
                                , y * size + size / 2);
            }

        }
    }

}
