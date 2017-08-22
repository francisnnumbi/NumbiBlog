package fnn.smirl.nb.utils;
import java.util.HashMap;
import java.util.Map;
import android.app.Activity;
import java.io.File;
import android.webkit.WebResourceResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.io.IOException;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.FileOutputStream;
import android.content.Context;

public class UrlCache
{
	public static final long
	ONE_SECOND = 1000L, 
	ONE_MINUTE = 60L * ONE_SECOND,
	ONE_HOUR = 60L * ONE_MINUTE,
	ONE_DAY = 24L * ONE_HOUR;
	
	protected Map<String, CacheEntry> cacheEntries = new HashMap<String, CacheEntry>();
	protected Activity activity = null;
	protected File rootDir = null;
	
	public UrlCache(Activity activity){
		this(activity, activity.getCacheDir());
	}
	
	public UrlCache(Activity activity, File rootDir){
		this.activity = activity;
		this.rootDir = rootDir;
	}
	
	public void register(String url, String cacheFileName, String mimeType, String encoding, long maxAgeMillis){
		CacheEntry entry = new CacheEntry(url, cacheFileName, mimeType, encoding, maxAgeMillis);
		this.cacheEntries.put(url, entry);
	}
	
	public WebResourceResponse load(String url){
		CacheEntry cacheEntry = this.cacheEntries.get(url);
		if(cacheEntry == null)return null;
		File cacheFile = new File(this.rootDir, cacheEntry.fileName);
		if(cacheFile.exists()){
			long cacheEntryAge = System.currentTimeMillis() - cacheFile.lastModified();
			if(cacheEntryAge > cacheEntry.maxAgeMillis){
				cacheFile.delete();
				return load(url);
			}
			
			try{
				return new WebResourceResponse(cacheEntry.mimeType, cacheEntry.encoding, new FileInputStream(cacheFile));
			}catch(FileNotFoundException e){}
		}else{
			try{
				downloadAndRestore(url, cacheEntry, cacheFile);
				return load(url);
			}catch(Exception e){}
		}
		return null;
	}

	private void downloadAndRestore(String url, UrlCache.CacheEntry cacheEntry, File cacheFile) throws IOException{
		URL urlObj = new URL(url);
		URLConnection urlConnection = urlObj.openConnection();
		InputStream urlInput = urlConnection.getInputStream();
		FileOutputStream fileOutputStream = this.activity.openFileOutput(cacheEntry.fileName, Context.MODE_PRIVATE);
		int data = urlInput.read();
		while(data != -1){
			fileOutputStream.write(data);
			data = urlInput.read();
		}
		
		urlInput.close();
		fileOutputStream.close();
	}
	
	
	
	/** private inner class */
	private static class CacheEntry{
		public String url, fileName, mimeType, encoding;
		public long maxAgeMillis;

		public CacheEntry(String url, String fileName, String mimeType, String encoding, long maxAgeMillis) {
			this.url = url;
			this.fileName = fileName;
			this.mimeType = mimeType;
			this.encoding = encoding;
			this.maxAgeMillis = maxAgeMillis;
		}
	}
}
