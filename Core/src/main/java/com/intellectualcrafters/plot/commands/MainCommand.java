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
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.StringComparison;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.helpmenu.HelpMenu;
import com.plotsquared.general.commands.Argument;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandHandlingOutput;
import com.plotsquared.general.commands.CommandManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * PlotSquared command class.
 */
public class MainCommand extends CommandManager<PlotPlayer> {

    private static MainCommand instance;

    private MainCommand() {
        super(null, new ArrayList<Command<PlotPlayer>>());
        instance = this;
        createCommand(new Buy());
        createCommand(new Save());
        createCommand(new Load());
        createCommand(new Confirm());
        createCommand(new Template());
        createCommand(new Download());
        createCommand(new Update());
        createCommand(new Template());
        createCommand(new Setup());
        createCommand(new Area());
        createCommand(new DebugSaveTest());
        createCommand(new DebugLoadTest());
        createCommand(new CreateRoadSchematic());
        createCommand(new DebugAllowUnsafe());
        createCommand(new RegenAllRoads());
        createCommand(new Claim());
        createCommand(new Auto());
        createCommand(new Visit());
        createCommand(new Home());
        createCommand(new Set());
        createCommand(new Toggle());
        createCommand(new Clear());
        createCommand(new Delete());
        createCommand(new Trust());
        createCommand(new Add());
        createCommand(new Deny());
        createCommand(new Untrust());
        createCommand(new Remove());
        createCommand(new Undeny());
        createCommand(new Info());
        createCommand(new com.intellectualcrafters.plot.commands.List());
        createCommand(new Help());
        createCommand(new Debug());
        createCommand(new SchematicCmd());
        createCommand(new Plugin());
        createCommand(new Purge());
        createCommand(new Reload());
        createCommand(new Merge());
        createCommand(new DebugPaste());
        createCommand(new Unlink());
        createCommand(new Kick());
        createCommand(new Rate());
        createCommand(new DebugClaimTest());
        createCommand(new Inbox());
        createCommand(new Comment());
        createCommand(new Database());
        createCommand(new Swap());
        createCommand(new Music());
        createCommand(new DebugRoadRegen());
        createCommand(new Trust());
        createCommand(new DebugExec());
        createCommand(new FlagCmd());
        createCommand(new Target());
        createCommand(new DebugFixFlags());
        createCommand(new Move());
        createCommand(new Condense());
        createCommand(new Condense());
        createCommand(new Copy());
        createCommand(new Chat());
        createCommand(new Trim());
        createCommand(new Done());
        createCommand(new Continue());
        createCommand(new BO3());
        createCommand(new Middle());
        createCommand(new Grant());
        // set commands
        createCommand(new Owner());
        createCommand(new Desc());
        createCommand(new Biome());
        createCommand(new Alias());
        createCommand(new SetHome());
        if (Settings.ENABLE_CLUSTERS) {
            MainCommand.getInstance().addCommand(new Cluster());
        }
    }

    public static MainCommand getInstance() {
        if (instance == null) {
            instance = new MainCommand();
        }
        return instance;
    }

    public static boolean no_permission(PlotPlayer player, String permission) {
        MainUtil.sendMessage(player, C.NO_PERMISSION, permission);
        return false;
    }

    public static List<Command<PlotPlayer>> getCommandAndAliases(CommandCategory category, PlotPlayer player) {
        List<Command<PlotPlayer>> commands = new ArrayList<>();
        for (Command<PlotPlayer> command : getInstance().getCommands()) {
            if ((category != null) && !command.getCategory().equals(category)) {
                continue;
            }
            if ((player != null) && !Permissions.hasPermission(player, command.getPermission())) {
                continue;
            }
            commands.add(command);
        }
        return commands;
    }

    public static List<Command<PlotPlayer>> getCommands(CommandCategory category, PlotPlayer player) {
        List<Command<PlotPlayer>> commands = new ArrayList<>();
        for (Command<PlotPlayer> command : new HashSet<>(getInstance().getCommands())) {
            if ((category != null) && !command.getCategory().equals(category)) {
                continue;
            }
            if ((player != null) && !Permissions.hasPermission(player, command.getPermission())) {
                continue;
            }
            commands.add(command);
        }
        return commands;
    }

