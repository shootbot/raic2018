package com.shootbot.raic2018.codeball.test;

import com.shootbot.raic2018.codeball.*;
import com.shootbot.raic2018.codeball.model.*;
import com.shootbot.raic2018.codeball.model.Robot;


public class JsonTest implements Strategy {
	public void act(Robot me, Rules rules, Game game, Action action) {
	
	}
	
	public String customRendering() {
		String json = "[" +
			"  {" +
			"    \"Sphere\": {" +
			"      \"x\": 2.0," +
			"      \"y\": 5.0," +
			"      \"z\": 10.0," +
			"      \"radius\": 2.0," +
			"      \"r\": 0.25," +
			"      \"g\": 0.25," +
			"      \"b\": 0.25," +
			"      \"a\": 0.5" +
			"    }" +
			"  }," +
			"  {" +
			"    \"Text\": \"Debug text #0\"" +
			"  }," +
			"  {" +
			"    \"Line\": {" +
			"      \"x1\": 0.0," +
			"      \"y1\": 0.0," +
			"      \"z1\": 0.0," +
			"      \"x2\": 10.0," +
			"      \"y2\": 20.0," +
			"      \"z2\": 30.0," +
			"       \"width\": 1.0," +
			"       \"r\": 1.0," +
			"       \"g\": 1.0," +
			"       \"b\": 1.0," +
			"       \"a\": 1.0" +
			"    }" +
			"  }" +
			"]";
		return json;
	}
	
	
}
