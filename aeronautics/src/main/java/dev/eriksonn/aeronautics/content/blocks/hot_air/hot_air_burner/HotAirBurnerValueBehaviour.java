package dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import dev.simulated_team.simulated.compat.create.ScrollValueBehaviour;
import dev.simulated_team.simulated.compat.create.ValueBoxTransform;
import dev.eriksonn.aeronautics.data.AeroLang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

public class HotAirBurnerValueBehaviour extends ScrollValueBehaviour {
    private static final MutableComponent TITLE = AeroLang.translate("generic.hot_air").component();

    private int interval = 5;

    private Supplier<Integer> minSupplier, maxSupplier;

    public HotAirBurnerValueBehaviour(final Component label, final SmartBlockEntity be, final ValueBoxTransform slot) {
        super(label, be, slot);
    }
    private void updateInterval()
    {
        this.interval = (this.maxSupplier.get()- this.minSupplier.get()+250)/500;
        this.interval *=5;
        this.interval = Math.max(1, this.interval);
    }
    @Override
    public ValueSettingsBoard createBoard(final Player player, final BlockHitResult hitResult) {
        this.updateInterval();
        return new ValueSettingsBoard(this.label, this.maxSupplier.get()/ this.interval, 10, ImmutableList.of(TITLE),
                new ValueSettingsFormatter(this::format));
    }

    private MutableComponent format(final ValueSettings valueSettings) {
        this.updateInterval();
        int value = Mth.clamp(valueSettings.value() * this.interval, this.minSupplier.get(), this.maxSupplier.get());
        return AeroLang.translate("unit.meter_cubed", value).component();
    }

    @Override
    public ValueSettings getValueSettings() {
        return new ValueSettings(0, this.value / this.interval);
    }

    @Override
    public void setValueSettings(final Player player, final ValueSettings valueSetting, final boolean ctrlDown) {
        if (!valueSetting.equals(this.getValueSettings()))
            this.playFeedbackSound(this);
        this.updateInterval();
        this.setValue(valueSetting.value() * this.interval);
    }

    public ScrollValueBehaviour between(final Supplier<Integer> min, final Supplier<Integer> max) {
        this.minSupplier = min;
        this.maxSupplier = max;

        this.between(min.get(), max.get());
        return this;
    }

    @Override
    public void setValue(int value) {
        value = Mth.clamp(value, this.minSupplier.get(), this.maxSupplier.get());
        if (value == this.value)
            return;

        this.value = value;

        this.blockEntity.setChanged();
        this.blockEntity.sendData();
    }

    public String getClipboardKey() {
        return "Hot Air";
    }
}
