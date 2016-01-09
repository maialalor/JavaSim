package com.marklalor.javasim.text;

/**
 * 
 * A simple Java Console for your application (Swing version)
 * Requires Java 1.1.5 or higher
 * 
 * Disclaimer the use of this source is at your own risk. 
 * 
 * Permission to use and distribute into your own applications
 * 
 * RJHM van den Bergh , rvdb@comweb.nl
 * 
 * Modified by Mark Lalor to integrate with JavaSim
 * 
 **/

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.marklalor.javasim.JavaSim;

public class Console extends WindowAdapter implements WindowListener, Runnable
{
	private JFrame frame;
	private JTextArea textArea;
	
	private Thread outThread;
	private Thread errThread;
	
	private boolean quit;
	
	private final PipedInputStream outInputStream = new PipedInputStream();
	private final PipedInputStream errInputStream = new PipedInputStream();
	
	public Console()
	{
		frame = new JFrame("JavaSim Console");
		frame.setSize(500, 500);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
		frame.setAutoRequestFocus(false);
		
		frame.addWindowListener(this);
		
		try
		{
			PipedOutputStream pipedOut = new PipedOutputStream(this.outInputStream);
			if (JavaSim.CONSOLE_BIND) System.setOut(new PrintStream(pipedOut, true));
		}
		catch(java.io.IOException io)
		{
			textArea.append("Couldn't redirect STDOUT to this console\n" + io.getMessage());
		}
		catch(SecurityException se)
		{
			textArea.append("Couldn't redirect STDOUT to this console\n" + se.getMessage());
		}
		
		try
		{
			PipedOutputStream pipedOut = new PipedOutputStream(this.errInputStream);
			if (JavaSim.CONSOLE_BIND) System.setErr(new PrintStream(pipedOut, true));
		}
		catch(java.io.IOException io)
		{
			textArea.append("Couldn't redirect STDERR to this console\n" + io.getMessage());
		}
		catch(SecurityException se)
		{
			textArea.append("Couldn't redirect STDERR to this console\n" + se.getMessage());
		}
		
		quit = false; // signals the Threads that they should exit
		
		// Starting two seperate threads to read from the PipedInputStreams
		outThread = new Thread(this);
		outThread.setDaemon(true);
		outThread.start();
		
		errThread = new Thread(this);
		errThread.setDaemon(true);
		errThread.start();
		
		System.out.println("Console Started…");
	}
	
	public synchronized void windowClosed(WindowEvent evt)
	{
		quit = true;
		this.notifyAll(); // stop all threads
		try
		{
			outThread.join(1000);
			outInputStream.close();
		}
		catch(Exception e)
		{
		}
		try
		{
			errThread.join(1000);
			errInputStream.close();
		}
		catch(Exception e)
		{
		}
		System.exit(0);
	}
	
	public synchronized void run()
	{
		try
		{
			while(Thread.currentThread() == outThread)
			{
				try
				{
					this.wait(100);
				}
				catch(InterruptedException ie)
				{
				}
				if(outInputStream.available() != 0)
				{
					String input = this.readLine(outInputStream);
					textArea.append(input);
				}
				if(quit)
					return;
			}
			
			while(Thread.currentThread() == errThread)
			{
				try
				{
					this.wait(100);
				}
				catch(InterruptedException ie)
				{
				}
				if(errInputStream.available() != 0)
				{
					String input = this.readLine(errInputStream);
					textArea.append(input);
				}
				if(quit)
					return;
			}
		}
		catch(Exception e)
		{
			textArea.append("Internal console error: " + e);
		}
	}
	
	public JFrame getFrame()
	{
		return frame;
	}
	
	public void setVisible(boolean visible)
	{
		frame.setVisible(visible);
	}
	
	public synchronized String readLine(PipedInputStream in) throws IOException
	{
		String input = "";
		do
		{
			int available = in.available();
			if(available == 0)
				break;
			byte b[] = new byte[available];
			in.read(b);
			input = input + new String(b, 0, b.length);
		}
		while(!input.endsWith("\n") && !input.endsWith("\r\n") && !quit);
		return input;
	}
}