import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author Leo
 */
public class View5 extends JPanel {

    // This implementation is little different from original source
    // since the horizontal spans are generated at the same time 
    // as vertical columns are added.
    // Reference: https://fabiensanglard.net/doomIphone/doomClassicRenderer.php
    // update 28/set/2022 now it works also for concave polygons
    public class Visplane {
        
        public static final int MAX_FRAGMENTED_SPANS = 10;
        
        public int minY;
        public int maxY;
        public final int[] horizontalSpansIndex;
        public final int[][] horizontalSpans;

        private int previousTop;
        private int previousBottom;
        private int previousX;

        private interface ColumnHandler {

            public boolean add(int addX, int addMinY, int addMaxY);

        }

        private final ColumnHandler firstColumnHandler;
        private final ColumnHandler intermediateColumnHandler;
        private ColumnHandler columnHandler;

        public Visplane(int screenHeight) {
            this.horizontalSpans = new int[screenHeight][MAX_FRAGMENTED_SPANS * 2];
            this.horizontalSpansIndex = new int[screenHeight];
            
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
                        horizontalSpans[y][horizontalSpansIndex[y]++] = addX;
                    }
                }
                else if (difTop > 0) { // topSignal >= 0 down or keep
                    for (int y = topY0 + 1; y <= topY1; y++) {
                        horizontalSpans[y - 1][horizontalSpansIndex[y - 1]++] = xMinusOne;
                    }
                }

                if (difBottom > 0) { // down
                    for (int y = bottomY0 + 1; y <= bottomY1; y++) {
                        horizontalSpans[y][horizontalSpansIndex[y]++] = addX;
                    }
                }
                else if (difBottom < 0) { // bottomSignal <= 0 // up
                    for (int y = bottomY0 - 1; y >= bottomY1; y--) {
                        horizontalSpans[y + 1][horizontalSpansIndex[y + 1]++] = xMinusOne;
                    }
                }
                
                return true;
            };

            firstColumnHandler = (addX, addMinY, addMaxY) -> {
                Arrays.fill(horizontalSpansIndex, minY, maxY + 1, 0);
                minY = addMinY;
                maxY = addMaxY;
                previousTop = addMinY;
                previousBottom = addMaxY;
                for (int y = addMinY; y <= addMaxY; y++) {
                    horizontalSpans[y][horizontalSpansIndex[y]++] = addX;
                }
                columnHandler = intermediateColumnHandler;
                
                return true;
            };
        }

        public void reset() {
            columnHandler = firstColumnHandler;
        }

        public boolean addColumn(int x, int minY, int maxY) {
            return columnHandler.add(x, minY, maxY);
        }

        public void finish() {
            for (int y = previousTop; y <= previousBottom; y++) {
                horizontalSpans[y][horizontalSpansIndex[y]++] = previousX;
            }
        }

        public void draw(Graphics2D g, int size, int tx, int ty, Color color) {
            for (int y = minY; y <= maxY; y++) {
                for (int x = 0; x < horizontalSpansIndex[y]; x += 2) {
                    //int x = 0;
                    g.setColor(color);
                    
                    int rx = tx + size * horizontalSpans[y][x + 0];
                    int ry = ty + size * y;
                    g.drawRect(rx, ry, size, size);

                    rx = tx + size * horizontalSpans[y][x + 1];
                    ry = ty + size * y;
                    g.drawRect(rx, ry, size, size);

                    g.setColor(Color.GREEN);
                    g.drawLine(horizontalSpans[y][x + 0] * size + size / 2, y * size + size / 2, horizontalSpans[y][x + 1] * size + size / 2, y * size + size / 2);
                }
                
            }
        }

    }
    
    private Visplane visplane;
    
    private BufferedImage offscreen;
    private Graphics2D offscreenG2D;
    
    public void start() {
        setPreferredSize(new Dimension(800, 600));
        visplane = new Visplane(800);
        
        offscreen = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        offscreenG2D = offscreen.createGraphics();
    }
    
    private final Polygon polygon = new Polygon();
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.drawLine(0, 0, getWidth(), getHeight());
        
        draw(offscreenG2D);
        
        //g2d.scale(5, 5);
        g2d.drawImage(offscreen, 0, 0, offscreen.getWidth() * 5, offscreen.getHeight() * 5, null);
        
        visplane.draw(g2d, 5, 0, 0, Color.RED);
        repaint();
    }
    
    double k = 0;
    
    private void draw(Graphics2D g) {
        polygon.reset();
        polygon.addPoint(10, 10);
        polygon.addPoint(30, 35);
        polygon.addPoint(45, 19);
        polygon.addPoint(30, 64);
        polygon.addPoint(20, (int) (29 + k));
        polygon.addPoint(13, 73);

        k += 0.01;

        polygon.reset();
        polygon.addPoint(50, 50);
        polygon.addPoint(50, 100);
        polygon.addPoint(75, 60);
        polygon.addPoint(100, 120);
        polygon.addPoint(100, 50);

        int minX = 0;
        int maxX = Integer.MAX_VALUE;
        int[] top = new int[600];
        int[] bottom = new int[600];
        
        g.setColor(Color.BLUE);
        g.fill(polygon);
        //g.drawLine(50, 50, 60, 100);
        
        boolean foundPost = false;
        for (int x = 0; x < 800; x++) {
            for (int y = 0; y < 600; y++) {
                if (offscreen.getRGB(x, y) == 0xff0000ff) {
                    top[x] = y;
                    if (!foundPost) {
                        foundPost = true;
                        minX = x;
                        maxX = x;
                    }
                    else {
                        maxX = x;
                    }
                    break;
                }
            }
            for (int y = 599; y >= 0; y--) {
                if (offscreen.getRGB(x, y) == 0xff0000ff) {
                    bottom[x] = y;
                    break;
                }
            }
        }
        visplane.reset();
        for (int x = minX; x <= maxX; x++) {
            visplane.addColumn(x, top[x], bottom[x]);
        }
        visplane.finish();
        
        
        System.out.println("");
    }
        
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            View5 view = new View5();
            view.start();
            JFrame frame = new JFrame();
            frame.setTitle("DOOM Visplane Vertical Columns to Horizontal Spans Conversion Test #5");
            frame.getContentPane().add(view);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            view.requestFocus();
        });
    }
    
}
