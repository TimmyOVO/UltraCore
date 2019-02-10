package com.github.skystardust.ultracore.bukkit.modules.skin.v1_12_2;

import com.github.skystardust.ultracore.bukkit.UltraCore;
import com.github.skystardust.ultracore.core.modules.AbstractModule;
import com.google.common.collect.Sets;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;

public class SkinManager extends AbstractModule {

    public void setPlayerSkin(Player player, String content, String sign) {
        EntityPlayer playerHandle = ((CraftPlayer) player).getHandle();
        PropertyMap properties = playerHandle.getProfile().getProperties();
        properties.clear();
        Property value = new Property("textures", content, sign);
        properties.put("textures", value);
    }

    private void sendPacket(Player player, Packet packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    public void updateSkin(Player player) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(UltraCore.getUltraCore(), () -> {
            if (player.isOnline()) {
                EntityPlayer handle = ((CraftPlayer) player).getHandle();
                World world = handle.getWorld();
                PacketPlayOutPlayerInfo removePlayerInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, handle);
                PacketPlayOutEntityDestroy entityDestroy = new PacketPlayOutEntityDestroy(player.getEntityId());
                PacketPlayOutNamedEntitySpawn namedEntitySpawn = new PacketPlayOutNamedEntitySpawn(handle);
                PacketPlayOutPlayerInfo addPlayerInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, handle);
                int dimension;
                dimension = ((WorldServer) world).dimension;
                if (dimension == 8 || dimension == 13) {
                    dimension = 0;
                }

                if (dimension == 1 || dimension == 14) {
                    dimension = -1;
                }

                if (dimension == 16 || dimension == 12) {
                    dimension = 1;
                }
                PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(dimension, world.getDifficulty(), world.getWorldData().getType(), handle.playerInteractManager.getGameMode());
                PacketPlayOutPosition position = new PacketPlayOutPosition(handle.locX, handle.locY, handle.locZ, handle.yaw, handle.pitch, Sets.newHashSet(), 666);
                PacketPlayOutEntityEquipment mainHand = new PacketPlayOutEntityEquipment(handle.getId(), EnumItemSlot.MAINHAND, handle.getItemInMainHand());
                PacketPlayOutEntityEquipment offHand = new PacketPlayOutEntityEquipment(handle.getId(), EnumItemSlot.OFFHAND, handle.getItemInOffHand());
                PacketPlayOutEntityEquipment head = new PacketPlayOutEntityEquipment(handle.getId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(player.getInventory().getHelmet()));
                PacketPlayOutEntityEquipment chest = new PacketPlayOutEntityEquipment(handle.getId(), EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(player.getInventory().getChestplate()));
                PacketPlayOutEntityEquipment legg = new PacketPlayOutEntityEquipment(handle.getId(), EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(player.getInventory().getLeggings()));
                PacketPlayOutEntityEquipment boot = new PacketPlayOutEntityEquipment(handle.getId(), EnumItemSlot.FEET, CraftItemStack.asNMSCopy(player.getInventory().getBoots()));
                PacketPlayOutHeldItemSlot heldItemSlot = new PacketPlayOutHeldItemSlot(player.getInventory().getHeldItemSlot());
                Bukkit.getOnlinePlayers().forEach(key -> {
                    if (key.getUniqueId().equals(handle.getUniqueID())) {
                        sendPacket(key, removePlayerInfo);
                        sendPacket(key, addPlayerInfo);
                        sendPacket(key, respawn);
                        handle.updateAbilities();
                        sendPacket(key, position);
                        sendPacket(key, heldItemSlot);
                        ((CraftPlayer) key).updateScaledHealth();
                        key.updateInventory();
                        handle.triggerHealthUpdate();
                        if (key.isOp()) {
                            key.setOp(false);
                            key.setOp(true);
                        }
                    } else if (key.isOnline() && key.getWorld().getName().equals(player.getWorld().getName()) && key.canSee(player)) {
                        sendPacket(key, entityDestroy);
                        sendPacket(key, removePlayerInfo);
                        sendPacket(key, addPlayerInfo);
                        sendPacket(key, namedEntitySpawn);
                        sendPacket(key, mainHand);
                        sendPacket(key, offHand);
                        sendPacket(key, head);
                        sendPacket(key, chest);
                        sendPacket(key, legg);
                        sendPacket(key, boot);
                    } else {
                        sendPacket(key, removePlayerInfo);
                        sendPacket(key, addPlayerInfo);
                    }
                });
            }
        });
    }

    @Override
    public String getModuleName() {
        return "SkinManager";
    }
}
