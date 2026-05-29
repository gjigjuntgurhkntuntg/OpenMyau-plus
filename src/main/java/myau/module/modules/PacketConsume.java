package myau.module.modules;

import myau.event.EventTarget;
import myau.events.Render2DEvent;
import myau.events.RightClickMouseEvent;
import myau.events.TickEvent;
import myau.module.Module;
import myau.property.properties.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;

import java.awt.*;

public class PacketConsume extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public final BooleanProperty food = new BooleanProperty("Food", true);
    public final BooleanProperty potion = new BooleanProperty("Potion", true);
    public final BooleanProperty instant = new BooleanProperty("Instant", false);

    private boolean consuming;
    private ItemStack consumeStack;
    private long consumeStartTime;

    public PacketConsume() {
        super("PacketConsume", false, true, "Packet-based eating and drinking");
    }

    @Override
    public void onDisabled() {
        if (consuming) {
            releaseKey();
            if (mc.thePlayer != null && mc.thePlayer.isUsingItem()) {
                mc.thePlayer.stopUsingItem();
            }
            consuming = false;
            consumeStack = null;
        }
    }

    @EventTarget
    public void onRightClick(RightClickMouseEvent event) {
        if (consuming) return;
        if (mc.thePlayer == null || mc.thePlayer.getHeldItem() == null) return;

        ItemStack stack = mc.thePlayer.getHeldItem();
        EnumAction action = stack.getItem().getItemUseAction(stack);

        boolean isEdible = action == EnumAction.EAT && food.getValue();
        boolean isDrinkable = action == EnumAction.DRINK && potion.getValue();
        if (!isEdible && !isDrinkable) return;

        consuming = true;
        consumeStack = stack;
        consumeStartTime = System.currentTimeMillis();

        if (instant.getValue()) {
            mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, stack, 0.0F, 0.0F, 0.0F));
            finishConsumption(stack);
            consuming = false;
            consumeStack = null;
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (!consuming || mc.thePlayer == null) return;

        if (!mc.thePlayer.isUsingItem()) {
            releaseKey();
            consuming = false;
            consumeStack = null;
        } else if (mc.thePlayer.getItemInUseCount() <= 1) {
            releaseKey();
        }
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!consuming || instant.getValue()) return;
        if (mc.thePlayer == null) return;

        ScaledResolution sr = new ScaledResolution(mc);
        int barWidth = 120;
        int barHeight = 6;
        int x = (sr.getScaledWidth() - barWidth) / 2;
        int y = sr.getScaledHeight() / 2 + 20;

        float progress = Math.min(1.0f, (System.currentTimeMillis() - consumeStartTime) / 1600.0f);
        int fillWidth = (int) (barWidth * progress);

        GlStateManager.enableBlend();
        Gui.drawRect(x, y, x + barWidth, y + barHeight, 0x90000000);
        int color = interpolateColor(new Color(0xFFFF4444), new Color(0xFF44FF44), progress);
        Gui.drawRect(x + 1, y + 1, x + fillWidth - 1, y + barHeight - 1, color);
        GlStateManager.disableBlend();
    }

    private void finishConsumption(ItemStack stack) {
        if (stack == null) return;

        if (stack.getItem() instanceof ItemFood && food.getValue()) {
            ItemFood itemFood = (ItemFood) stack.getItem();
            mc.thePlayer.getFoodStats().addStats(itemFood.getHealAmount(stack), itemFood.getSaturationModifier(stack));
        }

        if (stack.getItem() instanceof ItemPotion && potion.getValue()) {
            ItemPotion itemPotion = (ItemPotion) stack.getItem();
            java.util.Collection<PotionEffect> effects = itemPotion.getEffects(stack);
            if (effects != null) {
                for (PotionEffect effect : effects) {
                    mc.thePlayer.addPotionEffect(effect);
                }
            }
        }
    }

    private void releaseKey() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
    }

    private int interpolateColor(Color a, Color b, float t) {
        int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int blue = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        int alpha = (int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t);
        return (alpha << 24) | (r << 16) | (g << 8) | blue;
    }
}
