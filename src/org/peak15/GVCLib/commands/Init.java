package org.peak15.GVCLib.commands;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import org.peak15.GVCLib.*;

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
	public boolean run(String[] args) throws GVCException {
		Path currentDir = new File(".").getAbsoluteFile().getParentFile().toPath();
		File GVCDir = new File(currentDir.toFile(), ".GVC");
		if(GVCDir.exists()) {
			gvclib.err.println(".GVC already exists in this directory. Will not continue.");
			//TODO: make it actually not continue
			//return false;
		}
		
		GVCDir.mkdir();
		gvclib.findRootDirectory(currentDir);
		
		Map<String, Set<File>> fileSet = gvclib.getFileSet();
		
		//gvclib.out.println("Current file set:");
		//gvclib.printFileSet(fileSet);
		
		Revision rev = new Revision(null, fileSet, "Initial revision.");
		
		gvclib.saveRevision(rev);
		gvclib.setCurrentRevision(rev);
		
		return true;
	}

}
