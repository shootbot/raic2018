package com.shootbot.raic2018.codeball;

import com.shootbot.raic2018.codeball.model.Robot;

import java.awt.*;
import java.util.*;

public class TurnDraw {
	private boolean didntDraw = true;
	
	Vec3d curBall;
	Map<Integer, Vec3d> curRobots = new HashMap<>();
	State nextS;
	static StringBuilder msg = new StringBuilder(1_000);
	
	public TurnDraw(Vec3d ball, Robot[] robots) {
		curBall = ball.copy();
		
		for (Robot r : robots) {
			curRobots.put(r.id, new Vec3d(r.x, r.y, r.z));
		}
		
		msg.setLength(0);
	}
	
	public void setNextState(State state) {
		nextS = state;
		
		drawBall();
		if (didntDraw) {
			drawRobots();
		}
		
		didntDraw = false;
		saveState();
	}
	
	public StringBuilder getDrawLine() {
		return msg;
	}
	
	private void drawBall() {
		String line = Ut.getLine(curBall.x, curBall.y, curBall.z,
			nextS.ball.x, nextS.ball.y, nextS.ball.z,
			3, Color.RED);

		if (!didntDraw) {
			msg.append(',');
		}
		
		msg.append(line);
	}
	
	private void drawRobots() {
		for (MyRobot mr : nextS.robots) {
			Vec3d r = curRobots.get(mr.id);
			double dx = mr.x - r.x;
			double dy = mr.y - r.y;
			double dz = mr.z - r.z;
			
			double newX = r.x + 60 * dx;
			double newY = r.y + 60 * dy;
			double newZ = r.z + 60 * dz;
			String line = Ut.getLine(r.x, r.y, r.z,
				newX, newY, newZ,
				2, Color.BLUE);
			
			msg.append(',').append(line);
		}
	}
	
	private void saveState() {
		curBall = new Vec3d(nextS.ball.x, nextS.ball.y, nextS.ball.z);
		for (MyRobot mr : nextS.robots) {
			curRobots.put(mr.id, new Vec3d(mr.x, mr.y, mr.z));
		}
	}
	
}
