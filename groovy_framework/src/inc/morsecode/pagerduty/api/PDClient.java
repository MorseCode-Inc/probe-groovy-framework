package inc.morsecode.pagerduty.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


import inc.morsecode.NDS;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import util.json.JsonArray;
import util.json.JsonObject;
import util.json.ex.MalformedJsonException;
import util.kits.JsonParser;


public class PDClient {

	private NDS data= new NDS("pagerduty_client");
	private NDS services;
	private HttpClient restlet;
	
	private PagerDutyIncidentsAPI incidents;
	
	public PDClient(String subdomain, String apiKey) {
		this(subdomain, "pagerduty.com", apiKey);
	}
	
	public PDClient(String subdomain, String domain, String apiKey) {
		this.services= data.seek("services", true);
		data.set("subdomain", subdomain);
		data.set("domain", domain);
		data.set("auth/api_key", apiKey);
		
		this.restlet= HttpClients.createDefault();
		
	}
	
	public PagerDutyIncidentsAPI incidents() {
		if (this.incidents == null) {
			this.incidents= new PagerDutyIncidentsAPI(this);
		}
		return this.incidents;
	}
	
	public String getSubdomain() {
		return data.get("subdomain", "events");
	}
	
	public String getDomain() {
		return getSubdomain() +"."+ getTopLevelDomain();
	}
	
	public String getTopLevelDomain() {
		return data.get("domain", "pagerduty.com");
	}
	
	public String getBaseUrl(String protocol, String apiVer) {
		return (protocol +"://"+ getDomain()); // +"/api/"+ apiVer);
	}
	
	public String getApiToken() {
		return data.get("auth/api_key", (String)null);
	}
	
	public void addService(String name, String token) {
		NDS service= services.seek(name, true);
		service.set("name", name);
		service.set("token", token);
	}
	
	public String getServiceToken(String name) {
		return services.get(name +"/token", (String) null);
	}
	
    public HttpGet get(String uri, NDS params) {
    	return (HttpGet)this.http("get", uri, null, params);
    }
	
    public HttpRequest buildPostRequest(String uri, JsonObject data, NDS params) {
    	return this.http("post", uri, data, params);
    }
    
    public HttpRequest delete(String uri, JsonObject data, NDS params) {
    	return this.http("delete", uri, data, params);
    }

    public HttpRequest put(String uri, JsonObject data, NDS params) {
    	return this.http("put", uri, data, params);
    }
    
