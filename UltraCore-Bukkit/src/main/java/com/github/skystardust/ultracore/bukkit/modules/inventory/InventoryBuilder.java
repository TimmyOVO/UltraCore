package com.github.skystardust.ultracore.bukkit.modules.inventory;

import com.github.skystardust.ultracore.bukkit.UltraCore;
import javafx.util.Callback;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryBuilder {
    private String displayName;
    private int size;
    private Map<Integer, ItemStack> itemMap;
    private Callback<Player, HashMap<Integer, ItemStack>> updateMenu;

    public InventoryBuilder addItem(ItemStack itemStack) {
        addItem(itemStack, findEmptySlot());
        return this;
    }

    public InventoryBuilder addItem(ItemStack itemStack, int i) {
        itemMap.put(i, itemStack);
        return this;
    }

    public InventoryBuilder lock() {
        Validate.notNull(displayName);
        InventoryFactory.lockInventory(displayName);
        return this;
    }

    public InventoryBuilder fillWith(ItemStack itemStack) {
        for (int i = 0; i <= size - 1; i++) {
            ItemStack itemStack1 = itemMap.get(i);
            if (itemStack1 == null) {
                addItem(itemStack, i);
            }
        }
        return this;
    }

    public InventoryBuilder openWith(ItemStack itemStack) {
        Listener listener = new Listener() {
            @EventHandler
            public void onPlayerInteract(PlayerInteractEvent playerInteractEvent) {
                if (playerInteractEvent.getItem() == null) {
                    return;
                }
                if (playerInteractEvent.getAction() != Action.RIGHT_CLICK_AIR && playerInteractEvent.getAction() != Action.RIGHT_CLICK_BLOCK) {
                    return;
                }
                if (playerInteractEvent.getItem().isSimilar(itemStack)) {
                    show(playerInteractEvent.getPlayer());
                }
            }
        };
        Bukkit.getPluginManager().registerEvents(listener, UltraCore.getUltraCore());
        return this;
    }

    public InventoryBuilder onClickListener(onClickListener onClickListener) {
        Listener listener = new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent inventoryClickEvent) {
                Inventory clickedInventory = inventoryClickEvent.getClickedInventory();
                if (clickedInventory == null) {
                    return;
                }
                if (clickedInventory.getTitle() != null) {
                    if (clickedInventory.getTitle().equalsIgnoreCase(displayName)) {
                        onClickListener.onClick(inventoryClickEvent.getRawSlot(), inventoryClickEvent.getCurrentItem(), ((Player) inventoryClickEvent.getWhoClicked()));
                    }
                }
            }
        };
        Bukkit.getPluginManager().registerEvents(listener, UltraCore.getUltraCore());
        return this;
    }

    public InventoryBuilder onClickListenerAdv(onClickListenerAdv onClickListener) {
        Listener listener = new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent inventoryClickEvent) {
                Inventory clickedInventory = inventoryClickEvent.getClickedInventory();
                if (clickedInventory == null) {
                    return;
                }
                if (clickedInventory.getTitle() != null) {
                    if (clickedInventory.getTitle().equalsIgnoreCase(displayName)) {
                        onClickListener.onClick(inventoryClickEvent);
                    }
                }
            }
        };
        Bukkit.getPluginManager().registerEvents(listener, UltraCore.getUltraCore());
        return this;
    }

    public void show(Player player) {
        Validate.notNull(displayName);
        Inventory inventory = Bukkit.createInventory(null, size, displayName);
        if (updateMenu != null) {
            try {
                HashMap<Integer, ItemStack> call = updateMenu.call(player);
                if (itemMap != null) {
                    if (!call.isEmpty()) {
                        itemMap.clear();
                        call.forEach((key, value) -> {
                            itemMap.put(key, value);
                        });
                    }
                } else {
                    itemMap = call;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (itemMap != null) {
            itemMap.forEach(inventory::setItem);
        }
        player.openInventory(inventory);
    }

    public int findEmptySlot() {
        for (int i = 0; i <= size - 1; i++) {
            ItemStack itemStack = itemMap.get(i);
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                return i;
            }
        }
        return -1;
    }

    public interface onClickListener {
        void onClick(int slot, ItemStack itemStack, Player player);
    }

    public interface onClickListenerAdv {
        void onClick(InventoryClickEvent inventoryClickEvent);
    }
}
