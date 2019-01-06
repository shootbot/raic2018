import model.Arena;

import java.util.*;

public class Sim {
    private Random rng = new Random();
    private MyRobot[] robots;
    private MyNitroPack[] nitro_packs;
    private MyBall ball;
    private Arena arena;
    private double distanceToArena;
    private double collision_normal_x;
    private double collision_normal_y;
    private double collision_normal_z;
    private boolean no_collision;

    private final double ROBOT_MIN_RADIUS = 1;
    private final double ROBOT_MAX_RADIUS = 1.05;
    private final double ROBOT_MAX_JUMP_SPEED = 15;
    private final double ROBOT_ACCELERATION = 100;
    private final double ROBOT_NITRO_ACCELERATION = 30;
    private final double ROBOT_MAX_GROUND_SPEED = 30;
    private final double ROBOT_ARENA_E = 0;
    private final double ROBOT_RADIUS = 1;
    private final double ROBOT_MASS = 2;
    private final int TICKS_PER_SECOND = 60;
    private final int MICROTICKS_PER_TICK = 100;
    private final int RESET_TICKS = 2 * TICKS_PER_SECOND;
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
    private final int NITRO_PACK_RESPAWN_TICKS = 10 * TICKS_PER_SECOND;

    private final double GRAVITY = 30;
    private final double DELTA_TIME = 1.0 / TICKS_PER_SECOND;

    public void setRobots(MyRobot[] robots) {
        this.robots = robots;
    }

    public void setNitro_packs(MyNitroPack[] nitro_packs) {
        this.nitro_packs = nitro_packs;
    }

    public void setBall(MyBall ball) {
        this.ball = ball;
    }

