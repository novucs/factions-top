package net.novucs.ftop.hook.replacer;

import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;

import java.util.function.Function;

public class RankReplacer implements PlaceholderReplacer {

    private final Function<Integer, String> rankReplacer;
    private final int rank;

    public RankReplacer(Function<Integer, String> rankReplacer, int rank) {
        this.rankReplacer = rankReplacer;
        this.rank = rank;
    }

    @Override
    public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
        return rankReplacer.apply(rank);
    }
}
