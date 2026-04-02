package com.cheatmod.cheatmod.mixin;

import com.cheatmod.cheatmod.client.XRayRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.level.block.RenderShape;

@Mixin(BlockState.class)
public class BlockRenderMixin {

    @Inject(method = "getRenderShape", at = @At("RETURN"), cancellable = true)
    private void cheatmod_xray_getRenderShape(CallbackInfoReturnable<RenderShape> cir) {
        BlockState state = (BlockState)(Object)this;
        if (XRayRenderer.shouldHide(state.getBlock())) {
            cir.setReturnValue(RenderShape.INVISIBLE);
        }
    }
}
