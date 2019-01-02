package geometry;

import org.apache.commons.math3.util.FastMath;

import static java.lang.Math.*;

public class Vec2D {
    public double x;
    public double y;

    public Vec2D() {
        x = 0;
        y = 0;
    }

    public Vec2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2D(Vec2D v) {
        this.x = v.x;
        this.y = v.y;
    }

    public Vec2D(double angle) {
        this.x = cos(angle);
        this.y = sin(angle);
    }

    public Vec2D copy() {
        return new Vec2D(this);
    }

    public Vec2D add(Vec2D v) {
        x += v.x;
        y += v.y;
        return this;
    }

    public Vec2D sub(Vec2D v) {
        x -= v.x;
        y -= v.y;
        return this;
    }

    public Vec2D add(double dx, double dy) {
        x += dx;
        y += dy;
        return this;
    }

    public Vec2D sub(double dx, double dy) {
        x -= dx;
        y -= dy;
        return this;
    }

    public Vec2D mul(double f) {
        x *= f;
        y *= f;
        return this;
    }

    public double length() {
//        return hypot(x, y);
        return FastMath.hypot(x, y);
    }

    public double distance(Vec2D v) {

//        return hypot(x - v.x, y - v.y);
        return FastMath.hypot(x - v.x, y - v.y);
    }

    public double squareDistance(Vec2D v) {
        double tx = x - v.x;
        double ty = y - v.y;
        return tx * tx + ty * ty;
    }

    public double squareDistance(double x, double y) {
        double tx = this.x - x;
        double ty = this.y - y;
        return tx * tx + ty * ty;
    }

    public double squareLength() {
        return x * x + y * y;
    }

    public Vec2D reverse() {
        x = -x;
        y = -y;
        return this;
    }

    public Vec2D normalize() {
        double length = this.length();
        if (length == 0.0D) {
            throw new IllegalStateException("Can\'t set angle of zero-width vector.");
        } else {
            x /= length;
            y /= length;
            return this;
        }
    }

    public Vec2D length(double length) {
        double currentLength = this.length();
        if (currentLength == 0.0D) {
            throw new IllegalStateException("Can\'t resize zero-width vector.");
        } else {
            return this.mul(length / currentLength);
        }
    }

    public Vec2D perpendicular() {
        double a = y;
        y = -x;
        x = a;
        return this;
    }

    public double dotProduct(Vec2D vector) {
        return x * vector.x + y * vector.y;
    }

    public double angle() {
        return atan2(y, x);
    }

    public boolean nearlyEqual(Vec2D potentialIntersectionPoint, double epsilon) {
        return abs(x - potentialIntersectionPoint.x) < epsilon && abs(y - potentialIntersectionPoint.y) < epsilon;
    }

    public Vec2D rotate(Vec2D angle) {
        double newX = angle.x * x - angle.y * y;
        double newY = angle.y * x + angle.x * y;
        x = newX;
        y = newY;
        return this;
    }

    public Vec2D rotateBack(Vec2D angle) {
        double newX = angle.x * x + angle.y * y;
        double newY = angle.x * y - angle.y * x;
        x = newX;
        y = newY;
        return this;
    }

    @Override
    public String toString() {
        return "Vec2D ("
                + String.valueOf(x) + "; "
                + String.valueOf(y) + ")";
    }

    public Vec2D div(double f) {
        x /= f;
        y /= f;
        return this;
    }

    public Vec2D copyFrom(Vec2D position) {
        this.x = position.x;
        this.y = position.y;
        return this;
    }
}
