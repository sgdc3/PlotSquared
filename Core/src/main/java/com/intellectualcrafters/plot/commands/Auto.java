////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.ByteArrayUtilities;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "auto",
        permission = "plots.auto",
        category = CommandCategory.CLAIMING,
        requiredType = RequiredType.NONE,
        description = "Claim the nearest plot",
        aliases = "a",
        usage = "/plot auto [length,width]")
public class Auto extends SubCommand {

    public static PlotId getNextPlotId(PlotId id, int step) {
        int absX = Math.abs(id.x);
        int absY = Math.abs(id.y);
        if (absX > absY) {
            if (id.x > 0) {
                return new PlotId(id.x, id.y + 1);
            } else {
                return new PlotId(id.x, id.y - 1);
            }
        } else if (absY > absX) {
            if (id.y > 0) {
                return new PlotId(id.x - 1, id.y);
            } else {
                return new PlotId(id.x + 1, id.y);
            }
        } else {
            if (id.x == id.y && id.x > 0) {
                return new PlotId(id.x, id.y + step);
            }
            if (id.x == absX) {
                return new PlotId(id.x, id.y + 1);
            }
            if (id.y == absY) {
                return new PlotId(id.x, id.y - 1);
            }
            return new PlotId(id.x + 1, id.y);
        }
    }

    @Override
    public boolean onCommand(PlotPlayer plr, String[] args) {
        PlotArea plotarea = plr.getApplicablePlotArea();
        if (plotarea == null) {
            MainUtil.sendMessage(plr, C.NOT_IN_PLOT_WORLD);
            return false;
        }
        int size_x = 1;
        int size_z = 1;
        String schematic = null;
        if (args.length > 0) {
            if (Permissions.hasPermission(plr, "plots.auto.mega")) {
                try {
                    String[] split = args[0].split(",|;");
                    size_x = Integer.parseInt(split[0]);
                    size_z = Integer.parseInt(split[1]);
                    if (size_x < 1 || size_z < 1) {
                        MainUtil.sendMessage(plr, "&cError: size<=0");
                    }
                    if (args.length > 1) {
                        schematic = args[1];
                    }
                } catch (NumberFormatException e) {
                    size_x = 1;
                    size_z = 1;
                    schematic = args[0];
                    // PlayerFunctions.sendMessage(plr,
                    // "&cError: Invalid size (X,Y)");
                    // return false;
                }
            } else {
                schematic = args[0];
                // PlayerFunctions.sendMessage(plr, C.NO_PERMISSION);
                // return false;
            }
        }
        if (size_x * size_z > Settings.MAX_AUTO_SIZE) {
            MainUtil.sendMessage(plr, C.CANT_CLAIM_MORE_PLOTS_NUM, Settings.MAX_AUTO_SIZE + "");
            return false;
        }
        int currentPlots = Settings.GLOBAL_LIMIT ? plr.getPlotCount() : plr.getPlotCount(plotarea.worldname);
        int diff = currentPlots - plr.getAllowedPlots();
        if (diff + size_x * size_z > 0) {
            if (diff < 0) {
                MainUtil.sendMessage(plr, C.CANT_CLAIM_MORE_PLOTS_NUM, -diff + "");
                return false;
            } else if (plr.hasPersistentMeta("grantedPlots")) {
                int grantedPlots = ByteArrayUtilities.bytesToInteger(plr.getPersistentMeta("grantedPlots"));
                if (grantedPlots - diff < size_x * size_z) {
                    plr.removePersistentMeta("grantedPlots");
                    return sendMessage(plr, C.CANT_CLAIM_MORE_PLOTS);
                } else {
                    int left = grantedPlots - diff - size_x * size_z;
                    if (left == 0) {
                        plr.removePersistentMeta("grantedPlots");
                    } else {
                        plr.setPersistentMeta("grantedPlots", ByteArrayUtilities.integerToBytes(left));
                    }
                    sendMessage(plr, C.REMOVED_GRANTED_PLOT, "" + left, "" + (grantedPlots - left));
                }
            } else {
                MainUtil.sendMessage(plr, C.CANT_CLAIM_MORE_PLOTS);
                return false;
            }
        }
        if (EconHandler.manager != null && plotarea.USE_ECONOMY) {
            double cost = plotarea.PRICES.get("claim");
            cost = (size_x * size_z) * cost;
            if (cost > 0d) {
                if (EconHandler.manager.getMoney(plr) < cost) {
                    sendMessage(plr, C.CANNOT_AFFORD_PLOT, "" + cost);
                    return true;
                }
                EconHandler.manager.withdrawMoney(plr, cost);
                sendMessage(plr, C.REMOVED_BALANCE, cost + "");
            }
        }
        if (schematic != null && !schematic.isEmpty()) {
            if (!plotarea.SCHEMATICS.contains(schematic.toLowerCase())) {
                sendMessage(plr, C.SCHEMATIC_INVALID, "non-existent: " + schematic);
                return true;
            }
            if (!Permissions.hasPermission(plr, "plots.claim." + schematic) && !Permissions.hasPermission(plr, "plots.admin.command.schematic")) {
                MainUtil.sendMessage(plr, C.NO_SCHEMATIC_PERMISSION, schematic);
                return true;
            }
        }
        // TODO handle type 2 the same as normal worlds!
        if (plotarea.TYPE == 2) {
            PlotId bot = plotarea.getMin();
            PlotId top = plotarea.getMax();
            PlotId origin = new PlotId((bot.x + top.x) / 2, (bot.y + top.y) / 2);
            PlotId id = new PlotId(0, 0);
            int width = Math.max(top.x - bot.x + 1, top.y - bot.y + 1);
            int max = width * width;
            //
            for (int i = 0; i <= max; i++) {
                PlotId currentId = new PlotId(origin.x + id.x, origin.y + id.y);
                Plot current = plotarea.getPlotAbs(currentId);
                if (current.canClaim(plr)) {
                    current.claim(plr, true, null);
                    return true;
                }
                id = getNextPlotId(id, 1);
            }
            // no free plots
            MainUtil.sendMessage(plr, C.NO_FREE_PLOTS);
            return false;
        }
        plotarea.setMeta("lastPlot", new PlotId(0, 0));
        boolean br = false;
        while (true) {
            PlotId start = getNextPlotId(getLastPlotId(plotarea), 1);
            PlotId end = new PlotId(start.x + size_x - 1, start.y + size_z - 1);
            plotarea.setMeta("lastPlot", start);
            if (plotarea.canClaim(plr, start, end)) {
                for (int i = start.x; i <= end.x; i++) {
                    for (int j = start.y; j <= end.y; j++) {
                        Plot plot = plotarea.getPlotAbs(new PlotId(i, j));
                        boolean teleport = i == end.x && j == end.y;
                        plot.claim(plr, teleport, null);
                    }
                }
                if (size_x != 1 || size_z != 1) {
                    if (!plotarea.mergePlots(MainUtil.getPlotSelectionIds(start, end), Settings.MERGE_REMOVES_ROADS, true)) {
                        return false;
                    }
                }
                break;
            }
        }
        plotarea.setMeta("lastPlot", new PlotId(0, 0));
        return true;
    }

    public PlotId getLastPlotId(PlotArea area) {
        PlotId value = (PlotId) area.getMeta("lastPlot");
        if (value == null) {
            value = new PlotId(0, 0);
            area.setMeta("lastPlot", value);
            return value;
        }
        return value;
    }
}
