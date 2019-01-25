package com.github.skystardust.ultracore.bukkit.models;

import com.google.gson.internal.LinkedTreeMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
public class InventoryItem {
    private Map<String, Object> itemstackData;

    public ItemStack toItemStack() {
        org.bukkit.Material type = org.bukkit.Material.getMaterial((String) itemstackData.get("type"));
        short damage = 0;
        int amount = 1;
        if (itemstackData.containsKey("damage")) {
            damage = ((Number) itemstackData.get("damage")).shortValue();
        }

        if (itemstackData.containsKey("amount")) {
            amount = ((Number) itemstackData.get("amount")).intValue();
        }

        ItemStack result = new ItemStack(type, amount, damage);
        Object raw;
        if (itemstackData.containsKey("meta")) {

            raw = itemstackData.get("meta");
            if (raw instanceof ItemMeta) {
                result.setItemMeta((ItemMeta) raw);
                return result;
            }
            ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(type);
            LinkedTreeMap linkedTreeMap = (LinkedTreeMap) raw;
            if (linkedTreeMap.containsKey("displayName")) {
                itemMeta.setDisplayName((String) linkedTreeMap.get("displayName"));
            }
            if (linkedTreeMap.containsKey("lore")) {
                itemMeta.setLore(((List<String>) ((LinkedTreeMap) raw).get("lore")));
            }
            if (linkedTreeMap.containsKey("enchantments")) {
                LinkedTreeMap<String, Integer> enchantments = (LinkedTreeMap<String, Integer>) linkedTreeMap.get("enchantments");
                if (enchantments != null) {

                    for (Object o : enchantments.entrySet()) {
                        Map.Entry<?, ?> entry = (Map.Entry) o;
                        String[] split = ((String) entry.getKey()).split(",");
                        String replace = split[1].replace(" ", "").replace("]", "");
                        Enchantment enchantment = Enchantment.getByName(replace);
                        if (enchantment != null) {
                            int i = ((Double) entry.getValue()).intValue();
                            itemMeta.addEnchant(enchantment, i, false);
                        }
                    }
                }
            }
            result.setItemMeta(itemMeta);
        }

        return result;
    }
}
