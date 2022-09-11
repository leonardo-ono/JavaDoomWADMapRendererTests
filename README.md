# JavaDoomWADMapRendererTests

Test 001 - https://youtu.be/MpY0PICdcwM
First experiments to try to render the Doom WAD maps using only standard libraries. 

Test 002 (11/set/2022) - https://youtu.be/PqnyeUTJ9So
- draw the upper and lower partial walls of the portals
- covered the floor and ceiling parts
- height of player (camera) updated according to the floor's height of current subsector
- small test for the floor & ceiling renderer
note: currently it's still not using portals/clipping. it's drawing all the polygons returned by bsp from the furthest to the closest.

