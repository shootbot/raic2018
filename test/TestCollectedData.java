import org.junit.jupiter.api.*;


public class TestCollectedData {
	
	@Test
	public void test_sim_dan_to_arena() {
		double EPS = 1e-12;
		
		Sim sim = new Sim();
		_CollectedData cd = new _CollectedData();
		int count = 0;
		for (_DanInfo di : cd.data) {
			sim.dan_to_arena_to_A(di.point.copy());
			Dan resDan = sim.getResultDan();
			di.validDan.normal.normalize();
			
			if (!equalsWithEps(resDan, di.validDan, EPS)) {
				System.out.println("data" + di.point + "expected" + di.validDan + " actual" + resDan);
				count++;
			}
		}
		System.out.println("total: "  + cd.data.length + " wrong: " + count);
		assert count == 0;
	}
	
	private boolean equalsWithEps(Dan dan1, Dan dan2, double eps) {
		if (!equalsWithEps(dan1.distance, dan2.distance, eps)) return false;
		
		if (!equalsWithEps(dan1.normal.x, dan2.normal.x, eps)) return false;
		if (!equalsWithEps(dan1.normal.y, dan2.normal.y, eps)) return false;
		if (!equalsWithEps(dan1.normal.z, dan2.normal.z, eps)) return false;
		
		return true;
	}
	
	private boolean equalsWithEps(double d1, double d2, double eps) {
		if (d1 == d2) return true;
		if (Math.abs((d1 - d2) / (d1 + d2)) < eps) return true;
		
		return false;
	}
	
	
	
}
