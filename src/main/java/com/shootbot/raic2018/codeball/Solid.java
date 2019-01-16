package com.shootbot.raic2018.codeball;

public class Solid {
    Vec3d pos;
    Vec3d speed;
    
    public Solid() {
        pos = new Vec3d();
        speed = new Vec3d();
    }
    
    public Solid(Vec3d pos, Vec3d speed) {
        this.pos = pos;
        this.speed = speed;
    }

    @Override
    public String toString() {
        return String.format("{(%.2f %.2f %.2f)->(%.2f %.2f %.2f)}", pos.x, pos.y, pos.z, speed.x, speed.y, speed.z);
    }
}
