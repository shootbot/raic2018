public class _DanInfo {
	// Центр сферы, откуда производился поиск до арены
	public Vec3d point;
	
	// Валидные значения, которые должны получиться
	public Dan validDan;
	
	public _DanInfo(Vec3d point, Dan validDan) {
		this.point = point;
		this.validDan = validDan;
	}
	
	@Override
	public String toString() {
		return "{point=" + point + ", dan=" + validDan + "}";
	}
}
