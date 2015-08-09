package inc.morsecode;


import inc.morsecode.etc.ArrayUtils;
import inc.morsecode.etc.Mutex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import util.json.JsonArray;
import util.json.JsonObject;
import util.json.JsonValue;

import com.nimsoft.nimbus.NimConfig;
import com.nimsoft.nimbus.NimException;
import com.nimsoft.nimbus.NimLog;
import com.nimsoft.nimbus.PDS;

enum MergeRule {
	CLEAR("clear")
	, DELETE("delete")
	, OVERWRITE("overwrite")
	, DEFAULT("")
	;
	
	private String name;
	
	MergeRule(String name) {
		this.name= name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public String text() {
		return name;
	}
}


public class NDS extends NDSValue implements Iterable<NDS> {
	
	private String name= null;
	private HashMap<String, NDS> sections= new HashMap<String, NDS>();
	private ArrayList<NDS> sectionList= new ArrayList<NDS>();
	private MergeRule rule= MergeRule.DEFAULT;
	private long modified;
	private long last_access;
	
	private HashMap<String, NDSValue> attributes= new HashMap<String, NDSValue>();
	
	private Mutex mutex;

	public NDS() {
		super(DataType.NDS);
		super.setValue(this);
	}
	
	public NDS(String name) {
		super(DataType.NDS);
		super.setValue(this);
		setName(name);
	}
	


	public NDS(Map<String, Object> map) {
		this(null, map);
	}

	public NDS(String name, Map<String, Object> map) {
		super(DataType.NDS);
		super.setValue(this);
		setName(name);
		for (String key : map.keySet()) {
			
			Object value= map.get(key);
			if (value == null) { continue; }
			
			if (value instanceof Map) {
				NDS section= new NDS(key, (Map<String,Object>)value);
				set(key, section);
			} else if (value instanceof String) {
				set(key, (String)value);
			} else if (value instanceof Long) {
				set(key, (Long)value);
			} else if (value instanceof Double) {
				set(key, (Double)value);
			} else if (value instanceof Float) {
				set(key, (Float)value);
				
			} else {
				debug("Unsupported datatype: "+ value.getClass());
			}
		}
	}
	
	public NDS(NDS nds) {
		this(nds, true);	
	}
	
	public NDS(NDS nds, boolean reference) {
		super(DataType.NDS);
		super.setValue(this);
		if (nds == null) { return; }
		setName(nds.getName());
		
		// this.name= nds.name;
		
		// if reference, then just point our internal variables to the same as the source nds
		if (reference) {
			this.sections= nds.sections;
			this.sectionList= nds.sectionList;
			this.rule= nds.rule;
			this.attributes= nds.attributes;
			this.mutex= nds.mutex;
		} else {
			// make a copy of the strucutre
			for (String key : nds.keys()) {
				set(key, nds.get(key));
			}
			for (NDS section : nds) {
				NDS copy= new NDS(section, false);
				this.add(copy);
			}
		}
	}
	
	public NDS(String name, JsonObject json) {
		this(name);
		for (String key : json.keys()) {
			Object value= json.get(key, (Object)null);
			if (value == null) { continue; }
			if (value instanceof JsonObject) {
				set(key, new NDS(key, (JsonObject)value));
			} else if (value instanceof JsonArray) {
				
				copy(key, (JsonArray)value);
				
			} else {
				set(key, value.toString());
			}
		}
	}

	public void copy(String key, JsonArray array) {
		
		for (int i= 0; i < array.size(); i++) {
			JsonValue element= array.get(i);
			
			if (element instanceof JsonObject) {
				set(key +"/"+ i, new NDS(key, (JsonObject)element));
			} else if (element instanceof JsonArray) {
				
				copy(key +"/"+ i, (JsonArray)element);
				
			} else {
				set(key +"/"+ i, element.toString());
				
			}
		}
	}

	public static NDS create(NimConfig config) {
		Map<String, Object> map= config.getMap();
		
		NDS nds= new NDS();
		
		Set<String> keys = map.keySet();
		if (keys.size() == 1) {
			for (String key : keys) {
				Map<String, Object> sectionMap= (Map<String, Object>)map.get(key);
				NDS section= new NDS(key, sectionMap);
				// debug(section);
				return section;
			}
			
		} else {
		
			for (String key : keys) {
				Object value= map.get(key);
				
				if (value instanceof Map) {
					NDS section= new NDS(key, (Map<String,Object>)value);
					nds.add(section);
				} else {
					
				}
				
			}
		}
		
		return nds;
	}


