public class Sim {

    public static final int MICROTICKS_PER_TICK = 100; // CHANGED FROM 100
    public static final double ROBOT_MIN_RADIUS = 1;
    public static final double ROBOT_MAX_RADIUS = 1.05;
    public static final double ROBOT_MAX_JUMP_SPEED = 15;
    public static final double ROBOT_ACCELERATION = 100;
    public static final double ROBOT_NITRO_ACCELERATION = 30;
    public static final double ROBOT_MAX_GROUND_SPEED = 30;
    public static final double ROBOT_ARENA_E = 0;
    public static final double ROBOT_RADIUS = 1;
    public static final double ROBOT_MASS = 2;
    public static final int TICKS_PER_SECOND = 60;
    public static final int RESET_TICKS = 2 * TICKS_PER_SECOND;
    public static final double BALL_ARENA_E = 0.7;
    public static final double BALL_RADIUS = 2;
    public static final double BALL_MASS = 1;
    public static final double MIN_HIT_E = 0.4;
    public static final double MAX_HIT_E = 0.5;
    public static final double MAX_ENTITY_SPEED = 100;

    public static final double MAX_NITRO_AMOUNT = 100;
    public static final double START_NITRO_AMOUNT = 50;
    public static final double NITRO_POINT_VELOCITY_CHANGE = 0.6;
    public static final double NITRO_PACK_X = 20;
    public static final double NITRO_PACK_Y = 1;
    public static final double NITRO_PACK_Z = 30;
    public static final double NITRO_PACK_RADIUS = 0.5;
    public static final double NITRO_PACK_AMOUNT = 100;
    public static final int NITRO_PACK_RESPAWN_TICKS = 10 * TICKS_PER_SECOND;

    public static final double GRAVITY = 30;
    public static final double DELTA_TIME = 1.0 / TICKS_PER_SECOND;

    public static final double ARENA_WIDTH = 60;
    public static final double ARENA_HEIGHT = 20;
    public static final double ARENA_DEPTH = 80;
    public static final double ARENA_BOTTOM_RADIUS = 3;
    public static final double ARENA_TOP_RADIUS = 7;
    public static final double ARENA_CORNER_RADIUS = 13;

    public static final double GOAL_TOP_RADIUS = 3;
    public static final double GOAL_WIDTH = 30;
    public static final double GOAL_DEPTH = 10;
    public static final double GOAL_HEIGHT = 10;
    public static final double GOAL_SIDE_RADIUS = 1;

    private static final double MAX_ENTITY_SPEED_SQUARED = MAX_ENTITY_SPEED * MAX_ENTITY_SPEED;
    private static final double ROBOT_MAX_GROUND_SPEED_SQUARED = ROBOT_MAX_GROUND_SPEED * ROBOT_MAX_GROUND_SPEED;
    private static final double HIT_COEF = 1 + (MIN_HIT_E + MAX_HIT_E) / 2;
    private static final Vec3d GROUND_PLANE = new Vec3d(0, 0, 0);
    private static final Vec3d GROUND_NORMAL = new Vec3d(0, 1, 0);
    private static final Vec3d CEILING_PLANE = new Vec3d(0, ARENA_HEIGHT, 0);
    private static final Vec3d CEILING_NORMAL = new Vec3d(0, -1, 0);
    private static final Vec3d SIDE_X_PLANE = new Vec3d(ARENA_WIDTH / 2, 0, 0);
    private static final Vec3d SIDE_X_NORMAL = new Vec3d(-1, 0, 0);
    private static final Vec3d GOAL_Z_PLANE = new Vec3d(0, 0, (ARENA_DEPTH / 2) + GOAL_DEPTH);
    private static final Vec3d GOAL_Z_NORMAL = new Vec3d(0, 0, -1);
    private static final Vec3d SIDE_Z_PLANE = new Vec3d(0, 0, ARENA_DEPTH / 2);
    private static final Vec3d SIDE_Z_NORMAL = new Vec3d(0, 0, -1);
    private static final Vec3d GOAL_X_PLANE = new Vec3d(GOAL_WIDTH / 2, 0, 0);
    private static final Vec3d GOAL_X_NORMAL = new Vec3d(-1, 0, 0);
    private static final Vec3d GOAL_Y_PLANE = new Vec3d(0, GOAL_HEIGHT, 0);
    private static final Vec3d GOAL_Y_NORMAL = new Vec3d(0, -1, 0);

