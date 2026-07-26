package dev.simulated_team.simulated.ponder;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.util.SimDistUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import com.zurrtum.create.client.ponder.api.scene.SceneBuilder;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.PonderSceneBuilder;
import com.zurrtum.create.client.ponder.foundation.instruction.PonderInstruction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.function.Consumer;

/**
 * Utility to make writing ponder scenes with concurrent independent sequences easier
 */
public class SceneScheduler {
    private final SceneBuilder builder;
    private final List<Sequence> sequences = new ObjectArrayList<>();
    private final Map<Object, Set<Sequence>> syncs = new Object2ObjectOpenHashMap<>();
    private int time = 0;
    private boolean ran = false;

    public SceneScheduler(final SceneBuilder builder) {
        this.builder = builder;
    }

    public Sequence get(final int i) {
        if (this.sequences.size() <= i) {
            this.sequences.add(i, new Sequence(i, this, this.builder));
        }
        return this.sequences.get(i);
    }

    public void run() {
        this.run(false);
    }

    /**
     * Dequeues the instructions from every sequence, interweaving calls and idles to fit into a single linear sequence
     *
     * @param debug Whether to print the order of pInstruction calls + idle times
     */
    public void run(boolean debug) {
        if (this.ran) {
            SimDistUtil.getClientPlayer().displayClientMessage(
                    Component.literal("Set of scheduled sequences being re-run! See logs for more info")
                            .withStyle(ChatFormatting.RED), false);
            debug = true;
            Simulated.LOGGER.error("Trying to re-run scheduled sequences! Undefined behaviour ahead. " +
                    "A new instance should be made for running a new set of sequences");
        }
        this.ran = true;

        final StringJoiner joiner = new StringJoiner("\n");
        boolean done = false;
        while (!done) {
            done = true;
            boolean hasSyncing = false;
            int shortestIdle = Integer.MAX_VALUE;
            int shortestI = -1;
            boolean isDeadlocked = true;
            for (int i = 0; i < this.sequences.size(); i++) {
                final Sequence seq = this.sequences.get(i);
                while (!seq.isDone()) {
                    final int timeIdling = seq.time - this.time;

                    if (timeIdling > 0) {
                        if (timeIdling < shortestIdle) {
                            shortestIdle = timeIdling;
                            shortestI = i;
                        }
                        done = false;
                        isDeadlocked = false;
                        break;
                    } else {
                        if (seq.isSyncIdle()) {
                            hasSyncing = true;
                            final Object syncKey = seq.getSyncKey();
                            final Set<Sequence> sync = seq.getSyncSet();
                            if (sync.remove(seq)) {
                                done = false;
                                joiner.add("(" + this.time + ") " + i + " awaiting sync " + syncKey);
                            }
                            if (sync.isEmpty())
                                isDeadlocked = false;
                            break;
                        }
                        isDeadlocked = false;

                        final int idle = seq.getIdle();
                        if (idle > 0) {
                            seq.time += idle;
                        } else {
                            joiner.add("(" + this.time + ") " + i + " action " + seq.queue.element().pInstruction);
                        }
                        done = false;

                        seq.popAndTryRun();
                    }
                }
            }
            if (hasSyncing && isDeadlocked) {
                SimDistUtil.getClientPlayer().displayClientMessage(Component.literal(
                        "Ponder sequence deadlock! See logs for more info").withStyle(ChatFormatting.RED), false);
                debug = true;
                Simulated.LOGGER.error("Every sequence is awaiting syncs that will never happen");
                for (int i = 0; i < this.sequences.size(); i++) {
                    final Sequence seq = this.sequences.get(i);
                    if (!seq.isDone()) {
                        final Object sync = seq.queue.element().sync;
                        Simulated.LOGGER.error("Sequence {} syncing {} (blocked by {})", i, sync, this.syncs.get(sync));
                    }
                }
                this.sequences.forEach(seq -> {
                    if (!seq.isDone()) {
                        joiner.add("!! " + "(" + this.time + ") " + seq.id + " skipping sync " + seq.getSyncKey());
                        seq.popAndTryRun();
                        seq.time = this.time;
                    }
                });
                done = false;
            }

            this.sequences.forEach(seq -> {
                if (!seq.isDone() && seq.isSyncIdle() && seq.getSyncSet().isEmpty()) {
                    seq.time = this.time;
                    seq.queue.remove();
                }
            });

            this.syncs.entrySet().removeIf(e -> {
                if (e.getValue().isEmpty()) {
                    joiner.add("(" + this.time + ") Fully synced " + e.getKey());
                    return true;
                }
                return false;
            });

            if (shortestI != -1) {
                this.time += shortestIdle;
                this.builder.idle(shortestIdle);
                joiner.add("(" + this.time + ") " + shortestI + " idle " + shortestIdle);
            }
        }

        if (debug) {
            Simulated.LOGGER.info("Finalized sequence:\n" + joiner);
        }
    }

