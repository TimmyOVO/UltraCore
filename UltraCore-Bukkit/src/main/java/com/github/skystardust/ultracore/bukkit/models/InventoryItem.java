package com.github.skystardust.ultracore.bukkit.models;

import lombok.Builder;
import lombok.Data;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class InventoryItem {
    private Map<String, Object> itemstackData;

    public InventoryItem(Map<String, Object> itemstackData) {
        this.itemstackData = itemstackData;
        Object meta = itemstackData.get("meta");
        if (meta instanceof PotionMeta) {
            Map<String, Object> metaMap = new HashMap<>();
            if (((PotionMeta) meta).hasCustomEffects()) {
                Map<String, Object> potionMap = new HashMap<>();
                for (PotionEffect customEffect : ((PotionMeta) meta).getCustomEffects()) {
                    potionMap.put(customEffect.getType().getName(), customEffect.getAmplifier() + "," + customEffect.getDuration());
                }
                metaMap.put("potions", potionMap);
            }
            PotionData basePotionData = ((PotionMeta) meta).getBasePotionData();
            if (basePotionData != null) {
                itemstackData.put("damage", basePotionData.getType().getDamageValue());
            }
            if (((PotionMeta) meta).hasDisplayName()) {
                metaMap.put("displayName", ((PotionMeta) meta).getDisplayName());
            }
            if (((PotionMeta) meta).hasLore()) {
                metaMap.put("lore", ((PotionMeta) meta).getLore());
            }
            Color color = ((PotionMeta) meta).getColor();
            if (color != null) {
                metaMap.put("color", color.getRed() + "," + color.getGreen() + "," + color.getBlue());
            }
            itemstackData.put("meta", metaMap);

        }
    }

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
            Map rawMap = (Map) raw;
            if (rawMap.containsKey("displayName")) {
                itemMeta.setDisplayName((String) rawMap.get("displayName"));
            }
            if (rawMap.containsKey("lore")) {
                itemMeta.setLore(((List<String>) ((Map) raw).get("lore")));
            }
            if (rawMap.containsKey("enchantments")) {
                Map<String, Integer> enchantments = (Map<String, Integer>) rawMap.get("enchantments");
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
            if (rawMap.containsKey("unbreakable")) {
                itemMeta.setUnbreakable(((boolean) rawMap.get("unbreakable")));
            }
            if (type == Material.SKULL_ITEM && damage == 3 && rawMap.containsKey("profile")) {
                Map<String, Object> profile = (Map<String, Object>) rawMap.get("profile");
                ((SkullMeta) itemMeta).setOwningPlayer(new OfflinePlayer() {
                    @Override
                    public boolean isOnline() {
                        return false;
                    }

                    @Override
                    public String getName() {
                        return ((String) profile.get("name"));
                    }

                    @Override
                    public UUID getUniqueId() {
                        return UUID.fromString(((String) profile.get("id")));
                    }

                    @Override
                    public boolean isBanned() {
                        return false;
                    }

                    @Override
                    public boolean isWhitelisted() {
                        return false;
                    }

                    @Override
                    public void setWhitelisted(boolean b) {

                    }

                    @Override
                    public Player getPlayer() {
                        return null;
                    }

                    @Override
                    public long getFirstPlayed() {
                        return 0;
                    }

                    @Override
                    public long getLastPlayed() {
                        return 0;
                    }

                    @Override
                    public boolean hasPlayedBefore() {
                        return false;
                    }

                    @Override
                    public Location getBedSpawnLocation() {
                        return null;
                    }

                    @Override
                    public Map<String, Object> serialize() {
                        return null;
                    }

                    @Override
                    public boolean isOp() {
                        return false;
                    }

                    @Override
                    public void setOp(boolean b) {

                    }
                });
            }

            if (rawMap.containsKey("potions")) {
                Map<String, String> potion = (Map<String, String>) rawMap.get("potions");
                PotionMeta potionMeta = (PotionMeta) itemMeta;
                if (rawMap.containsKey("color")) {
                    String[] color = ((String) rawMap.get("color")).split(",");
                    potionMeta.setColor(Color.fromRGB(Integer.valueOf(color[0]), Integer.valueOf(color[1]), Integer.valueOf(color[2])));
                }

                potion.forEach((effect, data) -> {
                    String[] split = data.split(",");
                    potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.getByName(effect), Integer.valueOf(split[0]), Integer.valueOf(split[1])), true);
                });
            }

            result.setItemMeta(itemMeta);
        }

        return result;
    }
}
