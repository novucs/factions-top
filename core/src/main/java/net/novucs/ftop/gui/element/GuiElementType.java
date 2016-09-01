package net.novucs.ftop.gui.element;

import net.novucs.ftop.gui.element.button.GuiBackButton;
import net.novucs.ftop.gui.element.button.GuiButtonContent;
import net.novucs.ftop.gui.element.button.GuiNextButton;
import org.bukkit.Material;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.novucs.ftop.util.GenericUtils.*;
import static net.novucs.ftop.util.StringUtils.*;

public enum GuiElementType {

    FACTION_LIST(GuiElementType::parseFactionList),
    BUTTON_BACK(data ->
            new GuiBackButton(parseButton(getMap(data, "enabled").orElse(Collections.emptyMap())),
                    parseButton(getMap(data, "disabled").orElse(Collections.emptyMap())))
    ),
    BUTTON_NEXT(data ->
            new GuiNextButton(parseButton(getMap(data, "enabled").orElse(Collections.emptyMap())),
                    parseButton(getMap(data, "disabled").orElse(Collections.emptyMap())))
    );

    private final ElementParser parser;

    GuiElementType(ElementParser parser) {
        this.parser = parser;
    }

    public ElementParser getParser() {
        return parser;
    }

    private static GuiFactionList parseFactionList(Map<?,?> element) {
        GuiFactionList.Builder builder = new GuiFactionList.Builder();
        Optional<Integer> factionCount = getInt(element, "faction-count");
        if (factionCount.isPresent()) {
            builder.factionCount(factionCount.get());
        }

        Optional<Boolean> fillEmpty = getBoolean(element, "fill-empty");
        if (fillEmpty.isPresent()) {
            builder.fillEmpty(fillEmpty.get());
        }

        Optional<String> text = getString(element, "text");
        if (text.isPresent()) {
            builder.text(format(text.get()));
        }

        Optional<List> lore = getList(element, "lore");
        if (lore.isPresent()) {
            builder.lore(format(castList(String.class, lore.get())));
        }

        return builder.build();
    }

    private static GuiButtonContent parseButton(Map<?, ?> element) {
        GuiButtonContent.Builder builder = new GuiButtonContent.Builder();
        Optional<String> text = getString(element, "text");
        if (text.isPresent()) {
            builder.text(format(text.get()));
        }

        Optional<List> lore = getList(element, "lore");
        if (lore.isPresent()) {
            builder.lore(format(castList(String.class, lore.get())));
        }

        Optional<Material> material = getMaterial(element, "material");
        if (material.isPresent()) {
            builder.material(material.get());
        }

        Optional<Integer> data = getInt(element, "data");
        if (data.isPresent()) {
            builder.data(data.get().byteValue());
        }
        return builder.build();
    }
}
