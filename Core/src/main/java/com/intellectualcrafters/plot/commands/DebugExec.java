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

import com.google.common.io.Files;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotAnalysis;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.AbstractTitle;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.ExpireManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SetQueue;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;
import com.plotsquared.listener.WEManager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

@CommandDeclaration(command = "debugexec",
        permission = "plots.admin",
        description = "Mutli-purpose debug command",
        aliases = "exec",
        category = CommandCategory.DEBUG)
public class DebugExec extends SubCommand {

    private ScriptEngine engine;
    private Bindings scope;

    public DebugExec() {
        try {
            if (PS.get() != null) {
                File file = new File(PS.get().IMP.getDirectory(), "scripts" + File.separator + "start.js");
                if (file.exists()) {
                    init();
                    String script = StringMan.join(Files
                                    .readLines(new File(new File(PS.get().IMP.getDirectory() + File.separator + "scripts"), "start.js"),
                                            StandardCharsets.UTF_8),
                            System.getProperty("line.separator"));
                    this.scope.put("THIS", this);
                    this.scope.put("PlotPlayer", ConsolePlayer.getConsole());
                    this.engine.eval(script, this.scope);
                }
            }
        } catch (IOException | ScriptException e) {
        }
    }

    public ScriptEngine getEngine() {
        return this.engine;
    }

    public Bindings getScope() {
        return this.scope;
    }

    public void init() {
        if (this.engine != null) {
            return;
        }
        this.engine = new ScriptEngineManager(null).getEngineByName("nashorn");
        if (this.engine == null) {
            this.engine = new ScriptEngineManager(null).getEngineByName("JavaScript");
        }
        ScriptContext context = new SimpleScriptContext();
        this.scope = context.getBindings(ScriptContext.ENGINE_SCOPE);

        // stuff
        this.scope.put("MainUtil", new MainUtil());
        this.scope.put("Settings", new Settings());
        this.scope.put("StringMan", new StringMan());
        this.scope.put("MathMan", new MathMan());
        this.scope.put("FlagManager", new FlagManager());

        // Classes
        this.scope.put("Location", Location.class);
        this.scope.put("PlotBlock", PlotBlock.class);
        this.scope.put("Plot", Plot.class);
        this.scope.put("PlotId", PlotId.class);
        this.scope.put("Runnable", Runnable.class);
        this.scope.put("RunnableVal", RunnableVal.class);

        // Instances
        this.scope.put("PS", PS.get());
        this.scope.put("SetQueue", SetQueue.IMP);
        this.scope.put("ExpireManager", ExpireManager.IMP);
        if (PS.get().worldedit != null) {
            this.scope.put("WEManager", new WEManager());
        }
        this.scope.put("TaskManager", PS.get().TASK);
        this.scope.put("TitleManager", AbstractTitle.TITLE_CLASS);
        this.scope.put("ConsolePlayer", ConsolePlayer.getConsole());
        this.scope.put("SchematicHandler", SchematicHandler.manager);
        this.scope.put("ChunkManager", ChunkManager.manager);
        this.scope.put("BlockManager", WorldUtil.IMP);
        this.scope.put("SetupUtils", SetupUtils.manager);
        this.scope.put("EventUtil", EventUtil.manager);
        this.scope.put("EconHandler", EconHandler.manager);
        this.scope.put("UUIDHandler", UUIDHandler.implementation);
        this.scope.put("DBFunc", DBFunc.dbManager);
        this.scope.put("HybridUtils", HybridUtils.manager);
        this.scope.put("IMP", PS.get().IMP);
        this.scope.put("MainCommand", MainCommand.getInstance());

        // enums
        for (Enum<?> value : C.values()) {
            this.scope.put("C_" + value.name(), value);
        }
    }

