import java.util.*;

public class Sim {
    private Random rng = new Random();
    
    private final double ROBOT_MIN_RADIUS = 1;
    private final double ROBOT_MAX_RADIUS = 1.05;
    private final double ROBOT_MAX_JUMP_SPEED = 15;
    private final double ROBOT_ACCELERATION = 100;
    private final double ROBOT_NITRO_ACCELERATION = 30;
    private final double ROBOT_MAX_GROUND_SPEED = 30;
    private final double ROBOT_ARENA_E = 0;
    private final double ROBOT_RADIUS = 1;
    private final double ROBOT_MASS = 2;
    private final double TICKS_PER_SECOND = 60;
    private final double MICROTICKS_PER_TICK = 100;
    private final double RESET_TICKS = 2 * TICKS_PER_SECOND;
    private final double BALL_ARENA_E = 0.7;
    private final double BALL_RADIUS = 2;
    private final double BALL_MASS = 1;
    private final double MIN_HIT_E = 0.4;
    private final double MAX_HIT_E = 0.5;
    private final double MAX_ENTITY_SPEED = 100;
    private final double MAX_NITRO_AMOUNT = 100;
    private final double START_NITRO_AMOUNT = 50;
    private final double NITRO_POINT_VELOCITY_CHANGE = 0.6;
    private final double NITRO_PACK_X = 20;
    private final double NITRO_PACK_Y = 1;
    private final double NITRO_PACK_Z = 30;
    private final double NITRO_PACK_RADIUS = 0.5;
    private final double NITRO_PACK_AMOUNT = 100;
    private final double NITRO_PACK_RESPAWN_TICKS = 10 * TICKS_PER_SECOND;
    private final double GRAVITY = 30;
    
    void collideEntities(Entity a, Entity b) {
        double distance = dist(a, b);
        double penetration = a.radius + b.radius - distance;
        if (penetration > 0) {
            double k_a = (1 / a.mass) / ((1 / a.mass) + (1 / b.mass)); // change del to mul?
            double k_b = (1 / b.mass) / ((1 / a.mass) + (1 / b.mass)); // replace repeating parts here and after
            
            double dir_x = b.x - a.x; // normal
            double dir_y = b.y - a.y;
            double dir_z = b.z - a.z;
            
            a.x -= dir_x * penetration * k_a;
            a.y -= dir_y * penetration * k_a;
            a.z -= dir_z * penetration * k_a;
            
            b.x += dir_x * penetration * k_b;
            b.y += dir_y * penetration * k_b;
            b.z += dir_z * penetration * k_b;
            
            double delta_velocity = (b.velocity_x - a.velocity_x) * dir_x
                    + (b.velocity_y - a.velocity_y) * dir_y
                    + (b.velocity_z - a.velocity_z) * dir_z
                    - b.radius_change_speed - a.radius_change_speed;
            
            if (delta_velocity < 0) {
                // bug! use only single getRandom
                double impulse_x = (1 + getRandom(MIN_HIT_E, MAX_HIT_E)) * delta_velocity * dir_x;
                double impulse_y = (1 + getRandom(MIN_HIT_E, MAX_HIT_E)) * delta_velocity * dir_y;
                double impulse_z = (1 + getRandom(MIN_HIT_E, MAX_HIT_E)) * delta_velocity * dir_z;
                
                a.velocity_x += impulse_x * k_a;
                a.velocity_y += impulse_y * k_a;
                a.velocity_z += impulse_z * k_a;
                
                b.velocity_x -= impulse_x * k_b;
                b.velocity_y -= impulse_y * k_b;
                b.velocity_z -= impulse_z * k_b;
            }
        }
    }
    
    void move(Entity e, float delta_time) {
        clampSpeed(e);
        
        e.x += e.velocity_x * delta_time;
        e.y += e.velocity_y * delta_time;
        e.z += e.velocity_z * delta_time;
        
        e.y -= GRAVITY * delta_time * delta_time / 2;
        e.velocity_y -= GRAVITY * delta_time;
    }
    
    
    
    ////////////////////////////// utility funcs
    private void clampSpeed(Entity e) {
        double vel = len(e.velocity_x, e.velocity_y, e.velocity_z);
        if (vel > MAX_ENTITY_SPEED) {
            e.velocity_x *= MAX_ENTITY_SPEED / vel;
            e.velocity_y *= MAX_ENTITY_SPEED / vel;
            e.velocity_z *= MAX_ENTITY_SPEED / vel;
        }
    
    }
    
    double len(double x, double y, double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }
    
    double dist(Entity a, Entity b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        double dz = a.z - b.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz); // replace with fast sqrt?
    }
    
    double getRandom(double min, double max) {
        return rng.nextDouble() * (max - min) + min;
    }
    
}
