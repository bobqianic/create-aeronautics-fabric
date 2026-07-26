package foundry.veil.api.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.joml.Vector3d;

import java.util.List;

public final class CodecUtil {
    public static final Codec<Vector3d> VECTOR3D_CODEC = Codec.DOUBLE.listOf()
            .comapFlatMap(CodecUtil::vector3d, vector -> List.of(vector.x(), vector.y(), vector.z()));

    private CodecUtil() {
    }

    private static DataResult<Vector3d> vector3d(final List<Double> values) {
        if (values.size() != 3) {
            return DataResult.error(() -> "Vector3d must have 3 elements");
        }
        return DataResult.success(new Vector3d(values.get(0), values.get(1), values.get(2)));
    }
}
