package com.shootbot.raic2018.codeball;

import com.shootbot.raic2018.codeball.model.*;
import com.shootbot.raic2018.codeball.model.Robot;

import java.awt.*;
import java.util.*;

import static java.lang.Math.*;

/**
 * ideas
 * cut microticks per tick
 * sim only full speed and 0 15 jumps
 * pp for bot positioning
 * <p>
 * <p>
 * third robot:
 * keeper-2
 * attacker-2
 * toughguy
 */

public final class MyStrategy implements Strategy {
	private Random rng = new Random();
	private Vec3d ball = new Vec3d();
	private Vec3d ballSpeed = new Vec3d();
	
	private StringBuilder msg = new StringBuilder();
	private Rules rules;
	
	private Solid bot = new Solid();
	private boolean attacking = true;
	
	private Vec3d ENEMY_GATES = new Vec3d(0, 0, Sim.ARENA_DEPTH / 2 + Sim.GOAL_DEPTH);
	private Vec3d OUR_GATES = new Vec3d(0, 0, -Sim.ARENA_DEPTH / 2);
	
	private Vec3d targetSpeed;
	private double jumpSpeed;
	private boolean isAttacker;
	
	private State[] states = new State[18000];
	private Sim sim = new Sim();
	
	@Override
	public void act(Robot me, Rules rules, Game game, Action action) {
		init(game, rules, me);
//		sim(game);
		
		if (isAttacker) {
			moveAttacker();
		} else {
			moveDefender();
		}
		
		writeAction(action);
	}
	
	private void sim(Game game) {
		long timeTotal = 0;
		long TIME_LIMIT = 20L;
		int SIM_TICKS = 120;
		sim.setBall(new MyBall(ball, ballSpeed));
		sim.setNitro_packs(new MyNitroPack[0]);
		sim.setRobots(new MyRobot[0]);
		int currentTick = game.current_tick;
		Vec3d curBall = ball.copy();
		boolean isFirstTime = true;
		for (int i = 1; i < SIM_TICKS; i++) {
			long start = System.nanoTime();
			sim.tick();
			timeTotal += System.nanoTime() - start;
			State next = sim.getState();
			states[currentTick + i] = next;
			String line = Ut.getLine(curBall.x, curBall.y, curBall.z,
				next.ball.x, next.ball.y, next.ball.z,
				3, Color.RED);
			curBall = new Vec3d(next.ball.x, next.ball.y, next.ball.z);
			
			if (!isFirstTime) {
				msg.append(',');
			}
			isFirstTime = false;
			
			msg.append(line);
			
		}
		
	}
	
	private void writeAction(Action action) {
		action.target_velocity_x = targetSpeed.x;
		action.target_velocity_y = targetSpeed.y;
		action.target_velocity_z = targetSpeed.z;
		action.jump_speed = jumpSpeed;
	}
	
	private void moveAttacker() {
		double dz = ball.z - bot.pos.z; // - Sim.BALL_RADIUS - Sim.ROBOT_RADIUS
		if (attacking) {
			if (dz > 0) {
				tryAimShot();
			} else {
				attacking = false;
//				System.out.println("stop attack, att:" + bot + " ball:" + ball);
				Vec3d move = ball.copy();
				move.z -= 5 * Sim.BALL_RADIUS;
				targetSpeed = getMove30To(move);
			}
		} else {
			if (dz > 4 * Sim.BALL_RADIUS) {
				attacking = true;
//				System.out.println("start attack, att:" + bot + " ball:" + ball);
				tryAimShot();
			} else {
				Vec3d move = ball.copy();
				move.z -= 8 * Sim.BALL_RADIUS;
				targetSpeed = getMove30To(move);
			}
		}
		
		if (dz > 0 && Ut.dist(ball, OUR_GATES) < Sim.GOAL_WIDTH / 2) {
			throwBall();
		}
		
		dontScoreYourself();
	}
	
	private void dontScoreYourself() {
//        double dz = ball.z - bot.pos.z;
//        double dx = ball.x - bot.pos.x;
//        if (dz < 0 && dx < 1 && ball.x > -Sim.GOAL_WIDTH / 2 && ball.x < Sim.GOAL_WIDTH / 2)  {
//            Vec3d v = targetSpeed.copy().normalize();
//            Vec3d vb = ballSpeed.copy().normalize();
//        }
	}
	
