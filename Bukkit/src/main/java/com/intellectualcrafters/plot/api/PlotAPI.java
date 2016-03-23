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

package com.intellectualcrafters.plot.api;

import com.intellectualcrafters.configuration.file.YamlConfiguration;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.commands.SubCommand;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.flag.AbstractFlag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SetQueue;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;
import com.plotsquared.bukkit.util.BukkitUtil;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * PlotSquared API<br>
 * <br>
 * @version API 3.3.1
 * <br>
 * Useful classes:<br>
 * @see BukkitUtil
 * @see PlotPlayer
 * @see Plot
 * @see com.intellectualcrafters.plot.object.Location
 * @see PlotArea
 * @see PS
 */
public class PlotAPI {

    /**
     * Permission that allows for admin access, this permission node will allow the player to use any part of the
     * plugin, without limitations.
     * @deprecated Use C.PERMISSION_ADMIN instead
     */
    @Deprecated
    public static final String ADMIN_PERMISSION = C.PERMISSION_ADMIN.s();

    /**
     * @deprecated Use new PlotAPI() instead
     */
    @Deprecated
    public PlotAPI(final JavaPlugin plugin) {}

    /**
     * @see PS
     *
     * @deprecated Use this class if you just want to do a few simple things.<br>
     *  - It will remain stable for future versions of the plugin
     *  - The PlotPlayer and Plot class should be considered relatively safe
     *  - For more advanced/intensive tasks you should consider using other classes
     *
     *
     */
    @Deprecated
    public PlotAPI() {}

    /**
     * Get all plots
     *
     * @return all plots
     *
     * @see PS#getPlots()
     */
    public Set<Plot> getAllPlots() {
        return PS.get().getPlots();
    }

    /**
     * Return all plots for a player
     *
     * @param player Player, whose plots to search for
     *
     * @return all plots that a player owns
     */
    public Set<Plot> getPlayerPlots(final Player player) {
        return PS.get().getPlots(BukkitUtil.getPlayer(player));
    }

    /**
     * Add a plot world
     *
     * @param plotArea Plot World Object
     * @see PS#addPlotArea(PlotArea)
     */
    public void addPlotArea(final PlotArea plotArea) {
        PS.get().addPlotArea(plotArea);
    }

    /**
     * @return main configuration
     *
     * @see PS#config
     */
    public YamlConfiguration getConfig() {
        return PS.get().config;
    }

    /**
     * @return storage configuration
     *
     * @see PS#storage
     */
    public YamlConfiguration getStorage() {
        return PS.get().storage;
    }

    /**
     * Get the main class for this plugin <br> - Contains a lot of fields and methods - not very well organized <br>
     * Only use this if you really need it
     *
     * @return PlotSquared PlotSquared Main Class
     *
     * @see PS
     */
    public PS getMain() {
        return PS.get();
    }

    /**
     * ChunkManager class contains several useful methods<br>
     *  - Chunk deletion<br>
     *  - Moving or copying regions<br>
     *  - plot swapping<br>
     *  - Entity tracking<br>
     *  - region regeneration<br>
     *
     * @return ChunkManager
     *
     * @see com.intellectualcrafters.plot.util.ChunkManager
     */
    public ChunkManager getChunkManager() {
        return ChunkManager.manager;
    }

    /**
     * Get the block/biome set queue
     * @return SetQueue.IMP
     */
    public SetQueue getSetQueue() {
        return SetQueue.IMP;
    }

    /**
     * UUIDWrapper class has basic methods for getting UUIDS (it's recommended to use the UUIDHandler class instead)
     *
     * @return UUIDWrapper
     *
     * @see com.intellectualcrafters.plot.uuid.UUIDWrapper
     */
    public UUIDWrapper getUUIDWrapper() {
        return UUIDHandler.getUUIDWrapper();
    }

    /**
     * Do not use this. Instead use FlagManager.[method] in your code.
     *  - Flag related stuff
     *
     * @return FlagManager
     *
     * @see com.intellectualcrafters.plot.flag.FlagManager
     */
    @Deprecated
    public FlagManager getFlagManager() {
        return new FlagManager();
    }

    /**
     * Do not use this. Instead use MainUtil.[method] in your code.
     *  - Basic plot management stuff
     *
     * @return MainUtil
     *
     * @see MainUtil
     */
    @Deprecated
    public MainUtil getMainUtil() {
        return new MainUtil();
    }

    /**
     * Do not use this. Instead use C.PERMISSION_[method] in your code.
     *  - Basic permission management stuff
     *
     * @return Array of strings
     *
     * @see com.intellectualcrafters.plot.util.Permissions
     */
    @Deprecated
    public String[] getPermissions() {
        final ArrayList<String> perms = new ArrayList<>();
        for (final C c : C.values()) {
            if ("static.permissions".equals(c.getCat())) {
                perms.add(c.s());
            }
        }
        return perms.toArray(new String[perms.size()]);
    }

