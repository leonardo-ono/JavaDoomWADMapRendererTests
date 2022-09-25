# JavaDoomWADMapRendererTests

Test 001 (07/set/2022) - https://youtu.be/MpY0PICdcwM
- First experiments to try to render the Doom WAD maps using only standard libraries. 


Test 002 (11/set/2022) - https://youtu.be/PqnyeUTJ9So
- draw the upper and lower partial walls of the portals
- covered the floor and ceiling parts
- height of player (camera) updated according to the floor's height of current sector
- small test for the floor & ceiling renderer

note: currently it's still not using portals/clipping. it's drawing all the polygons returned by bsp from the furthest to the closest.


Test 003 (19/set/2022) - https://youtu.be/ogUHxQt_MMc
- changed the code to retrieve the walls from near to far using portals and cbuffer
- implemented visplane, but there are still some regions on the screen being filled in incorrectly
- walls are also practically ready, just need to replace the textures


Test 004 (25/set/2022) - https://youtu.be/-6mePgg7gXE
- extracted palette
- extracted colormap
- extracted pictures (patch)
- extracted textures
- rendering textured walls
- added some sector light effects
- diminishing lighting (i think it needs some tweaking)
- sky background (still not working correctly)

Note: I recently realized that visplane can actually be concave and my current routine for converting visplane vertical columns to horizontal spans only works with convex polygons, so there are still some regions in screen space that are not being filled in correctly.
