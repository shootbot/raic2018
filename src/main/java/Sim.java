import model.*;

import java.util.*;


public class Sim {
	private Random rng = new Random();
	private MyRobot[] robots;
	private MyNitroPack[] nitro_packs;
	private MyBall ball;
	private Arena arena;
	private int greenScore;
	private int redScore;
	
	private boolean collision;
	
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
	
	//////////////////////////////////////////////////////////////////////////////////
	// output variables
	private double distanceToArena;
	private double collision_normal_x;
	private double collision_normal_y;
	private double collision_normal_z;
	
	// for clamp_speed(action.target_velocity)
	private double target_velocity_x;
	private double target_velocity_y;
	private double target_velocity_z;
	
	private double dan_ret_distance;
	private double dan_ret_normal_x;
	private double dan_ret_normal_y;
	private double dan_ret_normal_z;
	
	//////////////////////////////////////////////////////////////////////////////////
	
	public void setRobots(MyRobot[] robots) {
		this.robots = robots;
	}
	
	public void setNitro_packs(MyNitroPack[] nitro_packs) {
		this.nitro_packs = nitro_packs;
	}
	
	public void setBall(MyBall ball) {
		this.ball = ball;
	}
	
	public State getState() {
		return new State(ball, robots, greenScore, redScore);
	}
	
