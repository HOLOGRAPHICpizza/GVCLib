package org.peak15.GVCLib.commands;

import org.peak15.GVCLib.GVCException;
import org.peak15.GVCLib.GVCLib;

public class Ignore implements Command {
	private GVCLib gvclib;
	
	public Ignore(GVCLib gvclib) {
		this.gvclib = gvclib;
	}

	@Override
	public String getName() {
		return "ignore";
	}

	@Override
	public String getHelp() {
		return "Adds patterns to ignore in this repository.\n" +
				"Ignores are stored in the .GVC/ignore file.\n" +
				"Lines starting with # in this file are comments.\n" +
				"Usage: ignore \"pattern1\" [\"pattern2\"] [...]\n" +
				"Example: ignore \"lolcats/\" \"randomCrap.txt\" \"*.bak\"\n";
	}

	@Override
	public boolean run(String[] args) throws GVCException {
		// TODO Auto-generated method stub
		return false;
	}

}