	public static NDS create(String tag, PDS pds) throws NimException {
		NDS nds= new NDS(tag);
		
		Enumeration<String> keys= pds.keys();
		
		while (keys.hasMoreElements()) {
			String key= keys.nextElement();
			
			switch (pds.getType(key)) {
			case PDS.PDS_F:
			case PDS.PDS_I:
			case PDS.PDS_I64:
			case PDS.PDS_PCH:
			case PDS.PDS_PPI:
				
				Object value= pds.get(key);
				
				nds.set(key, ""+ value);
				break;
				
			case PDS.PDS_PDS:
				// this is a section
				
				PDS section= pds.getPDS(key);
				
				nds.set(key, NDS.create(key, section));
				
				break;
			case PDS.PDS_PPDS:
				
				NDS sections= new NDS();
				
				int i= 0;
				for (PDS node : pds.getTablePDSs(key)) {
					sections.set(""+ (i), NDS.create(""+ (i++), node));
				}
				
				nds.set(key, sections);
				
				break;
			case PDS.PDS_PPCH:
			
				break;
			case PDS.PDS_VOID:
				break;
			}
			
		}
//		
//		Set<String> keys = map.keySet();
//		if (keys.size() == 1) {
//			for (String key : keys) {
//				Map<String, Object> sectionMap= (Map<String, Object>)map.get(key);
//				NDS section= new NDS(key, sectionMap);
//				debug(section);
//				return section;
//			}
//			
//		} else {
//		
//			for (String key : keys) {
//				Object value= map.get(key);
//				
//				if (value instanceof Map) {
//					NDS section= new NDS(key, (Map<String,Object>)value);
//					nds.add(section);
//				} else {
//					
//				}
//				
//			}
//		}
		
		return nds;
	}

	 public void set(String path, NDS section) {
		 this.set(path, (NDSValue)section);
	 }

	 /*
	 public void add(String path, NDS section) {
		if (path == null || "".equals(path.trim())) {
			throw new RuntimeException("Invalid path, cannot be null or empty when trimmed ("+ path +")");
		}
		path= path.trim();
		
		if (path.contains("/")) {
			
			String[] pieces= ArrayUtils.split(path, '/', true);
		} else {
			section.setName(path);
			add(section);
		}
	}
	*/
	
	
	public NDS getSection(String path, NDS ifNull) {
		
		String[] pathElements= ArrayUtils.split(path, '/', true);
		
		try {
			lock();
			if (pathElements.length == 1) {
				NDS section= this.sections.get(path);
				if (section == null) {
					return ifNull;
				}
				return section;
			} else {
				for (NDS section : sectionList) {
					if (section.isNamed(pathElements[1])) {
						if (pathElements[1].contains("/")) {
							return section.getSection(ArrayUtils.join(pathElements, "/", 1), ifNull);
						} else {
							return section;
						}
					}
				}
			}
			return ifNull;
		} finally {
			release();
		}
		
	}

	public boolean isNamed(String name) {
		if (this.getName() != null) {
			return this.getName().equals(name);
		}
		return false;
	}

	public void set(String key, Float value) {
		if (key == null) { return; }
		if (value == null) { delete(key); return; }
		set(key, new NDSValue(value));
	}

	public void set(String key, Long value) {
		if (key == null) { return; }
		if (value == null) { delete(key); return; }
		set(key, new NDSValue(value));
	}

	public void set(String key, Double value) {
		if (key == null) { return; }
		if (value == null) { delete(key); return; }
		set(key, new NDSValue(value));
	}

