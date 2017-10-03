package drone;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class App {
	public static void exhaustInputStream(InputStream in){
		new Thread(new Runnable() {
			@Override
			public void run() {
				String str;				
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				while (true) {
					try {
						while ((str = reader.readLine()) != null) {
							System.out.println(str);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
		}).start();
	}
	public static void main(String[] args) {
		/*List<String> cmd = new ArrayList<String>();
		cmd.add("ffmpeg");
		cmd.add("-i");
		cmd.add("udp://@223.194.158.187:10124");
		cmd.add("http://223.194.158.187:12390/feed1.ffm");		
		ProcessBuilder processBuilder = new ProcessBuilder(cmd);
		
		processBuilder.redirectErrorStream(true);// ���� ��Ʈ���� �и���������(stderr > stdout)	
		Process ffmpeg;
		try {
			ffmpeg = processBuilder.start();
			exhaustInputStream(ffmpeg.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		new Server();
	}
}
