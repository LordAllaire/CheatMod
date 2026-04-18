package com.cheatmod.cheatmod.mixin;

import com.cheatmod.cheatmod.config.CheatConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(BlockState.class)
public class BlockRenderMixin {

    private static final Set<Block> XRAY_VISIBLE = Set.of(
            Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
            Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
            Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
            Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
            Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE,
            Blocks.ANCIENT_DEBRIS,
            Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST,
            Blocks.SPAWNER, Blocks.BEDROCK, Blocks.OBSIDIAN,
            Blocks.CRYING_OBSIDIAN, Blocks.WATER, Blocks.LAVA
    );

    @Inject(method = "isOpaque", at = @At("RETURN"), cancellable = true)
    private void cheatmod_xray_isOpaque(CallbackInfoReturnable<Boolean> cir) {
        if (!CheatConfig.xrayEnabled) return;
        BlockState state = (BlockState)(Object)this;
        if (!XRAY_VISIBLE.contains(state.getBlock())) {
            cir.setReturnValue(false);
        }
    }
}
