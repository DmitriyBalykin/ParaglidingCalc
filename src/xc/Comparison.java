package xc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class Comparison extends JFrame{
	static final int WIDTH = 1200;
	static final int HEIGHT = 600;
	private String fileName = "Polars.txt";	//default file with polars
	private static Vector<String> gliders = new Vector<String>();	//list of available for analysis gliders
	GraphicGlides  gg = new GraphicGlides();							//analysis area
	GraphicPolar gp = new GraphicPolar();			// editing area
	JSlider windSlider = new JSlider(-110, 110, 0),
			climbSlider = new JSlider(JSlider.VERTICAL, 0, 110, 20);
	JPanel	workPanel = new JPanel(new BorderLayout(5,5)), //analysis page main panel
			polarPanel = new JPanel(new BorderLayout(5,5)), //edit page main panel
			analysisPanel = new JPanel(new BorderLayout(5,5)), //panel with analysis instruments
			glidersPanelAnalyze = new JPanel(new BorderLayout(5,5)), //panel with gliders analysis lists
			glidersPanelEdit = new JPanel(new BorderLayout(5,5)), //panel for edit list and buttons
			altPanel = new JPanel(new BorderLayout(5,5)), //panel with altitude, climb and distance spinners
			buttonsEditPanel = new JPanel(new GridLayout(2,1)); //panel with glider list buttons
	SelectorPanel choosenPolars = new SelectorPanel(SelectorPanel.Type.ANALYSER), //Polars which is choosen for analysis
					gliderEditList = new SelectorPanel(SelectorPanel.Type.EDITOR);
	JTabbedPane tabPane = new JTabbedPane();
			
	JSpinner alt = new JSpinner(),
			dist = new JSpinner();
	JButton newGlider = new JButton("New glider"),
			removeGlider = new JButton("Remove glider"),
			shGlidersList = new JButton("Show gliders list >>");
	JList glidersFullList;
	JScrollPane scroll;
	
	public Comparison() {
		setUpPolarPanel();
		setUpEditPanel();
		setUpAnalysisPanel();
		tabPane.addTab("Analysis", workPanel);
		tabPane.addTab("Polars editing", polarPanel);
		add(tabPane);
		
		addWindowListener(new WindowController());
		//test polars, remove it by real polars loading from external file
		openGliders();
		
		loadGliders();
	}
	public static void createGUI(){
		Comparison frame = new Comparison();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Paragliding calculator");
		
		frame.setBounds((dim.width - WIDTH)/2, (dim.height - Comparison.HEIGHT)/2, Comparison.WIDTH, Comparison.HEIGHT);
		
		frame.setVisible(true);
	}
	private Hashtable<Integer, JLabel> labelsDict(JSlider slider){
		Hashtable<Integer,JLabel> dict = new Hashtable<Integer, JLabel>(); 
		for(int i = slider.getMinimum(); i< slider.getMaximum(); i+=10)
			dict.put(i, new JLabel((float)i/10+""));
		return dict;
	}

	private void setUpEditPanel(){
		//Changeable left panel
		
		gliderEditList.addPropertyChangeListener("itemRemove", new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				removeGlider((String) evt.getOldValue());
			}
		});
		gp.addPropertyChangeListener("colorChanged", new EditColorChangedListener());
		gliderEditList.addPropertyChangeListener("colorChanged", new EditColorChangedListener());
		shGlidersList.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(scroll == null){
					glidersFullList = new GlidersList(gliders);
					scroll = new JScrollPane(glidersFullList);
					glidersPanelAnalyze.add(scroll, BorderLayout.EAST);
					shGlidersList.setText("Hide gliders list <<");
				}
				else{
					if(glidersPanelAnalyze.getComponentCount() > 2){
						glidersPanelAnalyze.remove(scroll);
						scroll = null;
						shGlidersList.setText("Show gliders list >>");
					}
				}
				glidersPanelAnalyze.revalidate();
			}
		});
		glidersPanelAnalyze.add(shGlidersList, BorderLayout.SOUTH);
		glidersPanelAnalyze.add(new JScrollPane(choosenPolars), BorderLayout.CENTER);
		
		
	}
	private void setUpAnalysisPanel(){
		
		alt.setBorder(new BevelBorder(BevelBorder.LOWERED));
		dist.setBorder(new BevelBorder(BevelBorder.LOWERED));
		alt.setValue(2000);
		dist.setValue(20);
		alt.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				gg.setBaseAlt((Integer) alt.getValue());
			}
		});
		dist.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				gg.setDistance(((Integer) dist.getValue())*1000);
			}
		});
		climbSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				gg.setClimb(climbSlider.getValue()/10);	
			}
		});
		windSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				gg.setWind(windSlider.getValue()/10);
			}
		});
		choosenPolars.addPropertyChangeListener("speedChanged", new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				gg.repaint();
			}
		});
		choosenPolars.addPropertyChangeListener("itemExclude", new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				choosenPolars.removeItem((String) evt.getOldValue());
				gg.remove((String) evt.getOldValue());
				loadGliders();
				choosenPolars.revalidate();
				choosenPolars.repaint();
			}
		});
		choosenPolars.addPropertyChangeListener("colorChanged", new AnalyzeColorChangedListener());
		climbSlider.setMajorTickSpacing(10);
		climbSlider.setMinorTickSpacing(2);
		climbSlider.setPaintTicks(true);
		climbSlider.setPaintLabels(true);
		climbSlider.setLabelTable(labelsDict(climbSlider));
		climbSlider.setBorder(new TitledBorder(new EtchedBorder(), "Climb",TitledBorder.CENTER,0));
		windSlider.setMajorTickSpacing(10);
		windSlider.setMinorTickSpacing(5);
		windSlider.setPaintTicks(true);
		windSlider.setPaintLabels(true);
		windSlider.setSnapToTicks(true);
		windSlider.setLabelTable(labelsDict(windSlider));
		windSlider.setBorder(new TitledBorder(new EtchedBorder(), "Wind",TitledBorder.CENTER,0));
		windSlider.setValue(-20);
		altPanel.add(climbSlider, BorderLayout.CENTER);
		altPanel.add(alt, BorderLayout.NORTH);
		altPanel.add(dist, BorderLayout.SOUTH);
		analysisPanel.add(altPanel, BorderLayout.EAST);
		analysisPanel.add(windSlider, BorderLayout.SOUTH);
		analysisPanel.add(gg, BorderLayout.CENTER);
		workPanel.add(glidersPanelAnalyze, BorderLayout.WEST);
		workPanel.add(analysisPanel, BorderLayout.CENTER);
		gg.setWind(windSlider.getValue()/10);
		gg.setClimb(climbSlider.getValue()/10);
		gg.setDistance((Integer)dist.getValue()*1000);
		
	}
	private void setUpPolarPanel(){
		//Changeable left panel
		newGlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				new NewGliderDialog(Comparison.this).setVisible(true);
			}
		});
		removeGlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				new RemoveGliderDialog(Comparison.this).setVisible(true);
				
			}
		});
		buttonsEditPanel.add(newGlider);
		buttonsEditPanel.add(removeGlider);
		glidersPanelEdit.add(gliderEditList, BorderLayout.CENTER);
		glidersPanelEdit.add(buttonsEditPanel, BorderLayout.SOUTH);
		polarPanel.add(new JScrollPane(glidersPanelEdit), BorderLayout.WEST);
		polarPanel.add(gp, BorderLayout.CENTER);
	}
	private void newGlider(String name){
		gliders.add(name);
		gp.addPolar(new Polar(name));
		loadGliders();
	}
	private void removeGlider(String gName){
		gp.removePolar(gName);
		gliderEditList.removeItem(gName);
		loadGliders();
	}
	//load gliders list into edit panel
	private void loadGliders(){
		if(gp.getPolars().size() == 0) return;
		gliders.clear();
		gliderEditList.removeAll();
		
		for(Map.Entry<Polar, Color> entry:gp.getPolars().entrySet()){
			gliderEditList.addItem(entry.getKey(), entry.getValue());
			gliders.add(entry.getKey().getName());
		}
		gliderEditList.revalidate();
		gliderEditList.repaint();
		repaint();
	}
	//save gliders to file
	private void saveGliders(){
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()+fileName));
			for(Polar polar:gp.getPolars().keySet()){
				writer.write(polar.getName()+":");
				writer.write((int)polar.getCurrentSpeed()+":");
				for(CPoint point:polar.getCPoints())
					writer.write(point.toRecord()+" ");
				
				writer.write("\n");
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	//load gliders from file
	private void openGliders(){
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()+fileName));
			String line = null;
			String name = null;
			while((line = reader.readLine())!=null){
				//parsing name
				int pStart = line.indexOf(":");
				name = line.substring(0, pStart);
				if(name.equals("")) continue;
				//parsing balancing speed
				line = line.substring(pStart+1);
				pStart = line.indexOf(":");
				int balSpeed = pStart == -1 ? 36:new Integer(line.substring(0, pStart));
				//parsing polar points
				line = line.substring(pStart+1);
				line = line.replaceAll(",", ".");
				String[] elements = line.split(" ");
				CPoint[] points = new CPoint[(elements.length)/2];
				//creating array of CPoints
				for(int i = 0,j = 0; i< points.length;i++){
					points[i] = new CPoint(new Double(elements[j++]),new Double(elements[j++]));
				}
				Polar p = new Polar(name, points, balSpeed);
				gp.addPolar(p);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(IndexOutOfBoundsException e){
			e.printStackTrace();
		}
		finally{
			try {
				if(reader != null) reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	class WindowController extends WindowAdapter{
		@Override
		public void windowClosing(WindowEvent e) {
			saveGliders();
		}
	}
	class EditColorChangedListener implements PropertyChangeListener{
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				// TODO Auto-generated method stub
				String name = (String) evt.getOldValue();
				Polar p = null;
				for(Polar ps:gp.getPolars().keySet())
					if(ps.getName().equals(name))
						{p = ps; break;}
				
				gp.getPolars().put(p, gp.newColor());
				gp.repaint();
				loadGliders();
			}
	}
	class AnalyzeColorChangedListener implements PropertyChangeListener{
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			// TODO Auto-generated method stub
			String name = (String) evt.getOldValue();
			Polar p = null;
			for(Polar ps:gg.getPolars().keySet())
				if(ps.getName().equals(name))
					{p = ps; break;}
			
			Color color = gp.newColor();
			gg.getPolars().put(p, color);
			gg.repaint();
			choosenPolars.setColor(name, color);
			loadGliders();
		}
}
	class NewGliderDialog extends JDialog{
		JTextField nameField = new JTextField(10);
		JPanel buttPanel = new JPanel(new GridLayout(1, 2));
		JButton sendButton = new JButton("OK"),
				cancelButton = new JButton("Cancel");
		public NewGliderDialog(JFrame parent){
			super(parent, "New glider", true);
			setLayout(new GridLayout(3, 1));
			setBounds(parent.getLocation().x + parent.getWidth()/2, parent.getLocation().y + parent.getHeight()/2, 200,100);
			nameField.setToolTipText("Enter glider name");
			nameField.addKeyListener(new KeyListener() {
				@Override
				public void keyTyped(KeyEvent e) {}
				@Override
				public void keyReleased(KeyEvent e) {
					for(String g:gliders){
						if(g.equals(nameField.getText())){
							nameField.setToolTipText("Glider with this name already exists");
							sendButton.setEnabled(false);
							return;
						}
					}
					nameField.setToolTipText("Enter glider name");
					sendButton.setEnabled(true);
				}
				@Override
				public void keyPressed(KeyEvent e) {}
			});
			sendButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(!nameField.getText().equals(""))
						newGlider(nameField.getText());
					dispose();
				}
			});
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			add(new JLabel("Enter name of new glider:"));
			add(nameField);
			sendButton.setEnabled(false);
			buttPanel.add(sendButton);
			buttPanel.add(cancelButton);
			add(buttPanel);
		}
	}
	class RemoveGliderDialog extends JDialog{
		JComboBox glidersList = new JComboBox(gliders);
		JPanel buttPanel = new JPanel(new GridLayout(1, 2));
		JButton removeButton = new JButton("Remove"),
				cancelButton = new JButton("Cancel");
		public RemoveGliderDialog(JFrame parent){
			super(parent, "Remove glider", true);
			setLayout(new GridLayout(3, 1));
			setBounds(parent.getLocation().x + parent.getWidth()/2, parent.getLocation().y + parent.getHeight()/2, 200,100);
			removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					removeGlider(glidersList.getSelectedItem().toString());
					dispose();
				}
			});
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			add(new JLabel("Select glider should be deleted"));
			add(glidersList);
			buttPanel.add(removeButton);
			buttPanel.add(cancelButton);
			add(buttPanel);
		}
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable(){

			@Override
			public void run() {
				createGUI();
			}
			
		});
	}
	
	class GlidersList extends JList{
		GlidersList(Vector<String> gldrs){
			super(gldrs);
			addMouseListener(new RCML());
		}
		private class RCML extends MouseAdapter{
			//Listener for doubleClick of full list
			@Override
			public void mouseClicked(MouseEvent e){
				if(e.getClickCount() == 2){
					String protoItemName = (String) glidersFullList.getSelectedValue();
					
					String itemName = protoItemName;
					Polar newAnalyzePolar = null;
					for(Polar p:gp.getPolars().keySet())
						if(p.getName().equals(protoItemName))
							newAnalyzePolar = p;	//temporary save founded polar
					if(choosenPolars.contains(protoItemName)){
					boolean nnf = false;			//searching for new name, which is not exists in list
					int count = 0;	
						do{
							count++;
							if(!choosenPolars.contains(protoItemName+" #"+count)){
								itemName = protoItemName+" #"+count;
									nnf = true;
							}
						}
						while(!nnf);
					}
					newAnalyzePolar = new Polar(itemName, newAnalyzePolar);
					Color col = gp.newColor();
					choosenPolars.addItem(newAnalyzePolar, protoItemName, col);
					gg.addPolar(newAnalyzePolar, col);
					gg.repaint();
					glidersPanelAnalyze.revalidate();
				}
			}
		}
	}

}