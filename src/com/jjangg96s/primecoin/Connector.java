package com.jjangg96s.primecoin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONObject;

import com.jjangg96s.primecoin.exception.NoKeyException;

/**
 * @author jjangg96
 */
public class Connector {

	/**
	 * sleep milliseconds
	 */
	private int sleep = 1000 * 2;

	/**
	 * Node.js app server
	 */
	private String serverUrl = null;

	private static final String ERROR = "Error";

	/**
	 * primecoind daemon path
	 */
	private String daemonPath = "./";

	/**
	 * user defined primecoind name
	 */
	private String name = "noname";

	/**
	 * saved command id
	 */
	private String lastCommandId;

	/**
	 * main function
	 * 
	 * @param args string array of arguments
	 */
	public static void main(String[] args) {
		Connector connector = new Connector();
		connector.run(args);
	}

	/**
	 * Connector run
	 * 
	 * @param args string array of arguments
	 */
	private void run(String[] args) {
		ArgumentUtil argumentUtil = setup(args);

		// check need help
		// and show help
		// and exit
		if (argumentUtil.getArgumentCount() == 0 || argumentUtil.isExist("-help")) {
			showHelp();
			return;
		}

		// loop
		try {
			if (this.serverUrl == null) {
				System.out.println("need -server");
				return;
			}

			while (true) {
				Thread process = new Thread(new Runnable() {

					@SuppressWarnings("unchecked")
					@Override
					public void run() {
						String command;
						String commandOutput;
						String errorOutput;

						HttpResponse response;
						HttpClient client = new DefaultHttpClient();

						// read command from web
						HttpGet getRequest = new HttpGet(Connector.this.serverUrl + "/command");

						// web reading
						try {
							response = client.execute(getRequest);
						} catch (Exception e) {
							System.out.println("Get command Error");
							System.out.println("URL : " + Connector.this.serverUrl + "/command");
							return;
						}

						// parsing
						String commandOrder = read(response);
						if (commandOrder.equals(ERROR)) {
							System.out.println("Command Parse Error");
							System.out.println("URL : " + Connector.this.serverUrl + "/command");
							return;
						}

						// check command is ok
						// if not return
						if (commandOrder.trim().equals("")) {

							System.out.println("No Command");
							return;
						}

						// check last command
						// which is same or not
						String[] commandAndId = commandOrder.trim().split("_");
						String commandId = commandAndId[1];
						command = commandAndId[0];

						// skip command which is not ordered
						if (lastCommandId != null && lastCommandId.equals(commandId)) {
							System.out.println("Skip command");
							return;
						}
						lastCommandId = commandId;

						// send command to daemon
						String runtimeCommand = Connector.this.daemonPath + "/primecoind " + command + "";
						System.out.println("Run : " + runtimeCommand);

						try {
							Process proc = Runtime.getRuntime().exec(runtimeCommand);

							// get result from daemon
							commandOutput = read(proc.getInputStream());
							errorOutput = read(proc.getErrorStream());

						} catch (IOException e) {
							System.out.println("Runtime Error");
							System.out.println(e.getMessage());
							return;
						}

						// check error
						if (errorOutput != null && !errorOutput.equals("")) {
							System.out.println("Return Error : " + errorOutput);
							commandOutput = errorOutput;
						}

						System.out.println("Command output : ");
						System.out.println(commandOutput);

						// send result to web
						HttpPost postRequest = new HttpPost(Connector.this.serverUrl + "/result");
						postRequest.setHeader("Content-Type", "application/json");
						postRequest.setHeader("Accept", "application/json");

						// set json post value
						try {
							JSONObject jsonObject = new JSONObject();
							jsonObject.put("id", Connector.this.name);
							jsonObject.put("result", commandOutput);

							StringEntity entity = new StringEntity(jsonObject.toJSONString(), "UTF-8");
							entity.setContentType("application/json");

							postRequest.setEntity(entity);
						} catch (UnsupportedEncodingException e) {
							System.out.println("Output Encoding Error");
							System.out.println("ID : " + Connector.this.name);
							System.out.println("Result : " + commandOutput);
							return;
						}

						// post
						try {
							response = client.execute(postRequest);
							System.out.println("Output send OK!");
						} catch (Exception e) {
							System.out.println("Post command Error");
							System.out.println("URL : " + Connector.this.serverUrl + "/result");
							return;
						}

					}
				});

				// background thread
				process.setDaemon(true);
				process.start();

				Thread.sleep(this.sleep);
			}
		} catch (Exception e) {

		}

	}

	/**
	 * Shell Arguments setup
	 * 
	 * @param args string array of arguments
	 * @return
	 */
	private ArgumentUtil setup(String[] args) {
		ArgumentUtil argUtil = new ArgumentUtil(args);

		if (argUtil.getArgumentCount() > 0) {
			System.out.println("Arguments");
			System.out.println(argUtil.toString());
		}
		// argument parse

		// set sleep seconds to milliseconds
		if (argUtil.isExist("-sleep")) {
			try {
				this.sleep = Integer.parseInt(argUtil.getValue("-sleep"));
			} catch (Exception e) {
				this.sleep = 1000 * 10;
			}
		}

		// node.js app server url with port
		if (argUtil.isExist("-server")) {
			String url;
			try {
				url = argUtil.getValue("-server");
				// add http://
				if (!url.contains("http://"))
					url = "http://" + url;
			} catch (NoKeyException e) {
				url = null;
			}
			this.serverUrl = url;
		}

		// set primecoind path
		if (argUtil.isExist("-path")) {
			try {
				this.daemonPath = argUtil.getValue("-path");
			} catch (NoKeyException e) {
				this.daemonPath = "./";
			}
		}

		// set user defined primecoind name
		if (argUtil.isExist("-name")) {
			try {
				this.name = argUtil.getValue("-name");
			} catch (NoKeyException e) {
				this.name = "noname";
			}
		}

		return argUtil;
	}

	/**
	 * Read from HttpResponse
	 * 
	 * @param response http response
	 * @return
	 */
	private String read(HttpResponse response) {
		try {
			return read(response.getEntity().getContent());
		} catch (Exception e) {
		}

		return ERROR;
	}

	/**
	 * Read from InputStream
	 * 
	 * @param inputStream IO inputstream
	 * @return
	 */
	private String read(InputStream inputStream) {
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(inputStream));
			String read = null;
			StringBuilder sb = new StringBuilder();

			while ((read = br.readLine()) != null) {
				sb.append(read + "\n");
			}

			// remove last enter
			if (sb != null && sb.length() > 0)
				return sb.substring(0, sb.length() - 1);

			return "";

		} catch (IOException e) {
		}

		return ERROR;
	}

	/**
	 * Show help
	 */
	private void showHelp() {
		System.out.println("PrimeCoin Connector");
		System.out.println("-help\t\t\t\tThis help");
		System.out.println("-name\t\t\t\tUser defined connector name");
		System.out.println("-sleep\t\t\t\tProcessing delay second");
		System.out.println("-server(Require)\t\tPrimeCoinRemoteManager app server url");
		System.out.println("-path\t\t\t\tPrimecoind path");
		System.out.println("Example : connector.sh -name node1 -sleep 10 -server http://localhost:3000 -path /usr/bin");
	}
}
