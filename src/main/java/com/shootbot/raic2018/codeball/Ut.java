package com.shootbot.raic2018.codeball;

import java.awt.*;
import java.util.*;

public class Ut {
	private static float[] c = new float[4];
	private static Random rng = new Random();
	
	public static double dist(Entity a, Entity b) {
		double dx = a.x - b.x;
		double dy = a.y - b.y;
		double dz = a.z - b.z;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	public static double dist(Vec3d a, Vec3d b) {
		double dx = a.x - b.x;
		double dy = a.y - b.y;
		double dz = a.z - b.z;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	public static double dist2d(Vec3d a, Vec3d b) {
		double dx = a.x - b.x;
		double dz = a.z - b.z;
		return Math.sqrt(dx * dx + dz * dz);
	}
	
	public static String getLine(double x1, double y1, double z1,
		double x2, double y2, double z2,
		double width, Color color) {
		
		color.getRGBComponents(c);
		return String.format("{\"Line\": {\"x1\": %.1f, \"y1\": %.1f, \"z1\": %.1f," +
				" \"x2\": %.1f, \"y2\": %.1f, \"z2\": %.1f," +
				" \"width\": %.1f, \"r\": %.1f, \"g\": %.1f, \"b\": %.1f, \"a\": %.1f }}",
			x1, y1, z1,
			x2, y2, z2,
			width, c[0], c[1], c[2], c[3]);
	}
	
	public static String getSphere(double x, double y, double z, double radius, Color color) {
		
		color.getRGBComponents(c);
		return String.format("{\"Sphere\": {\"x\": %.1f, \"y\": %.1f, \"z\": %.1f," +
				" \"radius\": %.1f," +
				" \"r\": %.1f, \"g\": %.1f, \"b\": %.1f, \"a\": %.1f }}",
			x, y, z,
			radius,
			c[0], c[1], c[2], c[3]);
	}
	
	public static double len(double x, double y, double z) {
		return Math.sqrt(x * x + y * y + z * z);
	}
	
	public static double getUniformRandom(double min, double max) {
		return rng.nextDouble() * (max - min) + min;
	}
	
	public static boolean nextBoolean() {
		return rng.nextBoolean();
	}
}
