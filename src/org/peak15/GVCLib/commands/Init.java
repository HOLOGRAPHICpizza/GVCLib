package org.peak15.GVCLib.commands;

import java.io.File;
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
		File currentDir = new File(".").getAbsoluteFile().getParentFile();
		File GVCDir = new File(currentDir, ".GVC");
		if(GVCDir.exists()) {
			gvclib.err.println(".GVC already exists in this directory. Will not continue.");
			//return false;
		}
		
		GVCDir.mkdir();
		gvclib.findGVCDirectory(currentDir);
		
		Map<byte[], Set<File>> fileSet = gvclib.getFileSet();
		gvclib.printFileSet(fileSet);
		
		return true;
	}

}