    /**
     * SchematicHandler class contains methods related to pasting, reading and writing schematics
     *
     * @return SchematicHandler
     *
     * @see com.intellectualcrafters.plot.util.SchematicHandler
     */
    public SchematicHandler getSchematicHandler() {
        return SchematicHandler.manager;
    }

    /**
     * Use C.[caption] instead
     *
     * @return C
     *
     * @see com.intellectualcrafters.plot.config.C
     */
    @Deprecated
    public C[] getCaptions() {
        return C.values();
    }

    /**
     * Get the plot manager for a world. - Most of these methods can be accessed through the MainUtil
     *
     * @param world Which manager to get
     *
     * @return PlotManager
     *
     * @see com.intellectualcrafters.plot.object.PlotManager
     * @see PS#getPlotManager(Plot)
     */
    @Deprecated
    public PlotManager getPlotManager(final World world) {
        if (world == null) {
            return null;
        }
        return getPlotManager(world.getName());
    }

    public Set<PlotArea> getPlotAreas(World world) {
        if (world == null) {
            return new HashSet<>();
        }
        return PS.get().getPlotAreas(world.getName());
    }

    /**
     * Get the plot manager for a world. - Contains useful low level methods for plot merging, clearing, and
     * tessellation
     *
     * @param world
     *
     * @return PlotManager
     *
     * @see PS#getPlotManager(Plot)
     * @see com.intellectualcrafters.plot.object.PlotManager
     */
    @Deprecated
    public PlotManager getPlotManager(final String world) {
        Set<PlotArea> areas = PS.get().getPlotAreas(world);
        switch (areas.size()) {
            case 0:
                return null;
            case 1:
                return areas.iterator().next().manager;
            default:
                PS.debug("PlotAPI#getPlotManager(org.bukkit.World) is deprecated and doesn't support multi plot area worlds.");
                return null;
        }
    }

    /**
     * Get the settings for a world (settings bundled in PlotArea class) - You will need to downcast for the specific
     * settings a Generator has. e.g. DefaultPlotWorld class implements PlotArea
     *
     * @param world (to get settings of)
     *
     * @return PlotArea class for that world ! will return null if not a plot world world
     *
     * @see #getPlotAreas(World)
     * @see com.intellectualcrafters.plot.object.PlotArea
     */
    @Deprecated
    public PlotArea getWorldSettings(final World world) {
        if (world == null) {
            return null;
        }
        return getWorldSettings(world.getName());
    }

    /**
     * Get the settings for a world (settings bundled in PlotArea class)
     *
     * @param world (to get settings of)
     *
     * @return PlotArea class for that world ! will return null if not a plot world world
     *
     * @see PS#getPlotArea(String, String)
     * @see com.intellectualcrafters.plot.object.PlotArea
     */
    @Deprecated
    public PlotArea getWorldSettings(final String world) {
        if (world == null) {
            return null;
        }
        Set<PlotArea> areas = PS.get().getPlotAreas(world);
        switch (areas.size()) {
            case 0:
                return null;
            case 1:
                return areas.iterator().next();
            default:
                PS.debug("PlotAPI#getWorldSettings(org.bukkit.World) is deprecated and doesn't support multi plot area worlds.");
                return null;
        }
    }

    /**
     * Send a message to a player.
     *
     * @param player Player that will receive the message
     * @param c      (Caption)
     *
     * @see MainUtil#sendMessage(PlotPlayer, C, String...)
     * com.intellectualcrafters.plot.config.C, String...)
     */
    public void sendMessage(final Player player, final C c) {
        MainUtil.sendMessage(BukkitUtil.getPlayer(player), c);
    }

    /**
     * Send a message to a player. - Supports color codes
     *
     * @param player Player that will receive the message
     * @param string The message
     *
     * @see MainUtil#sendMessage(PlotPlayer, String)
     */
    public void sendMessage(final Player player, final String string) {
        MainUtil.sendMessage(BukkitUtil.getPlayer(player), string);
    }

    /**
     * Send a message to the console. - Supports color codes
     *
     * @param msg Message that should be sent to the console
     *
     * @see MainUtil#sendConsoleMessage(C, String...)
     */
    public void sendConsoleMessage(final String msg) {
        PS.log(msg);
    }

    /**
     * Send a message to the console
     *
     * @param c (Caption)
     *
     * @see #sendConsoleMessage(String)
     * @see com.intellectualcrafters.plot.config.C
     */
    public void sendConsoleMessage(final C c) {
        sendConsoleMessage(c);
    }

