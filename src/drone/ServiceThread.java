package drone;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

public class ServiceThread extends Thread {
	private DatagramSocket dsocket;
	private DatagramPacket datagramPacket;
	private DataOutputStream dataoutputStream;
	private DataInputStream dataInputStream;
	private int UDP_PORT;
	private byte[] videoBuf;
	private int videoBufSize = 64 * 1024;// 64KB	
	private StreamingVideo streamingVideo;
	public ServiceThread(Socket p, int UDP_PORT, int feedNumber) {// Android Socket, UDP Video data Port
		// init socket;
		try {
			this.UDP_PORT = UDP_PORT;
			//dsocket = new DatagramSocket(UDP_PORT);
			videoBuf = new byte[videoBufSize];
			datagramPacket = new DatagramPacket(videoBuf, videoBuf.length);
			dataoutputStream = new DataOutputStream(p.getOutputStream());
			dataInputStream =new DataInputStream(p.getInputStream());
			System.out.println("기다림1!");
			String userName = dataInputStream.readUTF();
			System.out.println(userName);
			dataoutputStream.writeInt(UDP_PORT);// send to Android about UDP Port
			System.out.println("씀!");
			streamingVideo = new StreamingVideo(feedNumber, p, UDP_PORT, userName);
		
			videoBuf[0] = -1;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		/*while (true) {
			try {
				//System.out.println("대기중!");
				dsocket.receive(datagramPacket);//receive Video Data
				for(int i=0; i<5; i++) {
					System.out.print(videoBuf[i] + " " );
				}
				byte data[] = new byte[datagramPacket.getLength()];
				System.arraycopy(videoBuf, 0, data, 0, data.length);		
				streamingVideo.streamingData(data);			
			} catch (Exception e) {				
				e.printStackTrace();
				break;
			}
		}*/
	}
}
