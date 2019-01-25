package com.github.skystardust.ultracore.nukkit.models.vec;

import cn.nukkit.Server;
import cn.nukkit.level.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Vec3D {
    private double x;
    private double y;
    private double z;
    private double yaw;
    private double pitch;
    private String level;

    public Vec3D(String level) {
        this(0, 0, 0, 0, 0, level);
    }

    public Vec3D(double x, double y, double z, String level) {
        this(x, y, z, 0, 0, level);
    }

    public static Vec3D valueOf(Location location) {
        return new Vec3D(location.getX(), location.getY(), location.getZ(), location.getLevel().getName());
    }

    public Location toLocation() {
        return new Location(x, y, z, yaw, pitch, Server.getInstance().getLevelByName(level));
    }
}
