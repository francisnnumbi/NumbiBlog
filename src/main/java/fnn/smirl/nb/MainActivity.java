package fnn.smirl.nb;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import fnn.smirl.nb.utils.Constants;
import fnn.smirl.nb.utils.UrlCache;
import android.webkit.WebResourceResponse;
import android.webkit.WebResourceRequest;

public class MainActivity extends AppCompatActivity implements Constants
{
	private WebView webview;
	private ProgressBar pb;
	int _progress = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
				init();
    }

		private void init() {
			initIds();
			runUIUpdateInBackground();
		}

		private void initIds() {
			setupToolbar();
			pb = (ProgressBar) findViewById(R.id.mainProgressBar);
			webview = (WebView) findViewById(R.id.mainWebView);
		
			loadUrl(BASE_URL);
			}
			
			private void setupToolbar(){
				Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
				setSupportActionBar(toolbar);
				
			}
		
		private void loadUrl(String url){
			WebSettings ws = webview.getSettings();
			ws.setBuiltInZoomControls(false);
			ws.setUseWideViewPort(true);
			ws.setSupportZoom(false);
			ws.setDisplayZoomControls(false);
			webview.setInitialScale(100);
			ws.setJavaScriptEnabled(true);

			webview.setWebViewClient(new MyWebClient(this));

			webview.setWebChromeClient(new WebChromeClient(){
					public void	onProgressChanged(WebView view, int progress){
						_progress = progress;
					}
				});

			webview.loadUrl(url);
		}

		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// TODO: Implement this method
		//	return super.onCreateOptionsMenu(menu);
			getMenuInflater().inflate(R.menu.main_menu, menu);
			return true;
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			// TODO: Implement this method
			switch(item.getItemId()){
				case R.id.mm_refresh:
					webview.reload();
					break;
					case R.id.mm_exit:
						finish();
						break;
			}
			return true;
		}
		
		
	private void runUIUpdateInBackground(){
		final Handler handler = new Handler();
		runOnUiThread(new Runnable(){

				@Override
				public void run() {

					if(pb.isShown())	pb.setProgress(_progress);


					handler.postDelayed(this, 10);	
				}
			});
	}	
		
	@Override
	public void onBackPressed() {
		// TODO: Implement this method
		if(webview.canGoBack()){
			webview.goBack();
		}
		else	super.onBackPressed();
	}
	
	
	
		/** inner class */
		
	private class MyWebClient extends WebViewClient {

		private AppCompatActivity activity = null;
		private UrlCache urlCache = null;
		
		public MyWebClient(AppCompatActivity activity){
			this.activity = activity;
			this.urlCache = new UrlCache(activity);
			this.urlCache.register(BASE_URL, WEB_HOST, MIME_TYPE, ENCODING, 5*urlCache.ONE_MINUTE);
		}
		
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO: Implement this method
			super.onPageStarted(view, url, favicon);
			pb.setVisibility(View.VISIBLE);
			pb.setMax(100);
			pb.setProgress(0);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO: Implement this method
			super.onPageFinished(view, url);
			_progress = 0;
		pb.setVisibility(View.GONE);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// TODO: Implement this method
			if(Uri.parse(url).getHost().endsWith(WEB_HOST.toLowerCase())){
				//loadUrl(url);
				this.urlCache.register(url, Uri.parse(url).getLastPathSegment(), MIME_TYPE, ENCODING, 5*urlCache.ONE_MINUTE);
				return false;
			}else{
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
			return true;
		}
		}

//		@Override
//		public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//			// TODO: Implement this method
//			return this.urlCache.load(request.getUrl().toString());
//		}
		
	

	}
	
	

	
	
	
}
