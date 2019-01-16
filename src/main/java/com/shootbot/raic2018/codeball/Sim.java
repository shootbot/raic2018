package com.shootbot.raic2018.codeball;


public class Sim {
	private MyRobot[] robots;
	private MyNitroPack[] nitro_packs;
	private MyBall ball;
	private int greenScore;
	private int redScore;
	
	private boolean collision;
	
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
	public static final int MICROTICKS_PER_TICK = 100;
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
	
	//////////////////////////////////////////////////////////////////////////////////
	// output variables
	private double dan_ret_distance;
	private double dan_ret_normal_x;
	private double dan_ret_normal_y;
	private double dan_ret_normal_z;
	
	// for clamp_speed(action.target_velocity)
	private double target_velocity_x;
	private double target_velocity_y;
	private double target_velocity_z;
	
	
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
		if (ball.z > ARENA_DEPTH / 2 + ball.radius) {
			greenScore++;
		}
		if (ball.z < -ARENA_DEPTH / 2 - ball.radius) {
			redScore++;
		}
	}
	
	private void collideRobotsWithBallAndArena() {
		for (MyRobot robot : robots) {
			collide_entities(robot, ball);
			collide_with_arena(robot);
			if (collision) {
				robot.touch = true;
				robot.touch_normal_x = dan_ret_normal_x;
				robot.touch_normal_y = dan_ret_normal_y;
				robot.touch_normal_z = dan_ret_normal_z;
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
				
				double len = Ut.len(target_velocity_change_x, target_velocity_change_y, target_velocity_change_z);
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
				- b.radius_change_speed - a.radius_change_speed;
			
			if (delta_velocity < 0) {
				double k = (1 + Ut.getUniformRandom(MIN_HIT_E, MAX_HIT_E)) * delta_velocity;
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
		dan_to_arena(new Vec3d(e.x, e.y, e.z));
		double penetration = e.radius - dan_ret_distance;
		if (penetration > 0) {
			e.x += penetration * dan_ret_normal_x;
			e.y += penetration * dan_ret_normal_y;
			e.z += penetration * dan_ret_normal_z;
			
			double velocity = e.velocity_x * dan_ret_normal_x
				+ e.velocity_y * dan_ret_normal_y
				+ e.velocity_z * dan_ret_normal_z
				- e.radius_change_speed;
			
			if (velocity < 0) {
				double k = (1 + e.arena_e) * velocity;
				e.velocity_x -= k * dan_ret_normal_x;
				e.velocity_y -= k * dan_ret_normal_y;
				e.velocity_z -= k * dan_ret_normal_z;
				collision = true;
			}
		}
		collision = false;
	}
	
	private void move(Entity e, double delta_time) {
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
	
	public Dan dan_to_arena(Vec3d point) {
		boolean negate_x = point.x < 0;
		boolean negate_z = point.z < 0;
		if (negate_x) {
			point.x = -point.x;
		}
		
		if (negate_z) {
			point.z = -point.z;
		}
		
		Dan result = dan_to_arena_quarter(new Vec3d(point.x, point.y, point.z));
		if (negate_x) {
			result.normal.x = -result.normal.x;
		}
		
		if (negate_z) {
			result.normal.z = -result.normal.z;
		}
		
		dan_ret_distance = result.distance;
		dan_ret_normal_x = result.normal.x;
		dan_ret_normal_y = result.normal.y;
		dan_ret_normal_z = result.normal.z;
		
		return result;
	}
	
	private Dan dan_to_plane(Vec3d point, Vec3d point_on_plane, Vec3d plane_normal) {
		dan_ret_distance = (point.x - point_on_plane.x) * plane_normal.x;
		dan_ret_distance += (point.y - point_on_plane.y) * plane_normal.y;
		dan_ret_distance += (point.z - point_on_plane.z) * plane_normal.z;
		
		dan_ret_normal_x = plane_normal.x;
		dan_ret_normal_y = plane_normal.y;
		dan_ret_normal_z = plane_normal.z;
		
		return new Dan(dan_ret_distance, new Vec3d(dan_ret_normal_x, dan_ret_normal_y, dan_ret_normal_z));
	}
	
	private Dan dan_to_sphere_inner(Vec3d point, Vec3d sphere_center, double sphere_radius) {
		Vec3d v = point.copy();
		v.sub(sphere_center);
		dan_ret_distance = sphere_radius - v.length();
		
		v.reverse();
		v.normalize();
		dan_ret_normal_x = v.x;
		dan_ret_normal_y = v.y;
		dan_ret_normal_z = v.z;
		
		return new Dan(dan_ret_distance, new Vec3d(dan_ret_normal_x, dan_ret_normal_y, dan_ret_normal_z));
	}
	
	private Dan dan_to_sphere_outer(Vec3d point, Vec3d sphere_center, double sphere_radius) {
		Vec3d v = point.copy();
		v.sub(sphere_center);
		
		dan_ret_distance = v.length() - sphere_radius;
		v.normalize();
		dan_ret_normal_x = v.x;
		dan_ret_normal_y = v.y;
		dan_ret_normal_z = v.z;
		
		return new Dan(dan_ret_distance, new Vec3d(dan_ret_normal_x, dan_ret_normal_y, dan_ret_normal_z));
	}
	
	private Dan dan_to_arena_quarter(Vec3d point) {
		// Ground
		Dan dan = dan_to_plane(point, new Vec3d(0, 0, 0), new Vec3d(0, 1, 0));
		// Ceiling
		dan = min(dan, dan_to_plane(point, new Vec3d(0, ARENA_HEIGHT, 0), new Vec3d(0, -1, 0)));
		// Side x
		dan = min(dan, dan_to_plane(point, new Vec3d(ARENA_WIDTH / 2, 0, 0), new Vec3d(-1, 0, 0)));
		// Side z (goal)
		dan = min(dan, dan_to_plane(
			point,
			new Vec3d(0, 0, (ARENA_DEPTH / 2) + GOAL_DEPTH),
			new Vec3d(0, 0, -1)));
		// Side z
		Vec2d v = new Vec2d(point.x, point.y)
			.sub(new Vec2d((GOAL_WIDTH / 2) - GOAL_TOP_RADIUS, GOAL_HEIGHT - GOAL_TOP_RADIUS));
		if (point.x >= (GOAL_WIDTH / 2) + GOAL_SIDE_RADIUS
			|| point.y >= GOAL_HEIGHT + GOAL_SIDE_RADIUS
			|| (
			v.x > 0
				&& v.y > 0
				&& v.length() >= GOAL_TOP_RADIUS + GOAL_SIDE_RADIUS)) {
			dan = min(dan, dan_to_plane(
				point,
				new Vec3d(0, 0, ARENA_DEPTH / 2),
				new Vec3d(0, 0, -1)));
		}
		
		// Side x & ceiling (goal)
		if (point.z >= (ARENA_DEPTH / 2) + GOAL_SIDE_RADIUS) {
			// x
			dan = min(dan, dan_to_plane(
				point,
				new Vec3d(GOAL_WIDTH / 2, 0, 0),
				new Vec3d(-1, 0, 0)));
			// y
			dan = min(dan, dan_to_plane(
				point,
				new Vec3d(0, GOAL_HEIGHT, 0),
				new Vec3d(0, -1, 0)));
		}
		
		// Goal back corners
		assert ARENA_BOTTOM_RADIUS == GOAL_TOP_RADIUS;
		if (point.z > (ARENA_DEPTH / 2) + GOAL_DEPTH - ARENA_BOTTOM_RADIUS) {
			dan = min(dan, dan_to_sphere_inner(
				point,
				new Vec3d(
					clamp(
						point.x,
						ARENA_BOTTOM_RADIUS - (GOAL_WIDTH / 2),
						(GOAL_WIDTH / 2) - ARENA_BOTTOM_RADIUS),
					clamp(
						point.y,
						ARENA_BOTTOM_RADIUS,
						GOAL_HEIGHT - GOAL_TOP_RADIUS),
					(ARENA_DEPTH / 2) + GOAL_DEPTH - ARENA_BOTTOM_RADIUS),
				ARENA_BOTTOM_RADIUS));
		}
		
		// Corner
		if (point.x > (ARENA_WIDTH / 2) - ARENA_CORNER_RADIUS
			&& point.z > (ARENA_DEPTH / 2) - ARENA_CORNER_RADIUS) {
			dan = min(dan, dan_to_sphere_inner(
				point,
				new Vec3d(
					(ARENA_WIDTH / 2) - ARENA_CORNER_RADIUS,
					point.y,
					(ARENA_DEPTH / 2) - ARENA_CORNER_RADIUS),
				ARENA_CORNER_RADIUS));
		}
		
		// Goal outer corner
		if (point.z < (ARENA_DEPTH / 2) + GOAL_SIDE_RADIUS) {
			// Side x
			if (point.x < (GOAL_WIDTH / 2) + GOAL_SIDE_RADIUS) {
				dan = min(dan, dan_to_sphere_outer(
					point,
					new Vec3d(
						(GOAL_WIDTH / 2) + GOAL_SIDE_RADIUS,
						point.y,
						(ARENA_DEPTH / 2) + GOAL_SIDE_RADIUS),
					GOAL_SIDE_RADIUS));
			}
			
			// Ceiling
			if (point.y < GOAL_HEIGHT + GOAL_SIDE_RADIUS) {
				dan = min(dan, dan_to_sphere_outer(
					point,
					new Vec3d(
						point.x,
						GOAL_HEIGHT + GOAL_SIDE_RADIUS,
						(ARENA_DEPTH / 2) + GOAL_SIDE_RADIUS),
					GOAL_SIDE_RADIUS));
			}
			
			// Top corner
			Vec2d o = new Vec2d(
				(GOAL_WIDTH / 2) - GOAL_TOP_RADIUS,
				GOAL_HEIGHT - GOAL_TOP_RADIUS);
			Vec2d u = new Vec2d(point.x, point.y).sub(o);
			if (u.x > 0 && u.y > 0) {
				o.add(u.normalize().mul(GOAL_TOP_RADIUS + GOAL_SIDE_RADIUS));
				dan = min(dan, dan_to_sphere_outer(
					point,
					new Vec3d(o.x, o.y, (ARENA_DEPTH / 2) + GOAL_SIDE_RADIUS),
					GOAL_SIDE_RADIUS));
			}
		}
		
		
		// Goal inside top corners
		if (point.z > (ARENA_DEPTH / 2) + GOAL_SIDE_RADIUS
			&& point.y > GOAL_HEIGHT - GOAL_TOP_RADIUS) {
			// Side x
			if (point.x > (GOAL_WIDTH / 2) - GOAL_TOP_RADIUS) {
				dan = min(dan, dan_to_sphere_inner(
					point,
					new Vec3d(
						(GOAL_WIDTH / 2) - GOAL_TOP_RADIUS,
						GOAL_HEIGHT - GOAL_TOP_RADIUS,
						point.z),
					GOAL_TOP_RADIUS));
			}
			
			// Side z
			if (point.z > (ARENA_DEPTH / 2) + GOAL_DEPTH - GOAL_TOP_RADIUS) {
				dan = min(dan, dan_to_sphere_inner(
					point,
					new Vec3d(
						
						point.x,
						GOAL_HEIGHT - GOAL_TOP_RADIUS,
						(ARENA_DEPTH / 2) + GOAL_DEPTH - GOAL_TOP_RADIUS),
					GOAL_TOP_RADIUS));
			}
		}
		
		
		// Bottom corners
		if (point.y < ARENA_BOTTOM_RADIUS) {
			// Side x
			if (point.x > (ARENA_WIDTH / 2) - ARENA_BOTTOM_RADIUS) {
				dan = min(dan, dan_to_sphere_inner(
					point,
					new Vec3d(
						(ARENA_WIDTH / 2) - ARENA_BOTTOM_RADIUS,
						ARENA_BOTTOM_RADIUS,
						point.z
					),
					ARENA_BOTTOM_RADIUS));
			}
			
			// Side z
			if (point.z > (ARENA_DEPTH / 2) - ARENA_BOTTOM_RADIUS
				&& point.x >= (GOAL_WIDTH / 2) + GOAL_SIDE_RADIUS) {
				dan = min(dan, dan_to_sphere_inner(
					point,
					new Vec3d(
						point.x,
						ARENA_BOTTOM_RADIUS,
						(ARENA_DEPTH / 2) - ARENA_BOTTOM_RADIUS
					),
					ARENA_BOTTOM_RADIUS));
			}
			
			// Side z (goal)
			if (point.z > (ARENA_DEPTH / 2) + GOAL_DEPTH - ARENA_BOTTOM_RADIUS) {
				dan = min(dan, dan_to_sphere_inner(
					point,
					new Vec3d(
						point.x,
						ARENA_BOTTOM_RADIUS,
						(ARENA_DEPTH / 2) + GOAL_DEPTH - ARENA_BOTTOM_RADIUS
					),
					ARENA_BOTTOM_RADIUS));
			}
			
			// Goal outer corner
			Vec2d o = new Vec2d(
				(GOAL_WIDTH / 2) + GOAL_SIDE_RADIUS,
				(ARENA_DEPTH / 2) + GOAL_SIDE_RADIUS);
			Vec2d u = new Vec2d(point.x, point.z).sub(o);
			if (u.x < 0 && v.y < 0
				&& u.length() < GOAL_SIDE_RADIUS + ARENA_BOTTOM_RADIUS) {
				o.add(u.normalize().mul(GOAL_SIDE_RADIUS + ARENA_BOTTOM_RADIUS));
				dan = min(dan, dan_to_sphere_inner(
					point,
					new Vec3d(o.x, ARENA_BOTTOM_RADIUS, o.y),
					ARENA_BOTTOM_RADIUS));
			}
			
			// Side x (goal)
			if (point.z >= (ARENA_DEPTH / 2) + GOAL_SIDE_RADIUS
				&& point.x > (GOAL_WIDTH / 2) - ARENA_BOTTOM_RADIUS) {
				dan = min(dan, dan_to_sphere_inner(
					point,
					new Vec3d(
						(GOAL_WIDTH / 2) - ARENA_BOTTOM_RADIUS,
						ARENA_BOTTOM_RADIUS,
						point.z
					),
					ARENA_BOTTOM_RADIUS));
			}
			
			// Corner
			if (point.x > (ARENA_WIDTH / 2) - ARENA_CORNER_RADIUS
				&& point.z > (ARENA_DEPTH / 2) - ARENA_CORNER_RADIUS) {
				Vec2d corner_o = new Vec2d(
					(ARENA_WIDTH / 2) - ARENA_CORNER_RADIUS,
					(ARENA_DEPTH / 2) - ARENA_CORNER_RADIUS);
				Vec2d n = new Vec2d(point.x, point.z).sub(corner_o);
				double dist = n.length();
				if (dist > ARENA_CORNER_RADIUS - ARENA_BOTTOM_RADIUS) {
					n.div(dist);
					corner_o.add(n.mul(ARENA_CORNER_RADIUS - ARENA_BOTTOM_RADIUS));
					dan = min(dan, dan_to_sphere_inner(
						point,
						new Vec3d(corner_o.x, ARENA_BOTTOM_RADIUS, corner_o.y),
						ARENA_BOTTOM_RADIUS));
				}
				
			}
			
		}
		
		// Ceiling corners
		if (point.y > ARENA_HEIGHT - ARENA_TOP_RADIUS) {
			// Side x
			if (point.x > (ARENA_WIDTH / 2) - ARENA_TOP_RADIUS) {
				dan = min(dan, dan_to_sphere_inner(
					point,
					new Vec3d(
						(ARENA_WIDTH / 2) - ARENA_TOP_RADIUS,
						ARENA_HEIGHT - ARENA_TOP_RADIUS,
						point.z
					),
					ARENA_TOP_RADIUS));
			}
			
			// Side z
			if (point.z > (ARENA_DEPTH / 2) - ARENA_TOP_RADIUS) {
				dan = min(dan, dan_to_sphere_inner(
					point,
					new Vec3d(
						point.x,
						ARENA_HEIGHT - ARENA_TOP_RADIUS,
						(ARENA_DEPTH / 2) - ARENA_TOP_RADIUS
					),
					ARENA_TOP_RADIUS));
			}
			
			// Corner
			if (point.x > (ARENA_WIDTH / 2) - ARENA_CORNER_RADIUS
				&& point.z > (ARENA_DEPTH / 2) - ARENA_CORNER_RADIUS) {
				Vec2d corner_o = new Vec2d(
					(ARENA_WIDTH / 2) - ARENA_CORNER_RADIUS,
					(ARENA_DEPTH / 2) - ARENA_CORNER_RADIUS);
				Vec2d dv = new Vec2d(point.x, point.z).sub(corner_o);
				if (dv.length() > ARENA_CORNER_RADIUS - ARENA_TOP_RADIUS) {
					dv.normalize();
					corner_o.add(dv.mul((ARENA_CORNER_RADIUS - ARENA_TOP_RADIUS)));
					dan = min(dan, dan_to_sphere_inner(
						point,
						new Vec3d(corner_o.x, ARENA_HEIGHT - ARENA_TOP_RADIUS, corner_o.y),
						ARENA_TOP_RADIUS));
				}
			}
		}
		
		return dan;
	}
	
	private double clamp(double x, double min, double max) {
		if (x < min) return min;
		if (x > max) return max;
		return x;
	}
	
	////////////////////////////// utility funcs
	private Dan min(Dan a, Dan b) {
		return a.distance < b.distance ? a : b;
	}
	
	private Vec3d clampSpeed(Entity e, double maxSpeed) {
		double speed = Ut.len(e.velocity_x, e.velocity_y, e.velocity_z);
		
		if (speed < maxSpeed) return new Vec3d(e.velocity_x, e.velocity_y, e.velocity_z);
		
		double k = maxSpeed / speed;
		e.velocity_x *= k;
		e.velocity_y *= k;
		e.velocity_z *= k;
		
		return new Vec3d(e.velocity_x, e.velocity_y, e.velocity_z);
	}
	
	// USES vars target_velocity_xyz as output variables!!!
	private Vec3d clampSpeed(double x, double y, double z, double maxSpeed) {
		double speed = Ut.len(x, y, z);
		
		if (speed < maxSpeed) return new Vec3d(x, y, z);
		
		double k = maxSpeed / speed;
		target_velocity_x = x * k;
		target_velocity_y = y * k;
		target_velocity_z = z * k;
		
		return new Vec3d(target_velocity_x, target_velocity_y, target_velocity_z);
	}
	
	
}
