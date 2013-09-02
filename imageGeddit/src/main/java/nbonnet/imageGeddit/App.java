package nbonnet.imageGeddit;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


public class App 
{
	
	private static final String IMAGE_PATTERN = "http://i.imgur.com/(\\S+).jpg";
	private static int successful_images;
	private static List<String> urlList = new ArrayList<String>();
	private static String workingDir = "C:/Users/thebo_000/Desktop";
	private static String folderName = "images";
	
	public static void main( String[] args ) throws IOException {
    	init();
    }

	private static void init() throws IOException {
		long start = System.currentTimeMillis();
		getContent(false, null);
		getImages();
		long end = System.currentTimeMillis();
		System.out.println("Finished in: " + (end-start)/1000 + " seconds\n\t" + successful_images + " SUCCESSFUL images imported.");
	}

	private static InputStream getContent(boolean isGetImage, String imgURL) throws IOException {
		HttpHost targetHost = null;
		DefaultHttpClient httpclient = new DefaultHttpClient();
		File f = new File("C:/Users/thebo_000/Desktop/imgur.html");
		BufferedWriter bw = new BufferedWriter(new FileWriter(f.getCanonicalFile()));

        try {
            HttpGet httpget = null;
            
            if (!isGetImage) {
            	targetHost = new HttpHost("www.imgur.com", 80, "http");
            	httpget = new HttpGet("/r/outdoors");		
            } else {
            	targetHost = new HttpHost("www.i.imgur.com", 80, "http");
            	httpget = new HttpGet("/" + imgURL + ".jpg");
            }

            HttpResponse response = null;
			response = httpclient.execute(targetHost, httpget);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
            	if (!isGetImage) {
            		//IOUtils.copy(entity.getContent(), bw, "UTF-8");
            		processImgurHTML(entity.getContent());
                } else {
                	processImages(entity.getContent(), imgURL);
                }
            }
            EntityUtils.consume(entity);

        } finally {
            httpclient.getConnectionManager().shutdown();
            bw.flush();
            bw.close();
        }
		return null;
	}

	private static void processImgurHTML(InputStream in) throws IOException {
		String imgurHTML = IOUtils.toString(in);
		getImageURLs(imgurHTML);
	}

	private static void getImageURLs(String str) {
		Pattern pattern = Pattern.compile(IMAGE_PATTERN);
		Matcher matcher = pattern.matcher(str);
		while(matcher.find()) {
			urlList.add(matcher.group(1)); 
			// These have "b" attached at the end of them ... if this is removed it will get the full image 
			// and not just the thumbnail
		}
	}
	
	private static void getImages() throws IOException {
		Iterator<String> it = urlList.iterator();
		while(it.hasNext()) {
			String nextImageURL = it.next();
			getContent(true, nextImageURL);
		}
	}

	private static void processImages(InputStream in, String imgURL) throws IOException {
		BufferedInputStream bin= new BufferedInputStream(in);
		
		File imageFile = new File(workingDir + "/" + folderName + "/" + imgURL + ".jpg");
		FileOutputStream fo = new FileOutputStream(imageFile.getAbsoluteFile());
		try {
			int line;
			while((line = bin.read()) != -1) {
				fo.write(line);
			}
			successful_images++;
		} finally {
			bin.close();
			fo.flush();
			fo.close();
		}
	}
}
