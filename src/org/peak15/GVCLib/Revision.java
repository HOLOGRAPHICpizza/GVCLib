package org.peak15.GVCLib;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import com.twmacinta.util.MD5;

/**
 * Contains a list of files that are different from the parent revision.
 * This class is perhaps designed badly, all objects are serialized when they are created,
 * and when an object is loaded so are all of its parents, and all these are stored in memory
 * in their full serialized form. For large repositories this could use a lot of memory.
 */
public class Revision {
	private Revision parent;
	private Date date;
	private String comment;
	private String hash;
	private Map<String, Set<File>> filesAdded;
	private Map<String, Set<File>> filesRemoved;
	
	private String serialized;
	
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static JsonFactory jsonFactory = new MappingJsonFactory();
	private static DateFormat df = DateFormat.getDateTimeInstance();
	
	/**
	 * Create a revision as a child of another revision.
	 * @param parent Parent revision, or null if this is the root revision.
	 */
	public Revision(GVCLib gvclib, Revision parent, Map<String, Set<File>> fileSet, String comment) throws GVCException {
		this.parent = parent;
		this.date = new Date();
		this.comment = comment;
		
		if(parent == null) {
			// This is the initial revision.
			this.filesAdded = fileSet;
		}
		else {
			// Diff the given file set with parent's file set to get added and removed.
			List<Map<String, Set<File>>> diffList = filesetGetDiff(parent.getFileset(gvclib), fileSet);
			this.filesAdded = diffList.get(0);
			this.filesRemoved = diffList.get(1);
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
	
	/**
	 * Loads a revision (and all parents) from a json file.
	 * @param revF Json file to load revision from.
	 */
	public Revision(GVCLib gvclib, File revF) throws GVCException {
		
		JsonNode rootNode;
		try {
			rootNode = objectMapper.readValue(revF, JsonNode.class);
		} catch (Exception e) {
			throw new GVCException(e);
		}
		
		// Parent(s)
		String parentS = rootNode.path("parent").getTextValue();
		if(parentS.equals("null")) {
			this.parent = null;
		}
		else {
			this.parent = new Revision(gvclib, new File(gvclib.getRevisionDirectory().toFile(), parentS + ".json"));
		}
		
		// Date
		try {
			this.date = df.parse(rootNode.path("date").getTextValue());
		} catch (ParseException e) {
			throw new GVCException(e);
		}
		
		// Comment
		this.comment = rootNode.path("comment").getTextValue();
		
		// Hash
		this.hash = revF.getName().replace(".json", "");
		
		// Files Added
		JsonNode fAddNode = rootNode.path("filesAdded");
		this.filesAdded = parseFileset(fAddNode);
		
		// Files Removed
		JsonNode fRmNode = rootNode.path("filesRemoved");
		this.filesRemoved = parseFileset(fRmNode);
		
		// Serialized
		this.serialized = gvclib.fileToString(revF);
	}
	
	private Map<String, Set<File>> parseFileset(JsonNode fsNode) {
		Map<String, Set<File>> fileSet = new HashMap<String, Set<File>>();
		
		Iterator<String> fHashes = fsNode.getFieldNames();
		while(fHashes.hasNext()) {
			String fHash = fHashes.next();
			JsonNode fNameArray = fsNode.path(fHash);
			Iterator<JsonNode> fNames = fNameArray.getElements();
			while(fNames.hasNext()) {
				String fName = fNames.next().getTextValue();
				
				// Is this file already in the set?
				if(fileSet.containsKey(fHash)) {
					// Yes, so add this path to it.
					fileSet.get(fHash).add(new File(fName));
				}
				else {
					// Add a new entry to the set.
					Set<File> set = new HashSet<File>();
					
					set.add(new File(fName));
					fileSet.put(fHash, set);
				}
			}
		}
		
		return fileSet;
	}
	
	private void serialize() throws GVCException {
		JsonGenerator jsonGenerator;
		StringWriter stringWriter = new StringWriter();
		try {
			jsonGenerator = jsonFactory.createJsonGenerator(stringWriter);
		} catch (IOException e) {
			throw new GVCException(e);
		}
		jsonGenerator.useDefaultPrettyPrinter();
		
		ObjectNode rootNode = objectMapper.createObjectNode();
		
		// Parent
		if(this.parent == null)
			rootNode.put("parent", "null");
		else
			rootNode.put("parent", this.parent.getHash());
		
		// Date
		rootNode.put("date", df.format(this.date));
		
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
					// Normalize the filesystem separator to /
					String fileS = file.toString().replace(File.separator, "/");
					aNode.add(fileS);
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
					// Normalize the filesystem separator to /
					String fileS = file.toString().replace(File.separator, "/");
					rNode.add(fileS);
				}
			}
		}
		
