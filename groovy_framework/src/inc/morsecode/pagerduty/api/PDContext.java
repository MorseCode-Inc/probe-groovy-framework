package inc.morsecode.pagerduty.api;

/**
 * 
 * @author bcmorse
 * 
 * Reference Material:
 * https://developer.pagerduty.com/documentation/integration/events/trigger
 */

import util.json.JsonObject;
import inc.morsecode.NDS;
import inc.morsecode.pagerduty.PDConstants;
import inc.morsecode.pagerduty.PDConstants.ContextType;

public class PDContext extends NDS {
	
	 public PDContext(String name, String href, String text) {
		 this(name, href, ContextType.LINK);
	 }
	 
	 public PDContext(String name, String src, String href, String alt) {
		 this(name, href, ContextType.IMAGE);
		 set("src", src);
		 set("alt", alt);
	 }
	 
	 protected PDContext(String name, String href, ContextType type) {
		 super(name);
		 setContextType(type);
		 set("href", href);
	}
	 
	 
	protected void setContextType(ContextType type) {
		set("type", type.getName());
	}
	
	public String getHref() {
		return get("href");
	}
	
	public String getDescription() {
		return get("description");
	}
	
	public String getIncidentKey() {
		return get("incident_key");
	}
	
	public String getClient() {
		return get("client");
	}
	
	public String getClientUrl() {
		return get("client_url");
	}
	
	public JsonObject getDetails() {
		JsonObject details= new JsonObject();
		
		// set any attributes we want on the JSON object
		
		return details;
	}
	
	/*
	 * Contexts to be included with the incident trigger such as links to graphs or images.
	 * A "type" is required for each context submitted. For type "link", an "href" is
	 * required. You may optionally specify "text" with more information about the link.
	 * For type "image", "src" must be specified with the image src. You may optionally
	 * specify an "href" or an "alt" with this image.
	 */
	public JsonObject getContexts() {
		JsonObject contexts= new JsonObject();
		
		
		return contexts;
	}
	
}
