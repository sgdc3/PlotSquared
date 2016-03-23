package com.plotsquared.bukkit.events;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.Rating;
import org.bukkit.event.HandlerList;

/**
 * Created 2015-07-13 for PlotSquaredGit
 *

 */
public class PlotRateEvent extends PlotEvent {

    private static final HandlerList handlers = new HandlerList();
    private final PlotPlayer rater;
    private Rating rating;

    public PlotRateEvent(PlotPlayer rater, Rating rating, Plot plot) {
        super(plot);
        this.rater = rater;
        this.rating = rating;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PlotPlayer getRater() {
        return this.rater;
    }

    public Rating getRating() {
        return this.rating;
    }

    public void setRating(Rating rating) {
        this.rating = rating;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
