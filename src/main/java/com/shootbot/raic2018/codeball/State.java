package com.shootbot.raic2018.codeball;

public class State {
	MyBall ball;
	MyRobot[] robots;
	int greenScore;
	int redScore;
	
	public State(MyBall ball, MyRobot[] robots, int greenScore, int redScore) {
		this.ball = ball;
		this.robots = robots;
		this.greenScore = greenScore;
		this.redScore = redScore;
	}
}
