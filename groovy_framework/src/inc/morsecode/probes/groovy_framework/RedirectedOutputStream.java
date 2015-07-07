package inc.morsecode.probes.groovy_framework;

import java.io.IOException;
import java.io.OutputStream;

public class RedirectedOutputStream extends OutputStream {
	
	OutputStream target;
	
	public RedirectedOutputStream(OutputStream target) {
		this.target= target;
	}

	@Override
	public void write(int b) throws IOException {
		if (target != null) {
			target.write(b);
		}
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		if (target != null) {
			target.write(b);
		}
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (target != null) {
			target.write(b, off, len);
		}
	}
	
}
