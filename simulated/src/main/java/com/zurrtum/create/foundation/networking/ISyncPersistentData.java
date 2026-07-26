package com.zurrtum.create.foundation.networking;

/** Legacy marker for entities that synchronize additional persistent data. */
public interface ISyncPersistentData {
    default void onPersistentDataUpdated() {
    }
}
