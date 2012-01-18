package org.peak15.GVCLib;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.twmacinta.util.*;

public class FileSetVisitor implements FileVisitor<Path> {
	
	private GVCLib gvclib;
	private Map<String, Set<File>> fileSet = new HashMap<String, Set<File>>();
	
	/**
	 * Construct a FileSetVisitor with the given GVCLib instance.
	 * @param gvclib GVCLib instance to use.
	 */
	public FileSetVisitor(GVCLib gvclib) {
		this.gvclib = gvclib;
	}
	
	/**
	 * Get the file set that this FileVisitor generates.
	 * @return Generated file set. Will only be populated after directory is walked.
	 */
	public Map<String, Set<File>> getFileSet() {
		return fileSet;
	}
	
	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		//TODO: implement ignore file
		
		// MD5sum the files and add them to the set.
		String hash = MD5.asHex(MD5.getHash(file.toFile()));
		
		// make it relative to the root folder.
		file = gvclib.makeRelative(file);
		
		// Is this file already in the set?
		if(fileSet.containsKey(hash)) {
			// Yes, so add this path to it.
			fileSet.get(hash).add(file.toFile());
		}
		else {
			// Add a new entry to the set.
			Set<File> set = new HashSet<File>();
			
			set.add(file.toFile());
			fileSet.put(hash, set);
		}
		
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;
	}
}