	// todo check all nitro code when nitro is live
	public void tick() {
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
	
	private void update(float delta_time) {
		shuffle(robots);
		
		moveRobots(delta_time);
		move(ball, delta_time);
		
		collideRobotsWithThemselves();
		collideRobotsWithBallAndArena();
		collide_with_arena(ball);
		
		checkGoalScored();
		
		checkRobotsGettingNitro();
	}
	
	private void checkRobotsGettingNitro() {
		for (MyRobot robot : robots) {
			if (robot.nitro_amount == MAX_NITRO_AMOUNT) continue;
			
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
	
	private void checkGoalScored() {
		if (ball.z > arena.depth / 2 + ball.radius) {
			greenScore++;
		}
		if (ball.z < -arena.depth / 2 - ball.radius) {
			redScore++;
		}
	}
	
	private void collideRobotsWithBallAndArena() {
		for (MyRobot robot : robots) {
			collide_entities(robot, ball);
			collide_with_arena(robot);
			if (collision) {
				robot.touch = true;
				robot.touch_normal_x = collision_normal_x;
				robot.touch_normal_y = collision_normal_y;
				robot.touch_normal_z = collision_normal_z;
			} else {
				robot.touch = false;
			}
		}
	}
	
	private void moveRobots(float delta_time) {
		for (MyRobot robot : robots) {
			if (robot.touch) {
				clampSpeed(robot.target_velocity_x,
					robot.target_velocity_y,
					robot.target_velocity_z,
					ROBOT_MAX_GROUND_SPEED);
				
				double dot = robot.touch_normal_x * target_velocity_x
					+ robot.touch_normal_y * target_velocity_y
					+ robot.touch_normal_z * target_velocity_z;
				
				target_velocity_x -= robot.touch_normal_x * dot;
				target_velocity_y -= robot.touch_normal_y * dot;
				target_velocity_z -= robot.touch_normal_z * dot;
				
				double target_velocity_change_x = target_velocity_x - robot.velocity_x;
				double target_velocity_change_y = target_velocity_y - robot.velocity_y;
				double target_velocity_change_z = target_velocity_z - robot.velocity_z;
				
				double len = len(target_velocity_change_x, target_velocity_change_y, target_velocity_change_z);
				if (len > 0) {
					double acceleration = ROBOT_ACCELERATION * Math.max(0, robot.touch_normal_y);
					Vec3d v = Vec3d.normalize(target_velocity_change_x, target_velocity_change_y, target_velocity_change_z);
					double k = acceleration * delta_time;
					clampSpeed(v.x * k, v.y * k, v.z * k, len);
					
					robot.velocity_x += target_velocity_x;
					robot.velocity_y += target_velocity_y;
					robot.velocity_z += target_velocity_z;
				}
			}
			
			// todo check this code
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
			
			double len = len(b.x - a.x, b.y - a.y, b.z - a.z);
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
				- b.radius_change_speed - a.radius_change_speed;
			
			if (delta_velocity < 0) {
				double k = (1 + getUniformRandom(MIN_HIT_E, MAX_HIT_E)) * delta_velocity;
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
		dan_to_arena(e);
		double penetration = e.radius - distanceToArena;
		if (penetration > 0) {
			e.x += penetration * collision_normal_x;
			e.y += penetration * collision_normal_y;
			e.z += penetration * collision_normal_z;
			
			double velocity = e.velocity_x * collision_normal_x
				+ e.velocity_y * collision_normal_y
				+ e.velocity_z * collision_normal_z
				- e.radius_change_speed;
			
			if (velocity < 0) {
				double k = (1 + e.arena_e) * velocity;
				e.velocity_x -= k * collision_normal_x;
				e.velocity_y -= k * collision_normal_y;
				e.velocity_z -= k * collision_normal_z;
				collision = true;
			}
		}
		collision = false;
	}
	
	private void move(Entity e, float delta_time) {
		clampSpeed(e, MAX_ENTITY_SPEED);
		
		e.x += e.velocity_x * delta_time;
		e.y += e.velocity_y * delta_time;
		e.z += e.velocity_z * delta_time;
		
		e.y -= GRAVITY * delta_time * delta_time / 2;
		e.velocity_y -= GRAVITY * delta_time;
	}
	
	private void shuffle(MyRobot[] robots) {
		// todo make shuffle
	}
	
	private void dan_to_arena(Entity point) {
		boolean negate_x = point.x < 0;
		boolean negate_z = point.z < 0;
		if (negate_x) {
			point.x = -point.x;
		}
		
		if (negate_z)  {
			point.z = -point.z;
		}
		
//		let result = dan_to_arena_quarter(point);
//		if (negate_x) {
//			result.normal.x = -result.normal.x;
//		}
//
//		if (negate_z) {
//			result.normal.z = -result.normal.z
//		}
//
//		return result
		
		
		
		
		distanceToArena = 0;
		collision_normal_x = 0;
		collision_normal_y = 0;
		collision_normal_z = 0;
	}
	
	private void dan_to_plane(Vec3d point, Vec3d point_on_plane, Vec3d plane_normal) {
		
		dan_ret_distance = (point.x - point_on_plane.x) * plane_normal.x;
		dan_ret_distance += (point.y - point_on_plane.y) * plane_normal.y;
		dan_ret_distance += (point.z - point_on_plane.z) * plane_normal.z;
		
		dan_ret_normal_x = plane_normal.x;
		dan_ret_normal_y = plane_normal.y;
		dan_ret_normal_z = plane_normal.z;
	}
	
	private void dan_to_sphere_inner(Vec3d point, Vec3d sphere_center, float sphere_radius) {
		Vec3d v = point.copy();
		v.sub(sphere_center);
		dan_ret_distance = sphere_radius - v.length();
		
		v.reverse();
		v.normalize();
		dan_ret_normal_x = v.x;
		dan_ret_normal_y = v.y;
		dan_ret_normal_z = v.z;
	}
	
	private void dan_to_sphere_outer(Vec3d point, Vec3d sphere_center, float sphere_radius) {
		Vec3d v = point.copy();
		v.sub(sphere_center);
		
		dan_ret_distance = v.length() - sphere_radius;
		v.normalize();
		dan_ret_normal_x = v.x;
		dan_ret_normal_y = v.y;
		dan_ret_normal_z = v.z;
	}
	
	////////////////////////////// utility funcs
	private Vec3d clampSpeed(Entity e, double maxSpeed) {
		double speed = len(e.velocity_x, e.velocity_y, e.velocity_z);
		
		if (speed < maxSpeed) return new Vec3d(e.velocity_x, e.velocity_y, e.velocity_z);
		
		double k = maxSpeed / speed;
		e.velocity_x *= k;
		e.velocity_y *= k;
		e.velocity_z *= k;
		
		return new Vec3d(e.velocity_x, e.velocity_y, e.velocity_z);
	}
	
	// USES vars target_velocity_xyz as output variables!!!
	private Vec3d clampSpeed(double x, double y, double z, double maxSpeed) {
		double speed = len(x, y, z);
		
		if (speed < maxSpeed) return new Vec3d(x, y, z);
		
		double k = maxSpeed / speed;
		target_velocity_x = x * k;
		target_velocity_y = y * k;
		target_velocity_z = z * k;
		
		return new Vec3d(target_velocity_x, target_velocity_y, target_velocity_z);
	}
	
	private double len(double x, double y, double z) {
		return Math.sqrt(x * x + y * y + z * z);
	}
	
	private double getUniformRandom(double min, double max) {
		return rng.nextDouble() * (max - min) + min;
	}
	
}
