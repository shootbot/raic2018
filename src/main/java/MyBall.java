public class MyBall extends Entity {
    public MyBall(Vec3d ball, Vec3d speed) {
        this.x = ball.x;
        this.y = ball.y;
        this.z = ball.z;

        this.velocity_x = speed.x;
        this.velocity_y = speed.y;
        this.velocity_z = speed.z;

        this.arena_e = Sim.BALL_ARENA_E;
        this.radius = Sim.BALL_RADIUS;
        this.mass = Sim.BALL_MASS;
    }
}
