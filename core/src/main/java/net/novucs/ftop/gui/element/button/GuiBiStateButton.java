package net.novucs.ftop.gui.element.button;

import java.util.Objects;

abstract class GuiBiStateButton implements GuiButton {

    private final GuiButtonContent enabled;
    private final GuiButtonContent disabled;

    GuiBiStateButton(GuiButtonContent enabled, GuiButtonContent disabled) {
        this.enabled = enabled;
        this.disabled = disabled;
    }

    public GuiButtonContent getEnabled() {
        return enabled;
    }

    public GuiButtonContent getDisabled() {
        return disabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GuiBiStateButton that = (GuiBiStateButton) o;
        return Objects.equals(enabled, that.enabled) &&
                Objects.equals(disabled, that.disabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, disabled);
    }

    @Override
    public String toString() {
        return "GuiBiStateButton{" +
                "enabled=" + enabled +
                ", disabled=" + disabled +
                '}';
    }
}
