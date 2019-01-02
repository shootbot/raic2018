public class Vec3D {
    public double x;
    public double y;
    public double z;

    public Vec3D() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vec3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3D(Vec3D v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

//    public Vec3D(double angle) {
//        this.x = cos(angle);
//        this.y = sin(angle);
//    }

    public Vec3D copy() {
        return new Vec3D(this);
    }

    public Vec3D add(Vec3D v) {
        x += v.x;
        y += v.y;
        z += v.z;
        return this;
    }

    public Vec3D sub(Vec3D v) {
        x -= v.x;
        y -= v.y;
        z -= v.z;
        return this;
    }

    public Vec3D add(double dx, double dy, double dz) {
        x += dx;
        y += dy;
        z += dz;
        return this;
    }

    public Vec3D sub(double dx, double dy, double dz) {
        x -= dx;
        y -= dy;
        z -= dz;
        return this;
    }

    public Vec3D mul(double f) {
        x *= f;
        y *= f;
        z *= f;
        return this;
    }

    private double hypot(double x, double y, double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public double length() {
        return hypot(x, y, z);
//        return FastMath.hypot(x, y);
    }

    public double distance(Vec3D v) {
        return hypot(x - v.x, y - v.y, z - v.z);
//        return FastMath.hypot(x - v.x, y - v.y);
    }

    public double squareDistance(Vec3D v) {
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

    public Vec3D reverse() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    public Vec3D normalize() {
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

    public Vec3D length(double length) {
        double currentLength = this.length();
        if (currentLength == 0.0D) {
            throw new IllegalStateException("Can\'t resize zero-width vector.");
        } else {
            return this.mul(length / currentLength);
        }
    }

//    public Vec3D perpendicular() {
//        double a = y;
//        y = -x;
//        x = a;
//        return this;
//    }

    public double dotProduct(Vec3D v) {
        return x * v.x + y * v.y + z * v.z;
    }

//    public double angle() {
//        return atan2(y, x);
//    }

//    public boolean nearlyEqual(Vec3D potentialIntersectionPoint, double epsilon) {
//        return abs(x - potentialIntersectionPoint.x) < epsilon && abs(y - potentialIntersectionPoint.y) < epsilon;
//    }

//    public Vec3D rotate(Vec3D angle) {
//        double newX = angle.x * x - angle.y * y;
//        double newY = angle.y * x + angle.x * y;
//        x = newX;
//        y = newY;
//        return this;
//    }

//    public Vec3D rotateBack(Vec3D angle) {
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

    public Vec3D div(double f) {
        x /= f;
        y /= f;
        z /= f;
        return this;
    }

    public Vec3D copyFrom(Vec3D position) {
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
        return this;
    }
}