    /**
     * Register a flag for use in plots
     *
     * @param flag Flag that should be registered
     *
     * @see com.intellectualcrafters.plot.flag.FlagManager#addFlag(com.intellectualcrafters.plot.flag.AbstractFlag)
     * @see com.intellectualcrafters.plot.flag.AbstractFlag
     */
    public void addFlag(final AbstractFlag flag) {
        FlagManager.addFlag(flag);
    }

    /**
     * get all the currently registered flags
     *
     * @return array of Flag[]
     *
     * @see com.intellectualcrafters.plot.flag.FlagManager#getFlags()
     * @see com.intellectualcrafters.plot.flag.AbstractFlag
     */
    public AbstractFlag[] getFlags() {
        return FlagManager.getFlags().toArray(new AbstractFlag[FlagManager.getFlags().size()]);
    }

    /**
     * Get a plot based on the ID
     *
     * @param world World in which the plot is located
     * @param x     Plot Location X Co-ord
     * @param z     Plot Location Z Co-ord
     *
     * @return plot, null if ID is wrong
     *
     * @see PlotArea#getPlot(PlotId)
     */
    @Deprecated
    public Plot getPlot(final World world, final int x, final int z) {
        if (world == null) {
            return null;
        }
        PlotArea area = getWorldSettings(world);
        if (area == null) {
            return null;
        }
        return area.getPlot(new PlotId(x, z));
    }

    /**
     * Get a plot based on the location
     *
     * @param l The location that you want to to retrieve the plot from
     *
     * @return plot if found, otherwise it creates a temporary plot-
     *
     * @see Plot
     */
    public Plot getPlot(final Location l) {
        if (l == null) {
            return null;
        }
        return BukkitUtil.getLocation(l).getPlot();
    }

    /**
     * Get a plot based on the player location
     *
     * @param player Get the current plot for the player location
     *
     * @return plot if found, otherwise it creates a temporary plot
     *
     * @see #getPlot(org.bukkit.Location)
     * @see Plot
     */
    public Plot getPlot(final Player player) {
        return this.getPlot(player.getLocation());
    }

    /**
     * Check whether or not a player has a plot
     *
     * @param player Player that you want to check for
     *
     * @return true if player has a plot, false if not.
     *
     * @see #getPlots(World, Player, boolean)
     */
    @Deprecated
    public boolean hasPlot(final World world, final Player player) {
        return getPlots(world, player, true) != null && getPlots(world, player, true).length > 0;
    }

    /**
     * Get all plots for the player
     *
     * @param plr        to search for
     * @param just_owner should we just search for owner? Or with rights?
     */
    @Deprecated
    public Plot[] getPlots(final World world, final Player plr, final boolean just_owner) {
        final ArrayList<Plot> pPlots = new ArrayList<>();
        UUID uuid = BukkitUtil.getPlayer(plr).getUUID();
        for (final Plot plot : PS.get().getPlots(world.getName())) {
            if (just_owner) {
                if (plot.hasOwner() && plot.isOwner(uuid)) {
                    pPlots.add(plot);
                }
            } else {
                if (plot.isAdded(uuid)) {
                    pPlots.add(plot);
                }
            }
        }
        return pPlots.toArray(new Plot[pPlots.size()]);
    }

    /**
     * Get all plots for the world
     *
     * @param world to get plots of
     *
     * @return Plot[] - array of plot objects in world
     *
     * @see PS#getPlots(String)
     * @see Plot
     */
    @Deprecated
    public Plot[] getPlots(final World world) {
        if (world == null) {
            return new Plot[0];
        }
        Collection<Plot> plots = PS.get().getPlots(world.getName());
        return plots.toArray(new Plot[plots.size()]);
    }

    /**
     * Get all plot worlds
     *
     * @return World[] - array of plot worlds
     *
     */
    @Deprecated
    public String[] getPlotWorlds() {
        Set<String> plotWorldStrings = PS.get().getPlotWorldStrings();
        return plotWorldStrings.toArray(new String[plotWorldStrings.size()]);
    }

    /**
     * Get if plot world
     *
     * @param world (to check if plot world)
     *
     * @return boolean (if plot world or not)
     *
     * @see PS#hasPlotArea(String)
     */
    @Deprecated
    public boolean isPlotWorld(final World world) {
        return PS.get().hasPlotArea(world.getName());
    }

