package mapreduce;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

public class MyHttpClient {

	private String path;
	private Socket socket;
	private HashMap<String, String> params;
	private String body;
	private boolean connected;
	
	public MyHttpClient(String IPPort, String path){
		this.path = path;
		this.params = new HashMap<String, String>();
		this.connected = true;
		String address[] = IPPort.split(":");
		try {
			socket = new Socket(address[0], Integer.parseInt(address[1]));
		} catch (IOException e) {
			socket = null;
			connected = false;
		} 
		body= "";
	}
	
	/**
	 * Send POST request
	 * @return true if post sent
	 */
	public boolean sendPost(){
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.print("POST " + path + " HTTP/1.0\r\n");
			int counter = 0;
			String message = "";
			for(String param: params.keySet()){
				if(counter == 0)
					message += param + "=" + params.get(param);
				else
					message += "&"+param + "=" + params.get(param);
				counter++;	
			}
			if(!message.isEmpty()){
				out.print("Content-Length: "+ message.length() + "\r\n");
				out.print("Content-Type: application/x-www-form-urlencoded\r\n");
				out.print("\r\n");
				out.print(message + "\r\n");
			}
			else if(!body.isEmpty()){
				out.print("Content-Length: "+ body.length() + "\r\n");
				out.print("Content-Type: text/html\r\n");
				out.print("\r\n");
				out.print(body + "/r/n");
			}
			out.print("\r\n");
			out.flush();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * Add parameter to POST message
	 * @param key to add
	 * @param value to add
	 */
	public void addParams(String key, String value){
		params.put(key, value);
	}
	
	/**
	 * Set body for POST message
	 * @param body
	 */
	public void setBody(String body){
		this.body = body;
	}
	
	/**
	 * Check if socket connected
	 * @return true if connected
	 */
	public boolean connected(){
		return connected;
	}
	
	
}
