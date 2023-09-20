package sensorDataReader;

import javax.swing.*;

	
public class BeaconMain extends JFrame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BeaconMain(BeaconPannel pannel)
	{
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(800, 800);
		this.setTitle("KBeacon Sensor Reader");
		this.add(pannel);
		this.setResizable(false);
		this.setVisible(true);
	}

	public static void main(String[] args)
	{
		BeaconPannel panel = new BeaconPannel();
		new BeaconMain(panel);
	}
}
