package org.peak15.GVCLib.commands;

import org.peak15.GVCLib.*;

public class Commit implements Command {
	private GVCLib gvclib;
	
	public Commit(GVCLib gvclib) {
		this.gvclib = gvclib;
	}
	
	@Override
	public String getName() {
		return "commit";
	}

	@Override
	public String getHelp() {
		return "Commits the current state of the file system to the repository.\n" + 
				"Note: This is not git. commit will always update the repository\n" +
				"to the current state of the file system. There is no need nor ability to add\n" +
				"or remove individual files and directories.\n" +
				"Usage: commit \"comment\"";
	}

	@Override
	public boolean run(String[] args) throws GVCException {
		if(args == null || args.length <= 0 || args[0] == null || args[0].length() <= 0) {
			gvclib.err.println("You must specify a comment.");
			return false;
		}
		
		if(!gvclib.findRootDirectory()) {
			gvclib.err.println("Could not find the root directory," + 
					"this must not be a GVC repository.");
			return false;
		}
		
		if(gvclib.getCurrentRevision() == null) {
			gvclib.err.println("There is no current revision,\n" +
					"this GVC repositroy must be broken.");
			return false;
		}
		
		Revision newRev = new Revision(gvclib, gvclib.getCurrentRevision(), gvclib.getFileSet(), args[0]);
		gvclib.setCurrentRevision(newRev);
		gvclib.saveRevision(newRev);
		
		return false;
	}

}
