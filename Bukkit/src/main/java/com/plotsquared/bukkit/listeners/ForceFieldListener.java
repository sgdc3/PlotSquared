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
package com.plotsquared.bukkit.listeners;

import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.bukkit.events.PlayerEnterPlotEvent;
import com.plotsquared.bukkit.object.BukkitPlayer;
import com.plotsquared.bukkit.util.BukkitUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ForceFieldListener implements Listener {

    private Set<PlotPlayer> getNearbyPlayers(Player player, Plot plot) {
        Set<PlotPlayer> players = new HashSet<>();
        for (Entity entity : player.getNearbyEntities(5d, 5d, 5d)) {
            PlotPlayer pp;
            if (!(entity instanceof Player) || ((pp = BukkitUtil.getPlayer((Player) entity)) == null) || !plot.equals(pp.getCurrentPlot())) {
                continue;
            }
            if (!plot.isAdded(pp.getUUID())) {
                players.add(pp);
            }
        }
        return players;
    }

    private PlotPlayer hasNearbyPermitted(Player player, Plot plot) {
        for (Entity entity : player.getNearbyEntities(5d, 5d, 5d)) {
            PlotPlayer pp;
            if (!(entity instanceof Player) || ((pp = BukkitUtil.getPlayer((Player) entity)) == null) || !plot.equals(pp.getCurrentPlot())) {
                continue;
            }
            if (plot.isAdded(pp.getUUID())) {
                return pp;
            }
        }
        return null;
    }

    public Vector calculateVelocity(PlotPlayer pp, PlotPlayer e) {
        Location playerLocation = pp.getLocationFull();
        Location oPlayerLocation = e.getLocation();
        double playerX = playerLocation.getX();
        double playerY = playerLocation.getY();
        double playerZ = playerLocation.getZ();
        double oPlayerX = oPlayerLocation.getX();
        double oPlayerY = oPlayerLocation.getY();
        double oPlayerZ = oPlayerLocation.getZ();
        double x = 0d;
        if (playerX < oPlayerX) {
            x = 1.0d;
        } else if (playerX > oPlayerX) {
            x = -1.0d;
        }
        double y = 0d;
        if (playerY < oPlayerY) {
            y = 0.5d;
        } else if (playerY > oPlayerY) {
            y = -0.5d;
        }
        double z = 0d;
        if (playerZ < oPlayerZ) {
            z = 1.0d;
        } else if (playerZ > oPlayerZ) {
            z = -1.0d;
        }
        return new Vector(x, y, z);
    }

    @EventHandler
    public void onPlotEntry(PlayerEnterPlotEvent event) {
        Player player = event.getPlayer();
        PlotPlayer pp = BukkitUtil.getPlayer(player);
        Plot plot = event.getPlot();
        if (plot == null) {
            return;
        }
        if ((FlagManager.getPlotFlagRaw(plot, "forcefield") != null) && FlagManager.getPlotFlagRaw(plot, "forcefield").getValue().equals("true")) {
            if (!FlagManager.isBooleanFlag(plot, "forcefield", false)) {
                UUID uuid = pp.getUUID();
                if (plot.isAdded(uuid)) {
                    Set<PlotPlayer> players = getNearbyPlayers(player, plot);
                    for (PlotPlayer oPlayer : players) {
                        ((BukkitPlayer) oPlayer).player.setVelocity(calculateVelocity(pp, oPlayer));
                    }
                } else {
                    PlotPlayer oPlayer = hasNearbyPermitted(player, plot);
                    if (oPlayer == null) {
                        return;
                    }
                    player.setVelocity(calculateVelocity(oPlayer, pp));
                }
            }
        }
    }
}