    public static class Sequence extends PonderSceneBuilder {
        private final Queue<Instruction> queue = new ArrayDeque<>();
        private final int id;
        private final SceneScheduler scheduler;
        private final SceneBuilder builder;
        private int time = 0;
        /* when a sequence relies on syncing, its duration cannot be determined without
         * executing the entire scheduled sequence
         */
        private boolean independent = true;
        private int duration = 0;

        private Sequence(final int id, final SceneScheduler scheduler, final SceneBuilder builder) {
            super(builder.getScene());
            this.id = id;
            this.scheduler = scheduler;
            this.builder = builder;
        }

        @Override
        public void addInstruction(final PonderInstruction instruction) {
            this.queue.add(new Instruction(null, instruction, null));
        }

        @Override
        public void addInstruction(final Consumer<PonderScene> callback) {
            this.addInstruction(PonderInstruction.simple(callback));
        }

        @Override
        public void idle(final int ticks) {
            this.queue.add(new Instruction(ticks, null, null));
            this.duration += ticks;
        }

        public void sync(final Object o) {
            this.scheduler.syncs.computeIfAbsent(o, k -> new HashSet<>()).add(this);
            this.queue.add(new Instruction(null, null, o));
            this.independent = false;
        }

        private boolean isDone() {
            return this.queue.isEmpty();
        }

        private int getIdle() {
            return Objects.requireNonNullElse(this.queue.element().idle(), 0);
        }

        private boolean isSyncIdle() {
            return this.queue.element().sync != null;
        }

        private Object getSyncKey() {
            return this.queue.element().sync;
        }

        private Set<Sequence> getSyncSet() {
            return this.scheduler.syncs.get(this.queue.element().sync);
        }

        private void popAndTryRun() {
            final Instruction i = this.queue.remove();
            if (i.pInstruction != null) {
                this.builder.addInstruction(i.pInstruction);
            }
        }

        /**
         * Is inaccurate (and will yell at you) if any {@link Sequence#sync(Object)} instructions are added
         *
         * @return The total duration of all added instructions up to this point
         */
        public int getDuration() {
            if (!this.independent) {
                SimDistUtil.getClientPlayer().displayClientMessage(Component.literal(
                        "Getting independent timestamp of synced sequence " + this.id).withStyle(ChatFormatting.RED), false);
                Simulated.LOGGER.error("Getting independent timestamp of synced sequence " + this.id);
            }
            return this.duration;
        }

        @Override
        public String toString() {
            if (this.isDone()) {
                return "Finished sequence " + this.id;
            }
            final Instruction i = this.queue.element();
            if (i.idle != null) {
                return "Sequence " + this.id + " waiting " + i.idle + " ticks";
            } else if (i.pInstruction != null) {
                return "Sequence " + this.id + " running instruction " + i.pInstruction;
            } else {
                return "Sequence " + this.id + " waiting for sync of " + i.sync;
            }
        }
    }

    private record Instruction(Integer idle, PonderInstruction pInstruction, Object sync) {
    }
}
