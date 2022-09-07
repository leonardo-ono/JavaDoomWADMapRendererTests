package doom;


import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * References:
 * https://doomwiki.org/wiki/Doom_rendering_engine
 * 
 * https://github.com/amroibrahim/DIYDoom/tree/master/DIYDOOM/Notes002/notes
 * https://www.gamers.org/dhs/helpdocs/dmsp1666.html
 * 
 * drawing subsectors: https://www.youtube.com/watch?v=iqKrbF6PxWY&t=131s
 * 
 * MUS format: https://moddingwiki.shikadi.net/wiki/MUS_Format
 * 
 * @author Leo
 */
public class Test {
    
    public static class WADDirectory {
        
        public final long offset; // from the start of file
        public final long length; // size of lump in bytes
        public final String name; // name of the lump (8 bytes)
        public final ByteBuffer data; // data

        public WADDirectory(long offset, long length, String name, ByteBuffer data) {
            this.offset = offset;
            this.length = length;
            this.name = name;
            this.data = data;
        }

        @Override
        public String toString() {
            return "WADDirectory{" + "offset=" + offset + ", length=" + length 
                                    + ", name=" + name + ", data=" + data + '}';
        }
        
    }
    
    public static ByteBuffer bb;
    
    public static void loadWad(String wadFile) {
        try (
            InputStream is = new FileInputStream(wadFile);
        ) {
            bb = ByteBuffer.wrap(is.readAllBytes());
            bb.order(ByteOrder.LITTLE_ENDIAN);
            
            // wad id
            byte[] wadid = new byte[4];
            bb.get(wadid);
            System.out.println("wadid: " + new String(wadid));
            
            // lumps size
            long lumpsSize = bb.getInt() & 0xffffffff;
            System.out.println("number of entries: " + lumpsSize);
            
            long directoryOffset = bb.getInt() & 0xffffffff;
            System.out.println("directory offset: " + directoryOffset);
            
            extractDirectories(bb, directoryOffset, lumpsSize);
                    
        } 
        catch (IOException ex) {
            Logger.getLogger(Test.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
            
            System.exit(-1);
        }
    }
    
    private static List<WADDirectory> directories = new ArrayList<>();
    
    private static void extractDirectories(
                        ByteBuffer bb, long directoryOffset, long lumpsSize) {
        
        bb.position((int) directoryOffset);
        directories.clear();
        for (int i = 0; i < lumpsSize; i++) {
            long offset = bb.getInt() & 0xffffffff;
            long length = bb.getInt() & 0xffffffff;
            byte[] nameBytes = new byte[8];
            bb.get(nameBytes);
            String name = new String(nameBytes);
            ByteBuffer data = bb.slice((int) offset, (int) length);
            WADDirectory directory 
                            = new WADDirectory(offset, length, name, data);
            
            directories.add(directory);
        }
    }
    
    // vertex
    
    public static List<Point> vertexes = new ArrayList<>();

    public static void extractVertexes(ByteBuffer bb, int lumpIndex) {
        WADDirectory dir = directories.get(lumpIndex);
        System.out.println("extracting vertexes " + dir);
        //dir.data.position(0);
        bb.position((int) dir.offset);
        int totalLength = 0;
        while (totalLength < dir.length) {
            int x = bb.getShort() & 0xffff;
            int y = bb.getShort() & 0xffff;
            vertexes.add(new Point(x, y));
            totalLength += 2 * 2;
        } 
    }
    
    // linedef
    // https://doomwiki.org/wiki/Linedef
    //    0	2	int16_t	Start Vertex
    //    2	2	int16_t	End Vertex
    //    4	2	int16_t	Flags
    //    6	2	int16_t	Special Type
    //    8	2	int16_t	Sector Tag
    //    10	2	int16_t	Front Sidedef [doom 2]
    //    12	2	int16_t	Back Sidedef [doom 2]
    
    public static class LineDef {
        
        public final int startVertex;
        public final int endVertx;
        public final int flags;
        public final int specialType;
        public final int sectorTag;
        public final int frontSidedef;        
        public final int backSidedef;        

        public LineDef(int startVertex, int endVertx, int flags, int specialType, int sectorTag, int frontSidedef, int backSidedef) {
            this.startVertex = startVertex;
            this.endVertx = endVertx;
            this.flags = flags;
            this.specialType = specialType;
            this.sectorTag = sectorTag;
            this.frontSidedef = frontSidedef;
            this.backSidedef = backSidedef;
        }
        
    }
    
    public static List<LineDef> lineDefs = new ArrayList<>();

    public static void extractLineDefs(ByteBuffer bb, int lumpIndex) {
        WADDirectory dir = directories.get(lumpIndex);
        System.out.println("extracting lineDefs " + dir);
        //dir.data.position(0);
        int totalLength = 0;
        bb.position((int) dir.offset);
        while (totalLength < dir.length) {
            int startVertex = bb.getShort() & 0xffff;
            int endVertx = bb.getShort() & 0xffff;
            int flags = bb.getShort() & 0xffff;
            int specialType = bb.getShort() & 0xffff;
            int sectorTag = bb.getShort() & 0xffff;
            int frontSidedef = bb.getShort() & 0xffff;        
            int backSidedef = bb.getShort() & 0xffff;       
            lineDefs.add(new LineDef(startVertex, endVertx, flags, specialType, sectorTag, frontSidedef, backSidedef));
            totalLength += 7 * 2;
        } 
    }
    
    // sidedef
    // https://doomwiki.org/wiki/Sidedef
    //    0	2	int16_t	x offset
    //    2	2	int16_t	y offset
    //    4	8	int8_t [8]	Name of upper texture
    //    12	8	int8_t [8]	Name of lower texture
    //    20	8	int8_t [8]	Name of middle texture
    //    28	2	int16_t	Sector number this sidedef 'faces'

    public static class SideDef {
        
        public final int offsetX;
        public final int offsetY;
        public final String upperTextureName;
        public final String lowerTextureName;
        public final String middleTextureName;
        public final int sectorNumber;        

        public SideDef(int offsetX, int offsetY, String upperTextureName, String lowerTextureName, String middleTextureName, int sectorNumber) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.upperTextureName = upperTextureName;
            this.lowerTextureName = lowerTextureName;
            this.middleTextureName = middleTextureName;
            this.sectorNumber = sectorNumber;
        }
        
    }
    
