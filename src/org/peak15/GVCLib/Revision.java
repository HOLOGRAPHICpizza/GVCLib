package org.peak15.GVCLib;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import com.twmacinta.util.MD5;

/**
 * Contains a list of files that are different from the parent revision.
 */
public class Revision {
	private Revision parent;
	private Date date;
	private String comment;
	private String hash;
	private Map<String, Set<File>> filesAdded;
	private Map<String, Set<File>> filesRemoved;
	
	private String serialized;
	
	/**
	 * Create a revision as a child of another revision.
	 * @param parent Parent revision, or null if this is the root revision.
	 */
	public Revision(Revision parent, Map<String, Set<File>> fileSet, String comment) throws GVCException {
		this.parent = parent;
		this.date = new Date();
		this.comment = comment;
		
		if(parent == null) {
			// This is the initial revision.
			this.filesAdded = fileSet;
		}
		else {
			// Diff the given file set with parent's file set to get added and removed.
			//TODO: Implement diff operation.
		}
		
		if(this.filesAdded == null && this.filesRemoved == null) {
			throw new GVCException("Will not create a revision with no changes.");
		}
		
		this.serialize();
		
		// Generate hash.
		MD5 md5 = new MD5();
	    try {
			md5.Update(this.serialized, null);
		} catch (UnsupportedEncodingException e) {
			throw new GVCException(e);
		}
	    this.hash = md5.asHex();
	}
	
	private void serialize() throws GVCException {
		//TODO: make objectMapper global
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode rootNode = objectMapper.createObjectNode();
		
		// Parent
		if(this.parent == null)
			rootNode.put("parent", "null");
		else
			rootNode.put("parent", this.parent.getHash());
		
		// Date
		rootNode.put("date", this.date.toString());
		
		// Comment
		rootNode.put("comment", this.comment);
		
		// Files Added
		/* This is a JSON "object", it looks something like this:
		 * 	{
		 * 		filesAdded: {
		 * 			sd5df5s2: [file1, file2],
		 * 			f52s556c: [file3]
		 * 		}
		 * 	}
		 */
		if(this.filesAdded == null) {
			rootNode.put("filesAdded", "null");
		}
		else {
			ObjectNode addedOb = rootNode.putObject("filesAdded");
			for(String fHash : this.filesAdded.keySet()) {
				// this will be an array of filenames for this hash
				ArrayNode aNode = addedOb.putArray(fHash);
				
				for(File file : this.filesAdded.get(fHash)) {
					aNode.add(file.toString());
				}
			}
		}
		
		// Files Removed
		if(this.filesRemoved == null) {
			rootNode.put("filesRemoved", "null");
		}
		else {
			ObjectNode removedOb = rootNode.putObject("filesRemoved");
			for(String fHash : this.filesRemoved.keySet()) {
				// this will be an array of filenames for this hash
				ArrayNode rNode = removedOb.putArray(fHash);
				
				for(File file : this.filesRemoved.get(fHash)) {
					rNode.add(file.toString());
				}
			}
		}
		
		// And write it all out to a string.
		try {
			this.serialized = objectMapper.writeValueAsString(rootNode);
		} catch (Exception e) {
			throw new GVCException(e);
		}
	}
	
	/**
	 * Get the hash of the revision.
	 * @return This revison's hash.
	 */
	public String getHash() {
		return this.hash;
	}
	
	/**
	 * Get this revision's comment.
	 * @return This revision's comment.
	 */
	public String getComment() {
		return this.comment;
	}
	
	/**
	 * Get the date this revision was created.
	 * @return Date revision was created.
	 */
	public Date getDate() {
		return this.date;
	}
	
	/**
	 * Get the serialized form of this revision.
	 * @return Serialized form of this revision.
	 */
	public String getSerialized() {
		return this.serialized;
	}
}
