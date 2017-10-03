package drone;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class StreamingVideo extends Thread {

	private DatagramSocket ddsocket;
	private final String STREAMING_SERVER = "223.194.156.214";// UDP Video Send to this Adress And ffmpegProcess connect
																// this Adress;
	private final int STREAMING_SERVER_PORT = 14450;// UDP Video Send to this Port;
	private final String MESSAGE_SERVER = "192.168.0.7";// MessageServer
	private final int MESSAGE_SERVER_PORT = 12329;// UDP Video Send to this Port;
	private Process ffmpegProcess;
	private String ffmpegPath = "ffmpeg";
	private String protocol = "udp://@";
	private final String FFMPEG_STREAMING_SERVER = "http://192.168.0.3";// ffmpegProcess

	private final int FFMPEG_STREAMING_SERVER_PORT = 12390;
	private final String FFMPEG_STREAMING_SERVER_NAME = "feed";
	private int feedNumber;
	private String androidAddress;
	private int androidPort;
	private InetSocketAddress inetSocketAddress;
	private String userName;
	DataOutputStream dataOutputStream;
	Socket socket;
	Socket messageSocket;
	DataOutputStream messageDOS;
	boolean isRunning = false;

	long ffmpegstartTime = 0;

	long ffmpegfinishTime = 0;
	Thread pollingThread;
	public StreamingVideo(int feedNumber, Socket socket, int port, String userName) {
		this.feedNumber = feedNumber;
		this.socket = socket;
		this.androidAddress = socket.getInetAddress().getHostAddress();
		this.androidPort = port;
		this.userName = userName;
		try {
			// inetSocketAddress = new InetSocketAddress(STREAMING_SERVER, STREAMING_SERVER_PORT);
			dataOutputStream = new DataOutputStream(socket.getOutputStream());
			// ddsocket = new DatagramSocket();

			System.out.println(androidAddress + " " + androidPort + " is come!");
			ffmpegStreamingStart();// FFMPEG Process Start
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void exhaustInputStream(InputStream in) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String str;
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				System.out.println("FFMPEG Stream data exhausting");

				try {
					while ((str = reader.readLine()) != null) {
						if (isRunning == false) {
							isRunning = true;
							pollingThread = new Thread(new Runnable() {
								@Override
								public void run() {
									while (true) {
										try {
											sleep(1000);
											ffmpegfinishTime = System.currentTimeMillis();
											//System.out.println(ffmpegstartTime + " " + ffmpegstartTime + "~~~" + ((long)(ffmpegfinishTime-ffmpegstartTime)/1000.0));
											long time = (long)((ffmpegfinishTime-ffmpegstartTime)/1000.0);
											System.out.println(time);
											if (time > 35 && ffmpegstartTime > 0) {
												System.out.println(ffmpegfinishTime);												
												endService();
												System.out.println("종료2");
												return;
											}

										} catch (Exception e) {
											System.out.println("time is out");											
											e.printStackTrace();
											return;
										}
									}
								}
							});
							pollingThread.start();
						}
						System.out.println(str);
						ffmpegstartTime = System.currentTimeMillis();
					}
				} catch (IOException e) {
					endService();
					e.printStackTrace();
					return;
				}

			}
		}).start();
	}

	public void streamingData(byte data[]) {
		try {
			ddsocket.send(new DatagramPacket(data, data.length, inetSocketAddress));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void ffmpegStreamingStart() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					List<String> commands = new ArrayList<String>();
					commands.add(ffmpegPath);
					commands.add("-i");
					commands.add(protocol + androidAddress + ":" + androidPort);
					commands.add("-vcodec");
					//commands.add("libx264");
					commands.add("libx264");
					commands.add("-threads");
					commands.add("6");
					commands.add("-crf");
					commands.add("24");
					commands.add("-framerate");
					commands.add("15");
					commands.add("-maxrate");
					commands.add("10000k");
					commands.add("-bufsize");
					commands.add("10000k");

					commands.add("-c:v");
					commands.add("flv");
					commands.add("-preset");
					commands.add("ultrafast");
					commands.add("-tune");
					commands.add("zerolatency");

					commands.add("-movflags");
					commands.add("faststart");
					/*
					 * commands.add("-q"); commands.add("0");
					 */
					/*
					 * commands.add("-c"); commands.add("copy");
					 */
					commands.add("-an");
					/* commands.add("hello.flv"); */
					commands.add(FFMPEG_STREAMING_SERVER + ":" + FFMPEG_STREAMING_SERVER_PORT + "/"
							+ FFMPEG_STREAMING_SERVER_NAME + feedNumber + ".ffm");

					for (int i = 0; i < commands.size(); i++) {
						System.out.println(commands.get(i) + " ");
					}
					System.out.println();
					ProcessBuilder processBuilder = new ProcessBuilder(commands);

					processBuilder.redirectErrorStream(true);// error to print(stderr > stdout)

					ffmpegProcess = processBuilder.start();
					exhaustInputStream(ffmpegProcess.getInputStream());// exhaustInputStream about FFMPEG data

					ffmpegStreamingCheck();// Send I-Frame to FFSERVER by UDP -->  Maybe Change
					System.out.println("ffmpegStreamingCheck() Start!!");
				} catch (Exception e) {
					System.out.println("ffmpegStreamingStart");
					endService();
					e.printStackTrace();
					return;
						
				}
			}
		}).start();
	}

	public void ffmpegStreamingCheck() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				long startTime = System.currentTimeMillis();
				while (true) {
					if (ffmpegProcess != null && ffmpegProcess.isAlive()) {
						try {
							sleep(1000);
							System.out.println("보냄!");
							dataOutputStream.writeUTF("FFMPEG_START!");
							messageSocket = new Socket(MESSAGE_SERVER, MESSAGE_SERVER_PORT);
							messageDOS = new DataOutputStream(messageSocket.getOutputStream());
							messageDOS.writeUTF(userName + "&HIFLY&" + FFMPEG_STREAMING_SERVER + ":"
									+ FFMPEG_STREAMING_SERVER_PORT + "/" + "test" + feedNumber + ".flv");
							System.out.println("두번다보냄!");

						} catch (Exception e) {
							System.out.println("ffmpegStreamingCheck");
							endService();							
							e.printStackTrace();
							return;
						}
						return;
					}
					long elapsedTime = System.currentTimeMillis() - startTime;
					// System.out.println(elapsedTime + "걸림");
					if (elapsedTime > 10000) {
						System.out.println("10000 over");
						endService();
						return;
					}

				}

			}
		}).start();
	}

	public void endService() {
		System.out.println("end");
		try {
			if (messageSocket != null && messageSocket.isConnected())
				dataOutputStream.writeUTF("RoomClose");
		} catch (IOException e1) {
			
			e1.printStackTrace();
		}
		if (ffmpegProcess != null && ffmpegProcess.isAlive()) {
			System.out.println("ffmpeg destroy!");
			ffmpegProcess.destroyForcibly();
		}
		if (socket != null && socket.isConnected()) {
			try {
				System.out.println("socket disconnected!");
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (messageSocket != null && messageSocket.isConnected()) {
			try {
				System.out.println("msgsocket disconnected!");
				messageSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(pollingThread != null && pollingThread.isAlive()) {
			pollingThread.interrupt();
		}
		System.out.println(new Date());
/*	 if (ffmpegProcess.exitValue() != 0) {
		       System.out.println("변환 중 에러 발생");
		 }*/
	}
}