    void collide_entities(Entity a, Entity b) {
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
                // bug! use only single getUniformRandom
                double impulse_x = (1 + getUniformRandom(MIN_HIT_E, MAX_HIT_E)) * delta_velocity * dir_x;
                double impulse_y = (1 + getUniformRandom(MIN_HIT_E, MAX_HIT_E)) * delta_velocity * dir_y;
                double impulse_z = (1 + getUniformRandom(MIN_HIT_E, MAX_HIT_E)) * delta_velocity * dir_z;

                a.velocity_x += impulse_x * k_a;
                a.velocity_y += impulse_y * k_a;
                a.velocity_z += impulse_z * k_a;

                b.velocity_x -= impulse_x * k_b;
                b.velocity_y -= impulse_y * k_b;
                b.velocity_z -= impulse_z * k_b;
            }
        }
    }

    void collide_with_arena(Entity e) {
//        let distance, normal = dan_to_arena(e.position)
        dan_to_arena(e);
        double penetration = e.radius - distanceToArena;
        if (penetration > 0) {
            e.x += penetration * collision_normal_x;
            e.y += penetration * collision_normal_y;
            e.z += penetration * collision_normal_z;

            double velocity =
                    e.velocity_x * collision_normal_x
                            + e.velocity_y * collision_normal_y
                            + e.velocity_z * collision_normal_z
                            - e.radius_change_speed;
            if (velocity < 0) {
                e.velocity_x -= (1 + e.ARENA_E) * velocity * collision_normal_x;
                e.velocity_y -= (1 + e.ARENA_E) * velocity * collision_normal_y;
                e.velocity_z -= (1 + e.ARENA_E) * velocity * collision_normal_z;
              no_collision = false;
            }
        }
        no_collision = true;
    }

    private void dan_to_arena(Entity e) {
        distanceToArena = 0;
        collision_normal_x = 0;
        collision_normal_y = 0;
        collision_normal_z = 0;
    }

    void move(Entity e) {
        clampSpeed(e); // ?

        e.x += e.velocity_x * DELTA_TIME;
        e.y += e.velocity_y * DELTA_TIME;
        e.z += e.velocity_z * DELTA_TIME;

        e.y -= GRAVITY * DELTA_TIME * DELTA_TIME / 2;
        e.velocity_y -= GRAVITY * DELTA_TIME;
    }

    void tick() {
        for (int i = 0; i < MICROTICKS_PER_TICK; i++) {
            update((float) (DELTA_TIME / MICROTICKS_PER_TICK));
        }

        for (MyNitroPack pack : nitro_packs) {
            if (pack.alive) continue;

            pack.respawn_ticks -= 1;
            if (pack.respawn_ticks == 0) {
                pack.alive = true;
            }
        }
    }

    void update(float delta_time) {
        shuffle(robots);

        for (MyRobot robot : robots) {
            if (robot.touch) { // double check this wtf
                clampSpeed(robot); // !bug robot.target_velocity, ROBOT_MAX_GROUND_SPEED
                double dot = robot.touch_normal_x * robot.target_velocity_x
                        + robot.touch_normal_y * robot.target_velocity_y
                        + robot.touch_normal_z * robot.target_velocity_z;
                robot.target_velocity_x -= robot.touch_normal_x * dot;
                robot.target_velocity_y -= robot.touch_normal_y * dot;
                robot.target_velocity_z -= robot.touch_normal_z * dot;
                double target_velocity = len(robot.target_velocity_x, robot.target_velocity_y, robot.target_velocity_z);

                double target_velocity_change_x = robot.target_velocity_x - robot.velocity_x;
                double target_velocity_change_y = robot.target_velocity_y - robot.velocity_y;
                double target_velocity_change_z = robot.target_velocity_z - robot.velocity_z;
                double target_velocity_change = target_velocity - len(robot.velocity_x, robot.velocity_y, robot.velocity_z);
                if (target_velocity_change > 0) {
                    double acceleration = ROBOT_ACCELERATION * Math.max(0, robot.touch_normal_y);

                    robot.velocity_x += Math.min(
                            target_velocity_change_x / target_velocity_change * acceleration * delta_time,
                            target_velocity_change);
                    robot.velocity_y += Math.min(
                            target_velocity_change_y / target_velocity_change * acceleration * delta_time,
                            target_velocity_change);
                    robot.velocity_z += Math.min(
                            target_velocity_change_z / target_velocity_change * acceleration * delta_time,
                            target_velocity_change);
                }
            }

            if (robot.use_nitro) {
                double target_velocity_change_x = Math.min(
                        robot.target_velocity_x - robot.velocity_x,
                        robot.nitro_amount * NITRO_POINT_VELOCITY_CHANGE);
                double target_velocity_change_y = Math.min(
                        robot.target_velocity_y - robot.velocity_y,
                        robot.nitro_amount * NITRO_POINT_VELOCITY_CHANGE);
                double target_velocity_change_z = Math.min(
                        robot.target_velocity_z - robot.velocity_z,
                        robot.nitro_amount * NITRO_POINT_VELOCITY_CHANGE);
                double lenChange = len(target_velocity_change_x, target_velocity_change_y, target_velocity_change_z);
                if (lenChange > 0) {
                    double acceleration_x = target_velocity_change_x / lenChange * ROBOT_NITRO_ACCELERATION;
                    double acceleration_y = target_velocity_change_x / lenChange * ROBOT_NITRO_ACCELERATION;
                    double acceleration_z = target_velocity_change_x / lenChange * ROBOT_NITRO_ACCELERATION;
                    double velocity_change_x = Math.min(acceleration_x * delta_time, lenChange);
                    double velocity_change_y = Math.min(acceleration_y * delta_time, lenChange);
                    double velocity_change_z = Math.min(acceleration_z * delta_time, lenChange);
                    robot.velocity_x += velocity_change_x;
                    robot.velocity_y += velocity_change_y;
                    robot.velocity_z += velocity_change_z;
                    robot.nitro_amount -= len(velocity_change_x, velocity_change_y, velocity_change_z)
                            / NITRO_POINT_VELOCITY_CHANGE;
                }
            }

            move(robot);

            robot.radius = ROBOT_MIN_RADIUS + (ROBOT_MAX_RADIUS - ROBOT_MIN_RADIUS)
                    * robot.jump_speed / ROBOT_MAX_JUMP_SPEED;
            robot.radius_change_speed = robot.jump_speed;
        }

        move(ball);

        for (int i = 0; i < robots.length; i++) {
            for (int j = 0; j < i; j++) {
                collide_entities(robots[i], robots[j]);
            }
        }

        for (MyRobot robot : robots) {
            collide_entities(robot, ball);
            collide_with_arena(robot);
            if (no_collision) {
                robot.touch = false;
            } else {
                robot.touch = true;
                robot.touch_normal_x = collision_normal_x;
                robot.touch_normal_y = collision_normal_y;
                robot.touch_normal_z = collision_normal_z;
            }
        }
        collide_with_arena(ball);
        if (Math.abs(ball.z) > arena.depth / 2 + ball.radius) {
            goal_scored();
        }

        for (MyRobot robot : robots) {
            if (robot.nitro_amount == MAX_NITRO_AMOUNT) continue;
            for (MyNitroPack pack : nitro_packs) {
                if (!pack.alive) continue;
                if (dist(robot, pack) <= robot.radius + pack.radius) {
                    robot.nitro_amount = MAX_NITRO_AMOUNT;
                    pack.alive = false;
                    pack.respawn_ticks = NITRO_PACK_RESPAWN_TICKS;
                }
            }
        }
    }

    private void goal_scored() {
        //
    }

    private void shuffle(MyRobot[] robots) {
        // make shuffle!
    }

    ////////////////////////////// utility funcs
    private double clampSpeed(Entity e) {
        double vel = len(e.velocity_x, e.velocity_y, e.velocity_z);

        if (vel < e.MAX_SPEED) return vel;

        e.velocity_x *= e.MAX_SPEED / vel;
        e.velocity_y *= e.MAX_SPEED / vel;
        e.velocity_z *= e.MAX_SPEED / vel;
        return e.MAX_SPEED;
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

    double getUniformRandom(double min, double max) {
        return rng.nextDouble() * (max - min) + min;
    }

}
