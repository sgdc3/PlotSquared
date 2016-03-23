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
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "debug",
        category = CommandCategory.DEBUG,
        description = "Show debug information",
        usage = "/plot debug [msg]",
        permission = "plots.admin")
public class Debug extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer plr, String[] args) {
        if ((args.length > 0) && args[0].equalsIgnoreCase("msg")) {
            StringBuilder msg = new StringBuilder();
            for (C c : C.values()) {
                msg.append(c.s()).append("\n");
            }
            MainUtil.sendMessage(plr, msg.toString());
            return true;
        }
        StringBuilder information;
        String header, line, section;
        {
            information = new StringBuilder();
            header = C.DEBUG_HEADER.s();
            line = C.DEBUG_LINE.s();
            section = C.DEBUG_SECTION.s();
        }
        {
            final StringBuilder worlds = new StringBuilder("");
            PS.get().foreachPlotArea(new RunnableVal<PlotArea>() {
                @Override
                public void run(PlotArea value) {
                    worlds.append(value.toString()).append(" ");
                }
            });
            information.append(header);
            information.append(getSection(section, "PlotArea"));
            information.append(getLine(line, "Plot Worlds", worlds));
            information.append(getLine(line, "Owned Plots", PS.get().getPlots().size()));
            information.append(getSection(section, "Messages"));
            information.append(getLine(line, "Total Messages", C.values().length));
            information.append(getLine(line, "View all captions", "/plot debug msg"));
        }
        {
            MainUtil.sendMessage(plr, information.toString());
        }
        return true;
    }

    private String getSection(String line, String val) {
        return line.replaceAll("%val%", val) + "\n";
    }

    private String getLine(String line, String var, Object val) {
        return line.replaceAll("%var%", var).replaceAll("%val%", "" + val) + "\n";
    }
}
