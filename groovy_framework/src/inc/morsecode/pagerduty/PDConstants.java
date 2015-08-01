package inc.morsecode.pagerduty;

public class PDConstants {
	
	public static enum ContextType {
		LINK("link")
		, IMAGE("image");
		
		private String name;
		
		private ContextType(String name) {
			this.name= name;
		}
		
		public String getName() { return name; }
	};

	
	public static enum EventType {
		TRIGGER("trigger");
		
		private String name;
		
		private EventType(String name) {
			this.name= name;
		}
		
		public String getName() { return name; }
	};

}
