package doom.wad;

import doom.infra.Vec2;

/**
 * Node class.
 * 
 * References:
 * https://github.com/amroibrahim/DIYDoom/tree/master/DIYDOOM/Notes007/notes
 * https://doomwiki.org/wiki/Node
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Node {
    
    public final int partitionX; // int16_t
    public final int partitionY; // int16_t
    public final int changePartitionX; // int16_t
    public final int changePartitionY; // int16_t

    public final int rightBoxTop; // int16_t
    public final int rightBoxBottom; // int16_t
    public final int rightBoxLeft; // int16_t
    public final int rightBoxRight; // int16_t

    public final int leftBoxTop; // int16_t
    public final int leftBoxBottom; // int16_t
    public final int leftBoxLeft; // int16_t
    public final int leftBoxRight; // int16_t

    public final int rightChildID; // uint16_t
    public final int leftChildID; // uint16_t

    public final Vec2 divider;

    public Node(int partitionX, int partitionY, int changePartitionX
                , int changePartitionY, int rightBoxTop, int rightBoxBottom
                    , int rightBoxLeft, int rightBoxRight, int leftBoxTop
                        , int leftBoxBottom, int leftBoxLeft, int leftBoxRight
                            , int rightChildID, int leftChildID) {
        
        this.partitionX = partitionX;
        this.partitionY = partitionY;
        this.changePartitionX = changePartitionX;
        this.changePartitionY = changePartitionY;
        this.rightBoxTop = rightBoxTop;
        this.rightBoxBottom = rightBoxBottom;
        this.rightBoxLeft = rightBoxLeft;
        this.rightBoxRight = rightBoxRight;
        this.leftBoxTop = leftBoxTop;
        this.leftBoxBottom = leftBoxBottom;
        this.leftBoxLeft = leftBoxLeft;
        this.leftBoxRight = leftBoxRight;
        this.rightChildID = rightChildID;
        this.leftChildID = leftChildID;

        divider = new Vec2(changePartitionX, changePartitionY);
    }
        
    private final Vec2 vec2Tmp = new Vec2();

    public int getChildSide(double playerX, double playerY) {
        vec2Tmp.set(playerX - partitionX, playerY - partitionY);
        return divider.getSign(vec2Tmp) > 0 ? 1 : 0;
    }
        
}
