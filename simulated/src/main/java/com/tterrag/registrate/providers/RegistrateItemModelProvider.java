package com.tterrag.registrate.providers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import io.github.fabricators_of_create.porting_lib.models.generators.ModelFile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import io.github.fabricators_of_create.porting_lib.models.generators.ItemModelBuilder;
import io.github.fabricators_of_create.porting_lib.models.generators.ItemModelProvider;
import net.fabricmc.api.EnvType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RegistrateItemModelProvider extends ItemModelProvider implements RegistrateProvider {

    private final AbstractRegistrate<?> parent;
    private final PackOutput.PathProvider itemDefinitions;
    private final Map<ResourceLocation, ResourceLocation> customItemModels = new HashMap<>();

    public RegistrateItemModelProvider(AbstractRegistrate<?> parent, PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, parent.getModid(), existingFileHelper);
        this.parent = parent;
        this.itemDefinitions = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
    }

    @Override
    public EnvType getSide() {
        return EnvType.CLIENT;
    }

    @Override
    protected void registerModels() {
        parent.genData(ProviderType.ITEM_MODEL, this);
        registerFallbackModels();
    }

    /**
     * Registrate promises a default model for items and block items. Older
     * Porting Lib versions could lose the block-item callback while crossing
     * the blockstate/item-model provider boundary, so enforce that contract
     * once all explicit callbacks have run.
     */
    private void registerFallbackModels() {
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            if (!itemId.getNamespace().equals(parent.getModid())) {
                continue;
            }

            ResourceLocation modelId = ResourceLocation.fromNamespaceAndPath(
                    itemId.getNamespace(), "item/" + itemId.getPath());
            if (generatedModels.containsKey(modelId)) {
                continue;
            }

            if (item instanceof BlockItem blockItem) {
                ResourceLocation inventoryModel = ResourceLocation.fromNamespaceAndPath(
                        itemId.getNamespace(), "block/" + itemId.getPath() + "/item");
                Optional<String> variantModel = parent.getDataProvider(ProviderType.BLOCKSTATE)
                        .flatMap(provider -> provider.getExistingVariantBuilder(blockItem.getBlock())
                                .map(builder -> builder.toJson())
                                .or(() -> provider.getExistingMultipartBuilder(blockItem.getBlock())
                                        .map(builder -> builder.toJson())))
                        .flatMap(this::findFirstModel);

                String parentModel = hasPackModel(inventoryModel)
                        ? inventoryModel.toString()
                        : variantModel.orElseGet(() -> ResourceLocation.fromNamespaceAndPath(
                                itemId.getNamespace(), "block/" + itemId.getPath()).toString());
                withExistingParent(itemId.getPath(), parentModel);
            } else {
                generated(() -> item);
            }
        }
    }

    private boolean hasPackModel(ResourceLocation modelId) {
        String resourcePath = "assets/" + modelId.getNamespace() + "/models/"
                + modelId.getPath() + ".json";
        return RegistrateItemModelProvider.class.getClassLoader().getResource(resourcePath) != null;
    }

    private Optional<String> findFirstModel(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            JsonElement model = object.get("model");
            if (model != null && model.isJsonPrimitive() && model.getAsJsonPrimitive().isString()) {
                return Optional.of(model.getAsString());
            }
            for (var entry : object.entrySet()) {
                Optional<String> nested = findFirstModel(entry.getValue());
                if (nested.isPresent()) {
                    return nested;
                }
            }
        } else if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                Optional<String> nested = findFirstModel(child);
                if (nested.isPresent()) {
                    return nested;
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        CompletableFuture<?> legacyModels = super.run(cache);
        List<CompletableFuture<?>> futures = new ArrayList<>();
        futures.add(legacyModels);

        generatedModels.keySet().stream()
                .filter(modelId -> modelId.getPath().startsWith("item/"))
                .filter(modelId -> modelId.getPath().indexOf('/', "item/".length()) < 0)
                .forEach(modelId -> {
                    ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath(
                            modelId.getNamespace(), modelId.getPath().substring("item/".length()));
                    JsonObject model = new JsonObject();
                    ResourceLocation customModel = customItemModels.get(itemId);
                    model.addProperty("type", customModel == null ? "minecraft:model" : customModel.toString());
                    if (customModel == null) {
                        model.addProperty("model", modelId.toString());
                    }
                    JsonObject definition = new JsonObject();
                    definition.add("model", model);
                    futures.add(DataProvider.saveStable(cache, definition, itemDefinitions.json(itemId)));
                });

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Item models and client item definitions";
    }

    public void customItemModel(String itemName, ResourceLocation modelType) {
        customItemModels.put(modLoc(itemName), modelType);
    }

    public String modid(NonNullSupplier<? extends ItemLike> item) {
        return BuiltInRegistries.ITEM.getKey(item.get().asItem()).getNamespace();
    }

    public String name(NonNullSupplier<? extends ItemLike> item) {
        return BuiltInRegistries.ITEM.getKey(item.get().asItem()).getPath();
    }

    public ResourceLocation itemTexture(NonNullSupplier<? extends ItemLike> item) {
        return modLoc("item/" + name(item));
    }

    public ItemModelBuilder blockItem(NonNullSupplier<? extends ItemLike> block) {
        return blockItem(block, "");
    }

    public ItemModelBuilder blockItem(NonNullSupplier<? extends ItemLike> block, String suffix) {
        return withExistingParent(name(block), ResourceLocation.fromNamespaceAndPath(modid(block), "block/" + name(block) + suffix));
    }

    public ItemModelBuilder blockWithInventoryModel(NonNullSupplier<? extends ItemLike> block) {
        return withExistingParent(name(block), ResourceLocation.fromNamespaceAndPath(modid(block), "block/" + name(block) + "_inventory"));
    }

    public ItemModelBuilder blockSprite(NonNullSupplier<? extends ItemLike> block) {
        return blockSprite(block, modLoc("block/" + name(block)));
    }

    public ItemModelBuilder blockSprite(NonNullSupplier<? extends ItemLike> block, ResourceLocation texture) {
        return generated(() -> block.get().asItem(), texture);
    }

    public ItemModelBuilder generated(NonNullSupplier<? extends ItemLike> item) {
        return generated(item, itemTexture(item));
    }

    public ItemModelBuilder generated(NonNullSupplier<? extends ItemLike> item, ResourceLocation... layers) {
        ItemModelBuilder ret = getBuilder(name(item)).parent(new ModelFile.UncheckedModelFile("item/generated"));
        for (int i = 0; i < layers.length; i++) {
            ret = ret.texture("layer" + i, layers[i]);
        }
        return ret;
    }

    public ItemModelBuilder handheld(NonNullSupplier<? extends ItemLike> item) {
        return handheld(item, itemTexture(item));
    }

    public ItemModelBuilder handheld(NonNullSupplier<? extends ItemLike> item, ResourceLocation texture) {
        return withExistingParent(name(item), "item/handheld").texture("layer0", texture);
    }
}
