package org.peak15.GVCLib.commands;

import org.peak15.GVCLib.GVCException;
import org.peak15.GVCLib.GVCLib;

public class Init implements Command {
	
	private static final String name = "init";
	private static final String help = "Initializes a GVC repository in the current directory.";
	
	private GVCLib gvclib;
	
	/**
	 * Create an instance of the init command using the specified GVCLib instance.
	 * @param out
	 */
	public Init(GVCLib gvclib) {
		this.gvclib = gvclib;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getHelp() {
		return help;
	}

	@Override
	public void run(String[] args) throws GVCException {
		// Check all parent folders up to the filesystem boundary for a .GVC folder.
		gvclib.out.println("Durpin around...");
	}

}
