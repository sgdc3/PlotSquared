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
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SchematicHandler.Schematic;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.general.commands.CommandDeclaration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@CommandDeclaration(
        command = "schematic",
        permission = "plots.schematic",
        description = "Schematic command",
        aliases = {"sch"},
        category = CommandCategory.SCHEMATIC,
        usage = "/plot schematic <arg...>")
public class SchematicCmd extends SubCommand {

    private boolean running = false;

    @Override
    public boolean onCommand(final PlotPlayer plr, String... args) {
        if (args.length < 1) {
            sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
            return true;
        }
        String arg = args[0].toLowerCase();
        switch (arg) {
            case "paste": {
                if (!Permissions.hasPermission(plr, "plots.schematic.paste")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.schematic.paste");
                    return false;
                }
                if (args.length < 2) {
                    sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
                    break;
                }
                Location loc = plr.getLocation();
                final Plot plot = loc.getPlotAbs();
                if (plot == null) {
                    return !sendMessage(plr, C.NOT_IN_PLOT);
                }
                if (!plot.hasOwner()) {
                    MainUtil.sendMessage(plr, C.PLOT_UNOWNED);
                    return false;
                }
                if (!plot.isOwner(plr.getUUID()) && !Permissions.hasPermission(plr, "plots.admin.command.schematic.paste")) {
                    MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
                    return false;
                }
                if (this.running) {
                    MainUtil.sendMessage(plr, "&cTask is already running.");
                    return false;
                }
                final String location = args[1];
                this.running = true;
                TaskManager.runTaskAsync(new Runnable() {
                    @Override
                    public void run() {
                        Schematic schematic;
                        if (location.startsWith("url:")) {
                            try {
                                UUID uuid = UUID.fromString(location.substring(4));
                                URL base = new URL(Settings.WEB_URL);
                                URL url = new URL(base, "uploads/" + uuid + ".schematic");
                                schematic = SchematicHandler.manager.getSchematic(url);
                            } catch (Exception e) {
                                e.printStackTrace();
                                sendMessage(plr, C.SCHEMATIC_INVALID, "non-existent url: " + location);
                                SchematicCmd.this.running = false;
                                return;
                            }
                        } else {
                            schematic = SchematicHandler.manager.getSchematic(location);
                        }
                        if (schematic == null) {
                            SchematicCmd.this.running = false;
                            sendMessage(plr, C.SCHEMATIC_INVALID, "non-existent or not in gzip format");
                            return;
                        }
                        SchematicHandler.manager.paste(schematic, plot, 0, 0, 0, true, new RunnableVal<Boolean>() {
                            @Override
                            public void run(Boolean value) {
                                SchematicCmd.this.running = false;
                                if (value) {
                                    sendMessage(plr, C.SCHEMATIC_PASTE_SUCCESS);
                                } else {
                                    sendMessage(plr, C.SCHEMATIC_PASTE_FAILED);
                                }
                            }
                        });
                    }
                });
                break;
            }
            //            TODO test
            //            case "test": {
            //                if (!Permissions.hasPermission(plr, "plots.schematic.test")) {
            //                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.schematic.test");
            //                    return false;
            //                }
            //                if (args.length < 2) {
            //                    sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
            //                    return false;
            //                }
            //                final Location loc = plr.getLocation();
            //                final Plot plot = MainUtil.getPlot(loc);
            //                if (plot == null) {
            //                    sendMessage(plr, C.NOT_IN_PLOT);
            //                    return false;
            //                }
            //                file = args[1];
            //                schematic = SchematicHandler.manager.getSchematic(file);
            //                if (schematic == null) {
            //                    sendMessage(plr, C.SCHEMATIC_INVALID, "non-existent");
            //                    return false;
            //                }
            //                final int l1 = schematic.getSchematicDimension().getX();
            //                final int l2 = schematic.getSchematicDimension().getZ();
            //                final int length = MainUtil.getPlotWidth(loc.getWorld(), plot.id);
            //                if ((l1 < length) || (l2 < length)) {
            //                    sendMessage(plr, C.SCHEMATIC_INVALID, String.format("Wrong size (x: %s, z: %d) vs %d ", l1, l2, length));
            //                    break;
            //                }
            //                sendMessage(plr, C.SCHEMATIC_VALID);
            //                break;
            //            }
            case "saveall":
            case "exportall": {
                if (!ConsolePlayer.isConsole(plr)) {
                    MainUtil.sendMessage(plr, C.NOT_CONSOLE);
                    return false;
                }
                if (args.length != 2) {
                    MainUtil.sendMessage(plr, "&cNeed world arg. Use &7/plots sch exportall <area>");
                    return false;
                }
                PlotArea area = PS.get().getPlotAreaByString(args[1]);
                if (area == null) {
                    C.NOT_VALID_PLOT_WORLD.send(plr, args[1]);
                    return false;
                }
                Collection<Plot> plots = area.getPlots();
                if (plots.isEmpty()) {
                    MainUtil.sendMessage(plr, "&cInvalid world. Use &7/plots sch exportall <area>");
                    return false;
                }
                boolean result = SchematicHandler.manager.exportAll(plots, null, null, new Runnable() {
                    @Override
                    public void run() {
                        MainUtil.sendMessage(plr, "&aFinished mass export");
                    }
                });
                if (!result) {
                    MainUtil.sendMessage(plr, "&cTask is already running.");
                    return false;
                } else {
                    MainUtil.sendMessage(plr, "&3PlotSquared&8->&3Schemaitc&8: &7Mass export has started. This may take a while.");
                    MainUtil.sendMessage(plr, "&3PlotSquared&8->&3Schemaitc&8: &7Found &c" + plots.size() + "&7 plots...");
                }
                break;
            }
            case "export":
            case "save": {
                if (!Permissions.hasPermission(plr, "plots.schematic.save")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.schematic.save");
                    return false;
                }
                if (this.running) {
                    MainUtil.sendMessage(plr, "&cTask is already running.");
                    return false;
                }
                Plot p2;
                Location loc = plr.getLocation();
                Plot plot = loc.getPlotAbs();
                if (plot == null) {
                    return !sendMessage(plr, C.NOT_IN_PLOT);
                }
                if (!plot.hasOwner()) {
                    MainUtil.sendMessage(plr, C.PLOT_UNOWNED);
                    return false;
                }
                if (!plot.isOwner(plr.getUUID()) && !Permissions.hasPermission(plr, "plots.admin.command.schematic.save")) {
                    MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
                    return false;
                }
                p2 = plot;
                loc.getWorld();
                Collection<Plot> plots = new ArrayList<Plot>();
                plots.add(p2);
                boolean result = SchematicHandler.manager.exportAll(plots, null, null, new Runnable() {
                    @Override
                    public void run() {
                        MainUtil.sendMessage(plr, "&aFinished export");
                        SchematicCmd.this.running = false;
                    }
                });
                if (!result) {
                    MainUtil.sendMessage(plr, "&cTask is already running.");
                    return false;
                } else {
                    MainUtil.sendMessage(plr, "&7Starting export...");
                }
                break;
            }
            default: {
                sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
                break;
            }
        }
        return true;
    }
}
