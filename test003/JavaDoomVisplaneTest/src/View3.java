import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author Leo
 */
public class View3 extends JPanel {

    // This implementation is little different from original source
    // since the horizontal spans are generated at the same time 
    // as vertical columns are added.
    // Reference: https://fabiensanglard.net/doomIphone/doomClassicRenderer.php
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

        public void reset() {
            columnHandler = firstColumnHandler;
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
                //int rx = tx + size * left[y];
                //int ry = ty + size * y;
                //g.drawRect(rx, ry, size, size);

                //rx = tx + size * right[y];
                //ry = ty + size * y;
                //g.drawRect(rx, ry, size, size);
                g.drawLine(left[y], y, right[y], y);
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
        
        g2d.drawImage(offscreen, 0, 0, null);
        
        //g2d.scale(5, 5);
        visplane.draw(g2d, 1, 0, 0, Color.RED);
        
    }

    private void draw(Graphics2D g) {
        polygon.reset();
        polygon.addPoint(10, 10);
        polygon.addPoint(45, 19);
        polygon.addPoint(30, 64);
        polygon.addPoint(13, 73);

        //polygon.reset();
        //polygon.addPoint(10, 50);
        //polygon.addPoint(40, 70);
        //polygon.addPoint(390, 110);
        //polygon.addPoint(2, 90);

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
            View3 view = new View3();
            view.start();
            JFrame frame = new JFrame();
            frame.setTitle("DOOM Visplane Vertical Columns to Horizontal Spans Conversion Test #2");
            frame.getContentPane().add(view);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            view.requestFocus();
        });
    }
    
}
