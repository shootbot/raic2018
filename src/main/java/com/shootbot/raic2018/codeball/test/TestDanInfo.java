package com.shootbot.raic2018.codeball.test;


import com.shootbot.raic2018.codeball.*;

public class TestDanInfo {
	// Центр сферы, откуда производился поиск до арены
	public Vec3d point;
	
	// Валидные значения, которые должны получиться
	public Dan validDan;
	
	public TestDanInfo(Vec3d point, Dan validDan) {
		this.point = point;
		this.validDan = validDan;
	}
	
	@Override
	public String toString() {
		return "{point=" + point + ", dan=" + validDan + "}";
	}
}
