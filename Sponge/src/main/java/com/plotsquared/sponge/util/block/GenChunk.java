package com.plotsquared.sponge.util.block;

import com.intellectualcrafters.plot.util.PlotChunk;
import com.intellectualcrafters.plot.util.SetQueue.ChunkWrapper;
import com.plotsquared.sponge.util.SpongeUtil;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;

public class GenChunk extends PlotChunk<Chunk> {

    private final MutableBlockVolume terrain;
    private final MutableBiomeArea biome;
    private final int bz;
    private final int bx;
    public boolean modified = false;

    public GenChunk(MutableBlockVolume terrain, MutableBiomeArea biome, ChunkWrapper wrap) {
        super(wrap);
        this.bx = wrap.x << 4;
        this.bz = wrap.z << 4;
        this.terrain = terrain;
        this.biome = biome;
    }
    
    @Override
    public Chunk getChunkAbs() {
        ChunkWrapper wrap = getChunkWrapper();
        return SpongeUtil.getWorld(wrap.world).getChunk(wrap.x << 4, 0, wrap.z << 4).orElse(null);
    }
    
    @Override
    public void setBiome(int x, int z, int biome) {
        if (this.biome != null) {
            this.biome.setBiome(this.bx + x, this.bz + z, SpongeUtil.getBiome(biome));
        }
    }
    
    @Override
    public void setBlock(int x, int y, int z, int id, byte data) {
        this.terrain.setBlock(this.bx + x, y, this.bz + z, SpongeUtil.getBlockState(id, data));
    }

    @Override
    public void setChunkWrapper(ChunkWrapper loc) {
        super.setChunkWrapper(loc);
    }

    @Override
    public PlotChunk clone() {
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }

    @Override
    public PlotChunk shallowClone() {
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
}
