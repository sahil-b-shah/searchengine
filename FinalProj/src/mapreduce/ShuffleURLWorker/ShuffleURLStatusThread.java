package mapreduce.ShuffleURLWorker;

import java.util.HashMap;

import mapreduce.MyHttpClient;

public class ShuffleURLStatusThread implements Runnable {

	private String IPPort;
	private ShuffleURLWorkerServlet workerServlet;

	public ShuffleURLStatusThread(String IPPort, ShuffleURLWorkerServlet workerServlet){
		this.IPPort = IPPort;
		this.workerServlet = workerServlet;
	}

	@Override
	public void run() {

		try {

			while(true){

				//Send POST every 10 seconds
				MyHttpClient client = new MyHttpClient(IPPort, "/ShuffleURLMaster/workerstatus");
				if(client.connected()){

					HashMap<String, String> params = workerServlet.getStatusParameters();
					client.addParams("port", params.get("port"));
					client.addParams("status", params.get("status"));
					client.addParams("job", params.get("job"));
					client.addParams("keysRead", params.get("keysRead"));
					client.addParams("keysWritten", params.get("keysWritten"));

					client.sendPost();
				}
				Thread.sleep(10000);
			}
		} catch (InterruptedException e) {
			System.err.println("Status thread ended");
		}

	}

}
