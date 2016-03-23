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
package com.plotsquared.listener;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.AbstractTitle;
import com.intellectualcrafters.plot.util.CommentManager;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.ExpireManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.PlotGameMode;
import com.intellectualcrafters.plot.util.PlotWeather;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotListener {

    public static boolean plotEntry(final PlotPlayer pp, final Plot plot) {
        if (plot.isDenied(pp.getUUID()) && !Permissions.hasPermission(pp, "plots.admin.entry.denied")) {
            return false;
        }
        Plot last = pp.getMeta("lastplot");
        if ((last != null) && !last.getId().equals(plot.getId())) {
            plotExit(pp, last);
        }
        if (ExpireManager.IMP != null) {
            ExpireManager.IMP.handleEntry(pp, plot);
        }
        pp.setMeta("lastplot", plot);
        EventUtil.manager.callEntry(pp, plot);
        if (plot.hasOwner()) {
            HashMap<String, Flag> flags = FlagManager.getPlotFlags(plot);
            int size = flags.size();
            boolean titles = Settings.TITLES;
            final String greeting;

            if (size != 0) {
                Flag titleFlag = flags.get("titles");
                if (titleFlag != null) {
                    titles = (Boolean) titleFlag.getValue();
                }
                Flag greetingFlag = flags.get("greeting");
                if (greetingFlag != null) {
                    greeting = (String) greetingFlag.getValue();
                    MainUtil.format(C.PREFIX_GREETING.s() + greeting, plot, pp, false, new RunnableVal<String>() {
                        @Override
                        public void run(String value) {
                            MainUtil.sendMessage(pp, value);
                        }
                    });
                } else {
                    greeting = "";
                }
                Flag enter = flags.get("notify-enter");
                if (enter != null && (Boolean) enter.getValue()) {
                    if (!Permissions.hasPermission(pp, "plots.flag.notify-enter.bypass")) {
                        for (UUID uuid : plot.getOwners()) {
                            PlotPlayer owner = UUIDHandler.getPlayer(uuid);
                            if (owner != null && !owner.getUUID().equals(pp.getUUID())) {
                                MainUtil.sendMessage(owner,
                                        C.NOTIFY_ENTER.s().replace("%player", pp.getName()).replace("%plot", plot.getId().toString()));
                            }
                        }
                    }
                }
                Flag gamemodeFlag = flags.get("gamemode");
                if (gamemodeFlag != null) {
                    if (pp.getGameMode() != gamemodeFlag.getValue()) {
                        if (!Permissions.hasPermission(pp, "plots.gamemode.bypass")) {
                            pp.setGameMode((PlotGameMode) gamemodeFlag.getValue());
                        } else {
                            MainUtil.sendMessage(pp,
                                    StringMan.replaceAll(C.GAMEMODE_WAS_BYPASSED.s(), "{plot}", plot.getId(), "{gamemode}", gamemodeFlag.getValue()));
                        }
                    }
                }
                Flag flyFlag = flags.get("fly");
                if (flyFlag != null) {
                    pp.setFlight((boolean) flyFlag.getValue());
                }
                Flag timeFlag = flags.get("time");
                if (timeFlag != null) {
                    try {
                        long time = (long) timeFlag.getValue();
                        pp.setTime(time);
                    } catch (Exception e) {
                        FlagManager.removePlotFlag(plot, "time");
                    }
                }
                Flag weatherFlag = flags.get("weather");
                if (weatherFlag != null) {
                    pp.setWeather((PlotWeather) weatherFlag.getValue());
                }

                Flag musicFlag = flags.get("music");
                if (musicFlag != null) {
                    Integer id = (Integer) musicFlag.getValue();
                    if ((id >= 2256 && id <= 2267) || (id == 0)) {
                        Location loc = pp.getLocation();
                        Location lastLoc = pp.getMeta("music");
                        if (lastLoc != null) {
                            pp.playMusic(lastLoc, 0);
                            if (id == 0) {
                                pp.deleteMeta("music");
                            }
                        }
                        if (id != 0) {
                            try {
                                pp.setMeta("music", loc);
                                pp.playMusic(loc, id);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                } else {
                    Location lastLoc = pp.getMeta("music");
                    if (lastLoc != null) {
                        pp.deleteMeta("music");
                        pp.playMusic(lastLoc, 0);
                    }
                }
                CommentManager.sendTitle(pp, plot);
            } else if (titles) {
                greeting = "";
            } else {
                return true;
            }
            if (titles) {
                if (!C.TITLE_ENTERED_PLOT.s().isEmpty() || !C.TITLE_ENTERED_PLOT_SUB.s().isEmpty()) {
                    TaskManager.runTaskLaterAsync(new Runnable() {
                        @Override
                        public void run() {
                            Plot lastPlot = pp.getMeta("lastplot");
                            if ((lastPlot != null) && plot.getId().equals(lastPlot.getId())) {
                                Map<String, String> replacements = new HashMap<>();
                                replacements.put("%x%", lastPlot.getId().x + "");
                                replacements.put("%z%", lastPlot.getId().y + "");
                                replacements.put("%world%", plot.getArea().toString());
                                replacements.put("%greeting%", greeting);
                                replacements.put("%alias", plot.toString());
                                replacements.put("%s", MainUtil.getName(plot.owner));
                                String main = StringMan.replaceFromMap(C.TITLE_ENTERED_PLOT.s(), replacements);
                                String sub = StringMan.replaceFromMap(C.TITLE_ENTERED_PLOT_SUB.s(), replacements);
                                AbstractTitle.sendTitle(pp, main, sub);
                            }
                        }
                    }, 20);
                }
            }
            return true;
        }
        return true;
    }

    public static boolean plotExit(final PlotPlayer pp, Plot plot) {
        pp.deleteMeta("lastplot");
        EventUtil.manager.callLeave(pp, plot);
        if (plot.hasOwner()) {
            PlotArea pw = plot.getArea();
            if (pw == null) {
                return true;
            }
            if (FlagManager.getPlotFlagRaw(plot, "gamemode") != null) {
                if (pp.getGameMode() != pw.GAMEMODE) {
                    if (!Permissions.hasPermission(pp, "plots.gamemode.bypass")) {
                        pp.setGameMode(pw.GAMEMODE);
                    } else {
                        MainUtil.sendMessage(pp, StringMan
                                .replaceAll(C.GAMEMODE_WAS_BYPASSED.s(), "{plot}", plot.toString(), "{gamemode}", pw.GAMEMODE.name().toLowerCase()));
                    }
                }
            }
            Flag farewell = FlagManager.getPlotFlagRaw(plot, "farewell");
            if (farewell != null) {
                MainUtil.format(C.PREFIX_FAREWELL.s() + farewell.getValueString(), plot, pp, false, new RunnableVal<String>() {
                    @Override
                    public void run(String value) {
                        MainUtil.sendMessage(pp, value);
                    }
                });
            }
            Flag leave = FlagManager.getPlotFlagRaw(plot, "notify-leave");
            if ((leave != null) && (Boolean) leave.getValue()) {
                if (!Permissions.hasPermission(pp, "plots.flag.notify-enter.bypass")) {
                    for (UUID uuid : plot.getOwners()) {
                        PlotPlayer owner = UUIDHandler.getPlayer(uuid);
                        if ((owner != null) && !owner.getUUID().equals(pp.getUUID())) {
                            MainUtil.sendMessage(pp, C.NOTIFY_LEAVE.s().replace("%player", pp.getName()).replace("%plot", plot.getId().toString()));
                        }
                    }
                }
            }
            if (FlagManager.getPlotFlagRaw(plot, "fly") != null) {
                PlotGameMode gamemode = pp.getGameMode();
                if (gamemode == PlotGameMode.SURVIVAL || (gamemode == PlotGameMode.ADVENTURE)) {
                    pp.setFlight(false);
                }
            }
            if (FlagManager.getPlotFlagRaw(plot, "time") != null) {
                pp.setTime(Long.MAX_VALUE);
            }
            if (FlagManager.getPlotFlagRaw(plot, "weather") != null) {
                pp.setWeather(PlotWeather.RESET);
            }
            Location lastLoc = pp.getMeta("music");
            if (lastLoc != null) {
                pp.deleteMeta("music");
                pp.playMusic(lastLoc, 0);
            }
        }
        return true;
    }
}