    public static void displayHelp(PlotPlayer player, String cat, int page, String label) {
        CommandCategory catEnum = null;
        if (cat != null) {
            if (StringMan.isEqualIgnoreCase(cat, "all")) {
                catEnum = null;
            } else {
                for (CommandCategory c : CommandCategory.values()) {
                    if (StringMan.isEqualIgnoreCaseToAny(cat, c.name(), c.toString())) {
                        catEnum = c;
                        cat = c.name();
                        break;
                    }
                }
                if (catEnum == null) {
                    cat = null;
                }
            }
        }
        if (cat == null && page == 0) {
            StringBuilder builder = new StringBuilder();
            builder.append(C.HELP_HEADER.s());
            for (CommandCategory c : CommandCategory.values()) {
                builder.append(
                        "\n" + StringMan.replaceAll(C.HELP_INFO_ITEM.s(), "%category%", c.toString().toLowerCase(), "%category_desc%", c.toString()));
            }
            builder.append("\n").append(C.HELP_INFO_ITEM.s().replaceAll("%category%", "all").replaceAll("%category_desc%", "Display all commands"));
            builder.append("\n" + C.HELP_FOOTER.s());
            MainUtil.sendMessage(player, builder.toString(), false);
            return;
        }
        page--;
        new HelpMenu(player).setCategory(catEnum).getCommands().generateMaxPages().generatePage(page, label).render();
    }

    public static boolean onCommand(PlotPlayer player, String cmd, String... args) {
        // Clear perm caching //
        player.deleteMeta("perm");
        ////////////////////////
        int help_index = -1;
        String category = null;
        Location loc = null;
        Plot plot = null;
        boolean tp = false;
        switch (args.length) {
            case 0: {
                help_index = 0;
                break;
            }
            case 1: {
                if (MathMan.isInteger(args[0])) {
                    try {
                        help_index = Integer.parseInt(args[args.length - 1]);
                    } catch (NumberFormatException e) {
                    }
                    break;
                }
            }
            default: {
                switch (args[0].toLowerCase()) {
                    case "he":
                    case "help":
                    case "?": {
                        switch (args.length) {
                            case 1: {
                                help_index = 0;
                                break;
                            }
                            case 2: {
                                if (MathMan.isInteger(args[1])) {
                                    category = null;
                                    try {
                                        help_index = Integer.parseInt(args[1]);
                                    } catch (NumberFormatException e) {
                                        help_index = 1;
                                    }
                                } else {
                                    help_index = 1;
                                    category = args[1];
                                }
                                break;
                            }
                            case 3: {
                                category = args[1];
                                if (MathMan.isInteger(args[2])) {
                                    try {
                                        help_index = Integer.parseInt(args[2]);
                                    } catch (NumberFormatException e) {
                                        help_index = 1;
                                    }
                                }
                                break;
                            }
                            default: {
                                C.COMMAND_SYNTAX.send(player, "/" + cmd + "? [#|<term>|category [#]]");
                                return true;
                            }
                        }
                        break;
                    }
                    default: {
                        if (args.length >= 2) {
                            PlotArea area = player.getApplicablePlotArea();
                            Plot newPlot = Plot.fromString(area, args[0]);
                            if (newPlot == null) {
                                break;
                            }
                            if (!ConsolePlayer.isConsole(player) && (!newPlot.getArea().equals(area) || newPlot.isDenied(player.getUUID()))
                                    && !Permissions.hasPermission(player, C.PERMISSION_ADMIN)) {
                                break;
                            }
                            // Save meta
                            loc = player.getMeta("location");
                            plot = player.getMeta("lastplot");
                            tp = true;
                            // Set loc
                            player.setMeta("location", newPlot.getBottomAbs());
                            player.setMeta("lastplot", newPlot);
                            // Trim command
                            args = Arrays.copyOfRange(args, 1, args.length);
                        }
                    }
                }
            }
        }
        if (help_index != -1) {
            displayHelp(player, category, help_index, cmd);
            return true;
        }
        String fullCmd = StringMan.join(args, " ");
        getInstance().handle(player, cmd + " " + fullCmd);
        // Restore location
        if (tp) {
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
        }
        return true;
    }

