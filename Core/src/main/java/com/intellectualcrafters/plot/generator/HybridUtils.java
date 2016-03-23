package com.intellectualcrafters.plot.generator;

import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotAnalysis;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SetQueue;
import com.intellectualcrafters.plot.util.TaskManager;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class HybridUtils {

    public static HybridUtils manager;
    public static Set<ChunkLoc> regions;
    public static Set<ChunkLoc> chunks = new HashSet<>();
    public static PlotArea area;
    public static boolean UPDATE = false;

    public abstract void analyzeRegion(String world, RegionWrapper region, RunnableVal<PlotAnalysis> whenDone);

    public void analyzePlot(final Plot origin, final RunnableVal<PlotAnalysis> whenDone) {
        final ArrayDeque<RegionWrapper> zones = new ArrayDeque<>(origin.getRegions());
        final ArrayList<PlotAnalysis> analysis = new ArrayList<>();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (zones.isEmpty()) {
                    if (!analysis.isEmpty()) {
                        whenDone.value = new PlotAnalysis();
                        for (PlotAnalysis data : analysis) {
                            whenDone.value.air += data.air;
                            whenDone.value.air_sd += data.air_sd;
                            whenDone.value.changes += data.changes;
                            whenDone.value.changes_sd += data.changes_sd;
                            whenDone.value.data += data.data;
                            whenDone.value.data_sd += data.data_sd;
                            whenDone.value.faces += data.faces;
                            whenDone.value.faces_sd += data.faces_sd;
                            whenDone.value.variety += data.variety;
                            whenDone.value.variety_sd += data.variety_sd;
                        }
                        whenDone.value.air /= analysis.size();
                        whenDone.value.air_sd /= analysis.size();
                        whenDone.value.changes /= analysis.size();
                        whenDone.value.changes_sd /= analysis.size();
                        whenDone.value.data /= analysis.size();
                        whenDone.value.data_sd /= analysis.size();
                        whenDone.value.faces /= analysis.size();
                        whenDone.value.faces_sd /= analysis.size();
                        whenDone.value.variety /= analysis.size();
                        whenDone.value.variety_sd /= analysis.size();
                    } else {
                        whenDone.value = analysis.get(0);
                    }
                    List<Integer> result = new ArrayList<>();
                    result.add(whenDone.value.changes);
                    result.add(whenDone.value.faces);
                    result.add(whenDone.value.data);
                    result.add(whenDone.value.air);
                    result.add(whenDone.value.variety);

                    result.add(whenDone.value.changes_sd);
                    result.add(whenDone.value.faces_sd);
                    result.add(whenDone.value.data_sd);
                    result.add(whenDone.value.air_sd);
                    result.add(whenDone.value.variety_sd);
                    Flag flag = new Flag(FlagManager.getFlag("analysis"), result);
                    FlagManager.addPlotFlag(origin, flag);
                    TaskManager.runTask(whenDone);
                    return;
                }
                RegionWrapper region = zones.poll();
                final Runnable task = this;
                analyzeRegion(origin.getArea().worldname, region, new RunnableVal<PlotAnalysis>() {
                    @Override
                    public void run(PlotAnalysis value) {
                        analysis.add(value);
                        TaskManager.runTaskLater(task, 1);
                    }
                });
            }
        };
        run.run();
    }

    public abstract int checkModified(String world, int x1, int x2, int y1, int y2, int z1, int z2, PlotBlock[] blocks);

    public final ArrayList<ChunkLoc> getChunks(ChunkLoc region) {
        ArrayList<ChunkLoc> chunks = new ArrayList<>();
        int sx = region.x << 5;
        int sz = region.z << 5;
        for (int x = sx; x < sx + 32; x++) {
            for (int z = sz; z < sz + 32; z++) {
                chunks.add(new ChunkLoc(x, z));
            }
        }
        return chunks;
    }

    /**
     * Checks all connected plots.
     * @param plot
     * @param whenDone
     */
    public void checkModified(final Plot plot, final RunnableVal<Integer> whenDone) {
        if (whenDone == null) {
            return;
        }
        PlotArea plotworld = plot.getArea();
        if (!(plotworld instanceof ClassicPlotWorld)) {
            whenDone.value = -1;
            TaskManager.runTask(whenDone);
            return;
        }
        whenDone.value = 0;
        final ClassicPlotWorld cpw = (ClassicPlotWorld) plotworld;
        final ArrayDeque<RegionWrapper> zones = new ArrayDeque<>(plot.getRegions());
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (zones.isEmpty()) {

                    TaskManager.runTask(whenDone);
                    return;
                }
                RegionWrapper region = zones.poll();
                Location pos1 = new Location(plot.getArea().worldname, region.minX, region.minY, region.minZ);
                Location pos2 = new Location(plot.getArea().worldname, region.maxX, region.maxY, region.maxZ);
                ChunkManager.chunkTask(pos1, pos2, new RunnableVal<int[]>() {
                    @Override
                    public void run(int[] value) {
                        ChunkLoc loc = new ChunkLoc(value[0], value[1]);
                        ChunkManager.manager.loadChunk(plot.getArea().worldname, loc, false);
                        int bx = value[2];
                        int bz = value[3];
                        int ex = value[4];
                        int ez = value[5];
                        whenDone.value += checkModified(plot.getArea().worldname, bx, ex, 1, cpw.PLOT_HEIGHT - 1, bz, ez, cpw.MAIN_BLOCK);
                        whenDone.value += checkModified(plot.getArea().worldname, bx, ex, cpw.PLOT_HEIGHT, cpw.PLOT_HEIGHT, bz, ez, cpw.TOP_BLOCK);
                        whenDone.value += checkModified(
                                plot.getArea().worldname, bx, ex, cpw.PLOT_HEIGHT + 1, 255, bz, ez,
                                new PlotBlock[]{new PlotBlock((short) 0, (byte) 0)});
                    }
                }, this, 5);

            }
        };
        run.run();
    }

    public boolean scheduleRoadUpdate(PlotArea area, int extend) {
        if (HybridUtils.UPDATE) {
            return false;
        }
        HybridUtils.UPDATE = true;
        Set<ChunkLoc> regions = ChunkManager.manager.getChunkChunks(area.worldname);
        return scheduleRoadUpdate(area, regions, extend);
    }

    public boolean scheduleRoadUpdate(final PlotArea area, Set<ChunkLoc> rgs, final int extend) {
        HybridUtils.regions = rgs;
        HybridUtils.area = area;
        chunks = new HashSet<>();
        final AtomicInteger count = new AtomicInteger(0);
        final long baseTime = System.currentTimeMillis();
        final AtomicInteger last = new AtomicInteger();
        TaskManager.runTask(new Runnable() {
            @Override
            public void run() {
                if (!UPDATE) {
                    last.set(0);
                    Iterator<ChunkLoc> iter = chunks.iterator();
                    while (iter.hasNext()) {
                        ChunkLoc chunk = iter.next();
                        iter.remove();
                        regenerateRoad(area, chunk, extend);
                        ChunkManager.manager.unloadChunk(area.worldname, chunk, true, true);
                    }
                    PS.debug("&cCancelled road task");
                    return;
                }
                count.incrementAndGet();
                if (count.intValue() % 20 == 0) {
                    PS.debug("PROGRESS: " + 100 * (2048 - chunks.size()) / 2048 + "%");
                }
                if (regions.isEmpty() && chunks.isEmpty()) {
                    HybridUtils.UPDATE = false;
                    PS.debug(C.PREFIX.s() + "Finished road conversion");
                    // CANCEL TASK
                } else {
                    final Runnable task = this;
                    TaskManager.runTaskAsync(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (last.get() == 0) {
                                    last.set((int) (System.currentTimeMillis() - baseTime));
                                }
                                if (chunks.size() < 1024) {
                                    if (!regions.isEmpty()) {
                                        Iterator<ChunkLoc> iter = regions.iterator();
                                        ChunkLoc loc = iter.next();
                                        iter.remove();
                                        PS.debug("&3Updating .mcr: " + loc.x + ", " + loc.z + " (aprrox 1024 chunks)");
                                        PS.debug(" - Remaining: " + regions.size());
                                        chunks.addAll(getChunks(loc));
                                        System.gc();
                                    }
                                }
                                if (!chunks.isEmpty()) {
                                    long diff = System.currentTimeMillis() + 1;
                                    if (System.currentTimeMillis() - baseTime - last.get() > 2000 && last.get() != 0) {
                                        last.set(0);
                                        PS.debug(C.PREFIX.s() + "Detected low TPS. Rescheduling in 30s");
                                        Iterator<ChunkLoc> iter = chunks.iterator();
                                        final ChunkLoc chunk = iter.next();
                                        iter.remove();
                                        TaskManager.runTask(new Runnable() {
                                            @Override
                                            public void run() {
                                                regenerateRoad(area, chunk, extend);
                                            }
                                        });
                                        // DELAY TASK
                                        TaskManager.runTaskLater(task, 600);
                                        return;
                                    }
                                    if (System.currentTimeMillis() - baseTime - last.get() < 1500 && last.get() != 0) {
                                        while (System.currentTimeMillis() < diff && !chunks.isEmpty()) {
                                            Iterator<ChunkLoc> iter = chunks.iterator();
                                            final ChunkLoc chunk = iter.next();
                                            iter.remove();
                                            TaskManager.runTask(new Runnable() {
                                                @Override
                                                public void run() {
                                                    regenerateRoad(area, chunk, extend);
                                                }
                                            });
                                        }
                                    }
                                    last.set((int) (System.currentTimeMillis() - baseTime));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Iterator<ChunkLoc> iter = regions.iterator();
                                ChunkLoc loc = iter.next();
                                iter.remove();
                                PS.debug("&c[ERROR]&7 Could not update '" + area.worldname + "/region/r." + loc.x + "." + loc.z
                                        + ".mca' (Corrupt chunk?)");
                                int sx = loc.x << 5;
                                int sz = loc.z << 5;
                                for (int x = sx; x < sx + 32; x++) {
                                    for (int z = sz; z < sz + 32; z++) {
                                        ChunkManager.manager.unloadChunk(area.worldname, new ChunkLoc(x, z), true, true);
                                    }
                                }
                                PS.debug("&d - Potentially skipping 1024 chunks");
                                PS.debug("&d - TODO: recommend chunkster if corrupt");
                            }
                            SetQueue.IMP.addTask(new Runnable() {
                                @Override
                                public void run() {
                                    TaskManager.runTaskLater(task, 20);
                                }
                            });
                        }
                    });
                }
            }
        });
        return true;
    }

    public boolean setupRoadSchematic(Plot plot) {
        final String world = plot.getArea().worldname;
        Location bot = plot.getBottomAbs().subtract(1, 0, 1);
        Location top = plot.getTopAbs();
        final HybridPlotWorld plotworld = (HybridPlotWorld) plot.getArea();
        int sx = bot.getX() - plotworld.ROAD_WIDTH + 1;
        int sz = bot.getZ() + 1;
        int sy = plotworld.ROAD_HEIGHT;
        int ex = bot.getX();
        int ez = top.getZ();
        int ey = get_ey(world, sx, ex, sz, ez, sy);
        int bz = sz - plotworld.ROAD_WIDTH;
        int tz = sz - 1;
        int ty = get_ey(world, sx, ex, bz, tz, sy);

        Set<RegionWrapper> sideroad = new HashSet<>(Collections.singletonList(new RegionWrapper(sx, ex, sy, ey, sz, ez)));
        final Set<RegionWrapper> intersection = new HashSet<>(Collections.singletonList(new RegionWrapper(sx, ex, sy, ty, bz, tz)));

        final String dir = "schematics" + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator + plot
                .getArea().toString() + File.separator;
        SchematicHandler.manager.getCompoundTag(world, sideroad, new RunnableVal<CompoundTag>() {
            @Override
            public void run(CompoundTag value) {
                SchematicHandler.manager.save(value, dir + "sideroad.schematic");
                SchematicHandler.manager.getCompoundTag(world, intersection, new RunnableVal<CompoundTag>() {
                    @Override
                    public void run(CompoundTag value) {
                        SchematicHandler.manager.save(value, dir + "intersection.schematic");
                        plotworld.ROAD_SCHEMATIC_ENABLED = true;
                        plotworld.setupSchematics();
                    }
                });
            }
        });
        return true;
    }

    public abstract int get_ey(String world, int sx, int ex, int sz, int ez, int sy);

    public boolean regenerateRoad(final PlotArea area, final ChunkLoc chunk, int extend) {
        int x = chunk.x << 4;
        int z = chunk.z << 4;
        int ex = x + 15;
        int ez = z + 15;
        HybridPlotWorld plotworld = (HybridPlotWorld) area;
        extend = Math.min(extend, 255 - plotworld.ROAD_HEIGHT - plotworld.SCHEMATIC_HEIGHT);
        if (!plotworld.ROAD_SCHEMATIC_ENABLED) {
            return false;
        }
        boolean toCheck = false;
        if (plotworld.TYPE == 2) {
            boolean c1 = area.contains(x, z);
            boolean c2 = area.contains(ex, ez);
            if (!c1 && !c2) {
                return false;
            } else {
                toCheck = c1 ^ c2;
            }
        }
        PlotManager manager = area.getPlotManager();
        PlotId id1 = manager.getPlotId(plotworld, x, 0, z);
        PlotId id2 = manager.getPlotId(plotworld, ex, 0, ez);
        x -= plotworld.ROAD_OFFSET_X;
        z -= plotworld.ROAD_OFFSET_Z;
        if (id1 == null || id2 == null || id1 != id2) {
            boolean result = ChunkManager.manager.loadChunk(area.worldname, chunk, false);
            if (result) {
                if (id1 != null) {
                    Plot p1 = area.getPlotAbs(id1);
                    if (p1 != null && p1.hasOwner() && p1.isMerged()) {
                        toCheck = true;
                    }
                }
                if (id2 != null && !toCheck) {
                    Plot p2 = area.getPlotAbs(id2);
                    if (p2 != null && p2.hasOwner() && p2.isMerged()) {
                        toCheck = true;
                    }
                }
                int size = plotworld.SIZE;
                for (int X = 0; X < 16; X++) {
                    short absX = (short) ((x + X) % size);
                    for (int Z = 0; Z < 16; Z++) {
                        short absZ = (short) ((z + Z) % size);
                        if (absX < 0) {
                            absX += size;
                        }
                        if (absZ < 0) {
                            absZ += size;
                        }
                        boolean condition;
                        if (toCheck) {
                            condition = manager.getPlotId(plotworld, x + X + plotworld.ROAD_OFFSET_X, 1, z + Z + plotworld.ROAD_OFFSET_Z) == null;
                            //                            condition = MainUtil.isPlotRoad(new Location(plotworld.worldname, x + X, 1, z + Z));
                        } else {
                            boolean gx = absX > plotworld.PATH_WIDTH_LOWER;
                            boolean gz = absZ > plotworld.PATH_WIDTH_LOWER;
                            boolean lx = absX < plotworld.PATH_WIDTH_UPPER;
                            boolean lz = absZ < plotworld.PATH_WIDTH_UPPER;
                            condition = !gx || !gz || !lx || !lz;
                        }
                        if (condition) {
                            HashMap<Integer, PlotBlock> blocks = plotworld.G_SCH.get(MathMan.pair(absX, absZ));
                            for (short y = (short) plotworld.ROAD_HEIGHT; y <= plotworld.ROAD_HEIGHT + plotworld.SCHEMATIC_HEIGHT + extend; y++) {
                                SetQueue.IMP.setBlock(area.worldname, x + X + plotworld.ROAD_OFFSET_X, y, z + Z + plotworld.ROAD_OFFSET_Z, 0);
                            }
                            if (blocks != null) {
                                for (Entry<Integer, PlotBlock> entry : blocks.entrySet()) {
                                    SetQueue.IMP.setBlock(area.worldname, x + X + plotworld.ROAD_OFFSET_X, entry.getKey(),
                                            z + Z + plotworld.ROAD_OFFSET_Z, entry.getValue());
                                }
                            }
                        }
                    }
                }
                SetQueue.IMP.addTask(new Runnable() {
                    @Override
                    public void run() {
                        ChunkManager.manager.unloadChunk(area.worldname, chunk, true, true);
                    }
                });
                return true;
            }
        }
        return false;
    }
}
