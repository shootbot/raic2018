import model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class MyStrategy implements Strategy {
    private Random rng = new Random();
    private Vec3D ball = new Vec3D();
    private Vec3D ballSpeed = new Vec3D();
    private Vec3D enemyGates = new Vec3D();
    private String msg;
    private Rules rules;

    Map<Integer, Robot> robots = new HashMap<>();
    private Vec3D defPos = new Vec3D();
    private Vec3D attPos = new Vec3D();
    Vec3D robotVel = new Vec3D();
    int attId = Integer.MIN_VALUE;
    int defId = Integer.MAX_VALUE;
    boolean attacking = true;

    boolean displayOnce = true;

    @Override
    public void act(Robot me, Rules rules, Game game, Action action) {
        init(game, rules);

        Vec3D move;
        if (me.id == attId) {
            move = moveAttacker();
        } else {
            move = moveDefender(rules);
        }

        move = move.length(30);

        action.target_velocity_x = move.x;
        action.target_velocity_y = move.y;
        action.target_velocity_z = move.z;

//        msg += String.format("%d move to %.2f %.2f\n", me.id, move.x, move.z);
        if (defPos.sub(ball).length() < 4 * rules.BALL_RADIUS && ball.y > 2 * rules.BALL_RADIUS) {
            action.jump_speed = rules.ROBOT_MAX_JUMP_SPEED;
        }
    }

    private Vec3D moveAttacker() {
//        System.out.println("attacker: " + attPos);
        double dz = ball.z - attPos.z;
        if (attacking) {
            if (dz > 0) {
                Vec3D crashPoint = getCrashPoint();
                return moveTo(attId, crashPoint);
            } else {
                attacking = false;
                System.out.println("stop attack, att:" + attPos + " ball:" + ball);
                Vec3D move = ball.copy();
                move.z -= 5 * rules.BALL_RADIUS;
                return moveTo(attId, move);
            }
        } else {
            if (dz > 4 * rules.BALL_RADIUS) {
                attacking = true;
                System.out.println("start attack, att:" + attPos + " ball:" + ball);
                Vec3D crashPoint = getCrashPoint();
                return moveTo(attId, crashPoint);
            } else {
                Vec3D move = ball.copy();
                move.z -= 8 * rules.BALL_RADIUS;
                return moveTo(attId, move);
            }
        }

    }

    private Vec3D getCrashPoint() {
        Vec3D crashPoint = ball.copy();
        crashPoint.sub(enemyGates);
        crashPoint.length(crashPoint.length() + rules.BALL_RADIUS + rules.ROBOT_RADIUS);
        crashPoint.add(enemyGates);
        return crashPoint;
    }

    private Vec3D moveDefender(Rules rules) {
//        System.out.println("defender: " + defPos);
        double x =  ball.x + ballSpeed.x / 30;

        double lowLimit = -rules.arena.goal_width / 2 + rules.arena.bottom_radius;
        double highLimit = rules.arena.goal_width / 2 - rules.arena.bottom_radius;
        if (x < lowLimit) {
            x = lowLimit;
        }
        if (x > highLimit) {
            x = highLimit;
        }
        return moveTo(defId, new Vec3D(x, ball.y, -rules.arena.depth / 2));
    }

    private Vec3D moveTo(int id, Vec3D target) {
        Robot robot = robots.get(id);
        Vec3D pos = new Vec3D(robot.x, robot.y, robot.z);
        Vec3D move = target.copy();
        move.sub(pos);
        move.y =0;
        if (id == attId) {
            System.out.println("moveTo() attck target: " + target + " move: " + move.length(rules.MAX_ENTITY_SPEED));
        } else {
//            System.out.println("moveTo() defend target: " + target + " move: " + move);
        }

        return move.length(rules.MAX_ENTITY_SPEED);
    }

    private void init(Game game, Rules rules) {
        this.rules = rules;
        ball.set(game.ball.x, game.ball.y, game.ball.z);
        ballSpeed.set(game.ball.velocity_x, game.ball.velocity_y, game.ball.velocity_z);
//        System.out.println("ball: " + ball);

        if (robots.isEmpty()) {
            for (Robot r : game.robots) {
                if (!r.is_teammate) continue;

                if (r.id > attId) {
                    attId = r.id;
                }
                if (r.id < defId) {
                    defId = r.id;
                }
            }

            enemyGates.set(0, 1, rules.arena.depth / 2 + rules.arena.goal_depth);

//            System.out.println("attId: " + attId + " defId: " + defId);
        }

        for (Robot r : game.robots) {
            if (!r.is_teammate) continue;

            robots.put(r.id, r);
            if (r.id == attId) {
                attPos.set(r.x, r.y, r.z);
                System.out.println("att speed:" + (new Vec3D(r.velocity_x, r.velocity_y, r.velocity_z)).length());
            } else {
                defPos.set(r.x, r.y, r.z);
            }
//            System.out.println("attPos: " + attPos + " defPos: " + defPos);
        }

        msg = "";
    }

    @Override
    public String customRendering() {
//        System.out.println(msg);
        return msg;

    }
}