	public NDSValue delete(String path) {
		if (path == null) { return null; }
		String[] sections= ArrayUtils.split(path, '/', true);
		String tag= sections[0];
		String key= sections[sections.length - 1];
		
		NDSValue deleted= null;
		
		// if (!tag.equals(getName())) {
			// maybe it's in our direct child sections
			if (this.sections.containsKey(tag)) {
				try {
					lock();
					deleted= this.sections.remove(tag);
					this.sectionList.remove(deleted);
				} finally { 
					release();
				}
					
			}
		// }
		
		if (deleted != null) { return deleted; }
			
		if (sections.length == 2) {
			try {
				lock();
				deleted= attributes.remove(key);
			} finally {
				release();
			}
		} 
		
		if (deleted != null) { return deleted; }
		
		
		try {
			lock();
			for (int i= 0; i < sectionList.size(); i++) {
				NDS element= sectionList.get(i);
				
				if (element.getName().equals(sections[1])) {
					String remainder= ArrayUtils.join(sections, "/", 1);
					if (remainder.equals(element.getName())) {
						deleted= this.sections.remove(remainder);
						if (deleted != null) {
							sectionList.remove(i--);
						}
						return deleted;
					} else {
						return element.delete(remainder);
					}
					
				}
				
			}
		} finally { 
			release();
		}
		
		
		try {
			lock();
			for (int i= 0; i < sectionList.size(); i++) {
				NDS element= sectionList.get(i);
				
				if (element.getName().equals(sections[1])) {
					String remainder= ArrayUtils.join(sections, "/", 1);
					if (tag.equals(element.getName())) {
						deleted= this.sections.remove(remainder);
						if (deleted != null) {
							sectionList.remove(i--);
						}
						return deleted;
					} else {
						return element.delete(remainder);
					}
					
				}
				
			}
		} finally { 
			release();
		}
		
	
		
		
		
		

		return deleted;
	}


	public void add(NDS section) {
		try {
			lock();
			NDS nds= null;
			if ((nds= sections.get(section.getName())) != null) {
				
				for (String key : section.attributes.keySet()) {
					nds.set(key, section.get(key));
				}
				for (NDS child : section.sectionList) {
					nds.add(child);
				}
				
			} else {
				sections.put(section.getName(), section);
				sectionList.add(section);
			}
		} finally {
			release();
		}
		
		
		
	}

	public void set(String path, long value) { set(path, new NDSValue(value)); }
	public void set(String path, int value) { set(path, new NDSValue(value)); }
	public void set(String path, double value) { set(path, new NDSValue(value)); }
	public void set(String path, float value) { set(path, new NDSValue(value)); }
	public void set(String path, boolean value) { set(path, new NDSValue(value)); }
	public void set(String path, String value) { set(path, new NDSValue(value)); }
	
	public void set(String path, NDSValue value) {
		
		String[] sections= ArrayUtils.split(path, '/', true);
		
		if (sections == null) { 
			System.err.println("Error Invalid Data. Key cannot be null: "+ path +" = "+ value);
			return; 
		}
		
		if (value == null) {
			delete(path);
			return;
		}
		
		String tag= sections[0];
		String key= sections[sections.length - 1];
		
		try {
			lock();
			if (sections.length == 1) {
				if (value.getValue() instanceof NDS) {
					// System.err.println("Adding NDS "+ ((NDS)value).getName() + " under "+ this.getName());
					add((NDS)value);
				} else {
					this.attributes.put(key, value);
				}
			} else if (sections.length == 2) {
				NDS section= this.sections.get(tag);
				if (section == null) {
					section= new NDS(tag);
					add(section);
				}
				section.set(key, value);
			} else {
				// recurse
				NDS section= this.sections.get(tag);
				if (section == null) {
					section= new NDS(tag);
					add(section);
				}
				section.set(ArrayUtils.join(sections, "/", 1), value);
			}
			
		} finally {
			modified= System.currentTimeMillis();
			release();
		}
	}
		
	
	public Object seek(String path, String key) {
		
		NDS nds= seek(path, false);
		NDSValue v= nds.getValue(key);
		
		if (v != null) {
			return v.getValue();
		}
		
		return null;
	}
	
	public String seek(String path, String key, String ifNull) {
		NDS section= seek(path, false);
		if (section == null) {
			return ifNull;
		}
		
		return section.get(key, ifNull);
	}
	
	public int seek(String path, String key, int ifNull) {
		NDS section= seek(path, false);
		if (section == null) {
			return ifNull;
		}
		
		return section.get(key, ifNull);
	}
	
	public double seek(String path, String key, double ifNull) {
		NDS section= seek(path, false);
		if (section == null) {
			return ifNull;
		}
		
		return section.get(key, ifNull);
	}
	
	public long seek(String path, String key, long ifNull) {
		NDS section= seek(path, false);
		if (section == null) {
			return ifNull;
		}
		
		return section.get(key, ifNull);
	}
	
	public boolean seek(String path, String key, boolean ifNull) {
		NDS section= seek(path, false);
		if (section == null) {
			return ifNull;
		}
		
		return section.get(key, ifNull);
	}
	
	public NDS seek(String path) { return seek(path, false); }
	
