package com.lokamc.utils;

import com.destroystokyo.paper.MaterialTags;
import org.bukkit.Material;
import org.bukkit.Tag;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.bukkit.Material.*;

public class MaterialSets {
    public static final Set<Material> crops = new HashSet<>(Arrays.asList(
            CARROT,
            POTATO,
            WHEAT,
            BEETROOT,
            MELON,
            PUMPKIN,
            COCOA,
            CACTUS,
            BAMBOO
    ));

    public static final Set<Material> plants = new HashSet<>(Arrays.asList(SHORT_GRASS,
            DEAD_BUSH,
            FERN,
            DANDELION,
            POPPY,
            BLUE_ORCHID,
            ALLIUM,
            AZURE_BLUET,
            RED_TULIP,
            ORANGE_TULIP,
            WHITE_TULIP,
            PINK_TULIP,
            OXEYE_DAISY,
            SUNFLOWER,
            LILAC,
            TALL_GRASS,
            LARGE_FERN,
            ROSE_BUSH,
            PEONY
    ));

    public static final Set<Material> bypassLOSBlocks = new HashSet<>(Arrays.asList(
            AIR,
            WATER,
            LAVA,
            FIRE,
            TALL_GRASS,
            VINE,
            DIRT_PATH));

    static {
        bypassLOSBlocks.addAll(MaterialTags.PRESSURE_PLATES.getValues());
        bypassLOSBlocks.addAll(Tag.CARPETS.getValues());
        bypassLOSBlocks.addAll(MaterialTags.SIGNS.getValues());
        bypassLOSBlocks.addAll(crops);
        bypassLOSBlocks.addAll(plants);
    }
}
