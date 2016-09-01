package net.novucs.ftop.entity;

import java.util.List;
import java.util.Objects;

public class ButtonMessage {

    private final String enabled;
    private final String disabled;
    private final List<String> tooltip;

    public ButtonMessage(String enabled, String disabled, List<String> tooltip) {
        this.enabled = enabled;
        this.disabled = disabled;
        this.tooltip = tooltip;
    }

    public String getEnabled() {
        return enabled;
    }

    public String getDisabled() {
        return disabled;
    }

    public List<String> getTooltip() {
        return tooltip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ButtonMessage that = (ButtonMessage) o;
        return Objects.equals(enabled, that.enabled) &&
                Objects.equals(disabled, that.disabled) &&
                Objects.equals(tooltip, that.tooltip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, disabled, tooltip);
    }

    @Override
    public String toString() {
        return "ButtonMessage{" +
                "enabled='" + enabled + '\'' +
                ", disabled='" + disabled + '\'' +
                ", tooltip=" + tooltip +
                '}';
    }
}
