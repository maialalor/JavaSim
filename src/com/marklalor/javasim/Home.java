package com.marklalor.javasim;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.marklalor.javasim.simulation.Simulation;
import com.marklalor.javasim.simulation.SimulationInfo;

public class Home extends JFrame implements ListSelectionListener
{
	private static final long serialVersionUID = 6321955323521519657L;
	
	public static File homeDirectory;
	
	private List<SimulationInfo> simulations;
	private JList simulationList;
	private JPanel simulationInfoPanel;
	
	private JLabel name, date, author, version, description;
	private JButton run;
	
	public Home(File location)
	{
		Home.homeDirectory = location;
		loadSimulations();
		setupLayout();
		simulationList.setSelectedIndex(0);
	}

	private static final FilenameFilter jarFilter = new FilenameFilter()
	{
		@Override
		public boolean accept(File dir, String name) { return name.toLowerCase().endsWith("jar"); }
	};
	
	private void loadSimulations()
	{
		simulations = new ArrayList<SimulationInfo>();
		for (File jar : homeDirectory.listFiles(jarFilter))
		{
			SimulationInfo info = new SimulationInfo(jar);
			simulations.add(info);
		}
	}


	private void setupLayout()
	{
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Java Simulation Home");
		
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weighty = 1;
		constraints.gridwidth = 2;
		
		DefaultListModel model = new DefaultListModel();
		if (simulations != null)
			for(SimulationInfo info : simulations)
				model.addElement(info);
		
		simulationList = new JList(model);
		Font listFont = simulationList.getFont();
		simulationList.setFont(new Font(listFont.getName(), listFont.getStyle(), (int) (listFont.getSize()*1.50)));
		simulationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		simulationList.setLayoutOrientation(JList.VERTICAL);
		simulationList.setVisibleRowCount(-1);
		simulationList.addListSelectionListener(this);
		constraints.weightx = 0.25;
		getContentPane().add(simulationList, constraints);
		
		simulationList.addMouseListener(new MouseAdapter()
		{
		    public void mouseClicked(MouseEvent evt)
		    {
		        if (evt.getClickCount() == 2)
		            runSelected();
		    }
		});
		
		simulationList.addKeyListener(new KeyListener()
		{
			
			@Override
			public void keyTyped(KeyEvent e) { }
			
			@Override
			public void keyReleased(KeyEvent e) { }
			
			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					runSelected();
			}
		});
		
		simulationInfoPanel = new JPanel();
		simulationInfoPanel.setLayout(new BoxLayout(simulationInfoPanel, BoxLayout.Y_AXIS));
		simulationInfoPanel.setBackground(new Color(0.8f, 0.8f, 0.8f));
		simulationInfoPanel.setBorder(BorderFactory.createMatteBorder(0, 8, 0, 0, new Color(0.15f, 0.15f, 0.15f)));
		constraints.weightx = 0.75;
		getContentPane().add(simulationInfoPanel, constraints);
		
		this.name = new JLabel();
		this.name.setFont(this.name.getFont().deriveFont(26f).deriveFont(Font.BOLD));
		this.date = new JLabel();
		this.author = new JLabel();
		this.version = new JLabel();
		this.description = new JLabel();
		simulationInfoPanel.add(this.name);
		simulationInfoPanel.add(this.date);
		simulationInfoPanel.add(this.author);
		simulationInfoPanel.add(this.version);
		simulationInfoPanel.add(this.description);
		
		this.run = new JButton("Run");
		this.run.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				runSelected();
			}
		});
		this.run.setVisible(false);
		simulationInfoPanel.add(this.run);
	}
	
	private void runSelected()
	{
		run((SimulationInfo) simulationList.getSelectedValue());
	}
	
	public void run(SimulationInfo info)
	{
		System.out.println("Running " + info.getName());
		
		Class<? extends Simulation> simClass = null;
		
		try
		{
			simClass = SimulationInfo.loadSimulationClass(info.getFile());
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			System.out.println("Initializing " + simClass.toString());
			try
			{
				final Simulation simulation = simClass.getDeclaredConstructor(SimulationInfo.class).newInstance(info);
				
				simulation.setHome(this);
				simulation.initialize();
				simulation.resetAction();
			}
			catch(IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
			{
				e.printStackTrace();
			}
		}
		catch(InstantiationException e)
		{
			e.printStackTrace();
		}
		catch(IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		this.run.setVisible(true);
		SimulationInfo info = (SimulationInfo) simulationList.getSelectedValue();
		this.name.setText(info.getName());
		this.date.setText(info.getDate());
		this.author.setText(info.getAuthor());
		this.version.setText("Version " + info.getVersion());
		this.description.setText("<html><p>" + info.getDescription() + "</p></html>");
	}
}