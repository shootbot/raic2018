public class Dan {
	public double distance;
	public Vec3d normal;
	
	public Dan(double distance, Vec3d normal) {
		this.distance = distance;
		this.normal = normal;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Dan)) return false;
		
		Dan d = (Dan) other;
		if (d.distance == this.distance && d.normal.equals(this.normal)) {
			return true;
		}
		
		return false;
	}
	
	public String toString() {
		return "{" +
			"dist=" + distance +
			", normal=" + normal +
			'}';
	}
}
