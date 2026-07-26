package dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.graph;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * A graph / map of {@link BalloonLayerData BalloonLayers}
 */
public class BalloonLayerGraph {

    /**
     * Layers, in increasing y, starting at minY
     */
    private List<BalloonLayerData>[] layerMap;

    /**
     * The minimum Y coordinate in the graph
     */
    private int minY;

    public BalloonLayerGraph(final int yLevel) {
        this.minY = yLevel;
        this.layerMap = new List[]{new ObjectArrayList<>()};
    }

    public void addLayer(final int y, final BalloonLayerData layer) {
        int index = this.getIndex(y);

        if (index < 0) {
            this.resizeDownwards(y);
            index = this.getIndex(y);
        } else if (index >= this.layerMap.length) {
            this.resizeUpwards(y);
        }

        this.layerMap[index].add(layer);
    }

    public void removeLayer(final BalloonLayerData layer) {
        final int index = this.getIndex(layer.getYLevel());

        final List<BalloonLayerData> layers = this.layerMap[index];
        layers.remove(layer);
    }

    public void trim() {
        int start = 0;
        int end = this.layerMap.length - 1;

        while (start <= end && this.layerMap[start].isEmpty()) start++;
        while (end >= start && this.layerMap[end].isEmpty()) end--;

        if (start == 0 && end == this.layerMap.length - 1) return;

        final int newSize = end - start + 1;
        if (newSize <= 0) return;

        //noinspection unchecked
        final List<BalloonLayerData>[] newLayerMap = new List[newSize];
        System.arraycopy(this.layerMap, start, newLayerMap, 0, newSize);

        this.layerMap = newLayerMap;
        this.minY += start;
    }

    private int getIndex(final int y) {
        return y - this.minY;
    }

    public List<BalloonLayerData> getLayersAtY(final int y) {
        final int index = this.getIndex(y);

        if (index >= 0 && index < this.layerMap.length) {
            return this.layerMap[index];
        }

        return Collections.emptyList();
    }

    private void resizeUpwards(final int targetY) {
        final int requiredSize = targetY - this.minY + 1;

        if (requiredSize <= this.layerMap.length) {
            return;
        }

        //noinspection unchecked
        final List<BalloonLayerData>[] newLayers = new List[requiredSize];
        System.arraycopy(this.layerMap, 0, newLayers, 0, this.layerMap.length);

        for (int i = this.layerMap.length; i < requiredSize; i++) {
            newLayers[i] = new ObjectArrayList<>();
        }

        this.layerMap = newLayers;
    }

    private void resizeDownwards(final int newMinY) {
        final int deltaY = this.minY - newMinY;
        final int oldLength = this.layerMap.length;
        final int newLength = oldLength + deltaY;

        //noinspection unchecked
        final List<BalloonLayerData>[] newLayers = new List[newLength];

        for (int i = 0; i < deltaY; i++) {
            newLayers[i] = new ObjectArrayList<>();
        }

        System.arraycopy(this.layerMap, 0, newLayers, deltaY, oldLength);

        this.layerMap = newLayers;
        this.minY = newMinY;
    }

    public List<BalloonLayerData>[] getAllLayers() {
        return this.layerMap;
    }

    public int getMinY() {
        return this.minY;
    }

    public int getMaxY() {
        return this.minY + this.layerMap.length - 1;
    }

    /**
     * Adds all the layers from another graph
     */
    public void addAll(final BalloonLayerGraph otherGraph) {
        final List<BalloonLayerData>[] otherLayerMap = otherGraph.getAllLayers();
        for (int index = 0; index < otherLayerMap.length; index++) {
            final List<BalloonLayerData> layersAtY = otherLayerMap[index];
            final int layerY = index + otherGraph.getMinY();

            for (final BalloonLayerData otherLayer : layersAtY) {
                this.addLayer(layerY, otherLayer);
            }
        }
    }

    @Nullable
    public BalloonLayerData getLayerAt(final BlockPos pos) {
        final List<BalloonLayerData> layers = this.getLayersAtY(pos.getY());

        for (final BalloonLayerData layer : layers) {
            final int x = pos.getX();
            final int z = pos.getZ();

            if (layer.getHotAirBlock(x, z)) {
                return layer;
            }
        }

        return null;
    }

    public boolean hasBlockAt(final BlockPos pos) {
        return this.getLayerAt(pos) != null;
    }

    public void rebuildConnections(final BlockPos startPos) {
        // clear existing connections
        for (final List<BalloonLayerData> layersAtY : this.layerMap) {
            for (final BalloonLayerData layer : layersAtY) {
                layer.inwardConnections.clear();
                layer.outwardConnections.clear();
            }
        }

        // build all the new ones!
        final BalloonLayerData startLayer = this.getLayerAt(startPos);
        if (startLayer == null) {
            return;
        }

        final ObjectArrayList<BalloonLayerData> queue = new ObjectArrayList<>();
        final ObjectArrayList<BalloonLayerData> visited = new ObjectArrayList<>();

        queue.add(startLayer);
        visited.add(startLayer);

        while (!queue.isEmpty()) {
            final BalloonLayerData current = queue.removeLast();
            final int currentY = current.getYLevel();

            for (int dy = -1; dy <= 1; dy += 2) {
                final int neighborY = currentY + dy;

                final List<BalloonLayerData> neighborLayers = this.getLayersAtY(neighborY);
                if (neighborLayers.isEmpty()) continue;

                for (final BalloonLayerData neighbor : neighborLayers) {
                    if (current.overlaps(neighbor) && !neighbor.outwardConnections.contains(current) && !neighbor.inwardConnections.contains(current)) {
                        current.outwardConnections.add(neighbor);
                        neighbor.inwardConnections.add(current);

                        if (!visited.contains(neighbor)) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }
    }

    public Iterable<BalloonLayerData> propagateRemoval(final BalloonLayerData startLayer) {
        if (startLayer == null) return null;

        final ObjectArrayList<BalloonLayerData> frontier = new ObjectArrayList<>();
        final ObjectArrayList<BalloonLayerData> visited = new ObjectArrayList<>();

        frontier.add(startLayer);
        visited.add(startLayer);

        while (!frontier.isEmpty()) {
            final BalloonLayerData current = frontier.removeLast();

            for (final BalloonLayerData outward : current.outwardConnections) {
                if (!visited.contains(outward)) {
                    visited.add(outward);
                    frontier.add(outward);
                }
            }

            for (final BalloonLayerData inward : current.inwardConnections) {
                // only go down inward edges
                if (current.getYLevel() > inward.getYLevel() && !visited.contains(inward)) {
                    visited.add(inward);
                    frontier.add(inward);
                }
            }
        }

        for (final BalloonLayerData layer : visited) {
            this.removeLayer(layer);
        }

        this.trim();
        return visited;
    }
}