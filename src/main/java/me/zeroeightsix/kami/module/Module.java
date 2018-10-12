package me.zeroeightsix.kami.module;

import com.google.common.base.Converter;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.modules.movement.Sprint;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.Bind;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by 086 on 23/08/2017.
 */
public class Module {

    private final String name = getAnnotation().name();
    private final String description = getAnnotation().description();
    private final Category category = getAnnotation().category();
    private Setting<Bind> bind = Settings.custom("Bind", Bind.none(), new BindConverter(), true);
    private boolean enabled;
    public boolean alwaysListening;
    protected static final Minecraft mc = Minecraft.getMinecraft();

    public Module() {
        alwaysListening = getAnnotation().alwaysListening();

        enabled = false;
    }

    private Info getAnnotation() {
        return getClass().isAnnotationPresent(Info.class) ? getClass().getAnnotation(Info.class) : Sprint.class.getAnnotation(Info.class); // dummy annotation
    }

    public void onUpdate() {}
    public void onRender() {}
    public void onWorldRender(RenderEvent event) {}

    public Bind getBind() {
        return bind.getValue();
    }

    public String getBindName() {
        return bind.getValue().toString();
    }

    public enum Category
    {
        COMBAT("Combat", false),
        EXPLOITS("Exploits", false),
        RENDER("Render", false),
        MISC("Misc", false),
        PLAYER("Player", false),
        MOVEMENT("Movement", false),
        HIDDEN("Hidden", true);

        boolean hidden;
        String name;
        private int bind;

        Category(String name, boolean hidden) {
            this.name = name;
            this.hidden = hidden;
        }

        public boolean isHidden() {
            return hidden;
        }

        public String getName() {
            return name;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Info
    {
        String name();
        String description() default "Descriptionless";
        Module.Category category();
        boolean alwaysListening() default false;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    protected void onEnable() {}
    protected void onDisable() {}

    public void toggle() {
        setEnabled(!isEnabled());
    }

    public void enable() {
        enabled = true;
        onEnable();
        if (!alwaysListening)
            KamiMod.EVENT_BUS.subscribe(this);
    }

    public void disable() {
        enabled = false;
        onDisable();
        if (!alwaysListening)
            KamiMod.EVENT_BUS.unsubscribe(this);
    }

    public boolean isDisabled() {
        return !isEnabled();
    }

    public void setEnabled(boolean enabled) {
        boolean prev = this.enabled;
        if (prev != enabled)
            if (enabled)
                enable();
            else
                disable();
    }

    public String getHudInfo() {
        return null;
    }

    protected final void setAlwaysListening(boolean alwaysListening) {
        this.alwaysListening = alwaysListening;
        if (alwaysListening) KamiMod.EVENT_BUS.subscribe(this);
        if (!alwaysListening && isDisabled()) KamiMod.EVENT_BUS.unsubscribe(this);
    }

    /**
     * Cleanup method in case this module wants to do something when the client closes down
     */
    public void destroy(){};

    private class BindConverter extends Converter<Bind, String> {
        @Override
        protected String doForward(Bind bind) {
            return bind.toString();
        }

        @Override
        protected Bind doBackward(String s) {
            s = s.toLowerCase();
            if (s.equals("None")) return Bind.none();
            boolean ctrl = false, alt = false, shift = false;

            if (s.startsWith("Ctrl+")) {
                ctrl = true;
                s = s.substring(5);
            }
            if (s.startsWith("Alt+")) {
                alt = true;
                s = s.substring(4);
            }
            if (s.startsWith("Shift+")) {
                shift = true;
                s = s.substring(6);
            }

            int key = -1;
            try {
                key = Keyboard.getKeyIndex(s);
            } catch (Exception ignored) {}

            if (key == -1) return Bind.none();
            return new Bind(ctrl, alt, shift, key);
        }
    }
}
