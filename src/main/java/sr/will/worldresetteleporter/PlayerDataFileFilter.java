package sr.will.worldresetteleporter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.UUID;

@SuppressWarnings("unused")
public class PlayerDataFileFilter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String name) {
        if (!name.endsWith(".dat")) return false;

        try {
            UUID.fromString(name.split("\\.")[0]);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
