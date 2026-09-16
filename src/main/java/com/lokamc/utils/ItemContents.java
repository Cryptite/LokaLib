package com.lokamc.utils;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BundleContents;
import io.papermc.paper.datacomponent.item.ItemContainerContents;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * The one way to reach into items that carry other items in their own data: bundles ({@code BUNDLE_CONTENTS}) and
 * shulker boxes in item form, or anything else with a {@code CONTAINER} component. Those contents never pass through a
 * block inventory, so any per-item rule (tags, unstable/soulbound flags, sell/store eligibility) that only walks
 * inventory slots silently misses them. Every method here walks to any depth, so a bundle inside a shulker is covered.
 *
 * <p>Paper hands out <em>copies</em> of nested stacks, so mutating one never writes through on its own. The editing
 * methods take care of that: they rebuild and write back the component whenever an item actually changed, and leave
 * the container untouched otherwise. Container slot positions are preserved through edits and removals.
 */
@SuppressWarnings("UnstableApiUsage")
public final class ItemContents {
    private ItemContents() {
    }

    /**
     * @return copies of the items directly inside {@code item}, empties omitted; an empty list for a non-container.
     */
    public static List<ItemStack> of(ItemStack item) {
        List<ItemStack> contents = new ArrayList<>();
        if (item == null) return contents;

        BundleContents bundle = item.getData(DataComponentTypes.BUNDLE_CONTENTS);
        if (bundle != null) {
            addNonEmpty(contents, bundle.contents());
        }
        ItemContainerContents container = item.getData(DataComponentTypes.CONTAINER);
        if (container != null) {
            addNonEmpty(contents, container.contents());
        }
        return contents;
    }

    public static boolean hasContents(ItemStack item) {
        if (item == null) return false;

        BundleContents bundle = item.getData(DataComponentTypes.BUNDLE_CONTENTS);
        if (bundle != null && anyNonEmpty(bundle.contents())) return true;

        ItemContainerContents container = item.getData(DataComponentTypes.CONTAINER);
        return container != null && anyNonEmpty(container.contents());
    }

    /**
     * Visits every item nested inside {@code item} (not {@code item} itself), any depth. The visited stacks are copies;
     * use {@link #editContents} or {@link #mutateDeep} to change them.
     */
    public static void forEachNested(ItemStack item, Consumer<ItemStack> visitor) {
        for (ItemStack nested : of(item)) {
            visitor.accept(nested);
            forEachNested(nested, visitor);
        }
    }

    /**
     * @return whether any item nested inside {@code item} (not {@code item} itself), at any depth, matches.
     */
    public static boolean anyNested(ItemStack item, Predicate<ItemStack> predicate) {
        for (ItemStack nested : of(item)) {
            if (predicate.test(nested) || anyNested(nested, predicate)) return true;
        }
        return false;
    }

    /**
     * @return whether {@code item} itself, or anything nested inside it at any depth, matches.
     */
    public static boolean anyDeep(ItemStack item, Predicate<ItemStack> predicate) {
        return item != null && (predicate.test(item) || anyNested(item, predicate));
    }

    /**
     * Applies {@code edit} to every item nested inside {@code container} (not the container itself), innermost first.
     * {@code edit} receives a copy it may mutate in place, and returns the stack to keep: that same copy, a
     * replacement, or {@code null}/empty to remove it. Components are only written back when something changed.
     *
     * @return whether anything nested was changed, replaced, or removed.
     */
    public static boolean editContents(ItemStack container, UnaryOperator<ItemStack> edit) {
        if (container == null) return false;

        boolean changed = false;
        BundleContents bundle = container.getData(DataComponentTypes.BUNDLE_CONTENTS);
        if (bundle != null) {
            List<ItemStack> contents = new ArrayList<>(bundle.contents());
            if (editAll(contents, edit)) {
                contents.removeIf(ItemStack::isEmpty);
                container.setData(DataComponentTypes.BUNDLE_CONTENTS, BundleContents.bundleContents(contents));
                changed = true;
            }
        }
        ItemContainerContents blockContainer = container.getData(DataComponentTypes.CONTAINER);
        if (blockContainer != null) {
            List<ItemStack> contents = new ArrayList<>(blockContainer.contents());
            if (editAll(contents, edit)) {
                container.setData(DataComponentTypes.CONTAINER, ItemContainerContents.containerContents(contents));
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Mutates {@code item} and everything nested inside it in place, any depth, writing nested changes back.
     */
    public static void mutateDeep(ItemStack item, Consumer<ItemStack> mutator) {
        if (item == null) return;

        mutator.accept(item);
        editContents(item, nested -> {
            mutator.accept(nested);
            return nested;
        });
    }

    /**
     * Edits a raw component list in place, recursing into each entry before editing it. Empty slots are kept as
     * empties so container positions survive. Change detection is by value against a snapshot, so in-place mutation,
     * replacement, and removal are all caught.
     */
    private static boolean editAll(List<ItemStack> contents, UnaryOperator<ItemStack> edit) {
        boolean changed = false;
        for (int i = 0; i < contents.size(); i++) {
            ItemStack item = contents.get(i);
            if (item == null || item.isEmpty()) continue;

            ItemStack before = item.clone();
            editContents(item, edit);
            ItemStack after = edit.apply(item);
            if (after == null || after.isEmpty()) {
                contents.set(i, ItemStack.empty());
                changed = true;
            } else if (!after.equals(before)) {
                contents.set(i, after);
                changed = true;
            }
        }
        return changed;
    }

    private static boolean anyNonEmpty(List<ItemStack> items) {
        for (ItemStack item : items) {
            if (item != null && !item.isEmpty()) return true;
        }
        return false;
    }

    private static void addNonEmpty(List<ItemStack> into, List<ItemStack> from) {
        for (ItemStack item : from) {
            if (item != null && !item.isEmpty()) {
                into.add(item);
            }
        }
    }
}
