package org.peak15.GVCLib;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.peak15.GVCLib.commands.Command;

import com.twmacinta.util.MD5;

/**
 * Shared objects for GVC.
 */
public class GVCLib {
	private Map<String, Command> commands = new HashMap<String, Command>();
	private File GVCDir;
	
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
	 * @return True if command was successful, false otherwise.
	 * @param args Arguments to the command.
	 * @throws GVCException
	 */
	public boolean runCommand(String name, String[] args) throws GVCException {
		//Run the command
		return getCommand(name).run(args);
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
	
	/**
	 * Search all the parent folders of the given directory for a .GVC folder,
	 * then set that folder as the .GVC folder for this GVCLib instance.
	 * @param startDir Directory to being searching inside of and in parents of.
	 * @return True if the directory was found and set, false otherwise.
	 */
	public boolean findGVCDirectory(File startDir) throws GVCException {
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(startDir.toPath(), ".GVC")) {
			Iterator<Path> iter = ds.iterator();
			
			if(iter.hasNext()) {
				// Get a relative path name.
				Path dir = iter.next();
				Path currentDir = new File(".").getAbsoluteFile().getParentFile().toPath();
				dir = currentDir.relativize(dir);
				this.GVCDir = new File(".", dir.toString());
				//out.println(this.GVCDir);
				//System.exit(0);
				return true;
			}
			else {
				// Lets look in the parent folder.
				if(startDir.getParentFile() != null) {
					findGVCDirectory(startDir.getParentFile());
				}
			}
		} catch (IOException e) {
			throw new GVCException(e);
		}
		
		return false;
	}
	
	/**
	 * Get the .GVC folder used by this GVCLib instance.
	 * @return The .GVC folder used by this GVCLib instance.
	 */
	public File getGVCDirectory() {
		return this.GVCDir;
	}
	
	/**
	 * Gets a set of all files in the repository.
	 * @return Map with keys as hashes and values as files.
	 */
	public Map<byte[], Set<File>> getFileSet() throws GVCException {
		FileSetVisitor fsv = new FileSetVisitor();
		
		try {
			Files.walkFileTree(this.GVCDir.getParentFile().toPath(), fsv);
		} catch (IOException e) {
			throw new GVCException(e);
		}
		
		return fsv.getFileSet();
	}
	
	/**
	 * Print the given file set in a somewhat eye-pleasing manner.
	 * @param File set to print.
	 */
	public void printFileSet(Map<byte[], Set<File>> fileSet) {
		for(byte[] hash : fileSet.keySet()) {
			out.print(MD5.asHex(hash) + ":");
			for(File file : fileSet.get(hash)) {
				out.print(" " + file.getPath());
			}
			out.print("\n");
		}
	}
}