    /**
     * Get plot locations
     *
     * @param p Plot that you want to get the locations for
     *
     * @return [0] = bottomLc, [1] = topLoc, [2] = home
     *
     * @deprecated As merged plots may not have a rectangular shape
     *
     * @see Plot
     */
    @Deprecated
    public Location[] getLocations(final Plot p) {
        return new Location[] { BukkitUtil.getLocation(p.getBottom()), BukkitUtil.getLocation(p.getTop()), BukkitUtil.getLocation(p.getHome()) };
    }

    /**
     * Get home location
     *
     * @param p Plot that you want to get the location for
     *
     * @return plot bottom location
     *
     * @see Plot
     */
    public Location getHomeLocation(final Plot p) {
        return BukkitUtil.getLocation(p.getHome());
    }

    /**
     * Get Bottom Location (min, min, min)
     *
     * @param p Plot that you want to get the location for
     *
     * @return plot bottom location
     *
     * @deprecated As merged plots may not have a rectangular shape
     *
     * @see Plot
     */
    @Deprecated
    public Location getBottomLocation(final Plot p) {
        return BukkitUtil.getLocation(p.getBottom());
    }

    /**
     * Get Top Location (max, max, max)
     *
     * @param p Plot that you want to get the location for
     *
     * @return plot top location
     *
     * @deprecated As merged plots may not have a rectangular shape
     *
     * @see Plot
     */
    @Deprecated
    public Location getTopLocation(final Plot p) {
        return BukkitUtil.getLocation(p.getTop());
    }

    /**
     * Check whether or not a player is in a plot
     *
     * @param player who we're checking for
     *
     * @return true if the player is in a plot, false if not-
     *
     */
    public boolean isInPlot(final Player player) {
        return getPlot(player) != null;
    }

    /**
     * Register a subcommand
     *
     * @param c SubCommand, that we want to register
     *
     * @see com.intellectualcrafters.plot.commands.SubCommand
     */
    public void registerCommand(final SubCommand c) {
        if (c.getCommand() != null) {
            MainCommand.getInstance().addCommand(c);
        } else {
            MainCommand.getInstance().createCommand(c);
        }
    }

    /**
     * Get the PlotSquared class
     *
     * @return PlotSquared Class
     *
     * @see PS
     */
    public PS getPlotSquared() {
        return PS.get();
    }

    /**
     * Get the player plot count
     *
     * @param world  Specify the world we want to select the plots from
     * @param player Player, for whom we're getting the plot count
     *
     * @return the number of plots the player has
     *
     */
    public int getPlayerPlotCount(final World world, final Player player) {
        if (world == null) {
            return 0;
        }
        return BukkitUtil.getPlayer(player).getPlotCount(world.getName());
    }

    /**
     * Get a collection containing the players plots
     *
     * @param world  Specify the world we want to select the plots from
     * @param player Player, for whom we're getting the plots
     *
     * @return a set containing the players plots
     *
     * @see PS#getPlots(String, PlotPlayer)
     *
     * @see Plot
     */
    public Set<Plot> getPlayerPlots(final World world, final Player player) {
        if (world == null) {
            return new HashSet<>();
        }
        return BukkitUtil.getPlayer(player).getPlots(world.getName());
    }

    /**
     * Get the numbers of plots, which the player is able to build in
     *
     * @param player Player, for whom we're getting the plots (trusted, member and owner)
     *
     * @return the number of allowed plots
     *
     */
    public int getAllowedPlots(final Player player) {
        final PlotPlayer pp = BukkitUtil.getPlayer(player);
        return pp.getAllowedPlots();
    }

    /**
     * Get the PlotPlayer for a player<br>
     *  - The PlotPlayer is usually cached and will provide useful functions relating to players
     *
     * @see PlotPlayer#wrap(Object)
     *
     * @param player
     * @return
     */
    public PlotPlayer wrapPlayer(final Player player) {
        return PlotPlayer.wrap(player);
    }

    /**
     * Get the PlotPlayer for a UUID (Please note that PlotSquared can be configured to provide different UUIDs than bukkit)
     *
     * @see PlotPlayer#wrap(Object)
     *
     * @param uuid
     * @return
     */
    public PlotPlayer wrapPlayer(final UUID uuid) {
        return PlotPlayer.wrap(uuid);
    }

    /**
     * Get the PlotPlayer for a username
     *
     * @see PlotPlayer#wrap(Object)
     *
     * @param player
     * @return
     */
    public PlotPlayer wrapPlayer(final String player) {
        return PlotPlayer.wrap(player);
    }

    /**
     * Get the PlotPlayer for an offline player<br>
     * Note that this will work if the player is offline, however not all functionality will work
     *
     * @see PlotPlayer#wrap(Object)
     *
     * @param player
     * @return
     */
    public PlotPlayer wrapPlayer(final OfflinePlayer player) {
        return PlotPlayer.wrap(player);
    }
}
