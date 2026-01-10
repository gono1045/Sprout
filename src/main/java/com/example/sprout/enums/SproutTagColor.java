package com.example.sprout.enums;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public enum SproutTagColor {

    RED("bg-red-400"),
    ORANGE("bg-orange-400"),
    AMBER("bg-amber-400"),
    YELLOW("bg-yellow-400"),
    LIME("bg-lime-400"),
    GREEN("bg-green-400"),
    EMERALD("bg-emerald-400"),
    TEAL("bg-teal-400"),
    CYAN("bg-cyan-400"),
    SKY("bg-sky-400"),
    BLUE("bg-blue-400"),
    PURPLE("bg-purple-400");

    private final String tailwindClass;

    SproutTagColor(String tailwindClass) {
        this.tailwindClass = tailwindClass;
    }

    public String getTailwindClass() {
        return tailwindClass;
    }

    /* =========================
     * ユーティリティ
     * ========================= */

    private static final List<SproutTagColor> VALUES = List.of(values());

    /** ランダムで1色取得 */
    public static SproutTagColor random() {
        return VALUES.get(
            ThreadLocalRandom.current().nextInt(VALUES.size())
        );
    }
}
