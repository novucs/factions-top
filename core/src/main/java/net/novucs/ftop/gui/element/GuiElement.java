package net.novucs.ftop.gui.element;

import net.novucs.ftop.gui.GuiContext;

public interface GuiElement {

    void render(GuiContext context);

    void handleClick(GuiContext context);

}
