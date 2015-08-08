package inc.morsecode.core;

import inc.morsecode.NDS;

public class UIMMessage extends NDS {

	public UIMMessage(NDS nds) {
		super(nds);
	}
	
	public String getSubject() { return get("subject"); }
	
	public NDS getBody() { return seek("udata"); }
	
	
}
