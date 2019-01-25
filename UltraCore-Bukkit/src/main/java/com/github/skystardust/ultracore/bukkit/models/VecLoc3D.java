package com.github.skystardust.ultracore.bukkit.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;

@Data
@AllArgsConstructor
@Builder
public class VecLoc3D {
    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public VecLoc3D(String world) {
        this(world, 0, 0, 0);
    }

    public VecLoc3D(String world, double x, double y, double z) {
        this(world, 0, 0, 0, 0, 0);
    }

    public static VecLoc3D valueOf(Location location) {
        return new VecLoc3D(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public Location toBukkitLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }
}
