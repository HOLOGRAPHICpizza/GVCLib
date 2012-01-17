package org.peak15.GVCLib.commands;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import org.peak15.GVCLib.GVCException;
import org.peak15.GVCLib.GVCLib;

public class Debug implements Command {
	
	private GVCLib gvclib;
	
	public Debug(GVCLib gvclib) {
		this.gvclib = gvclib;
	}
	
	@Override
	public String getName() {
		return "debug";
	}

	@Override
	public String getHelp() {
		return "Debugging functions.";
	}

	@Override
	public boolean run(String[] args) throws GVCException {
			if(args != null && args.length > 0) {
				if(args[0].equals("fileset")) {
					fileSet();
				}
			}
		return true;
	}
	
	private void fileSet() throws GVCException {
		Path currentDir = new File(".").getAbsoluteFile().getParentFile().toPath();
		gvclib.out.println("Current directory: " + currentDir.toString());
		
		if(!gvclib.findRootDirectory(currentDir)) {
			gvclib.err.println("Couldn't find .GVC directory.");
			return;
		}
		gvclib.out.println("root directory: " + gvclib.getRootDirectory().toString());
		
		Map<String, Set<File>> fileSet = gvclib.getFileSet();
		gvclib.printFileSet(fileSet);
	}
	
}
