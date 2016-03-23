package com.plotsquared.bukkit.database.plotme;

import com.google.common.base.Charsets;
import com.intellectualcrafters.configuration.file.FileConfiguration;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.database.SQLite;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.UUIDHandler;

import java.io.File;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

public class ClassicPlotMeConnector extends APlotMeConnector {

    private String plugin;
    private String prefix;

    @Override
    public Connection getPlotMeConnection(String plugin, FileConfiguration plotConfig, String dataFolder) {
        this.plugin = plugin.toLowerCase();
        this.prefix = plotConfig.getString("mySQLprefix", plugin.toLowerCase());
        try {
            if (plotConfig.getBoolean("usemySQL")) {
                String user = plotConfig.getString("mySQLuname");
                String password = plotConfig.getString("mySQLpass");
                String con = plotConfig.getString("mySQLconn");
                return DriverManager.getConnection(con, user, password);
                //                return new MySQL(plotsquared, hostname, port, database, username, password)
            } else {
                return new SQLite(dataFolder + File.separator + "plots.db").openConnection();
            }
        } catch (SQLException | ClassNotFoundException ignored) {
            //ignored
        }
        return null;
    }

    @Override
    public HashMap<String, HashMap<PlotId, Plot>> getPlotMePlots(Connection connection) throws SQLException {
        HashMap<String, Integer> plotWidth = new HashMap<>();
        HashMap<String, Integer> roadWidth = new HashMap<>();
        HashMap<String, HashMap<PlotId, Plot>> plots = new HashMap<>();
        HashMap<String, HashMap<PlotId, boolean[]>> merges = new HashMap<>();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `" + this.prefix + "Plots`");
        ResultSet resultSet = statement.executeQuery();
        String column = null;
        boolean checkUUID = DBFunc.hasColumn(resultSet, "ownerid");
        boolean checkUUID2 = DBFunc.hasColumn(resultSet, "ownerId");
        if (checkUUID) {
            column = "ownerid";
        } else if (checkUUID2) {
            column = "ownerId";
        }
        boolean merge = !"plotme".equalsIgnoreCase(this.plugin) && Settings.CONVERT_PLOTME;
        int missing = 0;
        while (resultSet.next()) {
            PlotId id = new PlotId(resultSet.getInt("idX"), resultSet.getInt("idZ"));
            String name = resultSet.getString("owner");
            String world = LikePlotMeConverter.getWorld(resultSet.getString("world"));
            if (!plots.containsKey(world)) {
                plots.put(world, new HashMap<PlotId, Plot>());
                if (merge) {
                    int plot = PS.get().config.getInt("worlds." + world + ".plot.size");
                    int path = PS.get().config.getInt("worlds." + world + ".road.width");
                    plotWidth.put(world, plot);
                    roadWidth.put(world, path);
                    merges.put(world, new HashMap<PlotId, boolean[]>());
                }
            }
            if (merge) {
                int tx = resultSet.getInt("topX");
                int tz = resultSet.getInt("topZ");
                int bx = resultSet.getInt("bottomX") - 1;
                int bz = resultSet.getInt("bottomZ") - 1;
                int path = roadWidth.get(world);
                int plot = plotWidth.get(world);
                Location top = getPlotTopLocAbs(path, plot, id);
                Location bot = getPlotBottomLocAbs(path, plot, id);
                if (tx > top.getX()) {
                    setMerged(merges, world, id, 1);
                }
                if (tz > top.getZ()) {
                    setMerged(merges, world, id, 2);
                }
                if (bx < bot.getX()) {
                    setMerged(merges, world, id, 3);
                }
                if (bz > bot.getZ()) {
                    setMerged(merges, world, id, 0);
                }
            }
            UUID owner = UUIDHandler.getUUID(name, null);
            if (owner == null) {
                if ("*".equals(name)) {
                    owner = DBFunc.everyone;
                } else {
                    if (checkUUID || checkUUID2) {
                        try {
                            byte[] bytes = resultSet.getBytes(column);
                            if (bytes != null) {
                                try {
                                    ByteBuffer bb = ByteBuffer.wrap(bytes);
                                    long high = bb.getLong();
                                    long low = bb.getLong();
                                    owner = new UUID(high, low);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    owner = UUID.nameUUIDFromBytes(bytes);
                                }
                                UUIDHandler.add(new StringWrapper(name), owner);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    if (owner == null) {
                        if (!name.isEmpty()) {
                            owner = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name.toLowerCase()).getBytes(Charsets.UTF_8));
                        }
                        PS.log("&cCould not identify owner for plot: " + id + " -> '" + name + "'");
                        missing++;
                        continue;
                    }
                }
            } else {
                UUIDHandler.add(new StringWrapper(name), owner);
            }
            Plot plot = new Plot(PlotArea.createGeneric(world), id, owner);
            plots.get(world).put(id, plot);
        }
        if (missing > 0) {
            PS.log("&cSome names could not be identified:");
            PS.log("&7 - Empty quotes mean PlotMe just stored an unowned plot in the database");
            PS.log("&7 - Names you have never seen before could be from people mistyping commands");
            PS.log("&7 - Converting from a non-uuid version of PlotMe can't identify owners if the playerdata files are deleted (these plots will "
                    + "remain unknown until the player connects)");
        }

        for (Entry<String, HashMap<PlotId, boolean[]>> entry : merges.entrySet()) {
            String world = entry.getKey();
            for (Entry<PlotId, boolean[]> entry2 : entry.getValue().entrySet()) {
                HashMap<PlotId, Plot> newplots = plots.get(world);
                Plot plot = newplots.get(entry2.getKey());
                if (plot != null) {
                    plot.setMerged(entry2.getValue());
                }
            }
        }

        resultSet.close();
        statement.close();

        try {

            PS.log(" - " + this.prefix + "Denied");
            statement = connection.prepareStatement("SELECT * FROM `" + this.prefix + "Denied`");
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                PlotId id = new PlotId(resultSet.getInt("idX"), resultSet.getInt("idZ"));
                String name = resultSet.getString("player");
                String world = LikePlotMeConverter.getWorld(resultSet.getString("world"));
                UUID denied = UUIDHandler.getUUID(name, null);
                if (denied == null) {
                    if ("*".equals(name)) {
                        denied = DBFunc.everyone;
                    } else {
                        if (DBFunc.hasColumn(resultSet, "playerid")) {
                            try {
                                byte[] bytes = resultSet.getBytes("playerid");
                                if (bytes != null) {
                                    try {
                                        ByteBuffer bb = ByteBuffer.wrap(bytes);
                                        long high = bb.getLong();
                                        long low = bb.getLong();
                                        denied = new UUID(high, low);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        denied = UUID.nameUUIDFromBytes(bytes);
                                    }
                                    UUIDHandler.add(new StringWrapper(name), denied);
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (denied == null) {
                        PS.log("&6Could not identify denied for plot: " + id);
                        continue;
                    }
                }
                if (plots.get(world).containsKey(id)) {
                    plots.get(world).get(id).getDenied().add(denied);
                }
            }

            statement = connection.prepareStatement("SELECT * FROM `" + this.plugin + "Allowed`");
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                PlotId id = new PlotId(resultSet.getInt("idX"), resultSet.getInt("idZ"));
                String name = resultSet.getString("player");
                String world = LikePlotMeConverter.getWorld(resultSet.getString("world"));
                UUID helper = UUIDHandler.getUUID(name, null);
                if (helper == null) {
                    if ("*".equals(name)) {
                        helper = DBFunc.everyone;
                    } else if (DBFunc.hasColumn(resultSet, "playerid")) {
                        try {
                            byte[] bytes = resultSet.getBytes("playerid");
                            if (bytes != null) {
                                try {
                                    ByteBuffer bb = ByteBuffer.wrap(bytes);
                                    long high = bb.getLong();
                                    long low = bb.getLong();
                                    helper = new UUID(high, low);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    helper = UUID.nameUUIDFromBytes(bytes);
                                }
                                UUIDHandler.add(new StringWrapper(name), helper);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    if (helper == null) {
                        PS.log("&6Could not identify helper for plot: " + id);
                        continue;
                    }
                }
                if (plots.get(world).containsKey(id)) {
                    plots.get(world).get(id).getTrusted().add(helper);
                }
            }

            resultSet.close();
            statement.close();

        } catch (SQLException ignored) {
            //ignored
        }
        return plots;
    }

    @Override
    public boolean accepts(String version) {
        return version == null || PS.get().canUpdate(version, "0.17.0") || PS.get().canUpdate("0.999.999", version);
    }
}
