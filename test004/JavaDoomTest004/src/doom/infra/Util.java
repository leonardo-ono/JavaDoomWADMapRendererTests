package doom.infra;

/**
 *
 * @author Leo
 */
public class Util {
    
    private static int frameCount;
    private static double frameRandom;
    private static int frameRandomInt;
    private static int floorFrame;
    private static long frameNanoTime;
    
    public static int getFrameCount() {
        return frameCount;
    }

//    public static double getFrameRandom() {
//        return frameRandom;
//    }
//
//    public static int getFrameRandomInt() {
//        return frameRandomInt;
//    }

    public static int getFloorFrame() {
        return floorFrame;
    }

    public static long getFrameNanoTime() {
        return frameNanoTime;
    }
    
    public static void update() {
        frameCount++;
        //frameRandom = Math.random();
        //frameRandomInt = (int) (Integer.MAX_VALUE * frameRandom);
        frameNanoTime = System.nanoTime();
        floorFrame = (int) (frameNanoTime * 0.000000005) % 3;
    }
    
}
