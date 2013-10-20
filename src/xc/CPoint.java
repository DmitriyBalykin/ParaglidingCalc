package xc;

public class CPoint {
	public double x;
	public double y;
	private static int count = 0;
	private final int id = count ++;
	CPoint(double x, double y){
		this.x = x;
		this.y = y;
	}
	public String toString(){
		return String.format("[CPoint x = %.2f, y = %.2f]", x, y);
	}
	public String toRecord(){
		return String.format("%.2f %.2f", x, y);
	}
	public int id(){return id;}
	public int hashCode(){ return id;}
	public boolean equals(Object o){
		return (o instanceof CPoint) ? ((CPoint)o).id() == id : false;
	}
}
