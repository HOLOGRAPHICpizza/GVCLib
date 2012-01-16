package org.peak15.GVCLib;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.peak15.GVCLib.commands.Command;

/**
 * Shared objects for GVC.
 */
public class GVCLib {
	private Map<String, Command> commands = new HashMap<String, Command>();
	
	public PrintStream out = System.out;
	public PrintStream err = System.err;
	
	/**
	 * Specify the output streams this GVCLib instance should print to.
	 * @param out Standard output stream.
	 * @param err Error out put stream.
	 */
	public void setOutputStreams(PrintStream out, PrintStream err) {
		this.out = out;
		this.err = err;
	}
	
	/**
	 * Registers a command.
	 * @param command Command to register.
	 */
	public void registerCommand(Command command) {
		commands.put(command.getName(), command);
	}
	
	/**
	 * Runs a command by name.
	 * @param name Command to run.
	 * @param args Arguments to the command.
	 * @throws GVCException
	 */
	public void runCommand(String name, String[] args) throws GVCException {
		//Run the command
		getCommand(name).run(args);
	}
	
	/**
	 * Gets a command by name.
	 * @param name Command to get.
	 * @return Command matching specified name.
	 */
	public Command getCommand(String name) throws GVCException {
		if(commands.containsKey(name)) {
			// Return the command
			return commands.get(name);
		}
		else {
			throw new GVCException(name + " is not a registered command.");
		}
	}
	
	/**
	 * Get the names of all registered commands.
	 * @return Set of names of registered strings.
	 */
	public Set<String> getCommandNames() {
		return commands.keySet();
	}
}