    @Override
    public boolean onCommand(final PlotPlayer player, String... args) {
        java.util.List<String> allowed_params =
                Arrays.asList("calibrate-analysis", "remove-flag", "stop-expire", "start-expire", "show-expired", "update-expired", "seen");
        if (args.length > 0) {
            String arg = args[0].toLowerCase();
            String script;
            boolean async = false;
            switch (arg) {
                case "analyze": {
                    Plot plot = player.getCurrentPlot();
                    if (plot == null) {
                        MainUtil.sendMessage(player, C.NOT_IN_PLOT);
                        return false;
                    }
                    PlotAnalysis analysis = plot.getComplexity();
                    if (analysis != null) {
                        int complexity = analysis.getComplexity();
                        MainUtil.sendMessage(player, "Changes/column: " + analysis.changes / 1.0);
                        MainUtil.sendMessage(player, "Complexity: " + complexity);
                        return true;
                    }
                    MainUtil.sendMessage(player, "$1Starting task...");
                    HybridUtils.manager.analyzePlot(plot, new RunnableVal<PlotAnalysis>() {
                        @Override
                        public void run(PlotAnalysis value) {
                            MainUtil.sendMessage(player, "$1Done: $2Use $3/plot debugexec analyze$2 for more information");
                        }
                    });
                    return true;
                }
                case "calibrate-analysis":
                    if (args.length != 2) {
                        MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot debugexec analyze <threshold>");
                        MainUtil.sendMessage(player,
                                "$1<threshold> $2= $1The percentage of plots you want to clear (100 clears 100% of plots so no point calibrating "
                                        + "it)");
                        return false;
                    }
                    double threshold;
                    try {
                        threshold = Integer.parseInt(args[1]) / 100d;
                    } catch (NumberFormatException e) {
                        MainUtil.sendMessage(player, "$2Invalid threshold: " + args[1]);
                        MainUtil.sendMessage(player, "$1<threshold> $2= $1The percentage of plots you want to clear as a number between 0 - 100");
                        return false;
                    }
                    PlotAnalysis.calcOptimalModifiers(new Runnable() {
                        @Override
                        public void run() {
                            MainUtil.sendMessage(player, "$1Thank you for calibrating PlotSquared plot expiry");
                        }
                    }, threshold);
                    return true;
                case "stop-expire":
                    if (ExpireManager.IMP == null || !ExpireManager.IMP.cancelTask()) {
                        return MainUtil.sendMessage(player, "Task already halted");
                    }
                    return MainUtil.sendMessage(player, "Cancelled task.");
                case "remove-flag":
                    if (args.length != 2) {
                        MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot debugexec remove-flag <flag>");
                        return false;
                    }
                    String flag = args[1];
                    for (Plot plot : PS.get().getBasePlots()) {
                        if (FlagManager.getPlotFlagRaw(plot, flag) != null) {
                            FlagManager.removePlotFlag(plot, flag);
                        }
                    }
                    return MainUtil.sendMessage(player, "Cleared flag: " + flag);
                case "start-rgar": {
                    if (args.length != 2) {
                        MainUtil.sendMessage(player, "&cInvalid syntax: /plot debugexec start-rgar <world>");
                        return false;
                    }
                    PlotArea area = PS.get().getPlotAreaByString(args[1]);
                    if (area == null) {
                        MainUtil.sendMessage(player, C.NOT_VALID_PLOT_WORLD, args[1]);
                        return false;
                    }
                    boolean result;
                    if (HybridUtils.regions != null) {
                        result = HybridUtils.manager.scheduleRoadUpdate(area, HybridUtils.regions, 0);
                    } else {
                        result = HybridUtils.manager.scheduleRoadUpdate(area, 0);
                    }
                    if (!result) {
                        MainUtil.sendMessage(player, "&cCannot schedule mass schematic update! (Is one already in progress?)");
                        return false;
                    }
                    return true;
                }
                case "stop-rgar":
                    if (!HybridUtils.UPDATE) {
                        MainUtil.sendMessage(player, "&cTask not running!");
                        return false;
                    }
                    HybridUtils.UPDATE = false;
                    MainUtil.sendMessage(player, "&cCancelling task... (Please wait)");
                    return true;
                case "start-expire":
                    if (ExpireManager.IMP == null) {
                        ExpireManager.IMP = new ExpireManager();
                    }
                    boolean result;
                    if (Settings.AUTO_CLEAR_CONFIRMATION) {
                        result = ExpireManager.IMP.runConfirmedTask();
                    } else {
                        result = ExpireManager.IMP.runAutomatedTask();
                    }
                    if (result) {
                        return MainUtil.sendMessage(player, "Started plot expiry task");
                    } else {
                        return MainUtil.sendMessage(player, "Plot expiry task already started");
                    }
                case "seen":
                    if (args.length != 2) {
                        return MainUtil.sendMessage(player, "Use /plot debugexec seen <player>");
                    }
                    UUID uuid = UUIDHandler.getUUID(args[1], null);
                    if (uuid == null) {
                        return MainUtil.sendMessage(player, "Player not found: " + args[1]);
                    }
                    OfflinePlotPlayer op = UUIDHandler.getUUIDWrapper().getOfflinePlayer(uuid);
                    if (op == null || op.getLastPlayed() == 0) {
                        return MainUtil.sendMessage(player, "Player hasn't connected before: " + args[1]);
                    }
                    Timestamp stamp = new Timestamp(op.getLastPlayed());
                    Date date = new Date(stamp.getTime());
                    MainUtil.sendMessage(player, "PLAYER: " + args[1]);
                    MainUtil.sendMessage(player, "UUID: " + uuid);
                    MainUtil.sendMessage(player, "Object: " + date.toGMTString());
                    MainUtil.sendMessage(player, "GMT: " + date.toGMTString());
                    MainUtil.sendMessage(player, "Local: " + date.toLocaleString());
                    return true;
                case "h":
                case "he":
                case "?":
                case "help":
                    MainUtil.sendMessage(player, "Possible sub commands: /plot debugexec <" + StringMan.join(allowed_params, "|") + ">");
                    return false;
                case "addcmd":
                    try {
                        final String cmd = StringMan.join(Files
                                        .readLines(MainUtil.getFile(new File(PS.get().IMP.getDirectory() + File.separator + "scripts"), args[1]),
                                                StandardCharsets.UTF_8),
                                System.getProperty("line.separator"));
                        Command<PlotPlayer> subcommand = new Command<PlotPlayer>(args[1].split("\\.")[0]) {
                            @Override
                            public boolean onCommand(PlotPlayer plr, String[] args) {
                                try {
                                    DebugExec.this.scope.put("PlotPlayer", plr);
                                    DebugExec.this.scope.put("args", args);
                                    DebugExec.this.engine.eval(cmd, DebugExec.this.scope);
                                    return true;
                                } catch (ScriptException e) {
                                    e.printStackTrace();
                                    MainUtil.sendMessage(player, C.COMMAND_WENT_WRONG);
                                    return false;
                                }
                            }
                        };
                        MainCommand.getInstance().addCommand(subcommand);
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot debugexec addcmd <file>");
                        return false;
                    }
                case "runasync":
                    async = true;
                case "run":
                    try {
                        script = StringMan.join(Files
                                        .readLines(MainUtil.getFile(new File(PS.get().IMP.getDirectory() + File.separator + "scripts"), args[1]),
                                                StandardCharsets.UTF_8),
                                System.getProperty("line.separator"));
                        if (args.length > 2) {
                            HashMap<String, String> replacements = new HashMap<>();
                            for (int i = 2; i < args.length; i++) {
                                replacements.put("%s" + (i - 2), args[i]);
                            }
                            script = StringMan.replaceFromMap(script, replacements);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                    break;
                case "allcmd":
                    if (args.length < 3) {
                        C.COMMAND_SYNTAX.send(player, "/plot debugexec allcmd <condition> <command>");
                        return false;
                    }
                    long start = System.currentTimeMillis();
                    Command<PlotPlayer> cmd = MainCommand.getInstance().getCommand(args[3]);
                    String[] params = Arrays.copyOfRange(args, 4, args.length);
                    if ("true".equals(args[1])) {
                        Location loc = player.getMeta("location");
                        Plot plot = player.getMeta("lastplot");
                        for (Plot current : PS.get().getBasePlots()) {
                            player.setMeta("location", current.getBottomAbs());
                            player.setMeta("lastplot", current);
                            cmd.onCommand(player, params);
                        }
                        if (loc == null) {
                            player.deleteMeta("location");
                        } else {
                            player.setMeta("location", loc);
                        }
                        if (plot == null) {
                            player.deleteMeta("lastplot");
                        } else {
                            player.setMeta("lastplot", plot);
                        }
                        player.sendMessage("&c> " + (System.currentTimeMillis() - start));
                        return true;
                    }
                    init();
                    this.scope.put("_2", params);
                    this.scope.put("_3", cmd);
                    script = "_1=PS.getBasePlots().iterator();while(_1.hasNext()){plot=_1.next();if(" + args[1]
                            + "){PlotPlayer.setMeta(\"location\",plot.getBottomAbs());PlotPlayer.setMeta(\"lastplot\",plot);_3.onCommand"
                            + "(PlotPlayer,_2)}}";

                    break;
                case "all":
                    if (args.length < 3) {
                        C.COMMAND_SYNTAX.send(player, "/plot debugexec all <condition> <code>");
                        return false;
                    }
                    script = "_1=PS.getBasePlots().iterator();while(_1.hasNext()){plot=_1.next();if(" + args[1] + "){" + StringMan
                            .join(Arrays.copyOfRange(args, 2, args.length), " ")
                            + "}}";

                    break;
                default:
                    script = StringMan.join(args, " ");
            }
            if (!ConsolePlayer.isConsole(player)) {
                MainUtil.sendMessage(player, C.NOT_CONSOLE);
                return false;
            }
            init();
            this.scope.put("PlotPlayer", player);
            PS.debug("> " + script);
            try {
                if (async) {
                    final String toExec = script;
                    TaskManager.runTaskAsync(new Runnable() {
                        @Override
                        public void run() {
                            long start = System.currentTimeMillis();
                            Object result = null;
                            try {
                                result = DebugExec.this.engine.eval(toExec, DebugExec.this.scope);
                            } catch (ScriptException e) {
                                e.printStackTrace();
                            }
                            ConsolePlayer.getConsole().sendMessage("> " + (System.currentTimeMillis() - start) + "ms -> " + result);
                        }
                    });
                } else {
                    long start = System.currentTimeMillis();
                    Object result = this.engine.eval(script, this.scope);
                    ConsolePlayer.getConsole().sendMessage("> " + (System.currentTimeMillis() - start) + "ms -> " + result);
                }
                return true;
            } catch (ScriptException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}
