import com.shootbot.raic2018.codeball.*;
import com.shootbot.raic2018.codeball.model.*;
import org.junit.jupiter.api.*;

public class CheckSim {
	private static final int TICKS = 1000_000;
	
	@Test
	void test() {
		Sim sim = new Sim();
		sim.setBall(new MyBall(new Vec3d(0, 1, 0), new Vec3d(0, 0, 0)));
		MyRobot[] robots = genRobots();
		sim.setRobots(robots);
		sim.setNitro_packs(new MyNitroPack[0]);
		
		long totalTime = 0;
		for (int i = 0; i < TICKS; i++) {
			for (MyRobot mr : robots) {
				changeRobotsSpeed(mr);
			}
			long start = System.nanoTime();
			sim.tick();
			totalTime += System.nanoTime() - start;
		}
		System.out.println("time: " + totalTime / 1000_000 + "ms");
		System.out.println("time per tick: " + totalTime / TICKS + "ns");
		System.out.println("ticks per 20ms: " + 20_000_000 / (totalTime / TICKS));
		
	}
	
	private MyRobot[] genRobots() {
		MyRobot[] robots = new MyRobot[4];
		double x = Ut.getUniformRandom(5, 20);
		double z = Ut.getUniformRandom(5, 30);
		robots[0] = makeRobot(x, z);
		robots[1] = makeRobot(-x, z);
		robots[2] = makeRobot(x, -z);
		robots[3] = makeRobot(-x, -z);
		return robots;
	}
	
	private MyRobot makeRobot(double x, double z) {
		Robot r = new Robot();
		r.x = x;
		r.z = z;
		r.y = 0.5;
		return new MyRobot(r);
	}
	
	private void changeRobotsSpeed(MyRobot mr) {
		mr.target_velocity_x = Ut.getUniformRandom(-30, 30);
		mr.target_velocity_z = Ut.getUniformRandom(-30, 30);
		if (Ut.nextBoolean()) {
			mr.jump_speed = Ut.getUniformRandom(0, 15);
		} else {
			mr.jump_speed = 0;
		}
		
	}
}
