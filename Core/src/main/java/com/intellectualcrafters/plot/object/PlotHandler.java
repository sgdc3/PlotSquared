package com.intellectualcrafters.plot.object;

import java.util.HashSet;
import java.util.UUID;

public class PlotHandler {
    public static boolean sameOwners(final Plot plot1, final Plot plot2) {
        if ((plot1.owner == null) || (plot2.owner == null)) {
            return false;
        }
        final HashSet<UUID> owners = plot1.getOwners();
        owners.retainAll(plot2.getOwners());
        return !owners.isEmpty();
    }
}
