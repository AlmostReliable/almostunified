package com.almostreliable.unified.unification.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

public final class WorldStripper {

    private static final int BLOCKS_PER_TICK = 16 * 16 * 16;

    @Nullable private static Worker WORKER;

    private WorldStripper() {}

    @SuppressWarnings("StaticVariableUsedBeforeInitialization")
    public static void onServerTick(ServerTickEvent.Post ignoredEvent) {
        if (WORKER == null) return;
        WORKER.doWork();
        if (WORKER.isDone()) WORKER = null;
    }

    public static void stripWorld(ServerPlayer player, ServerLevel level, int radius) {
        BlockPos playerPos = player.blockPosition();
        BlockPos min = new BlockPos(playerPos.getX() - radius, level.getMinBuildHeight(), playerPos.getZ() - radius);
        BlockPos max = new BlockPos(playerPos.getX() + radius, level.getMaxBuildHeight(), playerPos.getZ() + radius);

        @SuppressWarnings("SimplifyStreamApiCallChains")
        var blockIterator = BlockPos.betweenClosedStream(min, max)
            .map(pos -> blockInWorld(level, pos))
            .collect(Collectors.toList())
            .iterator();

        WORKER = new Worker(level, blockIterator);
    }

    private static BlockInWorld blockInWorld(ServerLevel level, BlockPos pos) {
        return new BlockInWorld(level, pos, true);
    }

    private static final class Worker {

        private final ServerLevel level;
        private final Iterator<BlockInWorld> iterator;
        private final Map<Block, Boolean> removeCache = new IdentityHashMap<>();

        private Worker(ServerLevel level, Iterator<BlockInWorld> blockIterator) {
            this.level = level;
            this.iterator = blockIterator;
        }

        private void doWork() {
            int count = 0;
            while (iterator.hasNext() && count < BLOCKS_PER_TICK) {
                BlockInWorld block = iterator.next();
                iterator.remove();

                if (removeCache.computeIfAbsent(block.getState().getBlock(), $ -> shouldRemove(block))) {
                    level.setBlock(block.getPos(), Blocks.AIR.defaultBlockState(), 3);
                }

                count++;
            }
        }

        private boolean isDone() {
            return !iterator.hasNext();
        }

        private boolean shouldRemove(BlockInWorld block) {
            BlockState state = block.getState();
            return !(state.isAir() || state.is(Blocks.BEDROCK) ||
                     block.getEntity() != null ||
                     state.getBlock() instanceof DropExperienceBlock ||
                     BuiltInRegistries.BLOCK
                         .wrapAsHolder(state.getBlock())
                         .tags()
                         .anyMatch(t -> t.location().toString().startsWith("c:ores")));
        }
    }
}
