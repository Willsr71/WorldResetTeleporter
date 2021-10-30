# World Reset Teleporter

This program will teleport all players in specified dimensions to a different dimension. This is useful for dimensional resets where you delete or replace the world.

## Usage
To use this application, you just have to download the jar from the releases page and run it.

The program automatically makes a backup of any player data it modifies. These can be found in playerdata/teleporter_backups.


The server must be offline for this program to work. If it is not, corrupted player data may result.

### Examples
Teleport all players in the nether and end to the coordinates 467,200,-55 in the Overworld:
```
teleporter.jar -f "world/playerdata" -c 467,200,-55 -d -1,1
```

Teleport all players in all three dimensions to the center of the End.
```
teleporter.jar -f "world/playerdata" -c 0,100,0 -d -1,1,0 -spawnDim 1
```

### Arguments
|Name|Required|Description|
|---|---|---|
|-f --playerdataFolder|**Yes**|The location of the player data folder. This is usually located inside the world folder of the server|
|-c --coords|**Yes**|Coordinates where the players will be teleported. Seperated by spaces|
|-d --targetDimensions|**Yes**|Which dimension(s) players should be teleported from. If specifying multiple dimensions, seperate by commas|
|-spawnDim|No|What dimension players will be teleported to. The default is 0 (Overworld)|
|-dryRun|No|Gives output of what the program *would* do. Does not write anything to disk|