import model.*;

public class MyRobot extends Entity {
	public int id;
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
		this.id = r.id;
		
		this.x = r.x;
		this.y = r.y;
		this.z = r.z;
		
		this.velocity_x = r.velocity_x;
		this.velocity_y = r.velocity_y;
		this.velocity_z = r.velocity_z;

		this.arena_e = Sim.ROBOT_ARENA_E;
		this.radius = Sim.ROBOT_RADIUS;
		this.mass = Sim.ROBOT_MASS;
	}
}
