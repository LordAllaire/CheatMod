package com.cheatmod.cheatmod.client;

import com.cheatmod.cheatmod.CheatMod;
import com.cheatmod.cheatmod.config.CheatConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = CheatMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class XRayRenderer {

    public static final Set<Block> XRAY_BLOCKS = Set.of(
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.ANCIENT_DEBRIS,
            Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
            Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
            Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
            Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
            Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE
    );

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (!CheatConfig.xrayEnabled) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int radius = 16;
        BlockPos playerPos = mc.player.blockPosition();
        List<BlockPos> targets = new ArrayList<>();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    BlockState state = mc.level.getBlockState(pos);
                    if (XRAY_BLOCKS.contains(state.getBlock())) {
                        targets.add(pos);
                    }
                }
            }
        }

        if (targets.isEmpty()) return;

        double camX = mc.gameRenderer.getMainCamera().getPosition().x;
        double camY = mc.gameRenderer.getMainCamera().getPosition().y;
        double camZ = mc.gameRenderer.getMainCamera().getPosition().z;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthFunc(519); // GL_ALWAYS - render through walls

        Matrix4f matrix = event.getPoseStack().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (BlockPos pos : targets) {
            Block block = mc.level.getBlockState(pos).getBlock();
            float[] color = getColor(block);
            float r = color[0], g = color[1], b = color[2], a = 0.45f;

            float x0 = (float)(pos.getX() - camX);
            float y0 = (float)(pos.getY() - camY);
            float z0 = (float)(pos.getZ() - camZ);
            float x1 = x0 + 1, y1 = y0 + 1, z1 = z0 + 1;

            // Bottom face
            buffer.vertex(matrix, x0, y0, z0).color(r,g,b,a).endVertex();
            buffer.vertex(matrix, x1, y0, z0).color(r,g,b,a).endVertex();
            buffer.vertex(matrix, x1, y0, z1).color(r,g,b,a).endVertex();
            buffer.vertex(matrix, x0, y0, z1).color(r,g,b,a).endVertex();
            // Top face
            buffer.vertex(matrix, x0, y1, z0).color(r,g,b,a).endVertex();
            buffer.vertex(matrix, x0, y1, z1).color(r,g,b,a).endVertex();
            buffer.vertex(matrix, x1, y1, z1).color(r,g,b,a).endVertex();
            buffer.vertex(matrix, x1, y1, z0).color(r,g,b,a).endVertex();
            // North face
            buffer.vertex(matrix, x0, y0, z0).color(r,g,b,a).endVertex();
            buffer.vertex(matrix, x0, y1, z0).color(r,g,b,a).endVertex();
            buffer.vertex(matrix, x1, y1, z0).color(r,g,b,a).endVertex();
            buffer.vertex(matrix, x1, y0, z0).color(r,g,b,a).endVertex();
            // South face
            buffer.vertex(matrix, x0, y0, z1).color(r,g,b,a).endVertex();
            buffer.vertex(matrix, x1, y0, z1).color(r,g,b,a).endVertex();
            buffer.vertex(matrix, x1, y1, z1).color(r,g,b,a).endVertex();
            buffer.vertex(matrix, x0, y1, z1).color(r,g,b,a).endVertex();
            // West face
            buffer.vertex(matrix, x0, y0, z0).color(r,g,b,a).endVertex();
            buffer.vertex(matrix, x0, y0, z1).color(r,g,b,a).endVertex();
            buffer.vertex(matrix, x0, y1, z1).color(r,g,b,a).endVertex();
            buffer.vertex(matrix, x0, y1, z0).color(r,g,b,a).endVertex();
            // East face
            buffer.vertex(matrix, x1, y0, z0).color(r,g,b,a).endVertex();
            buffer.vertex(matrix, x1, y1, z0).color(r,g,b,a).endVertex();
            buffer.vertex(matrix, x1, y1, z1).color(r,g,b,a).endVertex();
            buffer.vertex(matrix, x1, y0, z1).color(r,g,b,a).endVertex();
        }

        Tesselator.getInstance().end();
        RenderSystem.depthFunc(515); // GL_LEQUAL - restore
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static float[] getColor(Block block) {
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) return new float[]{0.0f, 0.9f, 1.0f};
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) return new float[]{0.0f, 1.0f, 0.2f};
        if (block == Blocks.ANCIENT_DEBRIS) return new float[]{0.8f, 0.3f, 0.0f};
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE) return new float[]{1.0f, 0.85f, 0.0f};
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) return new float[]{0.8f, 0.6f, 0.4f};
        if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) return new float[]{1.0f, 0.0f, 0.0f};
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) return new float[]{0.1f, 0.2f, 1.0f};
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) return new float[]{0.9f, 0.5f, 0.2f};
        if (block == Blocks.NETHER_QUARTZ_ORE) return new float[]{1.0f, 1.0f, 1.0f};
        return new float[]{1.0f, 0.5f, 0.0f};
    }
}
