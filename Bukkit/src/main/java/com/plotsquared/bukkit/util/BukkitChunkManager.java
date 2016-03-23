package com.plotsquared.bukkit.util;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.AugmentedUtils;
import com.intellectualcrafters.plot.object.BlockLoc;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotLoc;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.PlotChunk;
import com.intellectualcrafters.plot.util.SetQueue;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.plotsquared.bukkit.object.entity.EntityWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.block.Jukebox;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class BukkitChunkManager extends ChunkManager {

    private static HashMap<BlockLoc, ItemStack[]> chestContents;
    private static HashMap<BlockLoc, ItemStack[]> furnaceContents;
    private static HashMap<BlockLoc, ItemStack[]> dispenserContents;
    private static HashMap<BlockLoc, ItemStack[]> dropperContents;
    private static HashMap<BlockLoc, ItemStack[]> brewingStandContents;
    private static HashMap<BlockLoc, ItemStack[]> beaconContents;
    private static HashMap<BlockLoc, ItemStack[]> hopperContents;
    private static HashMap<BlockLoc, Short[]> furnaceTime;
    private static HashMap<BlockLoc, Object[]> skullData;
    private static HashMap<BlockLoc, Material> jukeboxDisc;
    private static HashMap<BlockLoc, Short> brewTime;
    private static HashMap<BlockLoc, EntityType> spawnerData;
    private static HashMap<BlockLoc, String> cmdData;
    private static HashMap<BlockLoc, String[]> signContents;
    private static HashMap<BlockLoc, Note> noteBlockContents;
    private static HashMap<BlockLoc, List<Pattern>> bannerPatterns;
    private static HashMap<BlockLoc, DyeColor> bannerBase;
    private static HashSet<EntityWrapper> entities;
    private static HashMap<PlotLoc, PlotBlock[]> allBlocks;

    public static void initMaps() {
        chestContents = new HashMap<>();
        furnaceContents = new HashMap<>();
        dispenserContents = new HashMap<>();
        dropperContents = new HashMap<>();
        brewingStandContents = new HashMap<>();
        beaconContents = new HashMap<>();
        hopperContents = new HashMap<>();
        furnaceTime = new HashMap<>();
        skullData = new HashMap<>();
        brewTime = new HashMap<>();
        jukeboxDisc = new HashMap<>();
        spawnerData = new HashMap<>();
        noteBlockContents = new HashMap<>();
        signContents = new HashMap<>();
        cmdData = new HashMap<>();
        bannerBase = new HashMap<>();
        bannerPatterns = new HashMap<>();
        entities = new HashSet<>();
        allBlocks = new HashMap<>();
    }

    public static boolean isIn(RegionWrapper region, int x, int z) {
        return x >= region.minX && x <= region.maxX && z >= region.minZ && z <= region.maxZ;
    }

    public static void saveEntitiesOut(Chunk chunk, RegionWrapper region) {
        for (Entity entity : chunk.getEntities()) {
            Location loc = BukkitUtil.getLocation(entity);
            int x = loc.getX();
            int z = loc.getZ();
            if (isIn(region, x, z)) {
                continue;
            }
            if (entity.getVehicle() != null) {
                continue;
            }
            EntityWrapper wrap = new EntityWrapper(entity, (short) 2);
            entities.add(wrap);
        }
    }

    public static void saveEntitiesIn(Chunk chunk, RegionWrapper region) {
        saveEntitiesIn(chunk, region, 0, 0, false);
    }

    public static void saveEntitiesIn(Chunk chunk, RegionWrapper region, int offsetX, int offsetZ, boolean delete) {
        for (Entity entity : chunk.getEntities()) {
            Location loc = BukkitUtil.getLocation(entity);
            int x = loc.getX();
            int z = loc.getZ();
            if (!isIn(region, x, z)) {
                continue;
            }
            if (entity.getVehicle() != null) {
                continue;
            }
            EntityWrapper wrap = new EntityWrapper(entity, (short) 2);
            wrap.x += offsetX;
            wrap.z += offsetZ;
            entities.add(wrap);
            if (delete) {
                if (!(entity instanceof Player)) {
                    entity.remove();
                }
            }
        }
    }

    public static void restoreEntities(World world, int xOffset, int zOffset) {
        for (EntityWrapper entity : entities) {
            try {
                entity.spawn(world, xOffset, zOffset);
            } catch (Exception e) {
                PS.debug("Failed to restore entity (e): " + entity.x + "," + entity.y + "," + entity.z + " : " + entity.type);
                e.printStackTrace();
            }
        }
        entities.clear();
    }

    public static void restoreBlocks(World world, int xOffset, int zOffset) {
        for (Entry<BlockLoc, ItemStack[]> blockLocEntry : chestContents.entrySet()) {
            try {
                Block block =
                        world.getBlockAt(blockLocEntry.getKey().x + xOffset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + zOffset);
                BlockState state = block.getState();
                if (state instanceof Chest) {
                    InventoryHolder chest = (InventoryHolder) state;
                    chest.getInventory().setContents(blockLocEntry.getValue());
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to regenerate chest: " + (blockLocEntry.getKey().x + xOffset) + "," + blockLocEntry
                            .getKey().y + "," + (blockLocEntry.getKey().z + zOffset));
                }
            } catch (IllegalArgumentException e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate chest (e): " + (blockLocEntry.getKey().x + xOffset) + "," + blockLocEntry
                        .getKey().y + "," + (blockLocEntry.getKey().z + zOffset));
            }
        }
        for (Entry<BlockLoc, String[]> blockLocEntry : signContents.entrySet()) {
            try {
                Block block =
                        world.getBlockAt(blockLocEntry.getKey().x + xOffset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + zOffset);
                BlockState state = block.getState();
                if (state instanceof Sign) {
                    Sign sign = (Sign) state;
                    int i = 0;
                    for (String line : blockLocEntry.getValue()) {
                        sign.setLine(i, line);
                        i++;
                    }
                    state.update(true);
                } else {
                    PS.debug(
                            "&c[WARN] Plot clear failed to regenerate sign: " + (blockLocEntry.getKey().x + xOffset) + "," + blockLocEntry.getKey().y
                                    + "," + (
                                    blockLocEntry.getKey().z + zOffset));
                }
            } catch (IndexOutOfBoundsException e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate sign: " + (blockLocEntry.getKey().x + xOffset) + "," + blockLocEntry.getKey().y
                        + "," + (
                        blockLocEntry.getKey().z + zOffset));
            }
        }
        for (Entry<BlockLoc, ItemStack[]> blockLocEntry : dispenserContents.entrySet()) {
            try {
                Block block =
                        world.getBlockAt(blockLocEntry.getKey().x + xOffset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + zOffset);
                BlockState state = block.getState();
                if (state instanceof Dispenser) {
                    ((InventoryHolder) state).getInventory().setContents(blockLocEntry.getValue());
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to regenerate dispenser: " + (blockLocEntry.getKey().x + xOffset) + "," + blockLocEntry
                            .getKey().y + "," + (blockLocEntry.getKey().z + zOffset));
                }
            } catch (IllegalArgumentException e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate dispenser (e): " + (blockLocEntry.getKey().x + xOffset) + "," + blockLocEntry
                        .getKey().y + "," + (blockLocEntry.getKey().z + zOffset));
            }
        }
        for (Entry<BlockLoc, ItemStack[]> blockLocEntry : dropperContents.entrySet()) {
            try {
                Block block =
                        world.getBlockAt(blockLocEntry.getKey().x + xOffset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + zOffset);
                BlockState state = block.getState();
                if (state instanceof Dropper) {
                    ((InventoryHolder) state).getInventory().setContents(blockLocEntry.getValue());
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to regenerate dispenser: " + (blockLocEntry.getKey().x + xOffset) + "," + blockLocEntry
                            .getKey().y + "," + (blockLocEntry.getKey().z + zOffset));
                }
            } catch (IllegalArgumentException e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate dispenser (e): " + (blockLocEntry.getKey().x + xOffset) + "," + blockLocEntry
                        .getKey().y + "," + (blockLocEntry.getKey().z + zOffset));
            }
        }
        for (Entry<BlockLoc, ItemStack[]> blockLocEntry : beaconContents.entrySet()) {
            try {
                Block block =
                        world.getBlockAt(blockLocEntry.getKey().x + xOffset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + zOffset);
                BlockState state = block.getState();
                if (state instanceof Beacon) {
                    ((InventoryHolder) state).getInventory().setContents(blockLocEntry.getValue());
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to regenerate beacon: " + (blockLocEntry.getKey().x + xOffset) + "," + blockLocEntry
                            .getKey().y + "," + (blockLocEntry.getKey().z + zOffset));
                }
            } catch (IllegalArgumentException e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate beacon (e): " + (blockLocEntry.getKey().x + xOffset) + "," + blockLocEntry
                        .getKey().y + "," + (blockLocEntry.getKey().z + zOffset));
            }
        }
        for (Entry<BlockLoc, Material> blockLocMaterialEntry : jukeboxDisc.entrySet()) {
            try {
                Block block =
                        world.getBlockAt(blockLocMaterialEntry.getKey().x + xOffset, blockLocMaterialEntry.getKey().y, blockLocMaterialEntry
                                .getKey().z + zOffset);
                BlockState state = block.getState();
                if (state instanceof Jukebox) {
                    ((Jukebox) state).setPlaying(blockLocMaterialEntry.getValue());
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to restore jukebox: " + (blockLocMaterialEntry.getKey().x + xOffset) + ","
                            + blockLocMaterialEntry
                            .getKey().y + "," + (
                            blockLocMaterialEntry.getKey().z + zOffset));
                }
            } catch (Exception e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate jukebox (e): " + (blockLocMaterialEntry.getKey().x + xOffset) + ","
                        + blockLocMaterialEntry
                        .getKey().y + "," + (
                        blockLocMaterialEntry.getKey().z + zOffset));
            }
        }
        for (Entry<BlockLoc, Object[]> blockLocEntry : skullData.entrySet()) {
            try {
                Block block =
                        world.getBlockAt(blockLocEntry.getKey().x + xOffset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + zOffset);
                BlockState state = block.getState();
                if (state instanceof Skull) {
                    Object[] data = blockLocEntry.getValue();
                    if (data[0] != null) {
                        ((Skull) state).setOwner((String) data[0]);
                    }
                    if ((Integer) data[1] != 0) {
                        ((Skull) state).setRotation(BlockFace.values()[(int) data[1]]);
                    }
                    if ((Integer) data[2] != 0) {
                        ((Skull) state).setSkullType(SkullType.values()[(int) data[2]]);
                    }
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to restore skull: " + (blockLocEntry.getKey().x + xOffset) + "," + blockLocEntry.getKey().y
                            + "," + (
                            blockLocEntry.getKey().z + zOffset));
                }
            } catch (Exception e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate skull (e): " + (blockLocEntry.getKey().x + xOffset) + "," + blockLocEntry
                        .getKey().y + "," + (blockLocEntry.getKey().z + zOffset));
            }
        }
        for (Entry<BlockLoc, ItemStack[]> blockLocEntry : hopperContents.entrySet()) {
            try {
                Block block =
                        world.getBlockAt(blockLocEntry.getKey().x + xOffset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + zOffset);
                BlockState state = block.getState();
                if (state instanceof Hopper) {
                    ((InventoryHolder) state).getInventory().setContents(blockLocEntry.getValue());
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to regenerate hopper: " + (blockLocEntry.getKey().x + xOffset) + "," + blockLocEntry
                            .getKey().y + "," + (blockLocEntry.getKey().z + zOffset));
                }
            } catch (IllegalArgumentException e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate hopper (e): " + (blockLocEntry.getKey().x + xOffset) + "," + blockLocEntry
                        .getKey().y + "," + (blockLocEntry.getKey().z + zOffset));
            }
        }
        for (Entry<BlockLoc, Note> blockLocNoteEntry : noteBlockContents.entrySet()) {
            try {
                Block block = world.getBlockAt(
                        blockLocNoteEntry.getKey().x + xOffset, blockLocNoteEntry.getKey().y, blockLocNoteEntry.getKey().z + zOffset);
                BlockState state = block.getState();
                if (state instanceof NoteBlock) {
                    ((NoteBlock) state).setNote(blockLocNoteEntry.getValue());
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to regenerate note block: " + (blockLocNoteEntry.getKey().x + xOffset) + ","
                            + blockLocNoteEntry
                            .getKey().y + "," + (
                            blockLocNoteEntry.getKey().z + zOffset));
                }
            } catch (Exception e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate note block (e): " + (blockLocNoteEntry.getKey().x + xOffset) + ","
                        + blockLocNoteEntry
                        .getKey().y + "," + (
                        blockLocNoteEntry.getKey().z + zOffset));
            }
        }
        for (Entry<BlockLoc, Short> blockLocShortEntry : brewTime.entrySet()) {
            try {
                Block block = world.getBlockAt(
                        blockLocShortEntry.getKey().x + xOffset, blockLocShortEntry.getKey().y, blockLocShortEntry.getKey().z + zOffset);
                BlockState state = block.getState();
                if (state instanceof BrewingStand) {
                    ((BrewingStand) state).setBrewingTime(blockLocShortEntry.getValue());
                } else {
                    PS.debug("&c[WARN] Plot clear failed to restore brewing stand cooking: " + (blockLocShortEntry.getKey().x + xOffset) + ","
                            + blockLocShortEntry
                            .getKey().y + "," + (
                            blockLocShortEntry.getKey().z + zOffset));
                }
            } catch (Exception e) {
                PS.debug("&c[WARN] Plot clear failed to restore brewing stand cooking (e): " + (blockLocShortEntry.getKey().x + xOffset) + "," +
                        blockLocShortEntry
                                .getKey().y + "," + (
                        blockLocShortEntry.getKey().z + zOffset));
            }
        }
        for (Entry<BlockLoc, EntityType> blockLocEntityTypeEntry : spawnerData.entrySet()) {
            try {
                Block block =
                        world.getBlockAt(blockLocEntityTypeEntry.getKey().x + xOffset, blockLocEntityTypeEntry.getKey().y, blockLocEntityTypeEntry
                                .getKey().z + zOffset);
                BlockState state = block.getState();
                if (state instanceof CreatureSpawner) {
                    ((CreatureSpawner) state).setSpawnedType(blockLocEntityTypeEntry.getValue());
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to restore spawner type: " + (blockLocEntityTypeEntry.getKey().x + xOffset) + ","
                            + blockLocEntityTypeEntry
                            .getKey().y + "," + (
                            blockLocEntityTypeEntry.getKey().z + zOffset));
                }
            } catch (Exception e) {
                PS.debug("&c[WARN] Plot clear failed to restore spawner type (e): " + (blockLocEntityTypeEntry.getKey().x + xOffset) + "," +
                        blockLocEntityTypeEntry.getKey().y + "," + (blockLocEntityTypeEntry.getKey().z + zOffset));
            }
        }
        for (Entry<BlockLoc, String> blockLocStringEntry : cmdData.entrySet()) {
            try {
                Block block = world.getBlockAt(
                        blockLocStringEntry.getKey().x + xOffset, blockLocStringEntry.getKey().y, blockLocStringEntry.getKey().z + zOffset);
                BlockState state = block.getState();
                if (state instanceof CommandBlock) {
                    ((CommandBlock) state).setCommand(blockLocStringEntry.getValue());
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to restore command block: " + (blockLocStringEntry.getKey().x + xOffset) + ","
                            + blockLocStringEntry
                            .getKey().y + "," + (
                            blockLocStringEntry.getKey().z + zOffset));
                }
            } catch (Exception e) {
                PS.debug("&c[WARN] Plot clear failed to restore command block (e): " + (blockLocStringEntry.getKey().x + xOffset) + ","
                        + blockLocStringEntry
                        .getKey().y + "," + (
                        blockLocStringEntry.getKey().z + zOffset));
            }
        }
        for (Entry<BlockLoc, ItemStack[]> blockLocEntry : brewingStandContents.entrySet()) {
            try {
                Block block =
                        world.getBlockAt(blockLocEntry.getKey().x + xOffset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + zOffset);
                BlockState state = block.getState();
                if (state instanceof BrewingStand) {
                    ((InventoryHolder) state).getInventory().setContents(blockLocEntry.getValue());
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to regenerate brewing stand: " + (blockLocEntry.getKey().x + xOffset) + "," + blockLocEntry
                            .getKey().y + "," + (
                            blockLocEntry.getKey().z
                                    + zOffset));
                }
            } catch (IllegalArgumentException e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate brewing stand (e): " + (blockLocEntry.getKey().x + xOffset) + "," + blockLocEntry
                        .getKey().y + "," + (
                        blockLocEntry.getKey().z
                                + zOffset));
            }
        }
        for (Entry<BlockLoc, Short[]> blockLocEntry : furnaceTime.entrySet()) {
            try {
                Block block =
                        world.getBlockAt(blockLocEntry.getKey().x + xOffset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + zOffset);
                BlockState state = block.getState();
                if (state instanceof Furnace) {
                    Short[] time = blockLocEntry.getValue();
                    ((Furnace) state).setBurnTime(time[0]);
                    ((Furnace) state).setCookTime(time[1]);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to restore furnace cooking: " + (blockLocEntry.getKey().x + xOffset) + "," + blockLocEntry
                            .getKey().y + "," + (
                            blockLocEntry.getKey().z + zOffset));
                }
            } catch (Exception e) {
                PS.debug("&c[WARN] Plot clear failed to restore furnace cooking (e): " + (blockLocEntry.getKey().x + xOffset) + "," + blockLocEntry
                        .getKey().y + "," + (
                        blockLocEntry.getKey().z + zOffset));
            }
        }
        for (Entry<BlockLoc, ItemStack[]> blockLocEntry : furnaceContents.entrySet()) {
            try {
                Block block =
                        world.getBlockAt(blockLocEntry.getKey().x + xOffset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + zOffset);
                BlockState state = block.getState();
                if (state instanceof Furnace) {
                    ((InventoryHolder) state).getInventory().setContents(blockLocEntry.getValue());
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to regenerate furnace: " + (blockLocEntry.getKey().x + xOffset) + "," + blockLocEntry
                            .getKey().y + "," + (blockLocEntry.getKey().z + zOffset));
                }
            } catch (IllegalArgumentException e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate furnace (e): " + (blockLocEntry.getKey().x + xOffset) + "," + blockLocEntry
                        .getKey().y + "," + (blockLocEntry.getKey().z + zOffset));
            }
        }
        for (Entry<BlockLoc, DyeColor> blockLocByteEntry : bannerBase.entrySet()) {
            try {
                Block block = world.getBlockAt(
                        blockLocByteEntry.getKey().x + xOffset, blockLocByteEntry.getKey().y, blockLocByteEntry.getKey().z + zOffset);
                BlockState state = block.getState();
                if (state instanceof Banner) {
                    Banner banner = (Banner) state;
                    DyeColor base = blockLocByteEntry.getValue();
                    List<Pattern> patterns = bannerPatterns.get(blockLocByteEntry.getKey());
                    banner.setBaseColor(base);
                    banner.setPatterns(patterns);
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to regenerate banner: " + (blockLocByteEntry.getKey().x + xOffset) + "," + blockLocByteEntry
                            .getKey().y + "," + (
                            blockLocByteEntry.getKey().z + zOffset));
                }
            } catch (Exception e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate banner (e): " + (blockLocByteEntry.getKey().x + xOffset) + "," + blockLocByteEntry
                        .getKey().y + "," + (
                        blockLocByteEntry.getKey().z + zOffset));
            }
        }
    }

    public static void saveBlocks(World world, int maxY, int x, int z, int offsetX, int offsetZ,
            boolean storeNormal) {
        maxY = Math.min(255, maxY);
        PlotBlock[] ids;
        if (storeNormal) {
            ids = new PlotBlock[maxY + 1];
        } else {
            ids = null;
        }
        for (short y = 0; y <= maxY; y++) {
            Block block = world.getBlockAt(x, y, z);
            Material id = block.getType();
            if (!id.equals(Material.AIR)) {
                if (storeNormal) {
                    ids[y] = new PlotBlock((short) id.getId(), block.getData());
                }
                try {
                    BlockLoc bl = new BlockLoc(x + offsetX, y, z + offsetZ);
                    if (block.getState() instanceof InventoryHolder) {
                        InventoryHolder inventoryHolder = (InventoryHolder) block.getState();
                        ItemStack[] inventory = inventoryHolder.getInventory().getContents().clone();
                        if (id == Material.CHEST) {
                            chestContents.put(bl, inventory);
                        } else if (id == Material.DISPENSER) {
                            dispenserContents.put(bl, inventory);
                        } else if (id == Material.BEACON) {
                            beaconContents.put(bl, inventory);
                        } else if (id == Material.DROPPER) {
                            dropperContents.put(bl, inventory);
                        } else if (id == Material.HOPPER) {
                            hopperContents.put(bl, inventory);
                        } else if (id == Material.BREWING_STAND) {
                            BrewingStand brewingStand = (BrewingStand) inventoryHolder;
                            short time = (short) brewingStand.getBrewingTime();
                            if (time > 0) {
                                brewTime.put(bl, time);
                            }
                            ItemStack[] invBre = brewingStand.getInventory().getContents().clone();
                            brewingStandContents.put(bl, invBre);
                        } else if (id == Material.FURNACE || id == Material.BURNING_FURNACE) {
                            Furnace furnace = (Furnace) inventoryHolder;
                            short burn = furnace.getBurnTime();
                            short cook = furnace.getCookTime();
                            ItemStack[] invFur = furnace.getInventory().getContents().clone();
                            furnaceContents.put(bl, invFur);
                            if (cook != 0) {
                                furnaceTime.put(bl, new Short[]{burn, cook});
                            }
                        }
                    } else if (block.getState() instanceof CreatureSpawner) {
                        CreatureSpawner spawner = (CreatureSpawner) block.getState();
                        EntityType type = spawner.getSpawnedType();
                        if (type != null) {
                            spawnerData.put(bl, type);
                        }
                    } else if (block.getState() instanceof CommandBlock) {
                        CommandBlock cmd = (CommandBlock) block.getState();
                        String string = cmd.getCommand();
                        if (string != null && !string.isEmpty()) {
                            cmdData.put(bl, string);
                        }
                    } else if (block.getState() instanceof NoteBlock) {
                        NoteBlock noteBlock = (NoteBlock) block.getState();
                        Note note = noteBlock.getNote();
                        noteBlockContents.put(bl, note);
                    } else if (block.getState() instanceof Jukebox) {
                        Jukebox jukebox = (Jukebox) block.getState();
                        Material playing = jukebox.getPlaying();
                        if (playing != null) {
                            jukeboxDisc.put(bl, playing);
                        }
                    } else if (block.getState() instanceof Skull) {
                        Skull skull = (Skull) block.getState();
                        String o = skull.getOwner();
                        byte skullType = getOrdinal(SkullType.values(), skull.getSkullType());
                        skull.getRotation();
                        short rot = getOrdinal(BlockFace.values(), skull.getRotation());
                        skullData.put(bl, new Object[]{o, rot, skullType});
                    } else if (block.getState() instanceof Banner) {
                        Banner banner = (Banner) block.getState();
                        DyeColor base = banner.getBaseColor();
                        bannerBase.put(bl, base);
                        bannerPatterns.put(bl, banner.getPatterns());

                    }
                } catch (Exception e) {
                    PS.debug("------------ FAILED TO DO SOMETHING --------");
                    e.printStackTrace();
                    PS.debug("------------ but we caught it ^ --------");
                }
            }
        }
        PlotLoc loc = new PlotLoc(x + offsetX, z + offsetZ);
        allBlocks.put(loc, ids);
    }

    private static byte getOrdinal(Object[] list, Object value) {
        for (byte i = 0; i < list.length; i++) {
            if (list[i].equals(value)) {
                return i;
            }
        }
        return 0;
    }

    public static void swapChunk(World world1, World world2, Chunk pos1, Chunk pos2, RegionWrapper r1,
            RegionWrapper r2) {
        initMaps();
        int relX = r2.minX - r1.minX;
        int relZ = r2.minZ - r1.minZ;

        saveEntitiesIn(pos1, r1, relX, relZ, true);
        saveEntitiesIn(pos2, r2, -relX, -relZ, true);

        int sx = pos1.getX() << 4;
        int sz = pos1.getZ() << 4;

        String worldName1 = world1.getName();
        String worldName2 = world2.getName();

        for (int x = Math.max(r1.minX, sx); x <= Math.min(r1.maxX, sx + 15); x++) {
            for (int z = Math.max(r1.minZ, sz); z <= Math.min(r1.maxZ, sz + 15); z++) {
                saveBlocks(world1, 256, sx, sz, relX, relZ, false);
                for (int y = 0; y < 256; y++) {
                    Block block1 = world1.getBlockAt(x, y, z);
                    int id1 = block1.getTypeId();
                    byte data1 = block1.getData();
                    int xx = x + relX;
                    int zz = z + relZ;
                    Block block2 = world2.getBlockAt(xx, y, zz);
                    int id2 = block2.getTypeId();
                    byte data2 = block2.getData();
                    if (id1 == 0) {
                        if (id2 != 0) {
                            SetQueue.IMP.setBlock(worldName1, x, y, z, (short) id2, data2);
                            SetQueue.IMP.setBlock(worldName2, xx, y, zz, (short) 0, (byte) 0);
                        }
                    } else if (id2 == 0) {
                        SetQueue.IMP.setBlock(worldName1, x, y, z, (short) 0, (byte) 0);
                        SetQueue.IMP.setBlock(worldName2, xx, y, zz, (short) id1, data1);
                    } else if (id1 == id2) {
                        if (data1 != data2) {
                            block1.setData(data2);
                            block2.setData(data1);
                        }
                    } else {
                        SetQueue.IMP.setBlock(worldName1, x, y, z, (short) id2, data2);
                        SetQueue.IMP.setBlock(worldName2, xx, y, zz, (short) id1, data1);
                    }
                }
            }
        }
        while (SetQueue.IMP.forceChunkSet()) {
        }
        restoreBlocks(world1, 0, 0);
        restoreEntities(world1, 0, 0);
    }

    @Override
    public Set<ChunkLoc> getChunkChunks(String world) {
        Set<ChunkLoc> chunks = super.getChunkChunks(world);
        for (Chunk chunk : Bukkit.getWorld(world).getLoadedChunks()) {
            ChunkLoc loc = new ChunkLoc(chunk.getX() >> 5, chunk.getZ() >> 5);
            if (!chunks.contains(loc)) {
                chunks.add(loc);
            }
        }
        return chunks;
    }

    @Override
    public void regenerateChunk(String world, ChunkLoc loc) {
        World worldObj = Bukkit.getWorld(world);
        worldObj.regenerateChunk(loc.x, loc.z);
        SetQueue.IMP.queue.sendChunk(world, Collections.singletonList(loc));
        for (Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
            PlotPlayer pp = entry.getValue();
            Location pLoc = pp.getLocation();
            if (!StringMan.isEqual(world, pLoc.getWorld()) || !pLoc.getChunkLoc().equals(loc)) {
                continue;
            }
            pLoc.setY(WorldUtil.IMP.getHighestBlock(world, pLoc.getX(), pLoc.getZ()));
            pp.teleport(pLoc);
        }
    }

    @Override
    public boolean copyRegion(Location pos1, Location pos2, Location newPos, final Runnable whenDone) {
        final int relX = newPos.getX() - pos1.getX();
        final int relZ = newPos.getZ() - pos1.getZ();
        Location pos4 = new Location(newPos.getWorld(), newPos.getX() + relX, 256, newPos.getZ() + relZ);

        final RegionWrapper region = new RegionWrapper(pos1.getX(), pos2.getX(), pos1.getZ(), pos2.getZ());
        final World oldWorld = Bukkit.getWorld(pos1.getWorld());
        final World newWorld = Bukkit.getWorld(newPos.getWorld());
        final String newWorldName = newWorld.getName();
        List<ChunkLoc> chunks = new ArrayList<>();
        initMaps();
        ChunkManager.chunkTask(pos1, pos2, new RunnableVal<int[]>() {
            @Override
            public void run(int[] value) {
                int bx = value[2];
                int bz = value[3];
                int tx = value[4];
                int tz = value[5];
                ChunkLoc loc = new ChunkLoc(value[0], value[1]);
                int cxx = loc.x << 4;
                int czz = loc.z << 4;
                Chunk chunk = oldWorld.getChunkAt(loc.x, loc.z);
                saveEntitiesIn(chunk, region);
                for (int x = bx & 15; x <= (tx & 15); x++) {
                    for (int z = bz & 15; z <= (tz & 15); z++) {
                        saveBlocks(oldWorld, 256, cxx + x, czz + z, relX, relZ, true);
                    }
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                for (Entry<PlotLoc, PlotBlock[]> entry : allBlocks.entrySet()) {
                    PlotLoc loc = entry.getKey();
                    PlotBlock[] blocks = entry.getValue();
                    for (int y = 0; y < blocks.length; y++) {
                        PlotBlock block = blocks[y];
                        if (block != null) {
                            SetQueue.IMP.setBlock(newWorldName, loc.x, y, loc.z, block);
                        }
                    }
                }
                while (SetQueue.IMP.forceChunkSet()) {
                }
                restoreBlocks(newWorld, 0, 0);
                restoreEntities(newWorld, relX, relZ);
                TaskManager.runTask(whenDone);
            }
        }, 5);
        return true;
    }

    public void saveRegion(World world, int x1, int x2, int z1, int z2) {
        if (z1 > z2) {
            int tmp = z1;
            z1 = z2;
            z2 = tmp;
        }
        if (x1 > x2) {
            int tmp = x1;
            x1 = x2;
            x2 = tmp;
        }
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                saveBlocks(world, 256, x, z, 0, 0, true);
            }
        }
    }

    @Override
    public boolean regenerateRegion(final Location pos1, final Location pos2, final boolean ignoreAugment, final Runnable whenDone) {
        final String world = pos1.getWorld();

        final int p1x = pos1.getX();
        final int p1z = pos1.getZ();
        final int p2x = pos2.getX();
        final int p2z = pos2.getZ();
        final int bcx = p1x >> 4;
        final int bcz = p1z >> 4;
        final int tcx = p2x >> 4;
        final int tcz = p2z >> 4;

        final List<ChunkLoc> chunks = new ArrayList<>();

        for (int x = bcx; x <= tcx; x++) {
            for (int z = bcz; z <= tcz; z++) {
                chunks.add(new ChunkLoc(x, z));
            }
        }
        final World worldObj = Bukkit.getWorld(world);
        TaskManager.runTask(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                while (!chunks.isEmpty() && System.currentTimeMillis() - start < 5) {
                    final ChunkLoc chunk = chunks.remove(0);
                    int x = chunk.x;
                    int z = chunk.z;
                    int xxb = x << 4;
                    int zzb = z << 4;
                    int xxt = xxb + 15;
                    int zzt = zzb + 15;
                    Chunk chunkObj = worldObj.getChunkAt(x, z);
                    if (!chunkObj.load(false)) {
                        continue;
                    }
                    RegionWrapper currentPlotClear = new RegionWrapper(pos1.getX(), pos2.getX(), pos1.getZ(), pos2.getZ());
                    if (xxb >= p1x && xxt <= p2x && zzb >= p1z && zzt <= p2z) {
                        AugmentedUtils.bypass(ignoreAugment, new Runnable() {
                            @Override
                            public void run() {
                                regenerateChunk(world, chunk);
                            }
                        });
                        continue;
                    }
                    boolean checkX1 = false;

                    int xxb2;

                    if (x == bcx) {
                        xxb2 = p1x - 1;
                        checkX1 = true;
                    } else {
                        xxb2 = xxb;
                    }
                    boolean checkX2 = false;
                    int xxt2;
                    if (x == tcx) {
                        xxt2 = p2x + 1;
                        checkX2 = true;
                    } else {
                        xxt2 = xxt;
                    }
                    boolean checkZ1 = false;
                    int zzb2;
                    if (z == bcz) {
                        zzb2 = p1z - 1;
                        checkZ1 = true;
                    } else {
                        zzb2 = zzb;
                    }
                    boolean checkZ2 = false;
                    int zzt2;
                    if (z == tcz) {
                        zzt2 = p2z + 1;
                        checkZ2 = true;
                    } else {
                        zzt2 = zzt;
                    }
                    initMaps();
                    if (checkX1) {
                        saveRegion(worldObj, xxb, xxb2, zzb2, zzt2); //
                    }
                    if (checkX2) {
                        saveRegion(worldObj, xxt2, xxt, zzb2, zzt2); //
                    }
                    if (checkZ1) {
                        saveRegion(worldObj, xxb2, xxt2, zzb, zzb2); //
                    }
                    if (checkZ2) {
                        saveRegion(worldObj, xxb2, xxt2, zzt2, zzt); //
                    }
                    if (checkX1 && checkZ1) {
                        saveRegion(worldObj, xxb, xxb2, zzb, zzb2); //
                    }
                    if (checkX2 && checkZ1) {
                        saveRegion(worldObj, xxt2, xxt, zzb, zzb2); // ?
                    }
                    if (checkX1 && checkZ2) {
                        saveRegion(worldObj, xxb, xxb2, zzt2, zzt); // ?
                    }
                    if (checkX2 && checkZ2) {
                        saveRegion(worldObj, xxt2, xxt, zzt2, zzt); //
                    }
                    saveEntitiesOut(chunkObj, currentPlotClear);
                    AugmentedUtils.bypass(ignoreAugment, new Runnable() {
                        @Override
                        public void run() {
                            setChunkInPlotArea(null, new RunnableVal<PlotChunk<?>>() {
                                @Override
                                public void run(PlotChunk<?> value) {
                                    int cx = value.getX();
                                    int cz = value.getZ();
                                    int bx = cx << 4;
                                    int bz = cz << 4;
                                    for (int x = 0; x < 16; x++) {
                                        for (int z = 0; z < 16; z++) {
                                            PlotLoc loc = new PlotLoc(bx + x, bz + z);
                                            PlotBlock[] ids = allBlocks.get(loc);
                                            if (ids != null) {
                                                for (int y = 0; y < Math.min(128, ids.length); y++) {
                                                    PlotBlock id = ids[y];
                                                    if (id != null) {
                                                        value.setBlock(x, y, z, id);
                                                    } else {
                                                        value.setBlock(x, y, z, 0, (byte) 0);
                                                    }
                                                }
                                                for (int y = Math.min(128, ids.length); y < ids.length; y++) {
                                                    PlotBlock id = ids[y];
                                                    if (id != null) {
                                                        value.setBlock(x, y, z, id);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }, world, chunk);
                        }
                    });
                    restoreBlocks(worldObj, 0, 0);
                    restoreEntities(worldObj, 0, 0);
                }
                if (!chunks.isEmpty()) {
                    TaskManager.runTaskLater(this, 1);
                } else {
                    TaskManager.runTaskLater(whenDone, 1);
                }
            }
        });
        return true;
    }

    @Override
    public void clearAllEntities(Location pos1, Location pos2) {
        String world = pos1.getWorld();
        List<Entity> entities = BukkitUtil.getEntities(world);
        int bx = pos1.getX();
        int bz = pos1.getZ();
        int tx = pos2.getX();
        int tz = pos2.getZ();
        for (Entity entity : entities) {
            if (!(entity instanceof Player)) {
                org.bukkit.Location loc = entity.getLocation();
                if (loc.getX() >= bx && loc.getX() <= tx && loc.getZ() >= bz && loc.getZ() <= tz) {
                    entity.remove();
                }
            }
        }
    }

    @Override
    public boolean loadChunk(String world, ChunkLoc loc, boolean force) {
        return BukkitUtil.getWorld(world).getChunkAt(loc.x, loc.z).load(force);
    }

    @Override
    public void unloadChunk(final String world, final ChunkLoc loc, final boolean save, final boolean safe) {
        if (!PS.get().isMainThread(Thread.currentThread())) {
            TaskManager.runTask(new Runnable() {
                @Override
                public void run() {
                    BukkitUtil.getWorld(world).unloadChunk(loc.x, loc.z, save, safe);
                }
            });
        } else {
            BukkitUtil.getWorld(world).unloadChunk(loc.x, loc.z, save, safe);
        }
    }

    @Override
    public void swap(Location bot1, Location top1, Location bot2, Location top2, Runnable whenDone) {
        RegionWrapper region1 = new RegionWrapper(bot1.getX(), top1.getX(), bot1.getZ(), top1.getZ());
        RegionWrapper region2 = new RegionWrapper(bot2.getX(), top2.getX(), bot2.getZ(), top2.getZ());
        World world1 = Bukkit.getWorld(bot1.getWorld());
        World world2 = Bukkit.getWorld(bot2.getWorld());

        int relX = bot2.getX() - bot1.getX();
        int relZ = bot2.getZ() - bot1.getZ();

        for (int x = bot1.getX() >> 4; x <= top1.getX() >> 4; x++) {
            for (int z = bot1.getZ() >> 4; z <= top1.getZ() >> 4; z++) {
                Chunk chunk1 = world1.getChunkAt(x, z);
                Chunk chunk2 = world2.getChunkAt(x + (relX >> 4), z + (relZ >> 4));
                swapChunk(world1, world2, chunk1, chunk2, region1, region2);
            }
        }
        TaskManager.runTaskLater(whenDone, 1);
    }

    @Override
    public int[] countEntities(Plot plot) {
        PlotArea area = plot.getArea();
        World world = BukkitUtil.getWorld(area.worldname);

        Location bot = plot.getBottomAbs();
        Location top = plot.getTopAbs();
        int bx = bot.getX() >> 4;
        int bz = bot.getZ() >> 4;

        int tx = top.getX() >> 4;
        int tz = top.getZ() >> 4;

        int size = tx - bx << 4;

        Set<Chunk> chunks = new HashSet<>();
        for (int X = bx; X <= tx; X++) {
            for (int Z = bz; Z <= tz; Z++) {
                if (world.isChunkLoaded(X, Z)) {
                    chunks.add(world.getChunkAt(X, Z));
                }
            }
        }

        boolean doWhole = false;
        List<Entity> entities = null;
        if (size > 200) {
            entities = world.getEntities();
            if (entities.size() < 16 + size * size / 64) {
                doWhole = true;
            }
        }

        int[] count = new int[6];
        if (doWhole) {
            for (Entity entity : entities) {
                org.bukkit.Location loc = entity.getLocation();
                Chunk chunk = loc.getChunk();
                if (chunks.contains(chunk)) {
                    int X = chunk.getX();
                    int Z = chunk.getZ();
                    if (X > bx && X < tx && Z > bz && Z < tz) {
                        count(count, entity);
                    } else {
                        Plot other = area.getPlot(BukkitUtil.getLocation(loc));
                        if (plot.equals(other)) {
                            count(count, entity);
                        }
                    }
                }
            }
        } else {
            for (Chunk chunk : chunks) {
                int X = chunk.getX();
                int Z = chunk.getZ();
                Entity[] entities1 = chunk.getEntities();
                for (Entity entity : entities1) {
                    if (X == bx || X == tx || Z == bz || Z == tz) {
                        Plot other = area.getPlot(BukkitUtil.getLocation(entity));
                        if (plot.equals(other)) {
                            count(count, entity);
                        }
                    } else {
                        count(count, entity);
                    }
                }
            }
        }
        return count;
    }

    private void count(int[] count, Entity entity) {
        switch (entity.getType()) {
            case PLAYER:
                // not valid
                return;
            case SMALL_FIREBALL:
            case FIREBALL:
            case DROPPED_ITEM:
            case EGG:
            case THROWN_EXP_BOTTLE:
            case SPLASH_POTION:
            case SNOWBALL:
            case ENDER_PEARL:
            case ARROW:
            case TIPPED_ARROW:
            case SHULKER_BULLET:
            case SPECTRAL_ARROW:
            case DRAGON_FIREBALL:
                // projectile
            case PRIMED_TNT:
            case FALLING_BLOCK:
                // Block entities
            case ENDER_CRYSTAL:
            case COMPLEX_PART:
            case FISHING_HOOK:
            case ENDER_SIGNAL:
            case EXPERIENCE_ORB:
            case LEASH_HITCH:
            case FIREWORK:
            case WEATHER:
            case LIGHTNING:
            case WITHER_SKULL:
            case UNKNOWN:
            case AREA_EFFECT_CLOUD:
            case LINGERING_POTION:
                // non moving / unremovable
                break;
            case ITEM_FRAME:
            case PAINTING:
            case ARMOR_STAND:
                count[5]++;
                // misc
            case MINECART:
            case MINECART_CHEST:
            case MINECART_COMMAND:
            case MINECART_FURNACE:
            case MINECART_HOPPER:
            case MINECART_MOB_SPAWNER:
            case MINECART_TNT:
            case BOAT:
                count[4]++;
                break;
            case RABBIT:
            case SHEEP:
            case MUSHROOM_COW:
            case OCELOT:
            case PIG:
            case HORSE:
            case SQUID:
            case VILLAGER:
            case IRON_GOLEM:
            case WOLF:
            case CHICKEN:
            case COW:
            case SNOWMAN:
            case BAT:
                // animal
                count[3]++;
                count[1]++;
                break;
            case BLAZE:
            case CAVE_SPIDER:
            case CREEPER:
            case ENDERMAN:
            case ENDERMITE:
            case ENDER_DRAGON:
            case GHAST:
            case GIANT:
            case GUARDIAN:
            case MAGMA_CUBE:
            case PIG_ZOMBIE:
            case SILVERFISH:
            case SKELETON:
            case SLIME:
            case SPIDER:
            case WITCH:
            case WITHER:
            case ZOMBIE:
            case SHULKER:
                // monster
                count[3]++;
                count[2]++;
                break;
            default:
                if (entity instanceof Creature) {
                    count[3]++;
                    if (entity instanceof Animals) {
                        count[1]++;
                    } else {
                        count[2]++;
                    }
                } else {
                    count[4]++;
                }
        }
        count[0]++;
    }
}