	public NDS seek(String path, boolean autocreate) {
		
		String[] sections= ArrayUtils.split(path, '/', true);
		
		if (sections == null) {
			return null;
			// throw new RuntimeException("HERE");
		}
		
		try {
			lock();
		
			String tag= sections[0];
		
			if ("".equals(tag)) { throw new RuntimeException("Invalid path specified: "+ path); }
			
			if (!isNamed(tag)) { 
				// look for it in our children
				NDS section= this.sections.get(tag);
				
				if (section == null && autocreate) {
					section= new NDS(tag);
					add(section);
				} else if (section == null) { 
					return null; 
				}
				
				if (sections.length == 1) {
					return section;
				} else {
					return section.seek(ArrayUtils.join(sections, "/", 1, sections.length), autocreate);
				}
				
			} else if (isNamed(tag) && sections.length == 1) {
				return this;
			} else if (sections.length < 1) {
				// error
				throw new RuntimeException("error");
			}
		
			NDS section= this.sections.get(sections[1]);
		
			if (section == null) {
				if (autocreate) {
					section= new NDS(tag);
					this.add(section);
				}
			}
			
			if (sections.length > 1) {
				return section.seek(ArrayUtils.join(sections, "/", 1, sections.length), autocreate);
			}
		
			return section;
			
		} finally {
			release();
		}
	}
	
	public long getLastModified() {
		long modified= this.modified;
		/*
		for (NDS section : this) {
			modified= Math.max(modified, section.modified);
		}
		*/
		return modified;
	}
	
	private NDSValue getValue(String key) {
		last_access= System.currentTimeMillis();
		
		if (key.contains("/")) {
			String[] sections= ArrayUtils.split(key, '/', true);
			String tag= sections[0];
			String remainder= ArrayUtils.join(sections, "/", 1);
			
			if (this.isNamed(tag) && sections.length == 2) {
				NDSValue v= this.attributes.get(remainder);
				if (v == null) { return null; }
				// if (v.getValue() == null) { return null; }
				return v;
			} else if (this.isNamed(tag)) {
				return getValue(remainder);
			}
			
			for (NDS nds : this.sectionList) {
				if (nds.isNamed(tag)) {
					return nds.getValue(remainder);
				}
			}
		} else {
			NDSValue value = this.attributes.get(key);
		
			if (value == null) { return null; }
		
			return value;
		}
		
		return null;
	}
	
	public int getDataType(String key) {
		
		NDSValue value = this.attributes.get(key);
		
		if (value == null) { return -1; }
		
		return value.getType().getConstant();
	}
	
	public String get(String key) {
		return get(key, (String)null);
	}
	
	/*
	public ArrayList get(String key, ArrayList<Object> arrayList) {
		ArrayList<NDSValue> array= new ArrayList<NDSValue>();
		
		
		
		return array;
	}
	*/
	
	public String get(String key, String ifNull) {
		
		if (key.contains("/")) {
			
			NDSValue value= getValue(key);
			if (value == null) { return ifNull; }
			if (value.getValue() == null) { return ifNull; }
			return value.toString();
			
		} else {
		
			NDSValue value= attributes.get(key);
		
			if (value == null) { return ifNull; }
			if (value.getValue() == null) { return ifNull; }
			if ("".equals(value.getValue())) { return ifNull; }
		
			return value.toString();
		}
		
		
	}
	
	
	public boolean isEmptyString(String key) {
		String value= this.get(key, (String)null);
		if (value == null) { return false; }
		return "".equals(value);
	}
	
	public boolean isNull(String key) {
		String value= this.get(key, (String)null);
		return (value == null);
	}
	
	/**
	 * get integer value
	 */
	public int get(String key, int ifNull) {
		NDSValue value= attributes.get(key);
		
		if (value == null) { return ifNull; }
		if (value.getValue() == null) { return ifNull; }
		
		if (value.getType() == DataType.INT) {
			return (Integer)value.getValue();
		}
		
		try {
			int v= Integer.parseInt(value.toString());
			return v;
		} catch (NumberFormatException nfx) {
			return -1;
			// return Integer.MIN_VALUE;
		}
		
	}
	
	
	/**
	 * get integer value
	 */
	public int get(String key, int ifNull, int min, int max) {
		NDSValue value= getValue(key);
		
		if (value == null) { return ifNull; }
		if (value.getValue() == null) { return ifNull; }
		
		if (value.getType() == DataType.INT) {
			return (Integer)value.getValue();
		}
		
		try {
			int v= Integer.parseInt(value.toString());
			
			v= Math.min(v, max);
			v= Math.max(v, min);
			
			return v;
		} catch (NumberFormatException nfx) {
			System.err.println("WARN expected integer value for key '"+ key +"', instead got '"+ value +"'");
			return ifNull;
			// return Integer.MIN_VALUE;
		}
		
	}
	
	
	/**
	 * get long value
	 */
	public long get(String key, long ifNull) {
		NDSValue value= getValue(key);
		
		if (value == null) { return ifNull; }
		if (value.getValue() == null) { return ifNull; }
		
		try {
			long v= Long.parseLong(value.toString());
			return v;
		} catch (NumberFormatException nfx) {
			return -1;
			// return Integer.MIN_VALUE;
		}
		
	}
	
