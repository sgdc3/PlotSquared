package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.LazyBlock;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.Rating;
import com.plotsquared.listener.PlayerBlockEventType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public abstract class EventUtil {

    public static EventUtil manager = null;

    public abstract Rating callRating(PlotPlayer player, Plot plot, Rating rating);

    public abstract boolean callClaim(PlotPlayer player, Plot plot, boolean auto);

    public abstract boolean callTeleport(PlotPlayer player, Location from, Plot plot);

    public abstract boolean callClear(Plot plot);

    public abstract void callDelete(Plot plot);

    public abstract boolean callFlagAdd(Flag flag, Plot plot);

    public abstract boolean callFlagRemove(Flag flag, Plot plot);

    public abstract boolean callFlagRemove(Flag flag, PlotCluster cluster);

    public abstract boolean callMerge(Plot plot, ArrayList<PlotId> plots);

    public abstract boolean callUnlink(PlotArea area, ArrayList<PlotId> plots);

    public abstract void callEntry(PlotPlayer player, Plot plot);

    public abstract void callLeave(PlotPlayer player, Plot plot);

    public abstract void callDenied(PlotPlayer initiator, Plot plot, UUID player, boolean added);

    public abstract void callTrusted(PlotPlayer initiator, Plot plot, UUID player, boolean added);

    public abstract void callMember(PlotPlayer initiator, Plot plot, UUID player, boolean added);

    public void doJoinTask(final PlotPlayer pp) {
        if (ExpireManager.IMP != null) {
            ExpireManager.IMP.handleJoin(pp);
        }
        if (PS.get().worldedit != null) {
            if (pp.getAttribute("worldedit")) {
                MainUtil.sendMessage(pp, C.WORLDEDIT_BYPASSED);
            }
        }
        if (PS.get().update != null && Permissions.hasPermission(pp, C.PERMISSION_ADMIN_UPDATE) && Settings.UPDATE_NOTIFICATIONS) {
            MainUtil.sendMessage(pp, "&6An update for PlotSquared is available: &7/plot update");
        }
        final Plot plot = pp.getCurrentPlot();
        if (Settings.TELEPORT_ON_LOGIN && plot != null) {
            TaskManager.runTask(new Runnable() {
                @Override
                public void run() {
                    plot.teleportPlayer(pp);
                }
            });
            MainUtil.sendMessage(pp, C.TELEPORTED_TO_ROAD);
        }
    }

    public boolean checkPlayerBlockEvent(PlotPlayer pp, PlayerBlockEventType type, Location loc, LazyBlock block, boolean notifyPerms) {
        PlotArea area = PS.get().getPlotAreaAbs(loc);
        Plot plot;
        if (area != null) {
            plot = area.getPlot(loc);
        } else {
            plot = null;
        }
        if (plot == null) {
            if (area == null) {
                return true;
            }
        } else if (plot.isAdded(pp.getUUID())) {
            return true;
        }
        switch (type) {
            case TELEPORT_OBJECT:
                return false;
            case EAT:
            case READ:
                return true;
            case BREAK_BLOCK:
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
                }
                Flag use = FlagManager.getPlotFlagRaw(plot, "use");
                if (use != null) {
                    HashSet<PlotBlock> value = (HashSet<PlotBlock>) use.getValue();
                    if (value.contains(PlotBlock.EVERYTHING) || value.contains(block.getPlotBlock())) {
                        return true;
                    }
                }
                Flag destroy = FlagManager.getPlotFlagRaw(plot, "break");
                if (destroy != null) {
                    HashSet<PlotBlock> value = (HashSet<PlotBlock>) destroy.getValue();
                    if (value.contains(PlotBlock.EVERYTHING) || value.contains(block.getPlotBlock())) {
                        return true;
                    }
                }
                if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms)) {
                    return true;
                }
                return !(!notifyPerms || MainUtil.sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_USE.s() + "/" + C.FLAG_BREAK.s()));
            case BREAK_HANGING:
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), notifyPerms);
                }
                if (FlagManager.isPlotFlagTrue(plot, "hanging-break")) {
                    return true;
                }
                if (plot.hasOwner()) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms) || !(!notifyPerms || MainUtil
                            .sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_HANGING_BREAK.s()));
                }
                return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
            case BREAK_MISC:
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), notifyPerms);
                }
                if (FlagManager.isPlotFlagTrue(plot, "misc-break")) {
                    return true;
                }
                if (plot.hasOwner()) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms) || !(!notifyPerms || MainUtil
                            .sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_MISC_BREAK.s()));
                }
                return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
            case BREAK_VEHICLE:
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), notifyPerms);
                }
                if (FlagManager.isPlotFlagTrue(plot, "vehicle-break")) {
                    return true;
                }
                if (plot.hasOwner()) {
                    if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_VEHICLE_BREAK.s()));
                }
                return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
            case INTERACT_BLOCK: {
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
                }
                Flag flag = FlagManager.getPlotFlagRaw(plot, "use");
                HashSet<PlotBlock> value;
                if (flag == null) {
                    value = null;
                } else {
                    value = (HashSet<PlotBlock>) flag.getValue();
                }
                if (value == null || !value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock())) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms) || !(!notifyPerms || MainUtil
                            .sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_USE.s()));
                }
                return true;
            }
            case PLACE_BLOCK: {
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_ROAD.s(), notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_UNOWNED.s(), notifyPerms);
                }
                Flag flag = FlagManager.getPlotFlagRaw(plot, "place");
                HashSet<PlotBlock> value;
                if (flag == null) {
                    value = null;
                } else {
                    value = (HashSet<PlotBlock>) flag.getValue();
                }
                if (value == null || !value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock())) {
                    if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER.s(), notifyPerms)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_PLACE.s()));
                }
                return true;
            }
            case TRIGGER_PHYSICAL: {
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), false);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), false);
                }
                if (FlagManager.isPlotFlagTrue(plot, "device-interact")) {
                    return true;
                }
                Flag flag = FlagManager.getPlotFlagRaw(plot, "use");
                HashSet<PlotBlock> value;
                if (flag == null) {
                    value = null;
                } else {
                    value = (HashSet<PlotBlock>) flag.getValue();
                }
                if (value == null || !value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock())) {
                    // TODO: fix the commented dead code
                    return true; //!(!false || MainUtil.sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_USE.s() + "/" + C.FLAG_DEVICE_INTERACT.s()));
                }
                return true;
            }
            case INTERACT_HANGING: {
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
                }
                if (FlagManager.isPlotFlagTrue(plot, "hanging-interact")) {
                    return true;
                }
                Flag flag = FlagManager.getPlotFlagRaw(plot, "use");
                HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                if (value == null || !value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock())) {
                    if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_USE.s() + "/" + C.FLAG_HANGING_INTERACT.s()));
                }
                return true;
            }
            case INTERACT_MISC: {
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
                }
                if (FlagManager.isPlotFlagTrue(plot, "misc-interact")) {
                    return true;
                }
                Flag flag = FlagManager.getPlotFlagRaw(plot, "use");
                HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                if (value == null || !value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock())) {
                    if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_USE.s() + "/" + C.FLAG_MISC_INTERACT.s()));
                }
                return true;
            }
            case INTERACT_VEHICLE: {
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
                }
                if (FlagManager.isPlotFlagTrue(plot, "vehicle-use")) {
                    return true;
                }
                Flag flag = FlagManager.getPlotFlagRaw(plot, "use");
                HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                if (value == null || !value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock())) {
                    if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_USE.s() + "/" + C.FLAG_VEHICLE_USE.s()));
                }
                return true;
            }
            case SPAWN_MOB: {
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
                }

                if (FlagManager.isPlotFlagTrue(plot, "mob-place")) {
                    return true;
                }
                Flag flag = FlagManager.getPlotFlagRaw(plot, "place");
                HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                if (value == null || !value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock())) {
                    if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_MOB_PLACE.s() + "/" + C.FLAG_PLACE.s()));
                }
                return true;
            }
            case PLACE_HANGING: // Handled elsewhere
                return true;
            case PLACE_MISC: {
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
                }

                if (FlagManager.isPlotFlagTrue(plot, "misc-place")) {
                    return true;
                }
                Flag flag = FlagManager.getPlotFlagRaw(plot, "place");
                HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                if (value == null || !value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock())) {
                    if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_MISC_PLACE.s() + "/" + C.FLAG_PLACE.s()));
                }
                return true;
            }
            case PLACE_VEHICLE:
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
                }

                if (FlagManager.isPlotFlagTrue(plot, "vehicle-place")) {
                    return true;
                }
                Flag flag = FlagManager.getPlotFlagRaw(plot, "place");
                HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                if (value == null || !value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock())) {
                    if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_VEHICLE_PLACE.s() + "/" + C.FLAG_PLACE.s()));
                }
                return true;
            default:
                break;
        }
        return true;
    }
}
