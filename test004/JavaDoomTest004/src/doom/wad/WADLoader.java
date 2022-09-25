package doom.wad;

import doom.wad.Textures.Texture;
import java.awt.Point;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * WADLoader class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class WADLoader {

    public static ByteBuffer wadData;
    
    public static List<Directory> lumps = new ArrayList<>();

    public static List<Point> vertexes = new ArrayList<>();
    public static List<Sector> sectors = new ArrayList<>();
    public static List<Linedef> linedefs = new ArrayList<>();
    public static List<Sidedef> sidedefs = new ArrayList<>();
    public static List<Seg> segs = new ArrayList<>();
    public static List<Node> nodes = new ArrayList<>();
    public static List<Subsector> subsectors = new ArrayList<>();
    public static List<Thing> things = new ArrayList<>();
    
    public static void load(String wadFile) throws Exception {
        try (
            InputStream is = new FileInputStream(wadFile);
        ) {
            wadData = ByteBuffer.wrap(is.readAllBytes());
            wadData.order(ByteOrder.LITTLE_ENDIAN);
            
            // wad id
            byte[] wadid = new byte[4];
            wadData.get(wadid);
            System.out.println("wadid: " + new String(wadid));
            
            // lumps size
            long lumpsSize = wadData.getInt() & 0xffffffffL;
            System.out.println("number of entries: " + lumpsSize);
            
            long directoryOffset = wadData.getInt() & 0xffffffffL;
            System.out.println("directory offset: " + directoryOffset);
            
            extractDirectories(wadData, directoryOffset, lumpsSize);
       
            // 0 PLAYPAL
            Directory playpalLump = getLumpByName("PLAYPAL");
            Palette.extractPalette(playpalLump);

            // 1 COLORMAP
            Directory colorMapLump = getLumpByName("COLORMAP");
            ColorMap.extractColorMap(colorMapLump);
            
            // 106 PNAMES
            Directory pnamesLump = getLumpByName("PNAMES");
            PNames.load(pnamesLump.data);
            
            // 105 TEXTURE1
            Directory texture1Lump = getLumpByName("TEXTURE1");
            Textures.load(texture1Lump);
        } 
        catch (IOException ex) {
            throw new Exception("Could not load WAD file properly !", ex);
        }
    }

    private static void extractDirectories(
                        ByteBuffer bb, long directoryOffset, long lumpsSize) {
        
        bb.position((int) directoryOffset);
        lumps.clear();
        for (int i = 0; i < lumpsSize; i++) {
            long offset = bb.getInt() & 0xffffffff;
            long length = bb.getInt() & 0xffffffff;
            byte[] nameBytes = new byte[8];
            bb.get(nameBytes);
            String name = convertBytesArrayToString(nameBytes);
            ByteBuffer data = bb.slice((int) offset, (int) length);
            data.order(ByteOrder.LITTLE_ENDIAN);
            data.position(0);
            Directory directory = new Directory(offset, length, name, data);
            lumps.add(directory);
        }
    }
    
    // refs.:
    // https://github.com/amroibrahim/DIYDoom/tree/master/DIYDOOM/Notes002/notes
    // https://doomwiki.org/wiki/Vertex
    private static void extractVertexes(Directory lump) {
        System.out.println("extracting vertexes " + lump);
        wadData.position((int) lump.offset);
        int totalLength = 0;
        while (totalLength < lump.length) {
            int x = wadData.getShort();
            int y = wadData.getShort();
            vertexes.add(new Point(x, y));
            totalLength += 2 * 2;
        } 
    }

    public static void extractLinedefs(Directory lump) {
        System.out.println("extracting linedefs " + lump);
        int totalLength = 0;
        ByteBuffer lumpData = lump.data;
        lumpData.position(0);
        while (totalLength < lump.length) {
            int startVertexNumber = lumpData.getShort() & 0xffff;
            int endVertxNumber = lumpData.getShort() & 0xffff;
            Point startVertex = vertexes.get(startVertexNumber);
            Point endVertex = vertexes.get(endVertxNumber);
            int flags = lumpData.getShort() & 0xffff;
            int specialType = lumpData.getShort() & 0xffff;
            int sectorTag = lumpData.getShort() & 0xffff;
            int frontSidedefNumber = lumpData.getShort() & 0xffff;
            int backSidedefNumber = lumpData.getShort() & 0xffff;
            Sidedef frontSidedef = sidedefs.get(frontSidedefNumber);
            Sidedef backSidedef = backSidedefNumber == 0xffff 
                                    ? null : sidedefs.get(backSidedefNumber);

            double length = Math.hypot(
                    endVertex.x - startVertex.x, endVertex.y - startVertex.y);
            
            if (frontSidedef != null) {
                if (frontSidedef.offsetY < 0) {
                    throw new RuntimeException("negative texture offset not implemented yet !");
                }

                int floorCeilingDif = Math.abs(frontSidedef.sector.ceilingHeight - frontSidedef.sector.floorHeight);
                if (frontSidedef.lowerTexture != null) {
                    frontSidedef.lowerTexture.resize((int) (length + frontSidedef.offsetX + 2), (int) (floorCeilingDif + frontSidedef.offsetY + 2));
                }
                if (frontSidedef.upperTexture != null) {
                    frontSidedef.upperTexture.resize((int) (length + frontSidedef.offsetX + 2), (int) (floorCeilingDif + frontSidedef.offsetY + 2));
                }
                if (frontSidedef.middleTexture != null) {
                    frontSidedef.middleTexture.resize((int) (length + frontSidedef.offsetX + 2), (int) (floorCeilingDif + frontSidedef.offsetY + 2));
                }
            }
            if (backSidedef != null) {
                if (backSidedef.offsetY < 0) {
                    throw new RuntimeException("negative texture offset not implemented yet !");
                }
                
                int floorCeilingDif = Math.abs(backSidedef.sector.ceilingHeight - backSidedef.sector.floorHeight);
                if (backSidedef.lowerTexture != null) {
                    backSidedef.lowerTexture.resize((int) (length + backSidedef.offsetX + 2), (int) (floorCeilingDif + backSidedef.offsetY + 2));
                }
                if (backSidedef.upperTexture != null) {
                    backSidedef.upperTexture.resize((int) (length + backSidedef.offsetX + 2), (int) (floorCeilingDif + backSidedef.offsetY + 2));
                }
                if (backSidedef.middleTexture != null) {
                    backSidedef.middleTexture.resize((int) (length + backSidedef.offsetX + 2), (int) (floorCeilingDif + backSidedef.offsetY + 2));
                }
            }
            
            linedefs.add(new Linedef(startVertex, endVertex, flags
                , specialType, sectorTag, frontSidedef, backSidedef, length));

            totalLength += 7 * 2;
        } 
    }

    public static void extractSidedefs(Directory lump) {
        System.out.println("extracting sidedefs " + lump);
        int totalLength = 0;
        ByteBuffer lumpData = lump.data;
        lumpData.position(0);
        while (totalLength < lump.length) {
            int offsetX = lumpData.getShort();
            int offsetY = lumpData.getShort();
            
            byte[] upperTextureNameBytes = new byte[8];
            lumpData.get(upperTextureNameBytes);
            String upperTextureName 
                        = convertBytesArrayToString(upperTextureNameBytes);
            
            byte[] lowerTextureNameBytes = new byte[8];
            lumpData.get(lowerTextureNameBytes);
            String lowerTextureName 
                        = convertBytesArrayToString(lowerTextureNameBytes);
            
            byte[] middleTextureNameBytes = new byte[8];
            lumpData.get(middleTextureNameBytes);
            String middleTextureName 
                        = convertBytesArrayToString(middleTextureNameBytes);
            
            Texture upperTexture = Textures.textures.get(upperTextureName);
            Texture lowerTexture = Textures.textures.get(lowerTextureName);
            Texture middleTexture = Textures.textures.get(middleTextureName);
            
            int sectorNumber = lumpData.getShort() & 0xffff;
            Sector sector = sectors.get(sectorNumber);
            sidedefs.add(new Sidedef(offsetX, offsetY, upperTextureName
                            , lowerTextureName, middleTextureName, sector
                                , upperTexture, lowerTexture, middleTexture));
            
            totalLength += 3 * 2 + 3 * 8;
        } 
    }

    public static void extractSegs(Directory lump) {
        System.out.println("extracting segs " + lump);
        int totalLength = 0;
        ByteBuffer lumpData = lump.data;
        lumpData.position(0);
        while (totalLength < lump.length) {
            int startVertexNumber = lumpData.getShort() & 0xffff;
            int endVertexNumber = lumpData.getShort() & 0xffff;
            int angle = lumpData.getShort();
            int linedefNumber = lumpData.getShort() & 0xffff;
            int direction = lumpData.getShort() & 0xffff;
            int offset = lumpData.getShort();

            Point startVertex = vertexes.get(startVertexNumber);
            Point endVertex = vertexes.get(endVertexNumber);
            double length = Math.hypot(
                    endVertex.x - startVertex.x, endVertex.y - startVertex.y);
            
            Linedef linedef = linedefs.get(linedefNumber);
            
            Sidedef frontSidedef = null;
            Sidedef backSidedef = null;
            boolean isPortal = false;
                    
            if (direction == 0) {
                frontSidedef = linedef.frontSidedef;
                if (linedef.backSidedef != null) {
                    backSidedef = linedef.backSidedef;
                    isPortal = true;
                }
            }
            else {
                frontSidedef = linedef.backSidedef;
                if (linedef.frontSidedef != null) {
                    backSidedef = linedef.frontSidedef;
                    isPortal = true;
                }
            }
            Sector frontSector = frontSidedef.sector;
            Sector backSector = backSidedef != null ? backSidedef.sector : null;
            Seg seg = new Seg(startVertex, endVertex, angle, linedef
                        , frontSidedef, backSidedef, isPortal, offset, length
                            , frontSector, backSector);
            
            segs.add(seg);
            totalLength += 6 * 2;
        } 
    }

    public static void extractNodes(Directory lump) {
        System.out.println("extracting nodes " + lump);
        int totalLength = 0;
        ByteBuffer lumpData = lump.data;
        lumpData.position(0);
        while (totalLength < lump.length) {
            int partitionX = lumpData.getShort();
            int partitionY = lumpData.getShort();
            int changePartitionX = lumpData.getShort();
            int changePartitionY = lumpData.getShort();
            int rightBoxTop = lumpData.getShort();
            int rightBoxBottom = lumpData.getShort();
            int rightBoxLeft = lumpData.getShort();
            int rightBoxRight = lumpData.getShort();
            int leftBoxTop = lumpData.getShort();
            int leftBoxBottom = lumpData.getShort();
            int leftBoxLeft = lumpData.getShort();
            int leftBoxRight = lumpData.getShort();
            int rightChildId = lumpData.getShort();
            int leftChildId = lumpData.getShort();            
            Node node = new Node(partitionX, partitionY, changePartitionX
                            , changePartitionY, rightBoxTop, rightBoxBottom
                                , rightBoxLeft, rightBoxRight, leftBoxTop
                                    , leftBoxBottom, leftBoxLeft, leftBoxRight
                                        , rightChildId, leftChildId);
            
            nodes.add(node);
            totalLength += 14 * 2;
        } 
    }

    public static void extractSectors(Directory lump) {
        System.out.println("extracting sectors " + lump);
        int totalLength = 0;
        ByteBuffer lumpData = lump.data;
        lumpData.position(0);
        while (totalLength < lump.length) {
            int floorHeight = lumpData.getShort();
            int ceilingHeight = lumpData.getShort();
            
            byte[] floorTextureNameBytes = new byte[8];
            lumpData.get(floorTextureNameBytes);
            String floorTextureName 
                    = convertBytesArrayToString(floorTextureNameBytes);
            
            byte[] ceilingTextureNameBytes = new byte[8];
            lumpData.get(ceilingTextureNameBytes);
            String ceilingTextureName 
                    = convertBytesArrayToString(ceilingTextureNameBytes);
            
            int lightLevel = lumpData.getShort() & 0xff;
            int specialType = lumpData.getShort();
            int tagNumber = lumpData.getShort();

            // TODO: hardcoded for now ?
            // animated floor texture
            Flat floorFlat = null;
            if (floorTextureName.startsWith("NUKAGE")) {
                Directory floorFlat1Lump = getLumpByName("NUKAGE1");
                Directory floorFlat2Lump = getLumpByName("NUKAGE2");
                Directory floorFlat3Lump = getLumpByName("NUKAGE3");
                floorFlat = new AnimatedFlat(floorFlat1Lump, floorFlat2Lump, floorFlat3Lump);
            }
            else {
                Directory floorFlatLump = getLumpByName(floorTextureName);
                floorFlat = new Flat(floorFlatLump);
            }
            Directory ceilingFlatLump = getLumpByName(ceilingTextureName);
            Flat ceilingFlat = new Flat(ceilingFlatLump);
            Picture ceilingPicture = null;
            if (ceilingTextureName.contains("F_SKY")) {
                String picName = ceilingTextureName.replace("F_", "");
                Directory skyLump = getLumpByName(picName);
                ceilingPicture = new Picture(skyLump);
            }
            Sector sector = new Sector(floorHeight, ceilingHeight
                            , floorTextureName, ceilingTextureName
                                , lightLevel, specialType, tagNumber
                                    , floorFlat, ceilingFlat, ceilingPicture);
            
            sectors.add(sector);
            totalLength += 5 * 2 + 8 * 2;
        } 
    }

    public static void extractSubsectors(Directory lump) {
        System.out.println("extracting subsectors " + lump);
        int totalLength = 0;
        ByteBuffer lumpData = lump.data;
        lumpData.position(0);
        while (totalLength < lump.length) {
            int segCount = lumpData.getShort() & 0xffff;
            int firstSegNumber = lumpData.getShort() & 0xffff;
            Subsector subsector = new Subsector(segCount, firstSegNumber);
            
            int startIndex = subsector.firstSegNumber;
            int endIndex = subsector.firstSegNumber + subsector.segCount;
            for (int i = startIndex; i < endIndex; i++) {
                Seg seg = WADLoader.segs.get(i);
                subsector.segs.add(seg);
            }
            
            subsectors.add(subsector);
            totalLength += 2 * 2;
        } 
    }

    public static void extractThings(Directory lump) {
        System.out.println("extracting things " + lump);
        int totalLength = 0;
        ByteBuffer lumpData = lump.data;
        lumpData.position(0);
        while (totalLength < lump.length) {
            int positionX = lumpData.getShort();
            int positionY = lumpData.getShort();
            int angle = lumpData.getShort();
            int type = lumpData.getShort();
            int flags = lumpData.getShort();
            Thing thing = new Thing(positionX, positionY, angle, type, flags);
            things.add(thing);
            totalLength += 5 * 2;
        } 
    }
    
    public static String convertBytesArrayToString(byte[] bytes) {
        String stringTmp = new String(bytes);
        int nullCharIndex = stringTmp.indexOf('\0');
        if (nullCharIndex >= 0) {
            stringTmp = stringTmp.substring(0, nullCharIndex);
        }
        return  stringTmp;        
    }
    
    public static Directory getLumpByName(String lumpName) {
        for (Directory lump : lumps) {
            if (lump.name.equals(lumpName)) {
                return lump;
            }
        }
        return null;
    }

    public static Directory getLumpByName(String tagName, String lumpName) {
        for (int i = 0; i < lumps.size(); i++) {
            Directory tag = lumps.get(i);
            if (tag.name.equals(tagName)) {
                for (int i2 = i; i2 < lumps.size(); i2++) {
                    Directory lump = lumps.get(i2);
                    if (lump.name.equals(lumpName)) {
                        return lump;
                    }
                    
                }
            }
        }
        return null;
    }
    
    public static void loadMap(String mapName) {
        vertexes.clear();
        sectors.clear();
        sidedefs.clear();
        linedefs.clear();
        segs.clear();
        nodes.clear();
        subsectors.clear();
        things.clear();
        extractVertexes(getLumpByName(mapName, "VERTEXES"));
        extractSectors(getLumpByName(mapName, "SECTORS"));
        extractSidedefs(getLumpByName(mapName, "SIDEDEFS"));
        extractLinedefs(getLumpByName(mapName, "LINEDEFS"));
        extractSegs(getLumpByName(mapName, "SEGS"));
        extractNodes(getLumpByName(mapName, "NODES"));
        extractSubsectors(getLumpByName(mapName, "SSECTORS"));
        extractThings(getLumpByName(mapName, "THINGS"));
        
        // TODO fix later with better solution
        // workaround: remove opacity from textures 
        for (Sidedef sidedef : sidedefs) {
            if (sidedef.lowerTexture != null) sidedef.lowerTexture.removeOpacity();
            if (sidedef.upperTexture != null) sidedef.upperTexture.removeOpacity();
            if (sidedef.middleTexture != null) sidedef.middleTexture.removeOpacity();
        }
        
        // TODO sky hack
        // https://doomwiki.org/wiki/Sky_hack
        for (Linedef linedef : linedefs) {
            if ((linedef.frontSidedef != null && linedef.frontSidedef.sector.ceilingPic != null)
                    && (linedef.backSidedef != null && linedef.backSidedef.sector.ceilingPic != null)) {
                
                linedef.frontSidedef.setUseSkyHack(true);
            }
        }
    }
    
}
