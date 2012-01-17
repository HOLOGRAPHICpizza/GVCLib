package org.peak15.GVCLib;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Contains a list of files that are different from the parent revision.
 */
public class Revision {
	private GVCLib gvclib;
	private Revision parent;
	private Date date;
	private String comment;
	private int hash;
	private Map<Integer, Set<File>> filesAdded;
	private Map<Integer, Set<File>> filesDeleted;
	
	/**
	 * Create a revision as a child of another revision.
	 * @param parent Parent revision, or null if this is the root revision.
	 */
	public Revision(GVCLib gvclib, Revision parent, String comment) {
		this.parent = parent;
		this.date = new Date();
		this.comment = comment;
		
		// Get file set from gvclib.
		// Diff it with this revision's file set to get added and removed.
		
		//TODO: Generate actual hash.
		this.hash = 1337;
	}
}
