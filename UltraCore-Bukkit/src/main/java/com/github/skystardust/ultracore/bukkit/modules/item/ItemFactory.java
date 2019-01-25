package com.github.skystardust.ultracore.bukkit.modules.item;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ItemFactory {
    private ItemStack itemStack;
    private ItemMeta itemMetal;

    public ItemFactory(Supplier<ItemStack> itemStackSupplier) {
        this.itemStack = itemStackSupplier.get();
        if (!itemStack.hasItemMeta()) {
            itemStack.setItemMeta(Bukkit.getItemFactory().getItemMeta(itemStack.getType()));
        }
        this.itemMetal = itemStack.getItemMeta();
    }

    public ItemFactory setDisplayName(String name) {
        itemMetal.setDisplayName(name);
        return this;
    }

    public ItemFactory setLore(List<String> lore) {
        itemMetal.setLore(lore);
        return this;
    }

    public ItemFactory addLore(String... addString) {
        if (!itemMetal.hasLore()) {
            itemMetal.setLore(new ArrayList<>());
        }
        List<String> oldLore = itemMetal.getLore();
        for (String text : addString) {
            oldLore.add(text);
        }
        itemMetal.setLore(oldLore);
        return this;
    }

    public ItemStack pack() {
        itemStack.setItemMeta(itemMetal);
        return itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ItemFactory setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        return this;
    }

    public ItemMeta getItemMeta() {
        return itemMetal;
    }

    public ItemFactory setItemMeta(ItemMeta itemMetal) {
        this.itemMetal = itemMetal;
        return this;
    }
}
