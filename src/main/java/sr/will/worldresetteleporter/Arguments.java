package sr.will.worldresetteleporter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Arguments {
    File playerDataFolder;
    int x;
    int y;
    int z;
    int spawnDimension;
    List<Integer> targetDimensions = new ArrayList<>();
    boolean dryRun;
}
