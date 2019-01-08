public class Vec3d {
    public double x;
    public double y;
    public double z;

    public Vec3d() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vec3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3d(Vec3d v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public Vec3d set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }
    
    public Vec3d set(Vec3d position) {
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
        return this;
    }

//    public Vec3d(double angle) {
//        this.x = cos(angle);
//        this.y = sin(angle);
//    }

    public Vec3d copy() {
        return new Vec3d(this);
    }

    public Vec3d add(Vec3d v) {
        x += v.x;
        y += v.y;
        z += v.z;
        return this;
    }

    public Vec3d sub(Vec3d v) {
        x -= v.x;
        y -= v.y;
        z -= v.z;
        return this;
    }

    public Vec3d add(double dx, double dy, double dz) {
        x += dx;
        y += dy;
        z += dz;
        return this;
    }

    public Vec3d sub(double dx, double dy, double dz) {
        x -= dx;
        y -= dy;
        z -= dz;
        return this;
    }

    public Vec3d mul(double f) {
        x *= f;
        y *= f;
        z *= f;
        return this;
    }
    
    public Vec3d div(double f) {
        x /= f;
        y /= f;
        z /= f;
        return this;
    }

    private double hypot(double x, double y, double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public double length() {
        return hypot(x, y, z);
//        return FastMath.hypot(x, y);
    }

    public double distance(Vec3d v) {
        return hypot(x - v.x, y - v.y, z - v.z);
//        return FastMath.hypot(x - v.x, y - v.y);
    }

    public double squareDistance(Vec3d v) {
        double tx = x - v.x;
        double ty = y - v.y;
        double tz = z - v.z;
        return tx * tx + ty * ty + tz * tz;
    }

    public double squareDistance(double x, double y, double z) {
        double tx = this.x - x;
        double ty = this.y - y;
        double tz = this.z - z;
        return tx * tx + ty * ty + tz * tz;
    }

    public double squareLength() {
        return x * x + y * y + z * z;
    }

    public Vec3d reverse() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    public Vec3d normalize() {
        double length = length();
        if (length == 0.0D) {
            throw new IllegalStateException("Can\'t set angle of zero-width vector.");
        } else {
            x /= length;
            y /= length;
            z /= length;
            return this;
        }
    }

    public Vec3d setLength(double length) {
        double currentLength = length();
        if (currentLength == 0.0D) {
            throw new IllegalStateException("Can\'t resize zero-width vector.");
        } else {
            return this.mul(length / currentLength);
        }
    }

//    public Vec3d perpendicular() {
//        double a = y;
//        y = -x;
//        x = a;
//        return this;
//    }

    public double dotProduct(Vec3d v) {
        return x * v.x + y * v.y + z * v.z;
    }

//    public double angle() {
//        return atan2(y, x);
//    }

//    public boolean nearlyEqual(Vec3d potentialIntersectionPoint, double epsilon) {
//        return abs(x - potentialIntersectionPoint.x) < epsilon && abs(y - potentialIntersectionPoint.y) < epsilon;
//    }

//    public Vec3d rotate(Vec3d angle) {
//        double newX = angle.x * x - angle.y * y;
//        double newY = angle.y * x + angle.x * y;
//        x = newX;
//        y = newY;
//        return this;
//    }

//    public Vec3d rotateBack(Vec3d angle) {
//        double newX = angle.x * x + angle.y * y;
//        double newY = angle.x * y - angle.y * x;
//        x = newX;
//        y = newY;
//        return this;
//    }

    @Override
    public String toString() {
        return String.format("(%.2f %.2f %.2f)", x, y, z);
    }

    

    
}
