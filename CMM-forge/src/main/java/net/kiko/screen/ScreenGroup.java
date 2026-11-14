package net.kiko.screen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** All monitors that are joined together into one screen, and where each of them sits in it. */
public final class ScreenGroup {

    private static final int MAX_BLOCKS = 256;

    public final Direction facing;
    public final List<BlockPos> members;
    public final int minCol, minRow, cols, rows;
    /** The top-left monitor; one video player is kept per group, keyed by this position. */
    public final BlockPos controller;

    private ScreenGroup(Direction facing, List<BlockPos> members) {
        this.facing = facing;
        this.members = members;
        int minC = Integer.MAX_VALUE, maxC = Integer.MIN_VALUE, minR = Integer.MAX_VALUE, maxR = Integer.MIN_VALUE;
        BlockPos best = null;
        int bestR = 0, bestC = 0;
        for (BlockPos p : members) {
            int c = col(facing, p), r = row(p);
            minC = Math.min(minC, c); maxC = Math.max(maxC, c);
            minR = Math.min(minR, r); maxR = Math.max(maxR, r);
            if (best == null || r < bestR || (r == bestR && c < bestC)) { best = p; bestR = r; bestC = c; }
        }
        this.minCol = minC;
        this.minRow = minR;
        this.cols = maxC - minC + 1;
        this.rows = maxR - minR + 1;
        this.controller = best;
    }

    /** Column counted from the left edge of the screen, as seen from the front (grows to the right). */
    public static int col(Direction facing, BlockPos p) {
        Direction right = MonitorBlock.rightOf(facing);
        return p.getX() * right.getStepX() + p.getZ() * right.getStepZ();
    }

    /** Row counted from the top (grows downwards). */
    public static int row(BlockPos p) {
        return -p.getY();
    }

    public int columnOf(BlockPos p) { return col(facing, p) - minCol; }
    public int rowOf(BlockPos p) { return row(p) - minRow; }

    /** Finds the group that contains {@code start}, or null if there is no monitor there. */
    public static ScreenGroup find(BlockGetter level, BlockPos start) {
        BlockState first = level.getBlockState(start);
        if (!(first.getBlock() instanceof MonitorBlock)) return null;
        Direction facing = first.getValue(MonitorBlock.FACING);
        Set<BlockPos> seen = new HashSet<>();
        List<BlockPos> list = new ArrayList<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        seen.add(start);
        while (!queue.isEmpty() && list.size() < MAX_BLOCKS) {
            BlockPos p = queue.poll();
            list.add(p);
            for (Direction d : new Direction[]{MonitorBlock.leftOf(facing), MonitorBlock.rightOf(facing), Direction.UP, Direction.DOWN}) {
                BlockPos n = p.relative(d);
                if (seen.contains(n)) continue;
                BlockState s = level.getBlockState(n);
                if (s.getBlock() instanceof MonitorBlock && s.getValue(MonitorBlock.FACING) == facing) {
                    seen.add(n.immutable());
                    queue.add(n.immutable());
                }
            }
        }
        return new ScreenGroup(facing, list);
    }
}
