package io.github.opencubicchunks.cubicchunks.world.server;

import io.github.opencubicchunks.cubicchunks.world.level.chunk.LevelCube;

public interface CubicServerLevel {
    void onCubeUnloading(LevelCube cube);

    void tickCube(LevelCube cube, int randomTicks);
}