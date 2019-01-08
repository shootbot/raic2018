import model.*;

import java.util.*;

public final class MyStrategy implements Strategy {
    Random rng = new Random();
    Vec3d ball = new Vec3d();
    Vec3d ballSpeed = new Vec3d();
    Vec3d enemyGates = new Vec3d();
    String msg;
    Rules rules;
    
    Map<Integer, Robot> robots = new HashMap<>();
    Solid bot = new Solid();
    int attId, defId;
    boolean attacking = true;
    
    boolean displayOnce = true;
    
    Vec3d targetSpeed = new Vec3d();
    double jumpSpeed;
    
    @Override
    public void act(Robot me, Rules rules, Game game, Action action) {
        init(game, rules, me);
        
        if (me.id == attId) {
            targetSpeed = moveAttacker();
        } else {
            moveDefender();
        }
        
        targetSpeed = targetSpeed.setLength(30);

//        msg += String.format("%d move to %.2f %.2f\n", me.id, move.x, move.z);
        
        
        setAction(action);
    }
    
    private void setAction(Action action) {
        action.target_velocity_x = targetSpeed.x;
        action.target_velocity_y = targetSpeed.y;
        action.target_velocity_z = targetSpeed.z;
        action.jump_speed = jumpSpeed;
    }
    
    private Vec3d moveAttacker() {
//        System.out.println("attacker: " + attPos);
        double dz = ball.z - bot.pos.z;
        if (attacking) {
            if (dz > 0) {
                Vec3d crashPoint = getCrashPoint();
                return moveTo(crashPoint);
            } else {
                attacking = false;
                System.out.println("stop attack, att:" + bot + " ball:" + ball);
                Vec3d move = ball.copy();
                move.z -= 5 * rules.BALL_RADIUS;
                return moveTo(move);
            }
        } else {
            if (dz > 4 * rules.BALL_RADIUS) {
                attacking = true;
                System.out.println("start attack, att:" + bot + " ball:" + ball);
                Vec3d crashPoint = getCrashPoint();
                return moveTo(crashPoint);
            } else {
                Vec3d move = ball.copy();
                move.z -= 8 * rules.BALL_RADIUS;
                return moveTo(move);
            }
        }
        
    }
    
    private Vec3d getCrashPoint() {
        Vec3d crashPoint = ball.copy();
        crashPoint.sub(enemyGates);
        crashPoint.setLength(crashPoint.length() + rules.BALL_RADIUS + rules.ROBOT_RADIUS);
        crashPoint.add(enemyGates);
        return crashPoint;
    }
    
    private void moveDefender() {
//        System.out.println("defender: " + defPos);
        
        double x = ball.x + ballSpeed.x / 60;
        double TOO_NEAR = 4.75;
        
        double lowLimit = -rules.arena.goal_width / 2 + rules.arena.bottom_radius;
        double highLimit = rules.arena.goal_width / 2 - rules.arena.bottom_radius;
        if (x < lowLimit) {
            x = lowLimit;
        }
        if (x > highLimit) {
            x = highLimit;
        }
        
        targetSpeed = moveTo(new Vec3d(x, 0, -rules.arena.depth / 2));
        if (targetSpeed.x > 0) {
            double s = highLimit - bot.pos.x;
    
            if (s <TOO_NEAR) {
                targetSpeed.x = -100;
            }
        } else {
            double s = bot.pos.x - lowLimit;
            if (s <TOO_NEAR) {
                targetSpeed.x = 100;
            }
        }
    
        Vec3d pos = new Vec3d(bot.pos);
        if (pos.sub(ball).length() < 4 * rules.BALL_RADIUS && ball.y > 2 * rules.BALL_RADIUS) {
            jumpSpeed = rules.ROBOT_MAX_JUMP_SPEED;
        }
    }
    
    private Vec3d moveTo(Vec3d target) {
        Vec3d move = target.copy();
        move.sub(bot.pos);
        move.y = 0;
//        if (id == attId) {
//            System.out.println("moveTo() attck target: " + target + " move: " + move.setLength(rules.MAX_ENTITY_SPEED));
//        } else {
//            System.out.println("moveTo() defend target: " + target + " move: " + move);
//        }
        
        return move.setLength(rules.ROBOT_MAX_GROUND_SPEED);
    }
    
    private void init(Game game, Rules rules, Robot me) {
        this.rules = rules;
        ball.set(game.ball.x, game.ball.y, game.ball.z);
        ballSpeed.set(game.ball.velocity_x, game.ball.velocity_y, game.ball.velocity_z);
//        System.out.println("ball: " + ball);
        
        double minDist = 500;
        if (robots.isEmpty()) {
            for (Robot r : game.robots) {
                if (!r.is_teammate) continue;
                
                // biggest id is attacker, lowest is defender
                if (r.id > attId) {
                    attId = r.id;
                }
                if (r.id < defId) {
                    defId = r.id;
                }
                
//                if (ball.z >= 0) {
//                    double dist = Ut.dist(new MyBall(ball), new MyRobot(r));
//                    if (dist < minDist) {
//                        minDist = dist;
//                        attId = r.id;
//                    }
//                } else {
//
//                }
            }
            
            enemyGates.set(0, 0, rules.arena.depth / 2 + rules.arena.goal_depth);

//            System.out.println("attId: " + attId + " defId: " + defId);
        }
        
        for (Robot r : game.robots) {
            if (!r.is_teammate) continue;
            
            robots.put(r.id, r);
            if (me.id == r.id) {
                bot.pos = new Vec3d(r.x, r.y, r.z);
                bot.speed = new Vec3d(r.velocity_x, r.velocity_y, r.velocity_z);
            }
//            if (r.id == attId) {
//                System.out.println("att speed:" + (new Vec3d(r.velocity_x, r.velocity_y, r.velocity_z)).length());
//            }
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
