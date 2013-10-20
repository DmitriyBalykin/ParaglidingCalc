package xc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Polar {

	private String name;
	private int pointsNum, maxSpeed, minSpeed,currentSpeed;
	private double[] coeffs;
	private List<CPoint> cPoints;
	private double  minSink, maxSink; //currentSpeed - speed on current MC number
	
	public int getCurrentSpeed() {
		return currentSpeed;
	}
	public void setCurrentSpeed(int currentSpeed) {
		this.currentSpeed = currentSpeed;
	}
	private static int count = 0;
	private final int id = count++;

	Polar(String n, CPoint[] array, int balanceSpeed){
		name = n;
		currentSpeed = balanceSpeed;
		cPoints = new ArrayList<CPoint>(Arrays.asList(array));
		calculatePolar();
	}
	Polar(String n){
		name = n;
		currentSpeed = 36;
		cPoints = new ArrayList<CPoint>();
		cPoints.add(new CPoint(25,2.5));
		cPoints.add(new CPoint(36,2.5));
		cPoints.add(new CPoint(55,2.5));
		calculatePolar();
	}
	Polar(String newName, Polar p){
		//for duplicate purpose
		name = newName;
		currentSpeed = p.getCurrentSpeed();
		cPoints = p.getCPoints();
		calculatePolar();
	}
	Polar(String n, int balanceSpeed){
		this(n);
		currentSpeed = balanceSpeed;
	}
	public String getName() {
		return name;
	}
	public void setPoint(CPoint cp2, double x, double y){
//		System.out.println("Edit polar: x = "+x+", y = "+y);
		for(CPoint cp:cPoints){
			if(cp.id() == cp2.id()){
				cp.x = x;
				cp.y = y;
			}
		}
		
		calculatePolar();
	}
	public CPoint addPoint(double x, double y){
		CPoint cp = new CPoint(x,y);
		cPoints.add(cp);
		calculatePolar();
		return cp;
	}
	public void removePoint(CPoint cp){
		for(int i = 0; i<cPoints.size();i++)
			if(cPoints.get(i).id() == cp.id()){
				cPoints.remove(i);break;
			}
//		System.out.println("Polar size: "+cPoints.size());
		calculatePolar();
	}
	public double getSink(double speed){
		double sink = 0;
		for(int i = 0; i < pointsNum ; i++){
			sink += coeffs[i]*Math.pow(speed, pointsNum - i - 1);
		}
		return sink;
	}
	public double getCurrentSink(){
		return getSink(currentSpeed);
	}
	
	public double getMinSink() {
		return minSink;
	}
	public double getMaxSink() {
		return maxSink;
	}
	/*public double getSpeed(float sink){
		return 0;						//need number solution 
	}*/
	private void calculatePolar(){
		/*for n input point we are forming n-1 order function with n coefficients
		 *  
		 * function of polar is y = a1x(n) + a2x(n-1) + ... an
		 * 
		 */
		if(cPoints.size()<1) return;
		minSpeed = (int)cPoints.get(0).x;
		minSink = cPoints.get(0).y;
		maxSpeed = minSpeed;
		maxSink = minSink;
		for(int i = 0; i < cPoints.size(); i++){
			if(cPoints.get(i).x > maxSpeed) maxSpeed = (int)cPoints.get(i).x;
			if(cPoints.get(i).x < minSpeed) minSpeed = (int)cPoints.get(i).x;
			if(cPoints.get(i).y > maxSink) maxSink = cPoints.get(i).y;
			if(cPoints.get(i).y < minSink) minSink = cPoints.get(i).y;
		}
		pointsNum = cPoints.size();
		coeffs = new double[pointsNum];
//		System.out.println(pointsNum);
		int order = pointsNum - 1;
		/*creating matrix of coefficients
		 * size rows x columns
		 */
		double[][] matrix = new double[pointsNum][pointsNum + 1];
		
		for(int i=0;i < pointsNum; i++){ //i - index of row
			for(int j=0; j < pointsNum; j++){ // j - index of coefficient in a row (column)
				matrix[i][j] = Math.pow(cPoints.get(i).x, pointsNum - j - 1);
			}
			matrix[i][pointsNum] = cPoints.get(i).y;
		}
		/*
		 * Transforming matrix of coefficients to "triangle form" by method of Gauss
		 */
		for (int i = 0; i < order; i++) { // index of transformations
			for (int j = i+1; j < pointsNum; j++) { //index of processed row
				double divider = matrix[j][i]/matrix[i][i];
				for (int k = i; k < pointsNum+1; k++) { // index of processed coefficient
					matrix[j][k] -= divider*matrix[i][k];
				}
			}
		}
		/*
		 * Solving coefficients in back order 
		 */
		for (int i = pointsNum - 1; i > -1; i--) { // index of row
			coeffs[i] = matrix[i][pointsNum];
			for (int j = i+1; j < pointsNum; j++) { // index of coefficient
				coeffs[i] -= matrix[i][j]*coeffs[j];
			}
			coeffs[i] /= matrix[i][i];
		}
	}
	public void setMaxSpeed(int s){		maxSpeed = s;	}
	public void setMinSpeed(int s){		minSpeed = s;	}
	public int getMaxSpeed(){	return maxSpeed;	}
	public int getMinSpeed(){	return minSpeed;	}
	public List<CPoint> getCPoints(){ return cPoints; }
	public String toString(){
		return "Polar for "+name+", balance speed "+currentSpeed+", control points: "+cPoints;
	}
	public int id(){return id;}
	public boolean equals(Object o){
		return (o instanceof Polar)&&((Polar)o).getName().equals(name);
	}
	public int hashCode(){
		return name.hashCode();
	}
}