    public int getMatch(String[] args, Command<PlotPlayer> cmd) {
        int count = 0;
        String perm = cmd.getPermission();
        HashSet<String> desc = new HashSet<>();
        for (String alias : cmd.getAliases()) {
            if (alias.startsWith(args[0])) {
                count += 5;
            }
        }
        Collections.addAll(desc, cmd.getDescription().split(" "));
        for (String arg : args) {
            if (perm.startsWith(arg)) {
                count++;
            }
            if (desc.contains(arg)) {
                count++;
            }
        }
        String[] usage = cmd.getUsage().split(" ");
        for (int i = 0; i < Math.min(4, usage.length); i++) {
            int require;
            if (usage[i].startsWith("<")) {
                require = 1;
            } else {
                require = 0;
            }
            String[] split = usage[i].split("\\|| |\\>|\\<|\\[|\\]|\\{|\\}|\\_|\\/");
            for (String aSplit : split) {
                for (String arg : args) {
                    if (StringMan.isEqualIgnoreCase(arg, aSplit)) {
                        count += 5 - i + require;
                    }
                }
            }
        }
        count += StringMan.intersection(desc, args);
        return count;
    }

    @Override
    public int handle(PlotPlayer plr, String input) {
        String[] parts = input.split(" ");
        String[] args;
        String label;
        if (parts.length == 1) {
            label = null;
            args = new String[0];
        } else {
            label = parts[1];
            args = new String[parts.length - 2];
            System.arraycopy(parts, 2, args, 0, args.length);
        }
        Command<PlotPlayer> cmd;
        if (label != null) {
            if (label.contains(":")) {
                // Ref: c:v, this will push value to the last spot in the array
                // ex. /p h:2 SomeUsername
                // > /p h SomeUsername 2
                String[] temp = label.split(":");
                if (temp.length == 2) {
                    label = temp[0];
                    String[] tempArgs = new String[args.length + 1];
                    System.arraycopy(args, 0, tempArgs, 0, args.length);
                    tempArgs[tempArgs.length - 1] = temp[1];
                    args = tempArgs;
                }
            }
            cmd = getInstance().commands.get(label.toLowerCase());
        } else {
            cmd = null;
        }
        if (cmd == null) {
            MainUtil.sendMessage(plr, C.NOT_VALID_SUBCOMMAND);
            {
                List<Command<PlotPlayer>> cmds = getCommands(null, plr);
                if ((label == null) || cmds.isEmpty()) {
                    MainUtil.sendMessage(plr, C.DID_YOU_MEAN, "/plot help");
                } else {
                    HashSet<String> setargs = new HashSet<>(args.length + 1);
                    for (String arg : args) {
                        setargs.add(arg.toLowerCase());
                    }
                    setargs.add(label);
                    String[] allargs = setargs.toArray(new String[setargs.size()]);
                    int best = 0;
                    for (Command<PlotPlayer> current : cmds) {
                        int match = getMatch(allargs, current);
                        if (match > best) {
                            cmd = current;
                        }
                    }
                    if (cmd == null) {
                        cmd = new StringComparison<>(label, getCommandAndAliases(null, plr)).getMatchObject();
                    }
                    MainUtil.sendMessage(plr, C.DID_YOU_MEAN, cmd.getUsage().replaceAll("\\{label\\}", parts[0]));
                }
            }
            return CommandHandlingOutput.NOT_FOUND;
        }
        if (!cmd.getRequiredType().allows(plr)) {
            if (ConsolePlayer.isConsole(plr)) {
                MainUtil.sendMessage(plr, C.IS_CONSOLE);
            } else {
                MainUtil.sendMessage(plr, C.NOT_CONSOLE);
            }
            return CommandHandlingOutput.CALLER_OF_WRONG_TYPE;
        }
        if (!Permissions.hasPermission(plr, cmd.getPermission())) {
            MainUtil.sendMessage(plr, C.NO_PERMISSION, cmd.getPermission());
            return CommandHandlingOutput.NOT_PERMITTED;
        }
        Argument<?>[] requiredArguments = cmd.getRequiredArguments();
        if ((requiredArguments != null) && (requiredArguments.length > 0)) {
            boolean success = true;
            if (args.length < requiredArguments.length) {
                success = false;
            } else {
                for (int i = 0; i < requiredArguments.length; i++) {
                    if (requiredArguments[i].parse(args[i]) == null) {
                        success = false;
                        break;
                    }
                }
            }
            if (!success) {
                C.COMMAND_SYNTAX.send(plr, cmd.getUsage());
                return CommandHandlingOutput.WRONG_USAGE;
            }
        }
        try {
            boolean result = cmd.onCommand(plr, args);
            if (!result) {
                cmd.getUsage();
                return CommandHandlingOutput.WRONG_USAGE;
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return CommandHandlingOutput.ERROR;
        }
        return CommandHandlingOutput.SUCCESS;
    }
}
