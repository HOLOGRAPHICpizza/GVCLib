package org.peak15.GVCLib.commands;

import org.peak15.GVCLib.GVCException;
import org.peak15.GVCLib.GVCLib;

/**
 * Command to print help texts.
 */
public class Help implements Command {
	
	private static final String name = "help";
	private static final String help = "Prints help for the specified command.\n"
			+ "Usage: help command";
	
	private GVCLib gvclib;
	
	/**
	 * Create an instance of the help command using the specified GVCLib instance.
	 * @param out
	 */
	public Help(GVCLib gvclib) {
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
		// Print help for the command specified.
		if((args != null) && (args.length > 0)) {
			gvclib.out.println(gvclib.getCommand(args[0]).getHelp());
		}
		else {
			// Print list of commands if no command specified.
			gvclib.out.println("Registered commands:");
			for(String m : gvclib.getCommandNames()) {
				gvclib.out.println("\t" + m);
			}
		}
		
		return true;
	}

}
