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
		return String.format(Locale.ENGLISH,
			"{\"Line\": {\"x1\": %.2f, \"y1\": %.2f, \"z1\": %.2f," +
				" \"x2\": %.2f, \"y2\": %.2f, \"z2\": %.2f," +
				" \"width\": %.2f, \"r\": %.2f, \"g\": %.2f, \"b\": %.2f, \"a\": %.2f }}",
			x1, y1, z1,
			x2, y2, z2,
			width, c[0], c[1], c[2], c[3]);
	}
	
	public static String getSphere(double x, double y, double z, double radius, Color color) {
		
		color.getRGBComponents(c);
		return String.format(Locale.ENGLISH,
			"{\"Sphere\": {\"x\": %.2f, \"y\": %.2f, \"z\": %.2f," +
				" \"radius\": %.2f," +
				" \"r\": %.2f, \"g\": %.2f, \"b\": %.2f, \"a\": %.2f }}",
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
