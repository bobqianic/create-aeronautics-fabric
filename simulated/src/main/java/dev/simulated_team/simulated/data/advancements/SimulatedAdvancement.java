package dev.simulated_team.simulated.data.advancements;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import dev.simulated_team.simulated.util.SimColors;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

// i pretty much yoinked everything from Create's advancement/trigger stuff
// the only notable difference is it's not hardcoded to simulated, and addons *should* be able to use it
public class SimulatedAdvancement {
    static final String SECRET_SUFFIX = "\n鎼?(Hidden Advancement)";

    private SimpleSimulatedTrigger builtinTrigger;
    private SimulatedAdvancement parent;
    private final Builder configuration;

    AdvancementHolder datagenResult;

    private final ResourceLocation background;
    private final String lang;

    private final String id;
    private final String modid;
    private String title;
    private String description;

    public SimulatedAdvancement(final String id, final UnaryOperator<Builder> b, final ResourceLocation background, final String modid, final BiFunction<String,String, SimpleSimulatedTrigger> triggerHandler) {
        this.id = id;
        this.modid = modid;
        this.background = background;
        this.lang = "advancement." + modid + ".";

        this.configuration = b.apply(new Builder());

        if(!this.configuration.externalTrigger) {
            this.builtinTrigger = triggerHandler.apply(modid,id + "_builtin");
        }

        if(this.configuration.type == TaskType.SECRET)
            this.description += SECRET_SUFFIX;
    }

    private String titleKey() {
        return this.lang + this.id;
    }

    private String descriptionKey() {
        return this.titleKey() + ".desc";
    }

    public boolean isAlreadyAwardedTo(final Player player) {
        if (!(player instanceof final ServerPlayer sp))
            return true;
        final AdvancementHolder advancement = sp.level().getServer()
                .getAdvancements()
                .get(ResourceLocation.fromNamespaceAndPath(this.modid, this.id));
        if (advancement == null)
            return true;
        return sp.getAdvancements()
                .getOrStartProgress(advancement)
                .isDone();
    }

    public void awardTo(final Player player) {
        if(this.isAlreadyAwardedTo(player)) return;
        if (!(player instanceof final ServerPlayer sp)) {
            return;
        }
        if (this.builtinTrigger == null) {
            throw new UnsupportedOperationException("Advancement " + this.id + " uses external Triggers, it cannot be awarded directly");
        }

        this.builtinTrigger.trigger(sp);
    }

    /**
     * Only attempts to award nearby players every x amount of ticks, use this when you need to award something in a method called every tick
     */
    public void awardToNearby(final BlockPos pos, final Level level, final int ticks, final double radius) {
        if(level.getGameTime() % ticks == 0) {
            this.awardToNearby(pos, level, radius);
        }
    }

