package com.plotsquared.bukkit.util.block;

import static com.intellectualcrafters.plot.util.ReflectionUtils.getRefClass;

import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.PlotChunk;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefClass;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefConstructor;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefMethod;
import com.intellectualcrafters.plot.util.SetQueue;
import com.intellectualcrafters.plot.util.SetQueue.ChunkWrapper;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.bukkit.util.SendChunk;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class FastQueue_1_8 extends SlowQueue {

    private final RefMethod methodInitLighting;
    private final RefClass classBlock = getRefClass("{nms}.Block");
    private final RefClass classBlockPosition = getRefClass("{nms}.BlockPosition");
    private final RefClass classIBlockData = getRefClass("{nms}.IBlockData");
    private final RefClass classChunk = getRefClass("{nms}.Chunk");
    private final RefClass classWorld = getRefClass("{nms}.World");
    private final RefClass classCraftWorld = getRefClass("{cb}.CraftWorld");
    private final HashMap<ChunkWrapper, Chunk> toUpdate = new HashMap<>();
    private final RefMethod methodGetHandle;
    private final RefMethod methodGetChunkAt;
    private final RefMethod methodA;
    private final RefMethod methodGetByCombinedId;
    private final RefConstructor constructorBlockPosition;
    private final SendChunk chunksender;

    public FastQueue_1_8() throws NoSuchMethodException, RuntimeException {
        this.methodInitLighting = this.classChunk.getMethod("initLighting");
        this.constructorBlockPosition = this.classBlockPosition.getConstructor(int.class, int.class, int.class);
        this.methodGetByCombinedId = this.classBlock.getMethod("getByCombinedId", int.class);
        this.methodGetHandle = this.classCraftWorld.getMethod("getHandle");
        this.methodGetChunkAt = this.classWorld.getMethod("getChunkAt", int.class, int.class);
        this.methodA = this.classChunk.getMethod("a", this.classBlockPosition, this.classIBlockData);
        this.chunksender = new SendChunk();
        TaskManager.runTaskRepeat(new Runnable() {
            @Override
            public void run() {
                if (FastQueue_1_8.this.toUpdate.isEmpty()) {
                    return;
                }
                int count = 0;
                ArrayList<Chunk> chunks = new ArrayList<Chunk>();
                Iterator<Entry<ChunkWrapper, Chunk>> i = FastQueue_1_8.this.toUpdate.entrySet().iterator();
                while (i.hasNext() && (count < 128)) {
                    chunks.add(i.next().getValue());
                    i.remove();
                    count++;
                }
                if (count == 0) {
                    return;
                }
                update(chunks);
            }
        }, 1);
        MainUtil.initCache();
    }

    public void update(Collection<Chunk> chunks) {
        if (chunks.isEmpty()) {
            return;
        }
        if (!MainUtil.canSendChunk) {
            for (Chunk chunk : chunks) {
                chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
                chunk.unload(true, false);
                chunk.load();
            }
            return;
        }
        try {
            this.chunksender.sendChunk(chunks);
        } catch (Throwable e) {
            e.printStackTrace();
            MainUtil.canSendChunk = false;
        }
    }

    /**
     * This should be overridden by any specialized queues.
     * @param plotChunk
     */
    @Override
    public void execute(PlotChunk<Chunk> plotChunk) {
        SlowChunk sc = (SlowChunk) plotChunk;
        Chunk chunk = plotChunk.getChunk();
        ChunkWrapper wrapper = plotChunk.getChunkWrapper();
        if (!this.toUpdate.containsKey(wrapper)) {
            this.toUpdate.put(wrapper, chunk);
        }
        chunk.load(true);
        World world = chunk.getWorld();
        Object w = this.methodGetHandle.of(world).call();
        Object c = this.methodGetChunkAt.of(w).call(wrapper.x, wrapper.z);
        for (int i = 0; i < sc.result.length; i++) {
            PlotBlock[] result2 = sc.result[i];
            if (result2 == null) {
                continue;
            }
            for (int j = 0; j < 4096; j++) {
                int x = MainUtil.x_loc[i][j];
                int y = MainUtil.y_loc[i][j];
                int z = MainUtil.z_loc[i][j];
                PlotBlock newBlock = result2[j];
                if (newBlock.id == -1) {
                    chunk.getBlock(x, y, z).setData(newBlock.data, false);
                    continue;
                }
                // Start blockstate workaround //
                switch (newBlock.id) {
                    case 54:
                    case 130:
                    case 142:
                    case 132:
                    case 27:
                    case 137:
                    case 52:
                    case 154:
                    case 84:
                    case 25:
                    case 144:
                    case 138:
                    case 176:
                    case 177:
                    case 119:
                    case 63:
                    case 68:
                    case 323:
                    case 117:
                    case 116:
                    case 28:
                    case 66:
                    case 157:
                    case 61:
                    case 62:
                    case 140:
                    case 146:
                    case 149:
                    case 150:
                    case 158:
                    case 23:
                    case 123:
                    case 124:
                    case 29:
                    case 33:
                    case 151:
                    case 178: {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.getData() == newBlock.data) {
                            if (block.getTypeId() != newBlock.id) {
                                block.setTypeId(newBlock.id, false);
                            }
                        } else {
                            if (block.getTypeId() == newBlock.id) {
                                block.setData(newBlock.data, false);
                            } else {
                                block.setTypeIdAndData(newBlock.id, newBlock.data, false);
                            }
                        }
                        continue;
                    }
                }

                // Start data value shortcut
                Block block = world.getBlockAt(x, y, z);
                int currentId = block.getTypeId();
                if (currentId == newBlock.id) {
                    switch (newBlock.id) {
                        case 0:
                        case 2:
                        case 4:
                        case 13:
                        case 14:
                        case 15:
                        case 20:
                        case 21:
                        case 22:
                        case 25:
                        case 30:
                        case 32:
                        case 37:
                        case 39:
                        case 40:
                        case 41:
                        case 42:
                        case 45:
                        case 46:
                        case 47:
                        case 48:
                        case 49:
                        case 51:
                        case 52:
                        case 54:
                        case 55:
                        case 56:
                        case 57:
                        case 58:
                        case 60:
                        case 61:
                        case 62:
                        case 7:
                        case 8:
                        case 9:
                        case 10:
                        case 11:
                        case 73:
                        case 74:
                        case 78:
                        case 79:
                        case 80:
                        case 81:
                        case 82:
                        case 83:
                        case 84:
                        case 85:
                        case 87:
                        case 88:
                        case 101:
                        case 102:
                        case 103:
                        case 110:
                        case 112:
                        case 113:
                        case 117:
                        case 121:
                        case 122:
                        case 123:
                        case 124:
                        case 129:
                        case 133:
                        case 138:
                        case 137:
                        case 140:
                        case 165:
                        case 166:
                        case 169:
                        case 170:
                        case 172:
                        case 173:
                        case 174:
                        case 176:
                        case 177:
                        case 181:
                        case 182:
                        case 188:
                        case 189:
                        case 190:
                        case 191:
                        case 192: {
                            continue;
                        }
                    }
                    if (block.getData() == newBlock.data) {
                        return;
                    }
                    block.setData(newBlock.data, false);
                    return;
                }
                // blockstate
                switch (currentId) {
                    case 54:
                    case 130:
                    case 132:
                    case 142:
                    case 27:
                    case 137:
                    case 52:
                    case 154:
                    case 84:
                    case 25:
                    case 144:
                    case 138:
                    case 176:
                    case 177:
                    case 63:
                    case 68:
                    case 323:
                    case 117:
                    case 119:
                    case 116:
                    case 28:
                    case 66:
                    case 157:
                    case 61:
                    case 62:
                    case 140:
                    case 146:
                    case 149:
                    case 150:
                    case 158:
                    case 23:
                    case 123:
                    case 124:
                    case 29:
                    case 33:
                    case 151:
                    case 178: {
                        if (block.getData() == newBlock.data) {
                            block.setTypeId(newBlock.id, false);
                        } else {
                            block.setTypeIdAndData(newBlock.id, newBlock.data, false);
                        }
                        continue;
                    }
                }
                // End blockstate workaround //

                // check sign
                Object pos = this.constructorBlockPosition.create(x, y, z);
                Object combined = this.methodGetByCombinedId.call(newBlock.id + (newBlock.data << 12));
                this.methodA.of(chunk).call(pos, combined);
            }
        }
        int[][] biomes = sc.biomes;
        Biome[] values = Biome.values();
        if (biomes != null) {
            for (int x = 0; x < 16; x++) {
                int[] array = biomes[x];
                if (array == null) {
                    continue;
                }
                for (int z = 0; z < 16; z++) {
                    int biome = array[z];
                    if (biome == 0) {
                        continue;
                    }
                    chunk.getBlock(x, 0, z).setBiome(values[biome]);
                }
            }
        }
    }

    /**
     * This should be overridden by any specialized queues.
     * @param wrap
     */
    @Override
    public PlotChunk<Chunk> getChunk(ChunkWrapper wrap) {
        return new SlowChunk(wrap);
    }

    /**
     * This should be overridden by any specialized queues.
     * @param fixAll
     */
    @Override
    public boolean fixLighting(PlotChunk<Chunk> chunk, boolean fixAll) {
        Object c = this.methodGetHandle.of(chunk.getChunk()).call();
        this.methodInitLighting.of(c).call();
        return true;
    }

    /**
     * This should be overridden by any specialized queues.
     * @param locations
     */
    @Override
    public void sendChunk(String world, Collection<ChunkLoc> locations) {
        World worldObj = BukkitUtil.getWorld(world);
        for (ChunkLoc loc : locations) {
            ChunkWrapper wrapper = SetQueue.IMP.new ChunkWrapper(world, loc.x, loc.z);
            this.toUpdate.remove(wrapper);
        }
        this.chunksender.sendChunk(world, locations);
    }
}