	private void tryAimShot() {
		Vec3d crashPoint = getCrashPoint();
		targetSpeed = getMove30To(crashPoint);
		
		if (ball.y > 1.5 * Sim.BALL_RADIUS) {
			if (Ut.dist2d(ball, bot.pos) < 3) {
				jumpSpeed = Sim.ROBOT_MAX_JUMP_SPEED;
			}
		}
	}
	
	private void throwBall() {
		Vec3d target = ball;
		Vec3d vb = ballSpeed.copy();
		target.add(vb.mul(3.0 * Sim.DELTA_TIME));
		target.z -= Sim.BALL_RADIUS;
		targetSpeed = getMove30To(target);
		
		if (ball.y > 1.5 * Sim.BALL_RADIUS) {
			if (Ut.dist2d(ball, bot.pos) < 3) {
				jumpSpeed = Sim.ROBOT_MAX_JUMP_SPEED;
			}
		}
	}
	
	private Vec3d getCrashPoint() {
		Vec3d crashPoint = ball.copy();
		Vec3d vb = ballSpeed.copy();
		crashPoint.add(vb.mul(5.0 * Sim.DELTA_TIME)); // after 5 ticks
		crashPoint.sub(ENEMY_GATES);
		crashPoint.setLength(crashPoint.length() + Sim.BALL_RADIUS + Sim.ROBOT_RADIUS);
		crashPoint.add(ENEMY_GATES);
		return crashPoint;
	}
	
	private void moveDefender() {
		double x = ball.x + ballSpeed.x * 8 * Sim.DELTA_TIME;
		double TOO_NEAR = 1.125;
		
		
		double leftLimit = -Sim.GOAL_WIDTH / 2 + Sim.ARENA_BOTTOM_RADIUS;
		double rightLimit = Sim.GOAL_WIDTH / 2 - Sim.ARENA_BOTTOM_RADIUS;
		
		if (x < leftLimit) {
			x = leftLimit;
		}
		if (x > rightLimit) {
			x = rightLimit;
		}
		
		targetSpeed = getMove30To(new Vec3d(x, 0, -Sim.ARENA_DEPTH / 2));
		if (Ut.dist(bot.pos, OUR_GATES) < Sim.GOAL_WIDTH / 2) {
			targetSpeed.mul(0.5);
		}
		
		if (targetSpeed.x > 0) {
			double s = rightLimit - bot.pos.x;
			if (s < TOO_NEAR) {
				targetSpeed.x = -Sim.ROBOT_MAX_GROUND_SPEED / 2;
			}
		} else {
			double s = bot.pos.x - leftLimit;
			if (s < TOO_NEAR) {
				targetSpeed.x = Sim.ROBOT_MAX_GROUND_SPEED / 2;
			}
		}
		
		Vec3d pos = new Vec3d(bot.pos);
		if (pos.sub(ball).length() < 4 * Sim.BALL_RADIUS && ball.y > 2 * Sim.BALL_RADIUS) {
			jumpSpeed = Sim.ROBOT_MAX_JUMP_SPEED;
		}
		
		double dz = ball.z - bot.pos.z;
		if (dz > 0 && (Ut.dist(ball, OUR_GATES) < Sim.GOAL_WIDTH / 2 || ball.z < -Sim.ARENA_DEPTH / 2 + 2 * Sim.ARENA_BOTTOM_RADIUS)) {
			throwBall();
		}
	}
	