    public static List<SideDef> sideDefs = new ArrayList<>();

    public static void extractSideDefs(ByteBuffer bb, int lumpIndex) {
        WADDirectory dir = directories.get(lumpIndex);
        System.out.println("extracting sideDefs " + dir);
        //dir.data.position(0);
        int totalLength = 0;
        bb.position((int) dir.offset);
        while (totalLength < dir.length) {
            int offsetX = bb.getShort() & 0xffff;
            int offsetY = bb.getShort() & 0xffff;
            byte[] upperTextureNameBytes = new byte[8];
            bb.get(upperTextureNameBytes);
            String upperTextureName = new String(upperTextureNameBytes);
            byte[] lowerTextureNameBytes = new byte[8];
            bb.get(lowerTextureNameBytes);
            String lowerTextureName = new String(lowerTextureNameBytes);
            byte[] middleTextureNameBytes = new byte[8];
            bb.get(middleTextureNameBytes);
            String middleTextureName = new String(middleTextureNameBytes);
            int sectorNumber = bb.getShort() & 0xffff;    
             
            sideDefs.add(new SideDef(offsetX, offsetY, upperTextureName, lowerTextureName, middleTextureName, sectorNumber));
            totalLength += 3 * 2 + 3 * 8;
        } 
    }
    
    // segs
    // https://doomwiki.org/wiki/Seg
    //    0       2	int16_t	Starting vertex number
    //    2       2	int16_t	Ending vertex number
    //    4       2	int16_t	Angle, full circle is -32768 to 32767.
    //    6       2	int16_t	Linedef number
    //    8       2	int16_t	Direction: 0 (same as linedef) or 1 (opposite of linedef)
    //    10      2	int16_t	Offset: distance along linedef to start of seg    
    public static class Seg {
        
        public final int startingVertexNumber;
        public final int endingVertexNumber;
        public final int angle;
        public final int lineDefNumber;
        public final int direction;
        public final int offset;

        public Seg(int startingVertexNumber, int endingVertexNumber, int angle, int lineDefNumber, int direction, int offset) {
            this.startingVertexNumber = startingVertexNumber;
            this.endingVertexNumber = endingVertexNumber;
            this.angle = angle;
            this.lineDefNumber = lineDefNumber;
            this.direction = direction;
            this.offset = offset;
        }
        
    }

    public static List<Seg> segs = new ArrayList<>();

    public static void extractSegs(ByteBuffer bb, int lumpIndex) {
        WADDirectory dir = directories.get(lumpIndex);
        System.out.println("extracting segs " + dir);
        //dir.data.position(0);
        int totalLength = 0;
        bb.position((int) dir.offset);
        while (totalLength < dir.length) {
            int startingVertexNumber = bb.getShort() & 0xffff;
            int endingVertexNumber = bb.getShort() & 0xffff;
            int angle = bb.getShort() & 0xffff;
            int lineDefNumber = bb.getShort() & 0xffff;
            int direction = bb.getShort() & 0xffff;
            int offset = bb.getShort() & 0xffff;
            Seg seg = new Seg(startingVertexNumber, endingVertexNumber, angle, lineDefNumber, direction, offset);
            segs.add(seg);
            totalLength += 6 * 2;
        } 
    }
    
