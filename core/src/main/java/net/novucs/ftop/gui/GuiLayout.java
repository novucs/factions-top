package net.novucs.ftop.gui;

import com.google.common.collect.ImmutableList;
import net.novucs.ftop.gui.element.GuiElement;

public class GuiLayout {

    private final ImmutableList<GuiElement> elements;
    private final int factionsPerPage;

    public GuiLayout(ImmutableList<GuiElement> elements, int factionsPerPage) {
        this.elements = elements;
        this.factionsPerPage = factionsPerPage;
    }

    public int getFactionsPerPage() {
        return factionsPerPage;
    }

    public void render(GuiContext context) {
        elements.forEach(element -> element.render(context));
    }
}
