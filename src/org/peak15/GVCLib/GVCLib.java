package org.peak15.GVCLib;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

/**
 * Shared objects for GVC.
 */
public class GVCLib {
	private Map<String, Command> commands = new HashMap<String, Command>();
	private Path rootDir;
	private Path configDir;
	private Path revDir;
	private Path fsDir;
	private Revision currentRev;
	
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
	 * Calls findRootDirectory(Path) with the current directory.
	 * @return True if the directory was found and set, false otherwise.
	 */
	public boolean findRootDirectory() throws GVCException {
		return this.findRootDirectory(new File(".").getAbsoluteFile().getParentFile().toPath());
	}
	
	/**
	 * Search all the parent folders of the given directory for a .GVC folder,
	 * then sets it's parent folder as the root folder for this instance.
	 * Also sets the config, revision, and filestore directories.
	 * @param startDir Directory to being searching inside of and in parents of.
	 * @return True if the directory was found and set, false otherwise.
	 */
	public boolean findRootDirectory(Path startDir) throws GVCException {
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(startDir, ".GVC")) {
			Iterator<Path> iter = ds.iterator();
			
			if(iter.hasNext()) {
				Path dir = iter.next().toAbsolutePath().getParent();
				this.rootDir = dir;
				this.configDir = new File(dir.toFile(), ".GVC").toPath();
				this.revDir = new File(this.configDir.toFile(), "revisions").toPath();
				this.fsDir = new File(this.configDir.toFile(), "filestore").toPath();
				
				// Create these directories if they don't exist.
				if(!this.revDir.toFile().exists())
					this.revDir.toFile().mkdir();
				if(!this.fsDir.toFile().exists())
					this.fsDir.toFile().mkdir();
				
				return true;
			}
			else {
				// Lets look in the parent folder.
				if(startDir.toAbsolutePath().getParent() != null) {
					return findRootDirectory(startDir.toAbsolutePath().getParent());
				}
			}
		} catch (IOException e) {
			throw new GVCException(e);
		}
		
		return false;
	}
	
	/**
	 * Get the root folder used by this GVCLib instance.
	 * @return The root folder used by this GVCLib instance.
	 */
	public Path getRootDirectory() {
		return this.rootDir;
	}
	
	/**
	 * Get the .GVC folder used by this GVCLib instance.
	 * @return The .GVC folder used by this GVCLib instance.
	 */
	public Path getConfigDirectory() {
		return this.configDir;
	}
	
	/**
	 * Get the revision folder used by this GVCLib instance.
	 * @return The revision folder used by this GVCLib instance.
	 */
	public Path getRevisionDirectory() {
		return this.revDir;
	}
	
	/**
	 * Get the filestore folder used by this GVCLib instance.
	 * @return The filestore folder used by this GVCLib instance.
	 */
	public Path getFilestoreDirectory() {
		return this.fsDir;
	}
	
	/**
	 * Gets a set of all files in the repository.
	 * @return Map with keys as hashes and values as files.
	 */
	public Map<String, Set<File>> getFileSet() throws GVCException {
		FileSetVisitor fsv = new FileSetVisitor(this);
		
		try {
			Files.walkFileTree(this.getRootDirectory(), fsv);
		} catch (IOException e) {
			throw new GVCException(e);
		}
		
		return fsv.getFileSet();
	}
	
	/**
	 * Print the given file set in a somewhat eye-pleasing manner.
	 * @param File set to print.
	 */
	public void printFileSet(Map<String, Set<File>> fileSet) {
		for(String hash : fileSet.keySet()) {
			out.print(hash + ":");
			for(File file : fileSet.get(hash)) {
				out.print(" " + file.getPath());
			}
			out.print("\n");
		}
	}
	
	/**
	 * Makes an absolute file relative to the root directory.
	 * @param abs Absolute file.
	 * @return Equivalent file relative to the root directory.
	 */
	public File makeRelative(File abs) {
		return new File(this.makeRelative(abs.toString()));
	}
	
	/**
	 * Makes an absolute path relative to the root directory.
	 * @param abs Absolute path.
	 * @return Equivalent path relative to the root directory.
	 */
	public Path makeRelative(Path abs) {
		return this.makeRelative(abs.toFile()).toPath();
	}
	
	private String makeRelative(String abs) {
		String root = this.getRootDirectory().toString();
		
		// trim any ending slash on root
		if(root.endsWith(File.separator)){
			root = root.substring(0, root.length() - 1);
		}
		
		// remove root from abs
		// this assumes we actually have a path containing root
		// give us stupid paths and pay the price. ;)
		abs = abs.substring(root.length() + 1);
		
		return abs;
	}
	
	/**
	 * Makes a file relative to the root directory into an absolute file.
	 * @param rel Relative file.
	 * @return Equivalent absolute file.
	 */
	public File makeAbsolute(File rel) {
		return new File(this.makeAbsolute(rel.toString()));
	}
	
	/**
	 * Makes a path relative to the root directory into an absolute file.
	 * @param rel Relative path.
	 * @return Equivalent absolute path.
	 */
	public Path makeAbsolute(Path rel) {
		return this.makeAbsolute(rel.toFile()).toPath();
	}
	
	private String makeAbsolute(String rel) {
		String root = this.getRootDirectory().toString();
		
		// add a trailing slash to root
		if(!root.endsWith(File.separator)){
			root += File.separator;
		}
		
		rel = root + rel;
		
		return rel;
	}
	
	/**
	 * Save a revision to disk.
	 * @param rev Revision to save.
	 */
	public void saveRevision(Revision rev) throws GVCException {
		File revF = new File(this.getRevisionDirectory().toFile(), rev.getHash() + ".json");
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(revF))) {
			writer.write(rev.getSerialized());
		}
		catch (IOException e) {
			throw new GVCException(e);
		}
	}
	
	/**
	 * Sets current revision for this repository and writes this value to disk.
	 * @param rev Revision to set as current revision.
	 */
	public void setCurrentRevision(Revision rev) throws GVCException {
		this.currentRev = rev;
		File revF = new File(this.getConfigDirectory().toFile(), "current_revision");
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(revF))) {
			writer.write(rev.getHash());
		}
		catch (IOException e) {
			throw new GVCException(e);
		}
	}
	
	/**
	 * Get the current revision for this repository, loaded from disk if necessary.
	 * @return Current revision for this repository.
	 */
	public Revision getCurrentRevision() {
		return this.currentRev;
	}
}