    // nodes
    // https://doomwiki.org/wiki/Node
    // https://github.com/amroibrahim/DIYDoom/tree/master/DIYDOOM/Notes007/notes
    
    public static class Node {
        public final int XPartition; // int16_t
        public final int YPartition; // int16_t
        public final int ChangeXPartition; // int16_t
        public final int ChangeYPartition; // int16_t

        public final int RightBoxTop; // int16_t
        public final int RightBoxBottom; // int16_t
        public final int RightBoxLeft; // int16_t
        public final int RightBoxRight; // int16_t

        public final int LeftBoxTop; // int16_t
        public final int LeftBoxBottom; // int16_t
        public final int LeftBoxLeft; // int16_t
        public final int LeftBoxRight; // int16_t

        public final int RightChildID; // uint16_t
        public final int LeftChildID; // uint16_t
        
        public final Vec2 divider;

        public Node(int XPartition, int YPartition, int ChangeXPartition, int ChangeYPartition, int RightBoxTop, int RightBoxBottom, int RightBoxLeft, int RightBoxRight, int LeftBoxTop, int LeftBoxBottom, int LeftBoxLeft, int LeftBoxRight, int RightChildID, int LeftChildID) {
            this.XPartition = XPartition;
            this.YPartition = YPartition;
            this.ChangeXPartition = ChangeXPartition;
            this.ChangeYPartition = ChangeYPartition;
            this.RightBoxTop = RightBoxTop;
            this.RightBoxBottom = RightBoxBottom;
            this.RightBoxLeft = RightBoxLeft;
            this.RightBoxRight = RightBoxRight;
            this.LeftBoxTop = LeftBoxTop;
            this.LeftBoxBottom = LeftBoxBottom;
            this.LeftBoxLeft = LeftBoxLeft;
            this.LeftBoxRight = LeftBoxRight;
            this.RightChildID = RightChildID;
            this.LeftChildID = LeftChildID;
            
            divider = new Vec2((short) ChangeXPartition, (short)ChangeYPartition);
        }
        
        private final Vec2 vec2Tmp = new Vec2();
                
        public int getChildSide(double playerX, double playerY) {
            vec2Tmp.set(playerX - (short) XPartition, playerY - (short) YPartition);
            return (int) (divider.getSign(vec2Tmp) * 0.5 + 0.5);
        }
        
    }
    
    public static List<Node> nodes = new ArrayList<>();

    public static void extractNodes(ByteBuffer bb, int lumpIndex) {
        WADDirectory dir = directories.get(lumpIndex);
        System.out.println("extracting nodes " + dir);
        //dir.data.position(0);
        int totalLength = 0;
        bb.position((int) dir.offset);
        while (totalLength < dir.length) {
            int XPartition = bb.getShort() & 0xffff;
            int YPartition = bb.getShort() & 0xffff;
            int ChangeXPartition = bb.getShort() & 0xffff;
            int ChangeYPartition = bb.getShort() & 0xffff;
            int RightBoxTop = bb.getShort() & 0xffff;
            int RightBoxBottom = bb.getShort() & 0xffff;
            int RightBoxLeft = bb.getShort() & 0xffff;
            int RightBoxRight = bb.getShort() & 0xffff;
            int LeftBoxTop = bb.getShort() & 0xffff;
            int LeftBoxBottom = bb.getShort() & 0xffff;
            int LeftBoxLeft = bb.getShort() & 0xffff;
            int LeftBoxRight = bb.getShort() & 0xffff;
            int RightChildId = bb.getShort() & 0xffff;
            int LeftChildId = bb.getShort() & 0xffff;            
            Node node = new Node(XPartition, YPartition, ChangeXPartition, ChangeYPartition, RightBoxTop, RightBoxBottom, RightBoxLeft, RightBoxRight, LeftBoxTop, LeftBoxBottom, LeftBoxLeft, LeftBoxRight, RightChildId, LeftChildId);
            nodes.add(node);
            totalLength += 14 * 2;
        } 
    }

    // sector
    // https://doomwiki.org/wiki/Sector
    //    0	2	int16_t	Floor height
    //    2	2	int16_t	Ceiling height
    //    4	8	int8_t[8]	Name of floor texture
    //    12	8	int8_t[8]	Name of ceiling texture
    //    20	2	int16_t	Light level
    //    22	2	int16_t	Special Type
    //    24	2	int16_t	Tag number    


    public static class Sector {

