package dev.ryanhcode.offroad.service;

import com.zurrtum.create.content.contraptions.MountedStorageManager;
import dev.simulated_team.simulated.service.ServiceUtil;
import dev.ryanhcode.offroad.content.blocks.borehead_bearing.BoreheadAttachedStorage;

public interface OffroadMountedStorageService {

    OffroadMountedStorageService INSTANCE = ServiceUtil.load(OffroadMountedStorageService.class);

    <T extends MountedStorageManager & BoreheadAttachedStorage> T getSidedBoreheadContraptionMountedStorage();

}
