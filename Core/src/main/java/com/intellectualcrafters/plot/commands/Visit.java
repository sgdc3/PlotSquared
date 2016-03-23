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

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.Argument;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@CommandDeclaration(
        command = "visit",
        permission = "plots.visit",
        description = "Visit someones plot",
        usage = "/plot visit [player|alias|world|id] [#]",
        aliases = {"v", "tp", "teleport", "goto"},
        requiredType = RequiredType.NONE,
        category = CommandCategory.TELEPORT)
public class Visit extends SubCommand {

    public Visit() {
        this.requiredArguments = new Argument[]{Argument.String};
    }

    public List<Plot> getPlots(UUID uuid) {
        List<Plot> plots = new ArrayList<>();
        for (Plot p : PS.get().getPlots()) {
            if (p.hasOwner() && p.isOwner(uuid)) {
                plots.add(p);
            }
        }
        return plots;
    }

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        int page = Integer.MIN_VALUE;
        Collection<Plot> unsorted = null;
        if (args.length == 1 && args[0].contains(":")) {
            args = args[0].split(":");
        }
        switch (args.length) {
            case 2: {
                if (!MathMan.isInteger(args[1])) {
                    sendMessage(player, C.NOT_VALID_NUMBER, "(1, ∞)");
                    sendMessage(player, C.COMMAND_SYNTAX, "/plot visit " + args[0] + " [#]");
                    return false;
                }
                page = Integer.parseInt(args[1]);
            }
            case 1: {
                UUID user = UUIDHandler.getCachedUUID(args[0], null);
                if (page == Integer.MIN_VALUE && user == null && MathMan.isInteger(args[0])) {
                    page = Integer.parseInt(args[0]);
                    unsorted = PS.get().getBasePlots(player);
                    break;
                }
                if (user != null) {
                    unsorted = PS.get().getBasePlots(user);
                } else {
                    Plot plot = MainUtil.getPlotFromString(player, args[0], true);
                    if (plot != null) {
                        unsorted = Collections.singletonList(plot.getBasePlot(false));
                    }
                }
                break;
            }
            case 0: {
                page = 1;
                unsorted = PS.get().getPlots(player);
                break;
            }
            default: {

            }
        }
        if (page == Integer.MIN_VALUE) {
            page = 1;
        }
        if (unsorted == null || unsorted.isEmpty()) {
            sendMessage(player, C.FOUND_NO_PLOTS);
            return false;
        }
        Iterator<Plot> iter = unsorted.iterator();
        while (iter.hasNext()) {
            if (!iter.next().isBasePlot()) {
                iter.remove();
            }
        }
        if (page < 1 || page > unsorted.size()) {
            sendMessage(player, C.NOT_VALID_NUMBER, "(1, " + unsorted.size() + ")");
            return false;
        }
        ArrayList<Plot> plots = PS.get().sortPlotsByTemp(unsorted);
        Plot plot = plots.get(page - 1);
        if (!plot.hasOwner()) {
            if (!Permissions.hasPermission(player, "plots.visit.unowned")) {
                sendMessage(player, C.NO_PERMISSION, "plots.visit.unowned");
                return false;
            }
        } else if (plot.isOwner(player.getUUID())) {
            if (!Permissions.hasPermission(player, "plots.visit.owned") && !Permissions.hasPermission(player, "plots.home")) {
                sendMessage(player, C.NO_PERMISSION, "plots.visit.owned, plots.home");
                return false;
            }
        } else if (plot.isAdded(player.getUUID())) {
            if (!Permissions.hasPermission(player, "plots.visit.shared")) {
                sendMessage(player, C.NO_PERMISSION, "plots.visit.shared");
                return false;
            }
        } else {
            if (!Permissions.hasPermission(player, "plots.visit.other")) {
                sendMessage(player, C.NO_PERMISSION, "plots.visit.other");
                return false;
            }
        }
        plot.teleportPlayer(player);
        return true;
    }

}