        public final int floorHeight; // int16_t
        public final int ceilingHeight; // int16_t
        public final String floorTextureName; // int8_t[8]
        public final String ceilingTextureName; // int8_t[8]
        public final int lightLevel; // int16_t
        public final int specialType; // int16_t
        public final int tagNumber; // int16_t

        public Sector(int floorHeight, int ceilingHeight, String floorTextureName, String ceilingTextureName, int lightLevel, int specialType, int tagNumber) {
            this.floorHeight = floorHeight;
            this.ceilingHeight = ceilingHeight;
            this.floorTextureName = floorTextureName;
            this.ceilingTextureName = ceilingTextureName;
            this.lightLevel = lightLevel;
            this.specialType = specialType;
            this.tagNumber = tagNumber;
        }
        
    }
    
    public static List<Sector> sectors = new ArrayList<>();

    public static void extractSectors(ByteBuffer bb, int lumpIndex) {
        WADDirectory dir = directories.get(lumpIndex);
        System.out.println("extracting sectors " + dir);
        //dir.data.position(0);
        int totalLength = 0;
        bb.position((int) dir.offset);
        while (totalLength < dir.length) {
            int floorHeight = bb.getShort() & 0xffff; // int16_t
            int ceilingHeight = bb.getShort() & 0xffff; // int16_t
            byte[] floorTextureNameBytes = new byte[8];
            bb.get(floorTextureNameBytes);
            String floorTextureName = new String(floorTextureNameBytes); // int8_t[8]
            
            byte[] ceilingTextureNameBytes = new byte[8];
            bb.get(ceilingTextureNameBytes);
            String ceilingTextureName = new String(ceilingTextureNameBytes); // int8_t[8]
            int lightLevel = bb.getShort() & 0xffff; // int16_t
            int specialType = bb.getShort() & 0xffff; // int16_t
            int tagNumber = bb.getShort() & 0xffff; // int16_t            
        
            Sector sector = new Sector(floorHeight, ceilingHeight, floorTextureName, ceilingTextureName, lightLevel, specialType, tagNumber);
            sectors.add(sector);
            totalLength += 5 * 2 + 8 * 2;
        } 
    }
        
    
    // subsector
    // https://doomwiki.org/wiki/Subsector
    //    0	2	int16_t	Seg count
    //    2	2	int16_t	First seg number

    public static class Subsector {

        public final int segCount; // int16_t
        public final int firstSegNumber; // int16_t

        public Subsector(int segCount, int firstSegNumber) {
            this.segCount = segCount;
            this.firstSegNumber = firstSegNumber;
        }
        
    }
    
    public static List<Subsector> subsectors = new ArrayList<>();

    public static void extractSubsectors(ByteBuffer bb, int lumpIndex) {
        WADDirectory dir = directories.get(lumpIndex);
        System.out.println("extracting subsectors " + dir);
        //dir.data.position(0);
        int totalLength = 0;
        bb.position((int) dir.offset);
        while (totalLength < dir.length) {
            int segCount = bb.getShort() & 0xffff;
            int firstSegNumber = bb.getShort() & 0xffff;
            Subsector subsector = new Subsector(segCount, firstSegNumber);
            subsectors.add(subsector);
            totalLength += 2 * 2;
        } 
    }
    
    // Thing
    // https://doomwiki.org/wiki/Thing
    //    0	2	int16_t	x position
    //    2	2	int16_t	y position
    //    4	2	int16_t	Angle facing
    //    6	2	int16_t	DoomEd thing type
    //    8	2	int16_t	Flags


    public static class Thing {

        public final int xPosition; // int16_t
        public final int yPosition; // int16_t
        public final int angle; // int16_t
        public final int type; // int16_t
        public final int flags; // int16_t

        public Thing(int xPosition, int yPosition, int angle, int type, int flags) {
            this.xPosition = xPosition;
            this.yPosition = yPosition;
            this.angle = angle;
            this.type = type;
            this.flags = flags;
        }
        
    }
    
    public static List<Thing> things = new ArrayList<>();

    public static void extractThings(ByteBuffer bb, int lumpIndex) {
        WADDirectory dir = directories.get(lumpIndex);
        System.out.println("extracting things " + dir);
        //dir.data.position(0);
        int totalLength = 0;
        bb.position((int) dir.offset);
        while (totalLength < dir.length) {
            int xPosition = bb.getShort() & 0xffff; // int16_t
            int yPosition = bb.getShort() & 0xffff; // int16_t
            int angle = bb.getShort() & 0xffff; // int16_t
            int type = bb.getShort() & 0xffff; // int16_t
            int flags = bb.getShort() & 0xffff; // int16_t
            Thing thing = new Thing(xPosition, yPosition, angle, type, flags);
            things.add(thing);
            totalLength += 5 * 2;
        } 
    }
    
}