	private void moveDefender2() {
		double KEEPER_Z = -Sim.ARENA_DEPTH / 2 - 2;
		double GOAL_Z = -Sim.ARENA_DEPTH / 2;
		double GOAL_LEFT = -Sim.GOAL_WIDTH / 2;
		double GOAL_RIGHT = Sim.GOAL_WIDTH / 2;
		
		if (ballSpeed.z > 0) {
			targetSpeed = getMove30To(new Vec3d(0, 0, KEEPER_Z));
			return;
		}
		
		if (abs(bot.pos.x) > Sim.GOAL_WIDTH / 2 || bot.pos.z < KEEPER_Z || bot.pos.z > GOAL_Z + 2) {
			targetSpeed = getMove30To(new Vec3d(0, 0, KEEPER_Z));
			return;
		}
		
		double dz = ball.z - GOAL_Z;
		double t = dz / abs(ballSpeed.z);
		if (Double.isInfinite(t)) {
			t = 5;
		}
		double dx = t * ballSpeed.x;
		double x = ball.x + dx;
		if (x < GOAL_LEFT + Sim.ARENA_BOTTOM_RADIUS) {
			x = GOAL_LEFT + Sim.ARENA_BOTTOM_RADIUS;
		}
		if (x > GOAL_RIGHT - Sim.ARENA_BOTTOM_RADIUS) {
			x = GOAL_RIGHT - Sim.ARENA_BOTTOM_RADIUS;
		}
		double s = x - bot.pos.x;
		double v = bot.speed.x;
		double vt = v * t;
		double dt = 1.0 / Sim.TICKS_PER_SECOND;
		
		double a = 0;
		if (s > 0) {
			if (v > 0) {
				if (vt > s) {
					a = 2 * (s - vt) / (t * t);
					targetSpeed.x = v - a * dt;
				} else {
					targetSpeed.x = Sim.ROBOT_MAX_GROUND_SPEED;
				}
			} else {
				a = 2 * (s - vt) / (t * t);
				targetSpeed.x = v - a * dt;
			}
		} else {
			if (v > 0) {
				a = 2 * (s - vt) / (t * t);
				targetSpeed.x = v - a * dt;
			} else {
				if (vt < s) {
					a = 2 * (s - vt) / (t * t);
					targetSpeed.x = v - a * dt;
				} else {
					targetSpeed.x = -Sim.ROBOT_MAX_GROUND_SPEED;
				}
			}
		}
		if (Double.isNaN(targetSpeed.x)) {
			System.out.println("defender NaN");
			System.out.println("ball: " + ball + "->" + ballSpeed);
			System.out.println("bot: " + bot.pos + "->" + bot.speed);
			System.out.println("dz " + dz);
			System.out.println("dx " + dx);
			System.out.println("s " + s);
			System.out.println("v " + v);
			System.out.println("t " + t);
			System.out.println("a " + a);
		}
	}
	
	private Vec3d getMove30To(Vec3d target) {
		Vec3d move = target.copy();
		move.sub(bot.pos);
		move.setLength(rules.ROBOT_MAX_GROUND_SPEED);
		move.sub(bot.speed);
		
		return move;
	}
	
	private void init(Game game, Rules rules, Robot me) {
		this.rules = rules;
		msg.setLength(0);
		targetSpeed = new Vec3d();
		jumpSpeed = 0;
		isAttacker = false;
		
		ball.set(game.ball.x, game.ball.y, game.ball.z);
		ballSpeed.set(game.ball.velocity_x, game.ball.velocity_y, game.ball.velocity_z);
		
		Vec3d ballPos = ball.copy();
		ballPos.add(ballSpeed.copy().mul(10 * Sim.DELTA_TIME));
		
		for (Robot r : game.robots) {
			if (!r.is_teammate) continue;
			
			if (me.id != r.id) {
				bot.pos = new Vec3d(me.x, me.y, me.z);
				bot.speed = new Vec3d(me.velocity_x, me.velocity_y, me.velocity_z);
//				Vec3d newPos = bot.pos.copy().add(bot.speed.copy().mul(10 * Sim.DELTA_TIME));
//
//				Vec3d otherBotPos = new Vec3d(r.x, r.y, r.z);
//				Vec3d otherBotSpeed = new Vec3d(r.velocity_x, r.velocity_y, r.velocity_z);
//				otherBotPos.add(otherBotSpeed.mul(10 * Sim.DELTA_TIME));
//
//				double myDist = Ut.dist(ballPos, newPos);
//				double otherDist = Ut.dist(ballPos, otherBotPos);
				
				if (me.z > r.z) {
					isAttacker = true;
				}
			}
		}
	}
	
	@Override
	public String customRendering() {
		return '[' + msg.toString() + ']';
	}
}
