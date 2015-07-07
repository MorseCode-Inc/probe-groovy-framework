package inc.morsecode;

import inc.morsecode.etc.ArrayUtils;
import inc.morsecode.etc.Mutex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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


public class NDS implements Iterable<NDS> {
	
	private String name= null;
	private HashMap<String, NDS> sections= new HashMap<String, NDS>();
	private ArrayList<NDS> sectionList= new ArrayList<NDS>();
	private MergeRule rule= MergeRule.DEFAULT;
	
	private HashMap<String, NDSValue> attributes= new HashMap<String, NDSValue>();
	
	private Mutex mutex;

	public NDS() {
	}
	
	public NDS(String name) {
		setName(name);
	}
	


	public NDS(Map<String, Object> map) {
		this(null, map);
	}

	public NDS(String name, Map<String, Object> map) {
		setName(name);
		for (String key : map.keySet()) {
			
			Object value= map.get(key);
			if (value == null) { continue; }
			
			if (value instanceof Map) {
				NDS section= new NDS(key, (Map<String,Object>)value);
				add(key, section);
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
		// if reference, then just point our internal variables to the same as the source nds
		if (nds == null) { return; }
		
		this.name= nds.name;
		
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
	
	public static NDS create(NimConfig config) {
		Map<String, Object> map= config.getMap();
		
		NDS nds= new NDS();
		
		Set<String> keys = map.keySet();
		if (keys.size() == 1) {
			for (String key : keys) {
				Map<String, Object> sectionMap= (Map<String, Object>)map.get(key);
				NDS section= new NDS(key, sectionMap);
				debug(section);
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


	private void add(String path, NDS section) {
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
	
	
	public NDS getSection(String path, NDS ifNull) {
		
		String[] pathElements= ArrayUtils.split(path, '/', true);
		
		if (pathElements.length == 1) {
			return this.sections.get(path);
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

	public void delete(String key) {
		if (key == null) { return; }
		
		if (key.contains("/")) {
			
		} else {
			try {
				lock();
				attributes.remove(key);
			} finally {
				release();
			}
		}
	}


	public void add(NDS section) {
		try {
			lock();
			if (sections.get(section.getName()) != null) {
			
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
	
	private void set(String path, NDSValue value) {
		
		String[] sections= ArrayUtils.split(path, '/', true);
		String tag= sections[0];
		String key= sections[sections.length - 1];
		
		try {
			lock();
			if (sections.length == 1) {
				this.attributes.put(key, value);
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
	
	private NDSValue getValue(String key) {
		
		if (key.contains("/")) {
			String[] sections= ArrayUtils.split(key, '/', true);
			String tag= sections[0];
			String remainder= ArrayUtils.join(sections, "/", 1);
			
			if (this.isNamed(tag) && sections.length == 2) {
				NDSValue v= this.attributes.get(remainder);
				if (v == null) { return null; }
				// if (v.getValue() == null) { return null; }
				return v;
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
		
			return value.toString();
		}
		
		
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
		NDSValue value= attributes.get(key);
		
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
	
	
	
	
	
	public static void main(String[] args) {
		
		NDS n= new NDS();
		
		n.set("first", "Brad");
		n.set("last", "Morse");
		n.set("test/setting", true);
		n.set("test/my/setting", "it worked");
		
		// System.out.println(n);
		
		NDS m= new NDS(n.seek("test"), false);
		NDS o= new NDS(n, false);
		
		// m.set("test/setting", false);
		o.set("test/setting", false);
		m.set("my/setting", "wow");
		m.set("my/here", "is great");
		
		System.out.println("M:\n"+ m);
		
		System.out.println("O:\n"+ o);
		System.out.println("N:\n"+ n);
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
			string+= indent + key +" = "+ value +"\n";
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
				break;
			case STR_ARRAY:
				break;
			default:
			}
			try {
				pds.put(key, value.toString());
			} catch (NimException nx) {
				
			}
		}
		
		return pds;
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
	
}
