package sr.will.worldresetteleporter;

import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.DoubleTag;
import net.querz.nbt.tag.ListTag;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Main {
    public static final Logger logger = LoggerFactory.getLogger("Teleporter");

    public static void main(String[] args) {
        Arguments arguments = getArgs(args);

        logger.info("folder name: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")));
        File backupsFolder = new File(new File(arguments.playerDataFolder, "teleporter_backups"), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")));
        backupsFolder.mkdirs();

        logger.info("Teleporting all players in dimensions {} to {}, {}, {} in dimension {}", arguments.targetDimensions, arguments.x, arguments.y, arguments.z, arguments.spawnDimension);

        File[] playerDataFiles = arguments.playerDataFolder.listFiles(new PlayerDataFileFilter());
        logger.info("Found {} playerdata files, teleporting...", Objects.requireNonNull(playerDataFiles).length);

        int teleported = 0;
        for (File dataFile : playerDataFiles) {
            if (teleportPlayer(dataFile, backupsFolder, arguments)) teleported++;
        }

        logger.info("Teleported {} players", teleported);
    }

    @SuppressWarnings("unchecked")
    public static boolean teleportPlayer(File playerDataFile, File backupsFolder, Arguments arguments) {
        NamedTag playerData;
        try {
            playerData = NBTUtil.read(playerDataFile);
        } catch (IOException e) {
            logger.error("Unable to read player data {}: {}", playerDataFile.getName(), e.getMessage());
            return false;
        }

        CompoundTag data = (CompoundTag) playerData.getTag();

        // Don't teleport if the player is not in a target dimension
        if (!arguments.targetDimensions.contains(data.getInt("Dimension"))) return false;

        // Set the player dimension and coordinates
        data.getIntTag("Dimension").setValue(arguments.spawnDimension);
        ListTag<DoubleTag> position = (ListTag<DoubleTag>) data.getListTag("Pos");
        position.get(0).setValue(arguments.x);
        position.get(1).setValue(arguments.y);
        position.get(2).setValue(arguments.z);

        // Set the fall distance to 0 so they don't die immediately if they were flying
        data.getFloatTag("FallDistance").setValue(0f);

        // Set the motion to stationary
        ListTag<DoubleTag> motion = (ListTag<DoubleTag>) data.getListTag("Motion");
        motion.get(0).setValue(0);
        motion.get(1).setValue(0);
        motion.get(2).setValue(0);

        // Stop here if this is a dry run
        if (arguments.dryRun) return true;

        try {
            FileUtils.copyFile(playerDataFile, new File(backupsFolder, playerDataFile.getName()));
        } catch (IOException e) {
            logger.error("Failed to make player data backup, skipping file {}: {}", playerDataFile.getName(), e.getMessage());
            return false;
        }

        try {
            NBTUtil.write(playerData, playerDataFile);
        } catch (IOException e) {
            logger.error("Failed to write NBT data for file {}: {}", playerDataFile.getName(), e.getMessage());
            return false;
        }

        return true;
    }

    public static Arguments getArgs(String[] args) {
        CommandLine cmd = parseArgs(args);
        Arguments arguments = new Arguments();

        // Player data folder
        arguments.playerDataFolder = new File(cmd.getOptionValue("f"));
        if (!arguments.playerDataFolder.exists()) {
            logger.info("Player data folder \"{}\" does not exist", cmd.getOptionValue("f"));
            System.exit(1);
        }

        // Spawn coordinates
        String[] coords = cmd.getOptionValues("c");
        arguments.x = getIntOption(coords[0], "x");
        arguments.y = getIntOption(coords[1], "y");
        arguments.z = getIntOption(coords[2], "z");

        // Target dimensions
        for (String value : cmd.getOptionValues("d")) {
            arguments.targetDimensions.add(getIntOption(value, "dimension"));
        }

        // Spawn dimension
        arguments.spawnDimension = getIntOption(cmd.getOptionValue("spawnDim", "0"), "spawnDim");
        
        arguments.dryRun = cmd.hasOption("dryRun");

        // Dimension warning
        if (arguments.targetDimensions.contains(arguments.spawnDimension)) {
            if (cmd.hasOption("yesTeleportPlayersInSpawnDimension")) {
                logger.warn("You are teleporting all players in the spawn dimension");
            } else {
                logger.error("You are trying to teleport players in the spawn dimension. If you are sure you want to do this, use the -yesTeleportPlayersInSpawnDimension option");
                System.exit(1);
            }
        }

        return arguments;
    }

    public static int getIntOption(String value, String option) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.error("Invalid value \"{}\" for option {}, must be a number", value, option);
            System.exit(1);
            return 0;
        }
    }

    public static CommandLine parseArgs(String[] args) {
        Options options = new Options();

        options.addOption(Option.builder("f")
                .longOpt("playerDataFolder")
                .desc("Player data folder location. Typically world/playerdata")
                .hasArg()
                .required()
                .build());

        options.addOption(Option.builder("c").longOpt("coords").numberOfArgs(3).required().build());

        options.addOption(Option.builder("d")
                .longOpt("targetDimension")
                .longOpt("targetDimensions")
                .desc("The dimension(s) we should teleport from")
                .valueSeparator(',')
                .required()
                .hasArg()
                .numberOfArgs(Option.UNLIMITED_VALUES)
                .build());

        options.addOption(Option.builder("spawnDim")
                .longOpt("spawnDimension")
                .desc("The spawn dimension ID. Default is 0 (Overworld)")
                .hasArg()
                .build());

        options.addOption(Option.builder("heal").desc("Should we heal players when they are teleported").build());
        options.addOption(Option.builder("dryRun").desc("Run without editing player data").build());
        options.addOption(Option.builder("yesTeleportPlayersInSpawnDimension").build());

        CommandLineParser parser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();

        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            helpFormatter.printHelp("Teleporter -f <playerDataFolder> -c <x> <y> <z> -d <targetDimensions>", options);
            System.exit(1);
            return null;
        }
    }
}