    public HttpRequest post(String uri, JsonObject data, NDS params) {
    	return this.http("post", uri, data, params);
    }
    
    
    private HttpRequest http(String method, String uri, JsonObject data, NDS params) {
    	HttpRequest request= null;
    	if (uri == null) {
    		throw new RuntimeException("URI cannot be null, missing required argument to http(method, uri, data, params).");
    	}
    	
    	uri= uri.trim();
    	
    	if (!uri.toLowerCase().startsWith("http")) { 
    		if (!uri.startsWith("/")) { uri= "/"+ uri; }
    		uri= getBaseUrl("https", "v1") + uri;
    	}
    	
    	if (params != null && params.size() > 0) {
    		String delim= "?";
    		for (String key : params.keys()) {
    			try {
					uri+= delim + URLEncoder.encode(key, "UTF-8") +"="+ URLEncoder.encode(params.get(key), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
    			delim="&";
    		}
    		
    	}
		HttpEntity entity= null;
    	
    	if ("get".equalsIgnoreCase(method)) {
    		request= new HttpGet(uri);
    		HttpGet getUrl= (HttpGet)request;
    		
    		// System.err.println("uri: "+ uri);
    		// System.err.println("URL: "+ ((HttpUriRequest) getUrl).getURI());
    		
    	} else if ("delete".equalsIgnoreCase(method)) {
    		request= new HttpDelete(uri);
    	
    	} else {
			entity= entity(data);
			
			if ("put".equalsIgnoreCase(method)) {
			
				request= new HttpPut(uri);
				if (data != null) {
					((HttpPut)request).setEntity(entity);
					request.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
				}
			
			} else if ("post".equalsIgnoreCase(method)) {
				request= new HttpPost(uri);
				if (data != null) {
					((HttpPost)request).setEntity(entity);
					request.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
				}
			} else {
				throw new RuntimeException("Unsupported HTTP Method: "+ method +" "+ uri);
			}
		}
    	
		request.setHeader("Authorization", "Token token="+ getApiToken());
		
		
		boolean debugging= false;
		if (debugging) {
			for (Header header : request.getAllHeaders()) {
				System.out.println(header);
			}
			
			if (entity != null) {
				System.out.println(data);
			}
		}
    	
    	return request;
    }

	public StringEntity entity(JsonObject data) {
		if (data == null) { return null; }
		return new StringEntity(data.toString(), ContentType.APPLICATION_JSON);
	}
    
    private HttpResponse execute(HttpUriRequest request) {
    	try {
    		// System.out.println(request);

            // Create a custom response handler
    		/*
            ResponseHandler<HttpResponse> responseHandler = new ResponseHandler<HttpResponse>() {

                @Override
                public HttpResponse handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    System.out.println(" RESPONSE > ");
                    for (Header header : response.getAllHeaders()) {
                    	// System.out.println(header);
                    }
                    System.out.println(response.getEntity());
                    System.out.println();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                    
                    return response;
                }

            };
            */
            
            
            try {
            	HttpResponse response= restlet.execute(request); // , responseHandler);
            	return response;
            } catch (ClientProtocolException x) {
            	System.out.println(x.getMessage() +" "+ x.getCause());
            }
    		
    	} catch (IllegalArgumentException iax) {
    		System.err.println("ILLEGAL ARGUMENT:\n"+ iax);
    	} catch (NullPointerException npx) {
    		System.err.println("NULL:\n"+ npx);
    	} catch (Throwable error) {
    		System.err.println("ERROR:\n"+ error);
    		error.printStackTrace();
    		
    	} finally {
    		
    	System.out.flush();
    	System.err.flush();
    	}
    	
    	return null;
    }
    
	public JsonObject call(String httpMethod, String uri, JsonObject data, NDS params) throws IOException, MalformedJsonException {
		
		HttpRequest request= null;
		
		if ("put".equalsIgnoreCase(httpMethod)) {
			request= buildPostRequest(uri, data, params);
		} else if ("get".equalsIgnoreCase(httpMethod)) {
			request= get(uri, params);
		} else if ("delete".equalsIgnoreCase(httpMethod)) {
			request= delete(uri, data, params);
		} else if ("post".equalsIgnoreCase(httpMethod)) {
			request= post(uri, data, params);
		} else {
			// unsupported method
			throw new RuntimeException("Unsupported HTTP Method: "+ httpMethod +".  Denying access to "+ uri +" {"+ data.toString().replaceAll("\r\n\t",  " ") +"}");
		}
		
		
		// send it
		HttpResponse response= execute((HttpUriRequest)request);
		
		if (response != null) {
			
			String message= response.getStatusLine().getReasonPhrase();
			int code= response.getStatusLine().getStatusCode();
			
			// System.out.println(code + " "+ message);
			
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				ByteArrayOutputStream baos= new ByteArrayOutputStream();
				entity.writeTo(baos);
				
				baos.close();
				String string = baos.toString();
				JsonObject json= JsonParser.parse(string);
				
				return json;
			}
		}
		
		return null;
	}
	
	




	public static JsonObject nimalarm() {
		JsonObject alarmData= new JsonObject();
		
		alarmData.set("nimid", "11233333ABAB");
		alarmData.set("robot", "robot-name");
		alarmData.set("source", "alarm-source");
		alarmData.set("subsystem", "2.11.1");
		alarmData.set("origin", "origin");
		alarmData.set("assigned_to", "assigned_to");
		alarmData.set("assigned_by", "assigned_by");
		alarmData.set("assigned_ts", "2015-01-01 14:44:00");
		alarmData.set("nimts", "2015-01-01 14:44:00");
		alarmData.set("arrival_ts", "2015-01-01 14:44:00");
		alarmData.set("last_received_ts", "2015-01-01 14:44:00");
		alarmData.set("origin_ts", "2015-01-01 14:44:00");
		return alarmData;
	}

	public PDClient newInstance() {
		return new PDClient(getSubdomain(), getTopLevelDomain(), getApiToken());
	}

	
}