		// And write it all out to a string.
		try {
			jsonGenerator.writeObject(rootNode);
			this.serialized = stringWriter.getBuffer().toString();
		} catch (Exception e) {
			throw new GVCException(e);
		}
	}
	
	/**
	 * Finds files added and removed between two file sets.
	 * Files whose filenames have changed will be listed in both added and removed.
	 * @param oldFs File set to compare to.
	 * @param newFs File set to show differences in.
	 * @return List where first map is files added and second map is files removed.
	 */
	public static List<Map<String, Set<File>>> filesetGetDiff(Map<String, Set<File>> oldFs, Map<String, Set<File>> newFs) {
		Map<String, Set<File>> added = new HashMap<String, Set<File>>();
		Map<String, Set<File>> removed = new HashMap<String, Set<File>>();
		
		// Look for files in both sets, and files that are new.
		for(String hash : newFs.keySet()) {
			if(oldFs.containsKey(hash)) {
				// This file is in both old and new, check if the filenames are different
				if(!oldFs.get(hash).equals(newFs.get(hash))) {
					// Filenames have changed, add this to both the removed and added list.
					removed.put(hash, oldFs.get(hash));
					added.put(hash, newFs.get(hash));
				}
			}
			else {
				// This file is new.
				added.put(hash, newFs.get(hash));
			}
		}
		
		// Look for files that have been removed.
		for(String hash : oldFs.keySet()) {
			if(!newFs.containsKey(hash)) {
				// This file is in the old set but not the new set.
				removed.put(hash, oldFs.get(hash));
			}
		}
		
		List<Map<String, Set<File>>> diffList = new ArrayList<Map<String, Set<File>>>();
		diffList.add(added);
		diffList.add(removed);
		return diffList;
	}
	
	/**
	 * Applies the added and removed maps generated by fileSetGetDiff.
	 * @param add Map of files to add to the set.
	 * @param remove Map of files to remove from the set.
	 * @return Fileset with the diff applied.
	 */
	public static Map<String, Set<File>> filesetApplyDiff(
			GVCLib gvclib,
			Map<String, Set<File>> fileset,
			Map<String, Set<File>> add,
			Map<String, Set<File>> remove) {
		
		// Remove files from the set.
		for(String hash : remove.keySet()) {
			if(fileset.containsKey(hash)) {
				fileset.remove(hash);
			}
			else {
				gvclib.err.println("Warning: Attempted to remove " + hash +
						"\nfrom a fileset not containing it.\n" +
						"There is probably a corrupted revision file. Continuing with remaining files...");
			}
		}
		
		// Add files to the set.
		for(String hash : add.keySet()) {
			if(!fileset.containsKey(hash)) {
				fileset.put(hash, add.get(hash));
			}
			else {
				gvclib.err.println("Warning: Attempted to add " + hash +
						"\nto a fileset already containing it.\n" +
						"There is probably a corrupted revision file. Continuing with remaining files...");
			}
		}
		
		return fileset;
	}
	
	/**
	 * Get the complete fileset of this revision with all parent diffs applied.
	 * @return Complete fileset of this revision.
	 */
	public Map<String, Set<File>> getFileset(GVCLib gvclib) {
		if(this.parent != null) {
			return filesetApplyDiff(gvclib, parent.getFileset(gvclib), this.filesAdded, this.filesRemoved);
		}
		else {
			return this.filesAdded;
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
