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
public class View extends JPanel {

    public static class Visplane {
        
        public int minX;
        public int maxX;
        public final int[] top;
        public final int[] bottom;

        public Visplane(int screenWidth) {
            this.top = new int[screenWidth];
            this.bottom = new int[screenWidth];
        }
        
        public void draw(Graphics2D g, int size, int tx, int ty) {
            g.setColor(Color.RED);
            for (int x = minX; x <= maxX; x++) {
                for (int y = top[x]; y <= bottom[x]; y++) {
                    int rx = tx + size * x;
                    int ry = ty + size * y;
                    g.fillRect(rx, ry, size, size);
                }
            }
            
            //g.drawRect(minX, 0, maxX - minX, 599);
        }
        
    }
    
    public static class HorizontalSpans {

        public final int[] left = new int[600];
        public final int[] right = new int[600];

        // apparently this is working correctly :)
        public void convertToHorizontalSpans(Visplane visplane) {
            // first column
            int topMinX = visplane.top[visplane.minX];
            int bottomMinX = visplane.bottom[visplane.minX];
            for (int y = topMinX; y <= bottomMinX; y++) {
                left[y] = visplane.minX;
            }
            // intermediate columns
            for (int x = visplane.minX; x < visplane.maxX; x++) {
                final int xPlusOne = x + 1;
                
                final int topY0 = visplane.top[x];
                final int topY1 = visplane.top[xPlusOne];
                final int difTop = topY1 - topY0;

                final int bottomY0 = visplane.bottom[x];
                final int bottomY1 = visplane.bottom[xPlusOne];
                final int difBottom = bottomY1 - bottomY0;

                if (difTop < 0) { // up
                    for (int y = topY0 - 1; y >= topY1; y--) {
                        left[y] = xPlusOne;
                    }
                }
                else { // topSignal >= 0 down or keep
                    for (int y = topY0; y <= topY1; y++) {
                        right[y] = x;
                    }
                }
                
                if (difBottom > 0) { // down
                    for (int y = bottomY0 + 1; y <= bottomY1; y++) {
                        left[y] = xPlusOne;
                    }
                }
                else { // bottomSignal <= 0 // up
                    for (int y = bottomY0; y >= bottomY1; y--) {
                        right[y] = x;
                    }
                }
            }
            // last column
            int topMaxX = visplane.top[visplane.maxX];
            int bottomMaxX = visplane.bottom[visplane.maxX];
            for (int y = topMaxX; y <= bottomMaxX; y++) {
                right[y] = visplane.maxX;
            }
        }
        
        public void draw(Graphics2D g, int size, int tx, int ty) {
            g.setColor(Color.GREEN);
            for (int y = 0; y < 600; y++) {
                //for (int x = left[y]; x <= right[y]; x++) {
                    int rx = tx + size * left[y];
                    int ry = ty + size * y;
                    g.drawRect(rx, ry, size, size);
                    rx = tx + size * right[y];
                    ry = ty + size * y;
                    g.drawRect(rx, ry, size, size);
                    //g.drawLine(left[y], y, left[y], y);
                    //g.drawLine(right[y], y, right[y], y);
                //}
            }
        }
                
    }
    
    private Visplane visplane;
    private HorizontalSpans horizontalSpans;
    
    private BufferedImage offscreen;
    private Graphics2D offscreenG2D;
    
    public void start() {
        setPreferredSize(new Dimension(800, 600));
        visplane = new Visplane(800);
        horizontalSpans = new HorizontalSpans();
        
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
        
        horizontalSpans.convertToHorizontalSpans(visplane);
        
        horizontalSpans.draw(g2d, 2, 0, 0);
    }

    private void draw(Graphics2D g) {
        polygon.reset();
        polygon.addPoint(10, 20);
        polygon.addPoint(45, 59);
        polygon.addPoint(30, 34);
        polygon.addPoint(13, 23);

        polygon.reset();
        polygon.addPoint(50, 50);
        polygon.addPoint(52, 50);
        polygon.addPoint(152, 100);
        polygon.addPoint(150, 100);

        g.setColor(Color.BLUE);
        g.fill(polygon);
        boolean foundPost = false;
        for (int x = 0; x < 800; x++) {
            for (int y = 0; y < 600; y++) {
                if (offscreen.getRGB(x, y) == 0xff0000ff) {
                    visplane.top[x] = y;
                    if (!foundPost) {
                        foundPost = true;
                        visplane.minX = x;
                        visplane.maxX = x;
                    }
                    else {
                        visplane.maxX = x;
                    }
                    break;
                }
            }
            for (int y = 599; y >= 0; y--) {
                if (offscreen.getRGB(x, y) == 0xff0000ff) {
                    visplane.bottom[x] = y;
                    break;
                }
            }
        }
        
        
        
        System.out.println("");
    }
        
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            View view = new View();
            view.start();
            JFrame frame = new JFrame();
            frame.setTitle("DOOM Visplane Vertical Columns to Horizontal Spans Conversion Test #1");
            frame.getContentPane().add(view);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            view.requestFocus();
        });
    }
    
}
