package myau.module.modules;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.AttackEvent;
import myau.events.TickEvent;
import myau.module.Module;
import myau.property.properties.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;

public class Hitflick extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public final ModeProperty direction = new ModeProperty("Direction", 0, new String[]{"Left", "Right", "Back", "Custom"});
    public final FloatProperty customAngle = new FloatProperty("Custom-Angle", 90F, 1F, 180F, () -> this.direction.getValue() == 3);
    public final IntProperty cooldown = new IntProperty("Cooldown", 1, 1, 40);

    private long sinceLastFlick;
    private float originalYaw;
    private boolean flicking;

    public Hitflick() {
        super("Hitflick", false, true, "Flick away on hit then restore");
    }

    @Override
    public void onEnabled() {
        sinceLastFlick = 0;
        flicking = false;
    }

    @Override
    public void onDisabled() {
        flicking = false;
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        if (event.getTarget() == null || event.getTarget() == mc.thePlayer) return;
        if (flicking || sinceLastFlick < cooldown.getValue()) return;
        if (!(event.getTarget() instanceof EntityLivingBase)) return;

        originalYaw = mc.thePlayer.rotationYaw;
        mc.thePlayer.rotationYaw += getFlickAngle();
        flicking = true;
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (event.getType() != EventType.PRE) return;
        if (mc.thePlayer == null) return;

        if (flicking) {
            mc.thePlayer.rotationYaw = originalYaw;
            flicking = false;
            sinceLastFlick = 0;
        }

        sinceLastFlick++;
    }

    private float getFlickAngle() {
        switch (direction.getValue()) {
            case 0: return -90F;
            case 1: return 90F;
            case 2: return 180F;
            case 3: return customAngle.getValue();
            default: return 90F;
        }
    }
}
