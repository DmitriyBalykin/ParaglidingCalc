package xc;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class SelectorPanel extends JPanel{
	private int columns;
	private ArrayList<GliderSelectItem> items = new ArrayList<GliderSelectItem>();
	private HashMap<String, Polar> polars = new HashMap<String,Polar>();
	public enum Type {EDITOR, ANALYSER}

	private Type type;
	private JPanel headPanel = new JPanel();
	
	public SelectorPanel(Type t) {
		type = t;
		switch (t) {
			case EDITOR:{			//set up EDITOR specific options
				columns = 2;
				break;
			}
			case ANALYSER:{			//set up ANALYZER specific options
				columns = 3;
				break;
			}
			default:break;
		}
		//head panel setup
		headPanel.setLayout(new GridLayout(1, columns));
		headPanel.add(new JLabel(""));
		
		if(type == Type.ANALYSER){
			headPanel.add(new JLabel("Current speed"));
			
		}
		headPanel.add(new JLabel(""));
	}

	public Color getItemColor(String itemName){
		return items.get(items.indexOf(itemName)).getColor();
	}
	//constructor for editing panel
	public void addItem(Polar polar, Color color){
		polars.put(polar.getName(), polar);
		GliderSelectItem itm = new GliderSelectItem(polar.getName(), color, columns, polar.getMinSpeed(), polar.getMaxSpeed());
		items.add(itm);
		setLayout(new GridLayout(getHeight() == 0 ? 20:getHeight()/30, 1));
		refreshPanel();
	}
	//constructor for analyzing panel
	public void addItem(Polar polar, String protoName, Color color){
		polars.put(polar.getName(), polar);
		GliderSelectItem itm = new GliderSelectItem(polar.getName(), protoName, color, columns, polar.getMinSpeed(), polar.getMaxSpeed(),(int)polar.getCurrentSpeed());
		items.add(itm);
		
		setLayout(new GridLayout(getHeight() == 0 ? 20:getHeight()/30, 1));
		refreshPanel();
	}
	public void removeItem(String name){
		int index = 0;
		for(GliderSelectItem item:items){
			if(item.getName().equals(name)) break;
			index++;
		}
		items.remove(index);
		polars.remove(name);
		refreshPanel();
	}
	@Override
	public void removeAll(){
		super.removeAll();
		items.clear();
		polars.clear();
	}
/*	public double getItemSpeed(String name){
		return items.get(items.indexOf(name)).getSpeed();
	}*/
	private void setItemSpeed(String name, int val){
		polars.get(name).setCurrentSpeed(val);
	}
	public void setColor(String name, Color color){
		int index = 0;
		for(GliderSelectItem item:items){
			if(item.getName().equals(name)) break;
			index++;
		}
			
		items.get(index).setColor(color);
	}
	public boolean contains(String name){
		for(GliderSelectItem item:items)
			if(item.getName().equals(name)){
				return true;
			}
		return false;
	}
	public Collection<Polar> getPolars(){
		return polars.values();
	}
	private void refreshPanel(){
		super.removeAll();
		add(headPanel);
		for(Component item:items)
			add(item);
		revalidate();
	}
	public String toString(){
		return "SelectorPanel "+items;
	}
	class GliderSelectItem extends JPanel{
		private String name;//name, name of glider, polar of which was prototype of this polar
		private JPanel colorBox = new JPanel();
		private JSpinner speedSpinner = new JSpinner();
		private JPopupMenu itemMenu = new JPopupMenu();
		private JMenuItem hideItem = new JMenuItem("Hide this glider");
		private JMenuItem removeItem = new JMenuItem("Remove this glider");
		private JMenuItem colorItem = new JMenuItem("Change color");
		private Color unselColor = getBackground(),
					selColor = new Color(200,200,200);
		private int minSpeed, maxSpeed, balanceSpeed;
		boolean selected = false;
		
		GliderSelectItem(String itemName, Color color, int columns, int minS, int maxS){
			name = itemName;
			minSpeed = minS;
			maxSpeed = maxS;
			setLayout(new GridLayout(1, columns, 5, 5));
			setColor(color);
			setBackground(unselColor);
//			setBorder(new BevelBorder(BevelBorder.LOWERED));
			colorBox.setBorder(new BevelBorder(BevelBorder.RAISED));
			hideItem.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					SelectorPanel.this.firePropertyChange("itemExclude", name, null);
				}
			});
			removeItem.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					SelectorPanel.this.firePropertyChange("itemRemove", name, null);
				}
			});
			colorItem.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					SelectorPanel.this.firePropertyChange("colorChanged", name, null);
				}
			});
			add(new JLabel(name));
			if(type == Type.ANALYSER){
				speedSpinner.addChangeListener(new ChangeListener() {
					
					@Override
					public void stateChanged(ChangeEvent e) {
						if((Integer)speedSpinner.getValue() < minSpeed) {speedSpinner.setValue(minSpeed); return;}
						if((Integer)speedSpinner.getValue() > maxSpeed) {speedSpinner.setValue(maxSpeed); return;}
						setItemSpeed(name, (Integer)speedSpinner.getValue());
						SelectorPanel.this.firePropertyChange("speedChanged", speedSpinner.getValue(), name);
					}
				});
//				speedSpinner.setBorder(new BevelBorder(BevelBorder.RAISED));
				add(speedSpinner);
			}
			add(colorBox);
			addMouseListener(new SelML());
			
			switch (SelectorPanel.this.type) {
				case ANALYSER: itemMenu.add(hideItem); break;
				case EDITOR: itemMenu.add(removeItem); break;
				default:break;
			}
			itemMenu.add(colorItem);
			
		}
		GliderSelectItem(String itemName, String prototype, Color color, int columns, int minS, int maxS, int balSpeed){
			this(itemName,  color,  columns,  minS,  maxS);
			balanceSpeed = balSpeed;
			speedSpinner.setValue(balanceSpeed);
			
		}
		public Double getSpeed(){
			return (Double)speedSpinner.getValue();
		}
		public void setColor(Color color){
			colorBox.setBackground(color);
		}
		public Color getColor(){
			return colorBox.getBackground();
		}
		class SelML extends MouseAdapter{
			@Override
			public void mouseClicked(MouseEvent e){
				if(e.getButton() == MouseEvent.BUTTON1){
					selected = selected? false : true;
					setBackground(selected ? selColor:unselColor);
				}
				if(e.getButton() == MouseEvent.BUTTON3){
					itemMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		}
		@Override
		public String getName(){ return name;}
		public String toString(){
			return name;
		}
	}
}
