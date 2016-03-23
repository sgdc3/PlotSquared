package com.plotsquared.listener;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RegionWrapper;

import java.util.HashSet;
import java.util.UUID;

public class WEManager {

    public static boolean maskContains(HashSet<RegionWrapper> mask, int x, int y, int z) {
        for (RegionWrapper region : mask) {
            if (region.isIn(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    public static boolean maskContains(HashSet<RegionWrapper> mask, int x, int z) {
        for (RegionWrapper region : mask) {
            if (region.isIn(x, z)) {
                return true;
            }
        }
        return false;
    }

    public static HashSet<RegionWrapper> getMask(PlotPlayer player) {
        HashSet<RegionWrapper> regions = new HashSet<>();
        UUID uuid = player.getUUID();
        Location location = player.getLocation();
        String world = location.getWorld();
        if (!PS.get().hasPlotArea(world)) {
            regions.add(new RegionWrapper(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE));
            return regions;
        }
        PlotArea area = player.getApplicablePlotArea();
        if (area == null) {
            return regions;
        }
        for (Plot plot : area.getPlots()) {
            if (!plot.isBasePlot() || (Settings.DONE_RESTRICTS_BUILDING && (FlagManager.getPlotFlagRaw(plot, "done") != null))) {
                continue;
            }
            if (Settings.WE_ALLOW_HELPER && plot.isAdded(uuid) || !Settings.WE_ALLOW_HELPER && (plot.isOwner(uuid) || plot.getTrusted()
                    .contains(uuid))) {
                regions.addAll(plot.getRegions());
            }
        }
        return regions;
    }

    public static boolean intersects(RegionWrapper region1, RegionWrapper region2) {
        return (region1.minX <= region2.maxX) && (region1.maxX >= region2.minX) && (region1.minZ <= region2.maxZ) && (region1.maxZ >= region2.minZ);
    }

    public static boolean regionContains(RegionWrapper selection, HashSet<RegionWrapper> mask) {
        for (RegionWrapper region : mask) {
            if (intersects(region, selection)) {
                return true;
            }
        }
        return false;
    }
}