	/**
	 * get double value
	 */
	public double get(String key, double ifNull) {
		NDSValue value= getValue(key);
		
		if (value == null) { return ifNull; }
		if (value.getValue() == null) { return ifNull; }
		
		if (value.getType() == DataType.FLOAT) {
			return (double)value.getValue();
		}
		
		try {
			double v= Double.parseDouble(value.toString());
			return v;
		} catch (NumberFormatException nfx) {
			return -1;
			// return Integer.MIN_VALUE;
		}
		
		
	}
	
	
	/**
	 * get boolean value
	 */
	public boolean get(String key, boolean ifNull) {
		NDSValue value= getValue(key);
		
		if (value == null) { return ifNull; }
		if (value.getValue() == null) { return ifNull; }
		
		if (value.getType() == DataType.BOOL) {
			return (Boolean)value.getValue();
		}
		
		if ("yes".equalsIgnoreCase(value.toString())) {
			return true;
		} else if ("true".equalsIgnoreCase(value.toString())) {
			return true;
		} else if ("1".equalsIgnoreCase(value.toString())) {
			return true;
		} else if ("on".equalsIgnoreCase(value.toString())) {
			return true;
		}
		
		return false;
	}
	
	
	
	
	
	
	private static final void debug(Object message) {
		NimLog log= NimLog.getLogger(NDS.class);
		if (message != null) {
			log.info(message.toString());
		} else {
			log.info("[NULL MESSAGE]");
		}
	}
	
	
	public String getMergeBehavior() {
		return rule.toString();
	}

	public void setMergeBehavior(MergeRule rule) {
		this.rule= rule;
	}
	
	public void setMergeBehavior(String rule) {
		
		if ("overwrite".equalsIgnoreCase(rule)) {
			this.rule= MergeRule.OVERWRITE;
		} else if ("clear".equalsIgnoreCase(rule)) {
			this.rule= MergeRule.CLEAR;
		} else if ("delete".equalsIgnoreCase(rule)) {
			this.rule= MergeRule.DELETE;
		} else {
			this.rule= MergeRule.DEFAULT;
		}
		
	}
	
	
	
	public String toString() {
		
		String string= "";
		String indent= "";
		String name= getName();
		
		if (name != null) {
			string+= "<"+ name +">";
			string+= "\n";
			indent= "\t";
		}
		
		for (String key : attributes.keySet()) {
			String value= get(key, (String)null);
			if (sections.containsKey(key)) {
				string+= "\n"+ sections.get(key) +"\n";
			} else {
				string+= indent + key +" = "+ value +"\n";
			}
		}
		
		for (NDS section : sectionList) {
			string+= ArrayUtils.prefixMultiline(section.toString(), indent);
		}
		
		if (name != null) {
			string+= "</"+ name +">\n";
		}
		
		return string;
			
		
	}
	
	
	public PDS toPDS() throws NimException {
		
		PDS pds= new PDS();
		
		for (String key : attributes.keySet()) {
			NDSValue value= attributes.get(key);
			switch (value.getType()){
			case BOOL:
				pds.put(key, ((boolean)value.getValue() ? "yes" : "no"));
				break;
			case STR:
				pds.put(key, value.toString());
				break;
			case INT:
				pds.put(key, (int)value.getValue());
				break;
			case FLOAT:
				pds.put(key, (float)value.getValue());
				break;
			case LONG:
				pds.put(key, (long)value.getValue());
				break;
			case PDS_ARRAY:
				System.err.println("Unsupported PDS datatype: "+ value.getType() +" ("+ value.getType().name());
				break;
			case STR_ARRAY:
				System.err.println("Unsupported PDS datatype: "+ value.getType() +" ("+ value.getType().name());
				break;
			default:
				System.err.println("Unsupported PDS datatype: "+ value.getType() +" ("+ value.getType().name());
			}
			try {
				pds.put(key, value.toString());
			} catch (NimException nx) {
				System.err.println(nx.getMessage());
			}
		}
		
		for (NDS section : sectionList) {
			pds.put(section.getName(), section.toPDS());
		}
		
		return pds;
	}
	
