package com.shootbot.raic2018.codeball;

import com.shootbot.raic2018.codeball.model.*;

public class MyRobot extends Entity {
    public double nitro_amount;
    public boolean touch;
    public double touch_normal_x;
    public double touch_normal_y;
    public double touch_normal_z;

    // action:
    public double target_velocity_x;
    public double target_velocity_y;
    public double target_velocity_z;

    public double jump_speed;
    public boolean use_nitro;
    
    public MyRobot(Robot r) {
        this.x = r.x;
        this.y = r.y;
        this.z = r.z;
        
        this.velocity_x = r.velocity_x;
        this.velocity_y = r.velocity_y;
        this.velocity_z = r.velocity_z;
    }
}
