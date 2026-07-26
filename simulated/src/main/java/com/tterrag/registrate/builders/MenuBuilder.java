package com.tterrag.registrate.builders;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.fabric.EnvExecutor;
import com.tterrag.registrate.util.entry.MenuEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;

import io.github.fabricators_of_create.porting_lib.util.DeferredHolder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import net.fabricmc.api.EnvType;

public class MenuBuilder<T extends AbstractContainerMenu, S extends Screen & MenuAccess<T>, P> extends AbstractBuilder<MenuType<?>, MenuType<T>, P, MenuBuilder<T, S, P>> {
    
    public interface MenuFactory<T extends AbstractContainerMenu> {
        
        T create(MenuType<T> type, int windowId, Inventory inv);
    }

    public interface ForgeMenuFactory<T extends AbstractContainerMenu> {

        T create(MenuType<T> type, int windowId, Inventory inv, Object data);
    }
    
    public interface ScreenFactory<M extends AbstractContainerMenu, T extends Screen & MenuAccess<M>> {
        
        T create(M menu, Inventory inv, Component displayName);
    }

    private final MenuFactory<T> factory;
    private final ForgeMenuFactory<T> forgeFactory;
    private final NonNullSupplier<ScreenFactory<T, S>> screenFactory;
    private StreamCodec<? super RegistryFriendlyByteBuf, Object> extraDataCodec;

    public MenuBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, MenuFactory<T> factory, NonNullSupplier<ScreenFactory<T, S>> screenFactory) {
        super(owner, parent, name, callback, Registries.MENU);
        this.factory = factory;
        this.forgeFactory = null;
        this.screenFactory = screenFactory;
    }

    public MenuBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ForgeMenuFactory<T> factory, NonNullSupplier<ScreenFactory<T, S>> screenFactory) {
        super(owner, parent, name, callback, Registries.MENU);
        this.forgeFactory = factory;
        this.factory = null;
        this.screenFactory = screenFactory;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <D> MenuBuilder<T, S, P> extraDataCodec(final StreamCodec<? super RegistryFriendlyByteBuf, D> codec) {
        this.extraDataCodec = (StreamCodec) codec;
        return this;
    }

    @Override
    protected @NonnullType MenuType<T> createEntry() {
        NonNullSupplier<MenuType<T>> supplier = this.asSupplier();
        MenuType<T> ret;
        if (this.factory == null) {
            ForgeMenuFactory<T> factory = this.forgeFactory;
            if (this.extraDataCodec == null) {
                throw new IllegalStateException("Extended menu '" + this.getName() + "' is missing an opening-data codec");
            }
            ret = new ExtendedScreenHandlerType<T, Object>(
                    (windowId, inv, data) -> factory.create(supplier.get(), windowId, inv, data),
                    this.extraDataCodec
            );
        } else {
            MenuFactory<T> factory = this.factory;
            ret = new MenuType<>((syncId, inventory) -> factory.create(supplier.get(), syncId, inventory), FeatureFlags.VANILLA_SET);
        }
        EnvExecutor.runWhenOn(EnvType.CLIENT, () -> () -> {
            ScreenFactory<T, S> screenFactory = this.screenFactory.get();
            MenuScreens.register(ret, screenFactory::create);
        });
        return ret;
    }

    @Override
    protected RegistryEntry<MenuType<?>, MenuType<T>> createEntryWrapper(DeferredHolder<MenuType<?>, MenuType<T>> delegate) {
        return new MenuEntry<>(getOwner(), delegate);
    }

    @Override
    public MenuEntry<T> register() {
        return (MenuEntry<T>) super.register();
    }
}
