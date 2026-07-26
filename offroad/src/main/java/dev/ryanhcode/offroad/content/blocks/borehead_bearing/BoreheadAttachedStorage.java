package dev.ryanhcode.offroad.content.blocks.borehead_bearing;

public interface BoreheadAttachedStorage {
    void attachBlockEntity(BoreheadBearingBlockEntity be);

    void setInsertAllowed(boolean insertionAllowed);

    void invokeUnstall();
}
