package net.novucs.ftop.hook.replacer;

import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;

import java.util.function.Supplier;

public class LastReplacer implements PlaceholderReplacer {

    private final Supplier<String> lastReplacer;

    public LastReplacer(Supplier<String> lastReplacer) {
        this.lastReplacer = lastReplacer;
    }

    @Override
    public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
        return lastReplacer.get();
    }
}
