package org.peak15.GVCLib.commands;

import org.peak15.GVCLib.GVCException;

/**
 * A GVC command.
 */
public interface Command {
	/**
	 * Returns the name of the command.
	 * @return The name of the command.
	 */
	public String getName();
	
	/**
	 * Returns the help text for the command.
	 * @return The help text for this command.
	 */
	public String getHelp();
	
	/**
	 * Runs the command with the given arguments.
	 * @param args Arguments to the command.
	 * @throws GVCException
	 */
	public void run(String[] args) throws GVCException;
}
