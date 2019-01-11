

public class Entity {
    public double arena_e;

    public double x;
    public double y;
    public double z;
    
    public double velocity_x;
    public double velocity_y;
    public double velocity_z;
    
    public double radius;
    public double mass;
    public double radius_change_speed;

    @Override
    public String toString() {
        return String.format("{(%.2f %.2f %.2f)->(%.2f %.2f %.2f)}", x, y, z, velocity_x, velocity_y, velocity_z);
    }

}
