package inc.morsecode.probes.groovy_framework;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import com.nimsoft.nimbus.NimLog;

public class NimLogPrintWriter extends PrintStream {
	
	NimLog log= NimLog.getLogger(NimLogPrintWriter.class);
	int severity= NimLog.INFO;
	
	private StringBuffer buffer= new StringBuffer();

	public NimLogPrintWriter(NimLog log, int severity) {
		super(new RedirectedOutputStream(null));
		this.log= log;
		System.setOut(this);
	}
	
	
	@Override
	public void println() { log.log(severity, "\n"); }
	
	@Override
	public void println(int x) { log.log(severity, ""+ x); }
	public void println(float x) { log.log(severity, ""+ x); }
	public void println(double x) { log.log(severity, ""+ x); }
	public void println(long x) { log.log(severity, ""+ x); }
	public void println(boolean x) { log.log(severity, ""+ x); }
	public void println(String x) { log.log(severity, ""+ x); }
	public void println(Object o) { log.log(severity, ""+ o); }
	
	
	@Override
	public void print(int i) { log.log(severity, ""+ i); }
	public void print(float i) { log.log(severity, ""+ i); }
	public void print(double i) { log.log(severity, ""+ i); }
	public void print(long i) { log.log(severity, ""+ i); }
	public void print(boolean i) { log.log(severity, ""+ i); }
	public void print(String i) { log.log(severity, ""+ i); }
	public void print(Object i) { log.log(severity, ""+ i); }
	public void print(char i) { log.log(severity, ""+ i); }
	public void print(char[] i) { log.log(severity, ""+ i); }
	
	
	
	
	
}