	public void setName(int index) {
		setName(""+ Math.abs(index));
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	
	@Override
	public Iterator<NDS> iterator() {
		return sectionList.iterator();
	}
	
	
	public Iterable<String> keys() {
		return new Iterable<String>() {
			@Override
			public Iterator<String> iterator() {
				return attributes.keySet().iterator();
			}
		};
	}
	
	
	private boolean lock() {
		if (this.mutex == null) { this.mutex= new Mutex(); }
		return this.mutex.lock(1);
	}
	
	
	private void release() {
		if (this.mutex == null) { this.mutex= new Mutex(); }
		this.mutex.release();
	}
	/*
	public Iterable<String> values() {
		return new Iterable<String>() {
			@Override
			public Iterator<String> iterator() {
				return sections.keySet().iterator();
			}
		};
	}
	*/

	public void set(String key, StringBuffer value) {
		if (value == null) {
			return;
		}
		set(key, value.toString());
	}
	
	
	public JsonObject toJson() {
		
		JsonObject json= new JsonObject();
		
		
		for (String key : this.sections.keySet()) {
			NDS value= sections.get(key);
			json.set(key, value.toJson());
		}
		for (String key : this.attributes.keySet()) {
			NDSValue value= attributes.get(key);
			json.set(key, value.toJsonValue());
		}
		
		
		return json;
		
	}
	
	public Set<String> getKeys() {
		return attributes.keySet();
	}

	public boolean writeToFile(File persistentCache) throws IOException, FileNotFoundException {
		File dir= persistentCache.getParentFile();
		
		File tmp= new File(dir.getAbsolutePath() +"/"+ "_"+ persistentCache.getName() +".tmp");
		
		FileOutputStream out= null;
		
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new AccessDeniedException("Failure to create directories, NDS.writeToFile("+ persistentCache.getAbsolutePath() +") failure.");
			}
		}
		
		if (tmp.exists()) {
			tmp.delete();
		}
		
		try {
			out= new FileOutputStream(tmp);
			if (this.getName() == null) {
				this.setName("data");
			}
			out.write(this.toString().getBytes());
			out.close();
		} catch (FileNotFoundException nfx) {
			throw nfx;
		} catch (IOException iox) {
			throw iox;
		} finally {
			if (out != null) {
				try { out.close(); } catch (Throwable ignore) { }
			}
		}
		
		if (persistentCache.exists()) {
			if (!persistentCache.delete()) {
				// failure to delete old file;
				return false;
			}
		}
		tmp.renameTo(persistentCache);
		
		return true;
	}

	public void clear() {
		// TODO Auto-generated method stub
		
		try {
			lock();
			sections.clear();
			sectionList.clear();
			attributes.clear();
		} finally {
			release();
		}
		
	}

	public boolean isActive() {
		return get("active", get("enabled", false));
	}

	public int size() {
		return attributes.size();
		// return sectionList.size();	// + attributes.size();
	}
	

	public int length() {
		return sectionList.size();	// + attributes.size();
	}

	public long mass() {
		long mass= sectionList.size();
		for (NDS section : sectionList) {
			mass+= section.mass();
		}
		return mass;
	}

	public NDS pop() {
		
		if (sectionList.size() == 0) {
			return null;
		}
		
		NDS section= sectionList.get(0);
		
		return (NDS) delete(section.getName());
	}

	
	
//
//	public static void main(String[] args) throws FileNotFoundException, IOException {
//		
//		NDS n= new NDS();
//		
//		n.set("first", "Brad");
//		n.set("last", "Morse");
//		n.set("test/setting", true);
//		n.set("test/my/setting", "it worked");
//		
//		// System.out.println(n);
//		
//		NDS m= new NDS(n.seek("test"), false);
//		NDS o= new NDS(n, false);
//		
//		// m.set("test/setting", false);
//		o.set("test/setting", false);
//		m.set("my/setting", "wow");
//		m.set("my/here", "is great");
//		
//		System.out.println("M:\n"+ m);
//		
//		System.out.println("O:\n"+ o);
//		System.out.println("N:\n"+ n);
//		
//		
//		n.writeToFile(new File("/tmp/test.nds"));
//	}
//	
}