    public void awardToNearby(final BlockPos pos, final Level level) {
        this.awardToNearby(pos, level, 10);
    }
    public void awardToNearby(final BlockPos pos, final Level level, final double radius) {
        final AABB aabb = new AABB(pos).inflate(radius);
        final List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, aabb);
        for (final Player player : nearbyPlayers) {
            this.awardTo(player);
        }
    }

    public void save(final Consumer<AdvancementHolder> t) {
        Advancement.Builder builder = Advancement.Builder.advancement();
        if (this.builtinTrigger != null) {
            builder.addCriterion("0", this.builtinTrigger.createCriterion(this.builtinTrigger.instance()));
        }
        for (int index = 0; index < this.configuration.criteria.size(); index++) {
            builder.addCriterion(String.valueOf(index), this.configuration.criteria.get(index).get());
        }
        builder.display(this.configuration.icon.get(), Component.translatable(this.titleKey()),
                Component.translatable(this.descriptionKey()).withStyle(s -> s.withColor(SimColors.ADVANCABLE_GOLD)),
                id.equals("root") ? this.background : null, this.configuration.type.advancementType,
                this.configuration.type.toast, this.configuration.type.announce, this.configuration.type.hide);
        if (this.parent != null)
            builder.parent(this.parent.datagenResult);

        this.datagenResult = builder.save(t, ResourceLocation.fromNamespaceAndPath(this.modid, this.id)
                .toString());
    }

    public void provideLang(final BiConsumer<String, String> consumer) {
        consumer.accept(this.titleKey(), this.title);
        consumer.accept(this.descriptionKey(), this.description);
    }

    /**
     * <strong>Silent</strong> - No toast or chat announcement<br>
     * <strong>Normal</strong> - Toast but no chat announcement (default)<br>
     * <strong>Noisy </strong> - Toast and chat announcement<br>
     * <strong>Expert</strong> - Toast and chat announcement, but purple<br>
     * <strong>Secret</strong> - Toast and chat announcement, but purple and hidden<br>
     */
    public enum TaskType {

        SILENT(AdvancementType.TASK, false, false, false),
        NORMAL(AdvancementType.TASK, true, false, false),
        NOISY(AdvancementType.TASK, true, true, false),
        EXPERT(AdvancementType.GOAL, true, true, false),
        SECRET(AdvancementType.GOAL, true, true, true),

        ;

        private final AdvancementType advancementType;
        private final boolean toast;
        private final boolean announce;
        private final boolean hide;

        TaskType(final AdvancementType advancementType, final boolean toast, final boolean announce, final boolean hide) {
            this.advancementType = advancementType;
            this.toast = toast;
            this.announce = announce;
            this.hide = hide;
        }
    }
    public class Builder {

        private TaskType type = TaskType.NORMAL;
        private boolean externalTrigger;
        private Supplier<ItemStack> icon;
        private final List<Supplier<Criterion<? extends CriterionTriggerInstance>>> criteria = new ArrayList<>();

        public Builder special(final TaskType type) {
            this.type = type;
            return this;
        }

        public Builder after(final SimulatedAdvancement other) {
            SimulatedAdvancement.this.parent = other;
            return this;
        }

        public Builder icon(final ItemProviderEntry<?, ?> item) {
            this.icon = item::asStack;
            return this;
        }

        public Builder icon(final ItemLike item) {
            this.icon = () -> new ItemStack(item);
            return this;
        }

        public Builder lazyIcon(final Supplier<? extends ItemLike> item) {
            this.icon = () -> new ItemStack(item.get());
            return this;
        }

        public Builder icon(final ItemStack stack) {
            this.icon = () -> stack;
            return this;
        }

        public Builder title(final String title) {
            SimulatedAdvancement.this.title = title;
            return this;
        }

        public Builder description(final String description) {
            SimulatedAdvancement.this.description = description;
            return this;
        }

        public Builder whenBlockPlaced(final Block block) {
            return this.externalTrigger(ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(block));
        }

        public Builder whenIconCollected() {
            return this.deferredExternalTrigger(() ->
                    InventoryChangeTrigger.TriggerInstance.hasItems(this.icon.get().getItem()));
        }

        public Builder whenIconPlaced() {
            return this.deferredExternalTrigger(() -> {
                Item item = this.icon.get().getItem();
                if (item instanceof final BlockItem blockItem) {
                    return ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(blockItem.getBlock());
                }
                return InventoryChangeTrigger.TriggerInstance.hasItems(item);
            });
        }

        public Builder whenItemCollected(final ItemProviderEntry<?, ?> item) {
            return this.whenItemCollected(item.asStack()
                    .getItem());
        }

        public Builder whenItemCollected(final ItemLike itemProvider) {
            return this.externalTrigger(InventoryChangeTrigger.TriggerInstance.hasItems(itemProvider));
        }

        public Builder whenItemCollected(final TagKey<Item> tag) {
            return this.externalTrigger(InventoryChangeTrigger.TriggerInstance
                    .hasItems(ItemPredicate.Builder.item().of(BuiltInRegistries.ITEM, tag).build()));
        }

        public  Builder awardedForFree() {
            return this.externalTrigger(InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[] {}));
        }

        public Builder externalTrigger(final Criterion<? extends CriterionTriggerInstance> trigger) {
            return this.deferredExternalTrigger(() -> trigger);
        }

        private Builder deferredExternalTrigger(
                final Supplier<Criterion<? extends CriterionTriggerInstance>> trigger) {
            this.criteria.add(trigger);
            this.externalTrigger = true;
            return this;
        }
    }
}
