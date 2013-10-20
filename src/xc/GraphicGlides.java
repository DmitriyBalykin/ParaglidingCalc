package xc;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JPanel;

@SuppressWarnings("serial")
class GraphicGlides extends JPanel{
	private HashMap<Polar, Color> polars = new HashMap<Polar, Color>();
	private ArrayList<ArrayList<CPoint>> points = new ArrayList<ArrayList<CPoint>>();
	private int baseAlt = 2000, lowAlt = 500, distance;
	private float meanCR, wind; //sinkFactor - sink ratio between sink between thermals and climb speed in thermals

	public void paintComponent(Graphics g){
		super.paintComponent(g);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.drawRect(0, 0, getWidth(), getHeight());
		drawGrid(g);
		calculateCurvesToFinish();
		Iterator<Color> it = polars.values().iterator();
		for(ArrayList<CPoint> arrList:points){
			g.setColor(it.next());
			for(int i = 0; i < arrList.size() - 1; i++){
				g.drawLine(
						getLocalX(arrList.get(i).x),
						getLocalY(arrList.get(i).y),
						getLocalX(arrList.get(i+1).x),
						getLocalY(arrList.get(i+1).y));
			}
			//draw vertical short line
			g.drawLine(
					getLocalX(arrList.get(arrList.size()-1).x),
					getLocalY(arrList.get(arrList.size()-1).y)-2,
					getLocalX(arrList.get(arrList.size()-1).x),
					getLocalY(arrList.get(arrList.size()-1).y)+2);
		}
	}
	private void drawGrid(Graphics g){
		float stepMV = baseAlt/10; //vertical step of grid, meters
		float stepMH = distance/10; //horizontal step of grid, meters
		int stepsV = 11; // number of vertical intervals in grid
		int stepsH = 11;  // number of horizontal intervals in grid
		float stepV = getHeight()/stepsV;
		float stepH = getWidth()/stepsH;
		g.setColor(Color.GRAY);
		//draw horizontal dashed lines
		for(int i = 0; i< stepsV; i++){
			g.drawString((stepsV - i)*(int)stepMV+"", 5, (int) (i*stepV - 5));
			for(int j = 0; j < getWidth()-10; j++)
				g.drawLine(j*10, (int) (i*stepV), j*10 + 5 , i*(int)stepV);
		}
		//draw vertical dashed lines
		for(int i = 0; i< stepsH; i++){
			g.drawString(i*stepMH/1000+"", i*(int)stepH + 5, getHeight() - 5);
			for(int j = 0; j < getHeight()-10; j++)
				g.drawLine(i*(int)stepH, j*10, i*(int)stepH, j*10 + 5);
		}
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 70, 20);
		g.fillRect(getWidth() - 85, 0, 85, 20);
		g.fillRect(getWidth() - 85, getHeight() - 20, 85, 20);
		g.setColor(Color.GRAY);
		g.drawString("Altitude, m", 5, 12);
		g.drawString("Altitude, m", getWidth() - 65, 12);
		g.drawString("Distance, km", getWidth() - 80, getHeight() - 5);
	}
	private void calculateCurvesToFinish(){

		/*
		 * calculating fulfill in absolute units (m)
		 * In first pass we are looking for the fastest glider (shortest time to get target distance)
		 */
		points.clear();
		double minTime = 0, glide, dHeight, climbTime, glideTime, time, fullNegDist; 
		boolean first = true;
		for(Polar p:polars.keySet())
		{
			glide = (p.getCurrentSpeed()/3.6 + wind)/(p.getCurrentSink());
			fullNegDist = (wind*p.getCurrentSink()*distance)/(meanCR*p.getCurrentSpeed()/3.6 + meanCR*wind + wind*p.getCurrentSink());
			dHeight = (distance - fullNegDist)/glide;
			climbTime = dHeight/meanCR;
			glideTime = (distance - fullNegDist)/(p.getCurrentSpeed()/3.6 + wind);
			time = climbTime + glideTime;
			if(first) {minTime = time; first = false;}
			if(time < minTime){ minTime = time;}
		}
		/*
		 * In second pass we are building curves
		 */
		for(Polar p:polars.keySet())
		{
			double totalAltGain, altGain, deltaTime, x, y;
			ArrayList<CPoint> lst = new ArrayList<CPoint>();
			time = minTime;
			lst.add(new CPoint(0,0));
			glide = (p.getCurrentSpeed()/3.6 + wind)/p.getCurrentSink();
			fullNegDist = (wind*p.getCurrentSink()*distance)/(meanCR*p.getCurrentSpeed()/3.6 + meanCR*wind + wind*p.getCurrentSink());
			totalAltGain = (distance - fullNegDist)/glide;	//total altitude gain, needs to get finish at distance
			
			for(int i = 1; time > 0 ; i++){
				if(lst.get(i - 1).y <= lowAlt){
					//climbing step
					altGain = baseAlt - lst.get(i - 1).y;
					//climb to baseAlt or to height, needed for finish
					if(altGain >= totalAltGain) 
						altGain = totalAltGain;
					else
						totalAltGain -= altGain;
					
					deltaTime = altGain/meanCR;
					if((int)time > (int)deltaTime){
						//there is more time to fly
						x = lst.get(i - 1).x + deltaTime*wind;
						y = lst.get(i - 1).y + altGain;
					}
					else{
						x = lst.get(i - 1).x + time*wind;
						y = lst.get(i - 1).y + time*meanCR;
					}
					
					time -= deltaTime;
					lst.add(new CPoint(x,y));
				}
				else{
					//gliding step
					if((glide - (distance - lst.get(i - 1).x)/lst.get(i - 1).y) < -0.001){
						//not enough to reach finish
						altGain = baseAlt - lowAlt;
					}
					else{
						//enough to reach finish
						altGain = lst.get(i - 1).y;
					}
					deltaTime = altGain/p.getCurrentSink();
					if((int)time > (int)deltaTime){
						//if there is more time to fly
						x = lst.get(i - 1).x + deltaTime*(p.getCurrentSpeed()/3.6+wind);
						y = lowAlt;
					} else {
						x = lst.get(i - 1).x + time*(p.getCurrentSpeed()/3.6 + wind);
						y = lst.get(i - 1).y - time*p.getCurrentSink();
					}
					time -= deltaTime;
					lst.add(new CPoint(x,y));
				}
			}
			points.add(lst);
		}
	}

	private int getLocalX(double x){
		return (int) ((x)*getWidth()/(1.1*(distance)));
	}
	private int getLocalY(double y){
		return (int) (getHeight() - getHeight()*y/(1.1*baseAlt));
	}
	public void setBaseAlt(int alt){		baseAlt = alt;	repaint();}
	public void setLowAlt(int alt){		lowAlt = alt;	repaint();}
	public void setDistance(int dist){		distance = dist;	repaint();}
	public void setWind(float w){wind = w;repaint();}
	public void setClimb(float cr){meanCR = cr;repaint();}
	public void addPolar(Polar p, Color c){
		polars.put(p, c);
		repaint();
	}
	public Map<Polar,Color> getPolars(){ return polars;}
	public void setSpeed(String name, int speed){
		for(Polar p:polars.keySet())
			if(p.getName().equals(name))
				p.setCurrentSpeed(speed);
		repaint();
	}
	public void remove(String name){
		Polar pt = null;
		for(Polar p:polars.keySet())
			if(p.getName().equals(name))
				pt = p;
		polars.remove(pt);
		repaint();
	}
	public void removeAll(){
		polars.clear();
		repaint();
	}
}