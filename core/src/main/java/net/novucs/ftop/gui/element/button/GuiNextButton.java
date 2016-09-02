package net.novucs.ftop.gui.element.button;

import net.novucs.ftop.gui.GuiContext;

public class GuiNextButton extends GuiBiStateButton {

    public GuiNextButton(GuiButtonContent enabled, GuiButtonContent disabled) {
        super(enabled, disabled);
    }

    @Override
    public void render(GuiContext context) {
        GuiButtonContent content = context.hasNextPage() ? getEnabled() : getDisabled();
        context.getInventory().setItem(context.getAndIncrementSlot(), content.getItem());
        context.getSlots().add(this);
    }

    @Override
    public void handleClick(GuiContext context) {
        if (context.hasNextPage()) {
            context.getPlugin().getGuiManager().sendGui(context.getPlayer(), context.getThisPage() + 1);
        }
    }
}