    //////////////////////////////////////////////////////////////////////////////////
    // output variables
    private boolean collision;

    // for clamp_speed(action.target_velocity)
    private double target_velocity_x;
    private double target_velocity_y;
    private double target_velocity_z;

    private Dan danA = new Dan(0, new Vec3d());
    private Dan danB = new Dan(0, new Vec3d());
    private Vec3d vecP = new Vec3d();
    private Vec3d in = new Vec3d();
    private Vec3d vecT = new Vec3d();
    private Vec3d velChange = new Vec3d();

    private Vec2d vecU = new Vec2d();
    private Vec2d vecV = new Vec2d();
    //////////////////////////////////////////////////////////////////////////////////

    private MyRobot[] robots;
    private MyNitroPack[] nitro_packs;
    private MyBall ball;
    private int greenScore;
    private int redScore;

    public void setRobots(MyRobot[] robots) {
        this.robots = robots;
    }

    public void setNitro_packs(MyNitroPack[] nitro_packs) {
        this.nitro_packs = nitro_packs;
    }

    public void setScore(int greenScore, int redScore) {
        this.greenScore = greenScore;
        this.redScore = redScore;
    }

    public void setBall(MyBall ball) {
        this.ball = ball;
    }

    public State getState() {
        return new State(ball, robots, greenScore, redScore);
    }

    public Dan getResultDan() {
        return danA;
    }

    // todo check all nitro code when nitro is live
    public void tick() {
        for (int i = 0; i < MICROTICKS_PER_TICK; i++) {
            update(DELTA_TIME / MICROTICKS_PER_TICK);
        }

        for (MyNitroPack pack : nitro_packs) {
            if (pack.alive) continue;

            pack.respawn_ticks -= 1;
            if (pack.respawn_ticks == 0) {
                pack.alive = true;
            }
        }
    }

    private void update(double delta_time) {
        shuffle(robots);

        moveRobots(delta_time);
        move(ball, delta_time);

        collideRobotsWithThemselves();
        collideRobotsWithBallAndArena();
        collide_with_arena(ball);

        checkGoalScored();
        checkRobotsGettingNitro();
    }

    private void collideRobotsWithBallAndArena() {
        for (MyRobot robot : robots) {
            collide_entities(robot, ball);
            collide_with_arena(robot);
            if (collision) {
                robot.touch = true;
                robot.touch_normal_x = danA.normal.x;
                robot.touch_normal_y = danA.normal.y;
                robot.touch_normal_z = danA.normal.z;
            } else {
                robot.touch = false;
            }
        }
    }

    private void moveRobots(double delta_time) {
        for (MyRobot robot : robots) {
            if (robot.touch) {
                clampSpeed(robot.target_velocity_x,
                        robot.target_velocity_y,
                        robot.target_velocity_z,
                        ROBOT_MAX_GROUND_SPEED_SQUARED);

                double dot = robot.touch_normal_x * target_velocity_x
                        + robot.touch_normal_y * target_velocity_y
                        + robot.touch_normal_z * target_velocity_z;

                target_velocity_x -= robot.touch_normal_x * dot;
                target_velocity_y -= robot.touch_normal_y * dot;
                target_velocity_z -= robot.touch_normal_z * dot;

                velChange.x = target_velocity_x - robot.velocity_x;
                velChange.y = target_velocity_y - robot.velocity_y;
                velChange.z = target_velocity_z - robot.velocity_z;

                double lenSq = velChange.lenSq();
                if (lenSq > 0) {
                    double acceleration = ROBOT_ACCELERATION * Math.max(0, robot.touch_normal_y);
                    velChange.normalize();
                    double k = acceleration * delta_time;
                    clampSpeed(velChange.x * k, velChange.y * k, velChange.z * k, lenSq);

                    robot.velocity_x += target_velocity_x;
                    robot.velocity_y += target_velocity_y;
                    robot.velocity_z += target_velocity_z;
                }
            }

            // todo check this code // optimize
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
                double lenChange = Ut.len(target_velocity_change_x, target_velocity_change_y, target_velocity_change_z);
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
                    robot.nitro_amount -= Ut.len(velocity_change_x, velocity_change_y, velocity_change_z)
                            / NITRO_POINT_VELOCITY_CHANGE;
                }
            }

