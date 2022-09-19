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
public class View2 extends JPanel {

    // In this implementation, horizontal spans are generated 
    // as vertical columns are added.
    // Reference: https://fabiensanglard.net/doomIphone/doomClassicRenderer.php
    public static class Visplane {
        
        public int minY;
        public int maxY;
        public final int[] left;
        public final int[] right;

        public Visplane(int screenHeight) {
            left = new int[screenHeight];
            right = new int[screenHeight];
        }

        public void addFirstColumn(int x, int minY, int maxY) {
            this.minY = minY;
            this.maxY = maxY;
            previousTop = minY;
            previousBottom = maxY;
            for (int y = minY; y <= maxY; y++) {
                left[y] = x;
            }
        }
        
        private int previousTop;
        private int previousBottom;
        
        public void addColumn(int x, int minY, int maxY) {
            final int xMinusOne = x;
            
            final int topY0 = previousTop;
            final int topY1 = minY;
            final int difTop = topY1 - topY0;
            previousTop = minY;
            if (minY < this.minY) this.minY = minY;

            final int bottomY0 = previousBottom;
            final int bottomY1 = maxY;
            final int difBottom = bottomY1 - bottomY0;
            previousBottom = maxY;
            if (maxY > this.maxY) this.maxY = maxY;

            if (difTop < 0) { // up
                for (int y = topY0 - 1; y >= topY1; y--) {
                    left[y] = x;
                }
            }
            else { // topSignal >= 0 down or keep
                for (int y = topY0; y <= topY1; y++) {
                    right[y] = xMinusOne;
                }
            }

            if (difBottom > 0) { // down
                for (int y = bottomY0 + 1; y <= bottomY1; y++) {
                    left[y] = x;
                }
            }
            else { // bottomSignal <= 0 // up
                for (int y = bottomY0; y >= bottomY1; y--) {
                    right[y] = xMinusOne;
                }
            }            
        }
        
        public void addLastColumn(int x, int minY, int maxY) {
            addColumn(x, minY, maxY);
            for (int y = minY; y <= maxY; y++) {
                right[y] = x;
            }
        }
        
        public void draw(Graphics2D g, int size, int tx, int ty) {
            g.setColor(Color.CYAN);
            for (int y = minY; y <= maxY; y++) {
                int rx = tx + size * left[y];
                int ry = ty + size * y;
                g.drawRect(rx, ry, size, size);

                rx = tx + size * right[y];
                ry = ty + size * y;
                g.drawRect(rx, ry, size, size);
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
        visplane.draw(g2d, 2, 0, 0);
        
    }

    private void draw(Graphics2D g) {
        polygon.reset();
        polygon.addPoint(10, 20);
        polygon.addPoint(45, 59);
        polygon.addPoint(30, 34);
        polygon.addPoint(13, 23);

        polygon.reset();
        polygon.addPoint(10, 50);
        polygon.addPoint(40, 70);
        polygon.addPoint(390, 110);
        polygon.addPoint(2, 90);

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
        
        visplane.addFirstColumn(minX, top[minX], bottom[minX]);
        for (int x = minX + 1; x < maxX; x++) {
            visplane.addColumn(x, top[x], bottom[x]);
        }
        visplane.addLastColumn(maxX, top[maxX], bottom[maxX]);
        
        
        System.out.println("");
    }
        
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            View2 view = new View2();
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
