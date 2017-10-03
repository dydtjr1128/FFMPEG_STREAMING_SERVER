package drone;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;


public class Server implements Runnable{
	private ServerSocket serverSocket;
	private Socket socket;
	private final int SERVER_PORT = 10123;
	private int UDP_PORT = SERVER_PORT+1;
	private int feedNumber = 1;//"ffmpegServerName:ffmpegServerPort/feed" + feedNumber + ".ffm";
	private boolean feedUsable[];
	private int feedRoom = 10;
	public Server() {
		init();
		new Thread(this).start();//Server Thread Start!
		System.out.println("Server Start!");
	}
	public void init(){
		try {			   
				feedUsable = new boolean[feedRoom];
			 	serverSocket = new ServerSocket(SERVER_PORT);
			 	//make ServerSocket;
		} catch (IOException e) {
			e.printStackTrace();
		}				
	}
	public void run(){
		
		while(true){
			try {
				System.out.println("Listen!");			
				socket = serverSocket.accept();
				System.out.println(socket.getInetAddress() + "  " + socket.getLocalAddress() + " hello!");
				ServiceThread st = new ServiceThread(socket,UDP_PORT,feedNumber);
				/*while(feedUsable[feedRoom]){
					feedNumber+=1;
					feedNumber%=feedRoom;
				}*/
				st.start();
				feedNumber++;
				if(feedNumber>=4) {
					feedNumber=1;
				}
				UDP_PORT++;//Next Video Data UDP Port				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
