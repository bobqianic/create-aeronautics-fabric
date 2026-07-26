package dev.ryanhcode.sable.index;

import net.minecraft.server.level.TicketType;

/**
 * Sable's chunk ticket types. Fabric initializes these during common setup
 * before any level exists.
 */
public final class SableTicketTypes {

    /**
     * Keeps chunks inhabited by force-loaded sub-levels at entity-ticking
     * level. Assigned during Fabric common setup and available before a server
     * level ticks.
     */
    public static TicketType SUB_LEVEL_LOADED;

    private SableTicketTypes() {
    }
}
