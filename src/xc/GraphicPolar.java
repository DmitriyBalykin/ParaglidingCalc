package xc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class GraphicPolar extends JPanel{
	private double wind = 0, sink = 0, maxSpeed = 50, minSpeed = 0, minSink = 0, maxSink = 3,
			xStep = 0.1, yStep = 0.1; //step of speed samples
	private int borderX = 30, borderY = 20;
	private static Random rand = new Random(); 
	private HashMap<Polar, Color> polars = new HashMap<Polar, Color>();
	private HashMap<Polar, ArrayList<LinePoint>> points = new HashMap<Polar, ArrayList<LinePoint>>();
	private JPopupMenu menu = new JPopupMenu();
	private JMenuItem mItem = new JMenuItem("Add point");
	private JMenuItem cItem = new JMenuItem("New Color");
	private Polar tempPolar; //uses for temporary store of polar, which was selected on graph
	private Point tempPoint, tangP1, tangP2;	//tangP1, tangP2 uses to draw tangent line
	private boolean changed = false,
			drawTangent = false;
	private JLabel pointLabel = new JLabel("Point label");
	
	public boolean isChanged() {
		return changed;
	}
	public void setUnchanged(){
		changed = false;
	}
	GraphicPolar(){
		setLayout(null);
		mItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Point p = tempPoint;
				System.out.println(p);
				System.out.println(tempPolar == null);
				CPoint cp = tempPolar.addPoint(getRealX(p.x), getRealY(p.y));
				List<LinePoint> list = points.get(tempPolar);
				LinePoint lp = new LinePoint(cp, tempPolar);
				list.add(lp);
				System.out.println("LinePoints List size: "+list.size());
				add(lp);
				System.out.println("Components size: "+getComponentCount());
				repaint();
			}
		});
		cItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GraphicPolar.this.firePropertyChange("colorChanged", tempPolar.getName(), null);
				repaint();
			}
		});
		
		add(pointLabel);
		pointLabel.setBorder(BorderFactory.createEtchedBorder());
		pointLabel.setOpaque(true);
		pointLabel.setBackground(new Color(255,255,255,220));
		pointLabel.setSize(90, 60);
		pointLabel.setVisible(false);
				
		menu.add(mItem);
		menu.add(cItem);
		
		addMouseListener(new MList());
		addMouseMotionListener(new MList());
	}
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		checkSize();
		drawGrid(g);
		for(Polar p:polars.keySet()){
			g.setColor(polars.get(p));
			for(double i = p.getMinSpeed(); i < p.getMaxSpeed()-1;i += xStep)
				drawAALine(g,getLocalX(i), getLocalY(p.getSink(i)), getLocalX(i+1), getLocalY(p.getSink(i+1)));
		}
	}
	private void checkSize(){
		double polarsMaxSpeed = 0;
		double polarsMaxSink = 0;
		boolean firstPolar = true; 
		
		//searching max speed of polars
		for(Map.Entry<Polar, ArrayList<LinePoint>> entry:points.entrySet()){
			if(firstPolar){
				
				//used for initiating two fields with real values from polar
				
				polarsMaxSpeed = entry.getKey().getMaxSpeed();
				polarsMaxSink = entry.getKey().getMaxSink();
				firstPolar = false;
				continue;
			}
			if(entry.getKey().getMaxSpeed() > polarsMaxSpeed)
				polarsMaxSpeed = entry.getKey().getMaxSpeed();
			if(entry.getKey().getMaxSink() > polarsMaxSink)
				polarsMaxSink = entry.getKey().getMaxSink();
		}

		/**
		 * Correcting size of frame depends on limits of sink and speed of polars
		 */
		if(polarsMaxSpeed > maxSpeed) maxSpeed = polarsMaxSpeed;
		if((maxSpeed - polarsMaxSpeed) > 7) maxSpeed --;
		
		if(polarsMaxSink > maxSink) maxSink = polarsMaxSink;
		if((maxSink - polarsMaxSink) > 1) maxSink -= 0.1;
		
		for(Map.Entry<Polar, ArrayList<LinePoint>> entry:points.entrySet())
			for(LinePoint lp:entry.getValue())
				lp.refreshPos();
	}

	private void drawGrid(Graphics g){
		float stepMV = 0.5f; //vertical step of grid, meters
		float stepMH = 5; //horizontal step of grid, meters
		float stepsV = (float) ((maxSink - minSink)/stepMV + 1);
		float stepsH = (float) ((maxSpeed - minSpeed)/stepMH + 1);
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		g.setColor(Color.GRAY);
		//draw horizontal dashed lines
		for(int i = 0; i< stepsV; i++){
			g.drawString(i*stepMV+"", getLocalX(0) - 20, getLocalY(i*stepMV) - 5);
			for(int j = 0; j < getWidth(); j++)
				g.drawLine(j*10, getLocalY(i*stepMV), j*10 + 5 , getLocalY(i*stepMV));
		}
		//draw vertical dashed lines
		for(int i = 0; i< stepsH; i++){
			g.drawString((int)(i*stepMH)+"", getLocalX(i*stepMH) + 5,  getLocalY(0) - 8);
			for(int j = 0; j < getHeight()-10; j++)
				g.drawLine(getLocalX(i*stepMH), j*10, getLocalX(i*stepMH), j*10 + 5);
		}
		//draw axis
		g.setColor(Color.red);
		g.drawLine(getLocalX(0), borderY, getLocalX(0), getHeight() - borderY);
		g.drawLine(getLocalX(0)+1, borderY, getLocalX(0)+1, getHeight() - borderY); //bold red vertical line
		g.drawLine(borderX, getLocalY(0), getWidth() - borderX, getLocalY(0));
		g.drawLine(borderX, getLocalY(0)+1, getWidth() - borderX, getLocalY(0)+1); //bold red horizontal line
		g.setColor(Color.WHITE);
		//draw annotations
		g.fillRect(getLocalX(0) - 15, getHeight() - borderY, 70, 20);
		g.fillRect(getWidth() - 85, getLocalY(0) - 5, 85, 20);
		g.setColor(Color.GRAY);
		g.drawString("Sink, m/s", getLocalX(0) - 20, getHeight() - 5);
		g.drawString("Speed, km/h", getWidth() - 75, getLocalY(0)+5);
	}
	private void drawTangent(Graphics g){
		if(!drawTangent) return;
		
		
	}
	private void drawAALine(Graphics g, int x1, int y1, int x2, int y2){
		Color c = g.getColor();
		g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 20));
		g.drawLine(x1, y1+1, x2, y2+1);
		g.drawLine(x1, y1-1, x2, y2-1);
		g.setColor(c);
		g.drawLine(x1, y1, x2, y2);
	}
	public double getWind() {		return wind;	}
	public void setWind(double wind) {		this.wind = wind;	}
	public double getSink() {		return sink;	}
	public void setSink(double sink) {		this.sink = sink;	}
	private int getLocalX(double x){
		xStep = (maxSpeed - minSpeed)/(getWidth() - 2*borderX);
		return (int) (x/xStep + borderX - minSpeed);
	}
	private int getLocalY(double y){
		yStep = (maxSink - minSink)/(getHeight() - 2*borderY);
		return (int) (y/yStep + borderY);
	}
	private double getRealX(int x){	return (x - borderX)*xStep;	}
	private double getRealY(int y){ return (y - borderY)*yStep;	}
	public static Color newColor(){
		int r = rand.nextInt(255);
		int g = rand.nextInt(255);
		int b = rand.nextInt(255);
		return new Color(r, g, b);
	}
	public void addPolar(Polar p){
		polars.put(p, newColor());
		if(p.getMaxSpeed() > maxSpeed) maxSpeed = p.getMaxSpeed();
		if(p.getSink(p.getMaxSpeed()) > maxSink) maxSink = p.getSink(p.getMaxSpeed());
		ArrayList<LinePoint> list = new ArrayList<LinePoint>();
		for(CPoint cp:p.getCPoints()){
			LinePoint lp = new LinePoint(cp,p);
			list.add(lp);
			add(lp);
		}
		points.put(p, list);
	}
	public void addPolars(Polar[] p){ for(Polar p1:p) addPolar(p1); }
	public void removePolar(String pName){
		System.out.println("Removing "+pName);
		Polar p = null;
		for(Polar polar:polars.keySet())
			if(polar.getName().equals(pName))
				p = polar;
		if(p == null) return;
		System.out.println("Removing polar "+p);
		polars.remove(p);							//clear polar-color list
		List<LinePoint> lplist = new ArrayList<LinePoint>(points.get(p));
		for(LinePoint lp:lplist)				//clear graphical points list
			removeLPoint(lp);
		points.remove(p);							//clear polar-points list  
	}
	public Map<Polar, Color> getPolars(){ return polars;}
	private class MList extends MouseAdapter{
		@Override
		public void mouseClicked(MouseEvent e){
			if(e.getButton() == MouseEvent.BUTTON3){
				checkForNear(e, 5);
				if(tempPolar != null){
					tempPoint = new Point(e.getX(),e.getY());
					menu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		}
		@Override
		public void mouseMoved(MouseEvent e){
			
			checkForNear(e, 3);
			
			if(tempPolar != null){
				Point p = new Point(e.getX() + 20 , e.getY() - 20);
				double sink = tempPolar.getSink(getRealX(e.getX()));
				double speed = getRealX(e.getX());
				double glide = speed/(3.6*sink);		
				String labelText = String.format("<html>Speed: %.2f<br>Sink rate: %.2f<br>Glide: %.2f</html>", speed, sink, glide);
				pointLabel.setText(labelText);
				pointLabel.setLocation(p);
				pointLabel.setVisible(true);
				tangP1 = new Point(1,0);
				tangP1 = new Point(1,0);
				drawTangent = true;
			}
			else{
				pointLabel.setVisible(false);
				drawTangent = false;
			}
		}
		@Override
		public void mouseWheelMoved(MouseWheelEvent e){
			System.out.println("Mouse wheel moved");
		}
		/** Assigning temporal polar, if cursor is near with some polar, distance < 5*/
		private void checkForNear(MouseEvent e, int dist){
			int dy = e.getY();
			
			tempPolar = null;
				for(Polar pol:points.keySet()){
					double diff = Math.abs(dy - getLocalY(pol.getSink(getRealX(e.getX()))));
					if(diff < dist)
						if(tempPolar == null) tempPolar = pol; // check if more than one line is close to target point
						else return;
				}
		}
	}
	private void removeLPoint(LinePoint lp){
		lp.getPolar().removePoint(lp.getCPoint());
		List<LinePoint> list = points.get(lp.getPolar());
		list.remove(lp);
		System.out.println("LinePoints List size: "+list.size());
		remove(lp);
		System.out.println("Components size: "+getComponentCount());
		repaint();
	}
	private class LinePoint extends JPanel{
		private Color colorBorder = new Color(50,50,50,100),
					colorIdle = new Color(100,100,100,100),
					colorActive = new Color(255,0,0);
		private boolean active = false;
		private int size = 8;
		private Polar polar;
		private CPoint cpoint;
		private JPopupMenu menu = new JPopupMenu();
		private JMenuItem vItem = new JMenuItem("Set value");
		private JMenuItem mItem = new JMenuItem("Remove point");
		
		public LinePoint(CPoint cp, Polar pol) {
			setSize(size, size);
			polar = pol;
			cpoint = cp;
			setBackground(new Color(0,0,0,0));
			refreshPos();
			addMouseListener(new ML());
			addMouseMotionListener(new MML());
			mItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					removeLPoint(LinePoint.this);
				}
			});
			vItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try{
						new SetValueDialog((JFrame)GraphicPolar.this.getTopLevelAncestor()).setVisible(true);
					}catch(ClassCastException ex){
						ex.printStackTrace();
					}
				}
			});
			menu.add(vItem);
			menu.add(mItem);
		}
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			g.setColor(colorBorder);
			g.drawOval(0, 0, size, size);
			g.setColor(active ? colorActive:colorIdle);
			g.fillOval(0, 0, size, size);
		}
		public Polar getPolar(){return polar;}
		public CPoint getCPoint(){return cpoint;}
		private void refreshPos(){
			setLocation(getLocalX(cpoint.x) - size/2, getLocalY(cpoint.y) - size/2);
		}
		class ML extends MouseAdapter{
			@Override
			public void mouseClicked(MouseEvent e){
				if(e.getButton() == MouseEvent.BUTTON3) {
					menu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
			@Override
			public void mousePressed(MouseEvent e){
				active = true;
				GraphicPolar.this.repaint();
			}
			@Override
			public void mouseReleased(MouseEvent e){
				active = false;
				GraphicPolar.this.repaint();
			}
		}
		class MML extends MouseMotionAdapter{
			@Override
			public void mouseDragged(MouseEvent e){
				active = true;
				int x = e.getXOnScreen() - GraphicPolar.this.getLocationOnScreen().x - size/2;
				int y = e.getYOnScreen() - GraphicPolar.this.getLocationOnScreen().y - size/2;
				if(x < 0) x = 0;
				if(y < 0) y = 0;
				setLocation(x, y);
				polar.setPoint(cpoint, getRealX(x + size/2), getRealY(y + size/2));
				GraphicPolar.this.repaint();
			}
		}
		class SetValueDialog extends JDialog{
			JTextField xField = new JTextField(5),
						yField = new JTextField(5);
			JButton okButton = new JButton("OK"),
					cancelButton = new JButton("Cancel");
			public SetValueDialog(JFrame parent){
				super(parent, "Set point value", true);
				setLayout(new GridLayout(3, 2));
				setBounds(parent.getLocation().x + parent.getWidth()/2, parent.getLocation().y + parent.getHeight()/2, 200,100);
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						tryResults();
					}
				});
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				xField.addKeyListener(new ValueListener());
				yField.addKeyListener(new ValueListener());
				xField.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						tryResults();
					}
				});
				yField.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						tryResults();
					}
				});
				add(new JLabel("Speed, km/h"));
				add(new JLabel("Sink, m/s"));
				add(xField);
				add(yField);
				add(okButton);
				add(cancelButton);
			}
			private void tryResults(){
				if(xField.getText().equals("")) return;
				try{
					polar.setPoint(cpoint, new Double(xField.getText()), new Double(yField.getText()));
				} catch(NumberFormatException ex){
					JOptionPane.showMessageDialog(null, "Error in entered values", "Number format error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				GraphicPolar.this.repaint();
				dispose();
			}
			class ValueListener extends KeyAdapter{
				char[] digits = "0123456789,.".toCharArray();
				@Override
				public void keyTyped(KeyEvent e){
					char s = e.getKeyChar();
					if(s == ','){
						e.setKeyChar('.');
						return;
					}
					boolean isDigit = false;
					for(char c:digits)
						if(c == s)
							isDigit = true;
					if(!isDigit)
						e.consume();
				}
			}
		}
	}
}
