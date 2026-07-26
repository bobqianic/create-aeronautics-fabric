package com.zurrtum.create.foundation.item.render;

import net.minecraft.client.renderer.block.model.SimpleModelWrapper;

public class CustomRenderedItemModel {
    private final SimpleModelWrapper originalModel;

    public CustomRenderedItemModel() {
        this(null);
    }

    public CustomRenderedItemModel(SimpleModelWrapper originalModel) {
        this.originalModel = originalModel;
    }

    public SimpleModelWrapper getOriginalModel() {
        return originalModel;
    }
}
