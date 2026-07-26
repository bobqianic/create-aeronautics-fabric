package dev.eriksonn.aeronautics.content.blocks.levitite;

import dev.eriksonn.aeronautics.index.AeroBlocks;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyHelper;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import dev.ryanhcode.sable.physics.floating_block.FloatingBlockMaterial;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.util.SableMathUtils;
import org.joml.Matrix3f;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LevititeShaderManager {
    private static final double SMOOTHING_SPEED = 0.5;

    private static final Vector3d linearVelocity = new Vector3d();
    private static final Vector3d angularVelocity = new Vector3d();
    private static final Vector3d temp = new Vector3d();
    private static final Vector3d currentPos = new Vector3d();
    private static final Quaterniond currentOrientation = new Quaterniond();
    private static final Vector3d offset = new Vector3d();
    private static final Vector3d gravityVector1 = new Vector3d();

    public static HashMap<ClientSubLevel, LevititeShaderManager> managers = new HashMap<>();

    private final Vector3d smoothedLinearVelocity = new Vector3d();
    private final Vector3d lastSmoothedLinearVelocity = new Vector3d();
    private final Vector3d smoothedAngularVelocity = new Vector3d();
    private final Vector3d lastSmoothedAngularVelocity = new Vector3d();
    private final Vector3d accumulatedPosition = new Vector3d();

    public static void tick() {
        if (managers.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<ClientSubLevel, LevititeShaderManager>> iterator = managers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ClientSubLevel, LevititeShaderManager> entry = iterator.next();

            ClientSubLevel subLevel = entry.getKey();
            if (subLevel.isRemoved()) {
                iterator.remove();
                continue;
            }

            entry.getValue().internalTick(subLevel);
        }
    }

    public static LevititeShaderManager getInstance(ClientSubLevel subLevel) {
        managers.putIfAbsent(subLevel, new LevititeShaderManager());
        return managers.get(subLevel);
    }

    public static void disableShader() {
    }

    void internalTick(ClientSubLevel subLevel) {
        lastSmoothedLinearVelocity.set(smoothedLinearVelocity);
        lastSmoothedAngularVelocity.set(smoothedAngularVelocity);

        subLevel.logicalPose().position().sub(subLevel.lastPose().position(), linearVelocity);
        subLevel.logicalPose().rotationPoint().sub(subLevel.lastPose().rotationPoint(), temp);
        DimensionPhysicsData.getGravity(subLevel.getLevel(), subLevel.logicalPose().position(), gravityVector1);
        subLevel.logicalPose().orientation().transform(temp);
        linearVelocity.sub(temp);
        SableMathUtils.getAngularVelocity(subLevel.lastPose().orientation(), subLevel.logicalPose().orientation(), angularVelocity);

        smoothedLinearVelocity.lerp(linearVelocity, SMOOTHING_SPEED);
        smoothedAngularVelocity.lerp(angularVelocity, SMOOTHING_SPEED);
        accumulatedPosition.add(smoothedLinearVelocity);
        accumulatedPosition.set(
                (accumulatedPosition.x % 10000),
                (accumulatedPosition.y % 10000),
                (accumulatedPosition.z % 10000));

    }

    public static boolean isEnabled() {
        return false;
    }
}
