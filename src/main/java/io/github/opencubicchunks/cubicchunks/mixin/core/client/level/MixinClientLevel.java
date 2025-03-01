package io.github.opencubicchunks.cubicchunks.mixin.core.client.level;

import java.util.function.Supplier;

import io.github.opencubicchunks.cubicchunks.chunk.entity.IsCubicEntityContext;
import io.github.opencubicchunks.cubicchunks.client.multiplayer.CubicClientLevel;
import io.github.opencubicchunks.cubicchunks.world.ImposterChunkPos;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevelHeightAccessor;
import io.github.opencubicchunks.cubicchunks.world.level.chunk.LevelCube;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class MixinClientLevel extends Level implements CubicClientLevel {

    @Shadow @Final private TransientEntitySectionManager<Entity> entityStorage;

    protected MixinClientLevel(WritableLevelData levelData, ResourceKey<Level> resourceKey, DimensionType dimensionType,
                               Supplier<ProfilerFiller> profiler, boolean b0, boolean b1, long l) {
        super(levelData, resourceKey, dimensionType, profiler, b0, b1, l);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void setIsCubicContext(ClientPacketListener clientPacketListener, ClientLevel.ClientLevelData clientLevelData, ResourceKey<Level> resourceKey, DimensionType dimensionType, int i,
                                   Supplier<ProfilerFiller> supplier, LevelRenderer levelRenderer, boolean bl, long l, CallbackInfo ci) {
        ((IsCubicEntityContext) this.entityStorage).setIsCubic(((CubicLevelHeightAccessor) this).isCubic());
    }

    @Override
    public void onCubeLoaded(int cubeX, int cubeY, int cubeZ) {
        //TODO: implement colorCaches in onCubeLoaded
//        this.colorCaches.forEach((p_228316_2_, p_228316_3_) -> {
//            p_228316_3_.invalidateChunk(chunkX, chunkZ);
//        });
        this.entityStorage.startTicking(new ImposterChunkPos(cubeX, cubeY, cubeZ));
    }

    @Override public void unload(LevelCube cube) {
        this.entityStorage.stopTicking(new ImposterChunkPos(cube.getCubePos()));
    }

    @Redirect(method = "onChunkLoaded",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/entity/TransientEntitySectionManager;startTicking(Lnet/minecraft/world/level/ChunkPos;)V"))
    private void doNothingOnLoadIfCube(TransientEntitySectionManager<?> transientEntitySectionManager, ChunkPos pos) {
        if (!((CubicLevelHeightAccessor) this).isCubic()) {
            transientEntitySectionManager.startTicking(pos);
        }
    }


    @Redirect(method = "unload",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/entity/TransientEntitySectionManager;stopTicking(Lnet/minecraft/world/level/ChunkPos;)V"))
    private void doNothingOnUnloadIfCube(TransientEntitySectionManager<?> transientEntitySectionManager, ChunkPos pos) {
        if (!((CubicLevelHeightAccessor) this).isCubic()) {
            transientEntitySectionManager.stopTicking(pos);
        }
    }
}