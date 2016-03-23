package com.plotsquared.sponge.generator;

import com.flowpowered.math.vector.Vector2i;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import com.intellectualcrafters.plot.generator.IndependentPlotGenerator;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.ReflectionUtils;
import com.plotsquared.sponge.util.SpongeUtil;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

import java.util.ArrayList;
import java.util.List;

public class SpongePlotGenerator implements WorldGeneratorModifier, GeneratorWrapper<WorldGeneratorModifier> {
    
    private final IndependentPlotGenerator plotGenerator;
    private final List<GenerationPopulator> populators = new ArrayList<>();
    private final boolean loaded = false;
    private PlotManager manager;
    private final WorldGeneratorModifier platformGenerator;
    private final boolean full;
    
    public SpongePlotGenerator(IndependentPlotGenerator generator) {
        this.plotGenerator = generator;
        this.platformGenerator = this;
        this.full = true;
        MainUtil.initCache();
    }
    
    public SpongePlotGenerator(WorldGeneratorModifier wgm) {
        this.plotGenerator = null;
        this.platformGenerator = wgm;
        this.full = false;
        MainUtil.initCache();
    }

    @Override
    public String getId() {
        if (plotGenerator == null) {
            if (platformGenerator != this) {
                return platformGenerator.getId();
            }
            return "null";
        }
        return plotGenerator.getName();
    }
    
    @Override
    public String getName() {
        if (plotGenerator == null) {
            if (platformGenerator != this) {
                return platformGenerator.getName();
            }
            return "null";
        }
        return plotGenerator.getName();
    }
    
    @Override
    public void modifyWorldGenerator(WorldCreationSettings settings, DataContainer data, WorldGenerator wg) {
        final String worldname = settings.getWorldName();
        wg.setBaseGenerationPopulator(new SpongeTerrainGen(this, plotGenerator));
        wg.setBiomeGenerator(new BiomeGenerator() {
            @Override
            public void generateBiomes(MutableBiomeArea buffer) {
                PlotArea area = PS.get().getPlotArea(worldname, null);
                if (area != null) {
                    BiomeType biome = SpongeUtil.getBiome(area.PLOT_BIOME);
                    Vector2i min = buffer.getBiomeMin();
                    Vector2i max = buffer.getBiomeMax();
                    for (int x = min.getX(); x <= max.getX(); x++) {
                        for (int z = min.getY(); z <= max.getY(); z++) {
                            buffer.setBiome(x, z, biome);
                    }
                }
                }
        }
        });
        for (BiomeType type : ReflectionUtils.<BiomeType> getStaticFields(BiomeTypes.class)) {
            BiomeGenerationSettings biomeSettings = wg.getBiomeSettings(type);
            biomeSettings.getGenerationPopulators().clear();
            biomeSettings.getPopulators().clear();
            biomeSettings.getGroundCoverLayers().clear();
    }
        wg.getGenerationPopulators().clear();
        wg.getPopulators().clear();
        PS.get().loadWorld(worldname, this);
    }
    
    @Override
    public IndependentPlotGenerator getPlotGenerator() {
        return plotGenerator;
    }
    
    @Override
    public WorldGeneratorModifier getPlatformGenerator() {
        return platformGenerator;
    }
    
    @Override
    public void augment(PlotArea area) {
        SpongeAugmentedGenerator.get(SpongeUtil.getWorld(area.worldname));
    }
    
    @Override
    public boolean isFull() {
        return full;
    }
    
}
