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

	private static double hypot(double x, double y, double z) {
		return Math.sqrt(x * x + y * y + z * z);
	}

	public double length() {
		return hypot(x, y, z);
	}

	public double distance(Vec3d v) {
		return hypot(x - v.x, y - v.y, z - v.z);
	}

	public double lenSq() {
		return x * x + y * y + z * z;
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
	
	public static Vec3d normalize(double x, double y, double z) {
		double length = hypot(x, y, z);
		if (length == 0.0D) {
			throw new IllegalStateException("Can\'t set angle of zero-width vector.");
		} else {
			return new Vec3d(x / length, y / length, z / length);
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

    public Vec3d perpendicular2d() {
        double a = z;
        z = -x;
        x = a;
        return this;
    }

	public double dotProduct(Vec3d v) {
		return x * v.x + y * v.y + z * v.z;
	}
	

    public double angle() {
        return Math.atan2(z, x);
    }

//    public boolean nearlyEqual(Vec3d potentialIntersectionPoint, double epsilon) {
//        return abs(x - potentialIntersectionPoint.x) < epsilon && abs(y - potentialIntersectionPoint.y) < epsilon;
//    }

    public Vec3d rotate(Vec3d angle) {
        double newX = angle.x * x - angle.z * z;
        double newZ = angle.z * x + angle.x * z;
        x = newX;
        z = newZ;
        return this;
    }

    public Vec3d rotateBack(Vec3d angle) {
        double newX = angle.x * x + angle.z * z;
        double newZ = angle.x * z - angle.z * x;
        x = newX;
        z = newZ;
        return this;
    }

	@Override
	public String toString() {
		return String.format("(%.2f %.2f %.2f)", x, y, z);
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Vec3d)) return false;
		
		Vec3d o = (Vec3d) other;
		if (o.x == this.x && o.y == this.y && o.z == this.z) {
			return true;
		}
		
		return false;
	}

	
}