            move(robot, delta_time);

            robot.radius = ROBOT_MIN_RADIUS
                    + (ROBOT_MAX_RADIUS - ROBOT_MIN_RADIUS)
                    * robot.jump_speed / ROBOT_MAX_JUMP_SPEED;
            robot.radius_change_speed = robot.jump_speed;
        }
    }

    private void collideRobotsWithThemselves() {
        for (int i = 0; i < robots.length; i++) {
            for (int j = 0; j < i; j++) {
                collide_entities(robots[i], robots[j]);
            }
        }
    }

    private void collide_entities(Entity a, Entity b) {
        double distance = Ut.dist(a, b);
        double penetration = a.radius + b.radius - distance;
        if (penetration > 0) {
            double massSum = a.mass + b.mass;
            double k_a = b.mass / massSum;
            double k_b = a.mass / massSum;

            double len = Ut.len(b.x - a.x, b.y - a.y, b.z - a.z);
            double normal_x = (b.x - a.x) / len;
            double normal_y = (b.y - a.y) / len;
            double normal_z = (b.z - a.z) / len;

            double pa = penetration * k_a;
            a.x -= normal_x * pa;
            a.y -= normal_y * pa;
            a.z -= normal_z * pa;

            double pb = penetration * k_a;
            b.x += normal_x * pb;
            b.y += normal_y * pb;
            b.z += normal_z * pb;

            double delta_velocity = (b.velocity_x - a.velocity_x) * normal_x
                    + (b.velocity_y - a.velocity_y) * normal_y
                    + (b.velocity_z - a.velocity_z) * normal_z
                    + b.radius_change_speed - a.radius_change_speed;

            if (delta_velocity < 0) {
                double k = HIT_COEF * delta_velocity;
                double impulse_x = k * normal_x;
                double impulse_y = k * normal_y;
                double impulse_z = k * normal_z;

                a.velocity_x += impulse_x * k_a;
                a.velocity_y += impulse_y * k_a;
                a.velocity_z += impulse_z * k_a;

                b.velocity_x -= impulse_x * k_b;
                b.velocity_y -= impulse_y * k_b;
                b.velocity_z -= impulse_z * k_b;
            }
        }
    }

    private void collide_with_arena(Entity e) {
        in.x = e.x;
        in.y = e.y;
        in.z = e.z;
        dan_to_arena_to_A(in);

        double penetration = e.radius - danA.distance;
        if (penetration > 0) {
            e.x += penetration * danA.normal.x;
            e.y += penetration * danA.normal.y;
            e.z += penetration * danA.normal.z;

            double velocity = e.velocity_x * danA.normal.x
                    + e.velocity_y * danA.normal.y
                    + e.velocity_z * danA.normal.z
                    - e.radius_change_speed;

            if (velocity < 0) {
                double k = (1 + e.arena_e) * velocity;
                e.velocity_x -= k * danA.normal.x;
                e.velocity_y -= k * danA.normal.y;
                e.velocity_z -= k * danA.normal.z;
                collision = true;
                return;
            }
        }
        collision = false;
    }

    private void move(Entity e, double delta_time) {
        moveClampSpeed(e);

        e.x += e.velocity_x * delta_time;
        e.y += e.velocity_y * delta_time;
        e.z += e.velocity_z * delta_time;

        e.y -= GRAVITY * delta_time * delta_time / 2;
        e.velocity_y -= GRAVITY * delta_time;
    }

    private void shuffle(MyRobot[] robots) {
        // no shuffle because of optimization
    }

    private void checkGoalScored() {
        if (ball.z > ARENA_DEPTH / 2 + ball.radius) {
            greenScore++;
//            System.out.println("green scored");
        }
        if (ball.z < -ARENA_DEPTH / 2 - ball.radius) {
            redScore++;
//            System.out.println("red scored");
        }
    }

    private void checkRobotsGettingNitro() {
        for (MyRobot robot : robots) {
            if (robot.nitro_amount == MAX_NITRO_AMOUNT) continue;

            // todo didnt check this
            for (MyNitroPack pack : nitro_packs) {
                if (!pack.alive) continue;

                if (Ut.dist(robot, pack) <= robot.radius + pack.radius) {
                    robot.nitro_amount = MAX_NITRO_AMOUNT;
                    pack.alive = false;
                    pack.respawn_ticks = NITRO_PACK_RESPAWN_TICKS;
                }
            }
        }
    }

    public void dan_to_arena_to_A(Vec3d point) {
        boolean negate_x = point.x < 0;
        boolean negate_z = point.z < 0;
        if (negate_x) {
            point.x = -point.x;
        }

        if (negate_z) {
            point.z = -point.z;
        }

        dan_to_arena_quarter_to_B(point);
        if (negate_x) {
            danB.normal.x = -danB.normal.x;
        }

        if (negate_z) {
            danB.normal.z = -danB.normal.z;
        }

        danA.distance =  danB.distance;
        danA.normal.x = danB.normal.x;
        danA.normal.y = danB.normal.y;
        danA.normal.z = danB.normal.z;
    }

    private void dan_to_plane_to_A(Vec3d point, Vec3d point_on_plane, Vec3d plane_normal) {
        danA.distance = (point.x - point_on_plane.x) * plane_normal.x;
        danA.distance += (point.y - point_on_plane.y) * plane_normal.y;
        danA.distance += (point.z - point_on_plane.z) * plane_normal.z;

        danA.normal.x = plane_normal.x;
        danA.normal.y = plane_normal.y;
        danA.normal.z = plane_normal.z;
    }

    private void dan_to_sphere_inner_to_A(Vec3d point, Vec3d sphere_center, double sphere_radius) {
        Ut.copyVec3d(point, vecT);
        vecT.sub(sphere_center);
        danA.distance = sphere_radius - vecT.length();

        vecT.reverse();
        vecT.normalize();
        danA.normal.x = vecT.x;
        danA.normal.y = vecT.y;
        danA.normal.z = vecT.z;
    }

    private void dan_to_sphere_outer(Vec3d point, Vec3d sphere_center, double sphere_radius) {
        Ut.copyVec3d(point, vecT);
        vecT.sub(sphere_center);

        danA.distance = vecT.length() - sphere_radius;
        vecT.normalize();
        danA.normal.x = vecT.x;
        danA.normal.y = vecT.y;
        danA.normal.z = vecT.z;
    }

    private void dan_to_arena_quarter_to_B(Vec3d point) {
        // Ground
        dan_to_plane_to_A(point, GROUND_PLANE, GROUND_NORMAL);
        copyAtoB();
        // Ceiling
        dan_to_plane_to_A(point, CEILING_PLANE, CEILING_NORMAL);
        minToB();

        // Side x
        dan_to_plane_to_A(point, SIDE_X_PLANE, SIDE_X_NORMAL);
        minToB();

        // Side z (goal)
        dan_to_plane_to_A(point, GOAL_Z_PLANE, GOAL_Z_NORMAL);
        minToB();

        // Side z
        vecU.x = point.x - (GOAL_WIDTH / 2 - GOAL_TOP_RADIUS);
        vecU.y = point.y - (GOAL_HEIGHT - GOAL_TOP_RADIUS);
        if (point.x >= GOAL_WIDTH / 2 + GOAL_SIDE_RADIUS
                || point.y >= GOAL_HEIGHT + GOAL_SIDE_RADIUS
                || (vecU.x > 0 && vecU.y > 0 && vecU.length() >= GOAL_TOP_RADIUS + GOAL_SIDE_RADIUS)) {
            dan_to_plane_to_A(point, SIDE_Z_PLANE, SIDE_Z_NORMAL);
            minToB();
        }

        // Side x & ceiling (goal)
        if (point.z >= ARENA_DEPTH / 2 + GOAL_SIDE_RADIUS) {
            // x
            dan_to_plane_to_A(point, GOAL_X_PLANE, GOAL_X_NORMAL);
            minToB();
            // y
            dan_to_plane_to_A(point, GOAL_Y_PLANE, GOAL_Y_NORMAL);
            minToB();
        }

        // Goal back corners
        if (point.z > (ARENA_DEPTH / 2) + GOAL_DEPTH - ARENA_BOTTOM_RADIUS) {
            vecP.x = clamp(point.x,
                    ARENA_BOTTOM_RADIUS - (GOAL_WIDTH / 2),
                    (GOAL_WIDTH / 2) - ARENA_BOTTOM_RADIUS);
            vecP.y = clamp(point.y, ARENA_BOTTOM_RADIUS, GOAL_HEIGHT - GOAL_TOP_RADIUS);
            vecP.z = ARENA_DEPTH / 2 + GOAL_DEPTH - ARENA_BOTTOM_RADIUS;

            dan_to_sphere_inner_to_A(point, vecP, ARENA_BOTTOM_RADIUS);
            minToB();
        }

        // Corner
        if (point.x > ARENA_WIDTH / 2 - ARENA_CORNER_RADIUS && point.z > ARENA_DEPTH / 2 - ARENA_CORNER_RADIUS) {
            vecP.x = ARENA_WIDTH / 2 - ARENA_CORNER_RADIUS;
            vecP.y = point.y;
            vecP.z = ARENA_DEPTH / 2 - ARENA_CORNER_RADIUS;

            dan_to_sphere_inner_to_A(point, vecP, ARENA_CORNER_RADIUS);
            minToB();
        }

        // Goal outer corner
        if (point.z < ARENA_DEPTH / 2 + GOAL_SIDE_RADIUS) {
            // Side x
            if (point.x < GOAL_WIDTH / 2 + GOAL_SIDE_RADIUS) {
                vecP.x = (GOAL_WIDTH / 2) + GOAL_SIDE_RADIUS;
                vecP.y = point.y;
                vecP.z = (ARENA_DEPTH / 2) + GOAL_SIDE_RADIUS;

                dan_to_sphere_outer(point, vecP, GOAL_SIDE_RADIUS);
                minToB();
            }

            // Ceiling
            if (point.y < GOAL_HEIGHT + GOAL_SIDE_RADIUS) {
                vecP.x = point.x;
                vecP.y = GOAL_HEIGHT + GOAL_SIDE_RADIUS;
                vecP.z = (ARENA_DEPTH / 2) + GOAL_SIDE_RADIUS;

                dan_to_sphere_outer(point, vecP, GOAL_SIDE_RADIUS);
                minToB();
            }

            // Top corner
            vecV.x = GOAL_WIDTH / 2 - GOAL_TOP_RADIUS;
            vecV.y = GOAL_HEIGHT - GOAL_TOP_RADIUS;
            vecU.x = point.x;
            vecU.y = point.y;
            vecU.sub(vecV);
            if (vecU.x > 0 && vecU.y > 0) {
                vecV.add(vecU.normalize().mul(GOAL_TOP_RADIUS + GOAL_SIDE_RADIUS));
                vecP.x = vecV.x;
                vecP.y = vecV.y;
                vecP.z = ARENA_DEPTH / 2 + GOAL_SIDE_RADIUS;

                dan_to_sphere_outer(point, vecP, GOAL_SIDE_RADIUS);
                minToB();
            }
        }

        // Goal inside top corners
        if (point.z > ARENA_DEPTH / 2 + GOAL_SIDE_RADIUS && point.y > GOAL_HEIGHT - GOAL_TOP_RADIUS) {
            // Side x
            if (point.x > GOAL_WIDTH / 2 - GOAL_TOP_RADIUS) {
                vecP.x = (GOAL_WIDTH / 2) - GOAL_TOP_RADIUS;
                vecP.y = GOAL_HEIGHT - GOAL_TOP_RADIUS;
                vecP.z = point.z;

                dan_to_sphere_inner_to_A(point, vecP, GOAL_TOP_RADIUS);
                minToB();
            }

            // Side z
            if (point.z > (ARENA_DEPTH / 2) + GOAL_DEPTH - GOAL_TOP_RADIUS) {
                vecP.x = point.x;
                vecP.y = GOAL_HEIGHT - GOAL_TOP_RADIUS;
                vecP.z = (ARENA_DEPTH / 2) + GOAL_DEPTH - GOAL_TOP_RADIUS;

                dan_to_sphere_inner_to_A(point, vecP, GOAL_TOP_RADIUS);
                minToB();
            }
        }

        // Bottom corners
        if (point.y < ARENA_BOTTOM_RADIUS) {
            // Side x
            if (point.x > (ARENA_WIDTH / 2) - ARENA_BOTTOM_RADIUS) {
                vecP.x = (ARENA_WIDTH / 2) - ARENA_BOTTOM_RADIUS;
                vecP.y = ARENA_BOTTOM_RADIUS;
                vecP.z = point.z;

                dan_to_sphere_inner_to_A(point, vecP, ARENA_BOTTOM_RADIUS);
                minToB();
            }

            // Side z
            if (point.z > ARENA_DEPTH / 2 - ARENA_BOTTOM_RADIUS && point.x >= GOAL_WIDTH / 2 + GOAL_SIDE_RADIUS) {
                vecP.x = point.x;
                vecP.y = ARENA_BOTTOM_RADIUS;
                vecP.z = (ARENA_DEPTH / 2) - ARENA_BOTTOM_RADIUS;

                dan_to_sphere_inner_to_A(point, vecP, ARENA_BOTTOM_RADIUS);
                minToB();
            }

            // Side z (goal)
            if (point.z > (ARENA_DEPTH / 2) + GOAL_DEPTH - ARENA_BOTTOM_RADIUS) {
                vecP.x = point.x;
                vecP.y = ARENA_BOTTOM_RADIUS;
                vecP.z = (ARENA_DEPTH / 2) + GOAL_DEPTH - ARENA_BOTTOM_RADIUS;

                dan_to_sphere_inner_to_A(point, vecP, ARENA_BOTTOM_RADIUS);
                minToB();
            }

            // Goal outer corner
            vecV.x = (GOAL_WIDTH / 2) + GOAL_SIDE_RADIUS;
            vecV.y = (ARENA_DEPTH / 2) + GOAL_SIDE_RADIUS;
            vecU.x = point.x;
            vecU.y = point.z;
            vecU.sub(vecV);
            if (vecU.x < 0 && vecU.y < 0 && vecU.length() < GOAL_SIDE_RADIUS + ARENA_BOTTOM_RADIUS) {
                vecV.add(vecU.normalize().mul(GOAL_SIDE_RADIUS + ARENA_BOTTOM_RADIUS));
                vecP.x = vecV.x;
                vecP.y = ARENA_BOTTOM_RADIUS;
                vecP.z = vecV.y;

                dan_to_sphere_inner_to_A(point, vecP, ARENA_BOTTOM_RADIUS);
                minToB();
            }

            // Side x (goal)
            if (point.z >= ARENA_DEPTH / 2 + GOAL_SIDE_RADIUS && point.x > GOAL_WIDTH / 2 - ARENA_BOTTOM_RADIUS) {
                vecP.x = (GOAL_WIDTH / 2) - ARENA_BOTTOM_RADIUS;
                vecP.y = ARENA_BOTTOM_RADIUS;
                vecP.z = point.z;

                dan_to_sphere_inner_to_A(point, vecP, ARENA_BOTTOM_RADIUS);
                minToB();
            }

            // Corner
            if (point.x > (ARENA_WIDTH / 2) - ARENA_CORNER_RADIUS && point.z > (ARENA_DEPTH / 2) - ARENA_CORNER_RADIUS) {
                vecV.x = (ARENA_WIDTH / 2) - ARENA_CORNER_RADIUS;
                vecV.y = (ARENA_DEPTH / 2) - ARENA_CORNER_RADIUS;
                vecU.x = point.x;
                vecU.y = point.z;
                vecU.sub(vecV);
                double dist = vecU.length();
                if (dist > ARENA_CORNER_RADIUS - ARENA_BOTTOM_RADIUS) {
                    vecU.div(dist);
                    vecV.add(vecU.mul(ARENA_CORNER_RADIUS - ARENA_BOTTOM_RADIUS));

                    vecP.x = vecV.x;
                    vecP.y = ARENA_BOTTOM_RADIUS;
                    vecP.z = vecV.y;

                    dan_to_sphere_inner_to_A(point, vecP, ARENA_BOTTOM_RADIUS);
                    minToB();
                }
            }
        }

        // Ceiling corners
        if (point.y > ARENA_HEIGHT - ARENA_TOP_RADIUS) {
            // Side x
            if (point.x > (ARENA_WIDTH / 2) - ARENA_TOP_RADIUS) {
                vecP.x = (ARENA_WIDTH / 2) - ARENA_TOP_RADIUS;
                vecP.y = ARENA_HEIGHT - ARENA_TOP_RADIUS;
                vecP.z = point.z;

                dan_to_sphere_inner_to_A(point, vecP, ARENA_TOP_RADIUS);
                minToB();
            }

            // Side z
            if (point.z > (ARENA_DEPTH / 2) - ARENA_TOP_RADIUS) {
                vecP.x = point.x;
                vecP.y = ARENA_HEIGHT - ARENA_TOP_RADIUS;
                vecP.z = (ARENA_DEPTH / 2) - ARENA_TOP_RADIUS;

                dan_to_sphere_inner_to_A(point, vecP, ARENA_TOP_RADIUS);
                minToB();
            }

            // Corner
            if (point.x > ARENA_WIDTH / 2 - ARENA_CORNER_RADIUS && point.z > ARENA_DEPTH / 2 - ARENA_CORNER_RADIUS) {
                vecV.x = (ARENA_WIDTH / 2) - ARENA_CORNER_RADIUS;
                vecV.y = (ARENA_DEPTH / 2) - ARENA_CORNER_RADIUS;
                vecU.x = point.x;
                vecU.y = point.z;
                vecU.sub(vecV);
                if (vecU.length() > ARENA_CORNER_RADIUS - ARENA_TOP_RADIUS) {
                    vecU.normalize();
                    vecV.add(vecU.mul((ARENA_CORNER_RADIUS - ARENA_TOP_RADIUS)));

                    vecP.x = vecV.x;
                    vecP.y = ARENA_HEIGHT - ARENA_TOP_RADIUS;
                    vecP.z = vecV.y;

                    dan_to_sphere_inner_to_A(point, vecP, ARENA_TOP_RADIUS);
                    minToB();
                }
            }
        }
    }

    private void copyAtoB() {
        danB.distance = danA.distance;
        danB.normal.x = danA.normal.x;
        danB.normal.y = danA.normal.y;
        danB.normal.z = danA.normal.z;
    }


    private double clamp(double val, double min, double max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }

    private void minToB() {
        if (danA.distance < danB.distance) {
            copyAtoB();
        }
    }

    private void moveClampSpeed(Entity e) {
        double speedSq = Ut.lenSq(e.velocity_x, e.velocity_y, e.velocity_z);

        if (speedSq < MAX_ENTITY_SPEED_SQUARED) return;

        double k = Math.sqrt(MAX_ENTITY_SPEED_SQUARED / speedSq);
        e.velocity_x *= k;
        e.velocity_y *= k;
        e.velocity_z *= k;
    }

    // USES target_velocity_xyz as out parameters!
    private void clampSpeed(double x, double y, double z, double maxSpeedSq) {
        double speedSq = Ut.lenSq(x, y, z);

        if (speedSq < maxSpeedSq) return;

        double k = Math.sqrt(maxSpeedSq / speedSq);
        target_velocity_x = x * k;
        target_velocity_y = y * k;
        target_velocity_z = z * k;
    }

}
