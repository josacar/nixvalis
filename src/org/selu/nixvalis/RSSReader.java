package org.selu.nixvalis;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class RSSReader extends Activity implements OnItemClickListener {

    public final String RSSFEEDOFCHOICE = "http://nixvalis.nixval.com";

    public final String tag = "Nixvalis";
    private RSSFeed feed = null;
    private LayoutInflater mInflater;
    String user = null;
    String pass = null;
    final Handler mHandler = new Handler();
    ProgressDialog mBusy;

    private ProgressDialog mRefresh;

    /** Called when the activity is first created. */

    public void onCreate(Bundle icicle) {
	super.onCreate(icicle);

	Bundle bundle = this.getIntent().getExtras();
	user = bundle.getString("user");
	pass = bundle.getString("pass");

	setContentView(R.layout.main);

	mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	
	Object retained = this.getLastNonConfigurationInstance();

	if (retained != null) {
	    feed = (RSSFeed) retained;
	    UpdateDisplay();
	} else {
	    mBusy = ProgressDialog.show(RSSReader.this, "Accediendo",
		    "Accediendo a Nixvalis", true, false);

	    final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
		    mBusy.dismiss();
		    UpdateDisplay();
		}
	    };

	    new Thread() {
		public void run() {
		    feed = getFeed(RSSFEEDOFCHOICE);
		    handler.sendEmptyMessage(0);
		}
	    }.start();

	    // feed = getFeed(RSSFEEDOFCHOICE);

	    // display UI
	    // UpdateDisplay();
	}
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
//	final RSSFeed rss = new RSSFeed();
	return feed;
    }
    // No recarga la activity ( No compatible con cambios dinamicos )
    public void onConfigurationChanged(Configuration newConfig) {
	// Ignore orientation change not to restart activity
	super.onConfigurationChanged(newConfig);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
	// final RSSFeed rss = new RSSFeed();
	return feed;
    }

    /*
     * private RSSFeed getFeedOld(String urlToRssFeed) { try { // setup the url
     * URL url = new URL(urlToRssFeed);
     * 
     * // create the factory SAXParserFactory factory =
     * SAXParserFactory.newInstance(); // create a parser SAXParser parser =
     * factory.newSAXParser();
     * 
     * // create the reader (scanner) XMLReader xmlreader =
     * parser.getXMLReader(); // instantiate our handler RSSHandler
     * theRssHandler = new RSSHandler(); // assign our handler
     * xmlreader.setContentHandler(theRssHandler); // get our data via the url
     * class InputSource is = new InputSource(url.openStream()); // perform the
     * synchronous parse xmlreader.parse(is); // get the results - should be a
     * fully populated RSSFeed instance, // or null on error return
     * theRssHandler.getFeed(); } catch (Exception ee) { // if we have a
     * problem, simply return null return null; } }
     */
    private RSSFeed getFeed(String urlToRssFeed) {
	try {

	    // create the factory
	    SAXParserFactory factory = SAXParserFactory.newInstance();
	    // create a parser
	    SAXParser parser = factory.newSAXParser();
	    // create the reader (scanner)
	    XMLReader xmlreader = parser.getXMLReader();
	    // instantiate our handler
	    RSSHandler theRssHandler = new RSSHandler();
	    // assign our handler
	    xmlreader.setContentHandler(theRssHandler);
	    // xr.setContentHandler(this);
	    // xr.setErrorHandler(this);

	    // Creamos un cliente HTTP con autenticacion basica
	    // y nos bajamos la URL
	    final DefaultHttpClient client = new DefaultHttpClient();

	    UsernamePasswordCredentials upc = new UsernamePasswordCredentials(
		    user, pass);
	    BasicCredentialsProvider cP = new BasicCredentialsProvider();
	    cP.setCredentials(AuthScope.ANY, upc);
	    // if ( user != null && pass != null)
	    client.setCredentialsProvider(cP);

	    final HttpGet httpget = new HttpGet(urlToRssFeed);

	    HttpResponse response;
	    InputSource is;

	    response = client.execute(httpget);

	    client.getParams().setParameter("User-Agent", "Android/Nixvalis");

	    is = new InputSource(response.getEntity().getContent());

	    if (response.getStatusLine().getStatusCode() == 401) {

		mHandler.post(new Runnable() {
		    public void run() {

			new AlertDialog.Builder(RSSReader.this).setTitle(
				"Nixvalis error").setMessage(
				"Credenciales incorrectos").setNeutralButton(
				"Cerrar",
				new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog,
					    int whichButton) {
					/*
					 * User clicked OK so do some stuff
					 */
					setResult(RESULT_CANCELED, null);
					finish();
				    }
				}).create().show();
		    }
		});

	    }

	    // perform the synchronous parse
	    xmlreader.parse(is);
	    // get the results - should be a fully populated RSSFeed instance,
	    // or null on error

	    return theRssHandler.getFeed();
	} catch (Exception ee) {
	    // if we have a problem, simply return null
	    // ee.printStackTrace();
	    mHandler.post(new Runnable() {
		public void run() {

		    new AlertDialog.Builder(RSSReader.this).setTitle(
			    "Nixvalis error").setMessage(
			    "Error accediendo a Nixvalis").setNeutralButton(
			    "Cerrar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
					int whichButton) {
				    /*
				     * User clicked OK so do some stuff
				     */
				    setResult(RESULT_CANCELED, null);
				    finish();
				}
			    }).create().show();
		}
	    });
	    return null;
	}
    }

    public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);

	menu.add(0, 0, 0, "Elegir usuario");
	menu.add(0, 1, 0, "Refrescar");
	Log.i(tag, "onCreateOptionsMenu");
	return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case 0:
	    Log.i(tag, "Elegir usuario");
	    return true;
	case 1:
	    Log.i(tag, "Refrescando Nixvalis");

	    mRefresh = ProgressDialog.show(RSSReader.this, "Refrescando",
		    "Refrescando Nixvalis", true, false);
	    mRefresh.show();
	    final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
		    mRefresh.dismiss();
		    UpdateDisplay();
		}
	    };
	    new Thread() {
		public void run() {
		    feed = getFeed(RSSFEEDOFCHOICE);
		    handler.sendEmptyMessage(0);
		}
	    }.start();
	    return true;
	}
	return false;
    }

    private void UpdateDisplay() {
	TextView feedtitle = (TextView) findViewById(R.id.feedtitle);
	TextView feedpubdate = (TextView) findViewById(R.id.feedpubdate);
	ListView itemlist = (ListView) findViewById(R.id.itemlist);

	if (feed == null) {
	    feedtitle.setText("No hay elementos");
	    return;
	}

	feedtitle.setText(feed.getTitle());
	feedpubdate.setText(feed.getPubDate());

	ArrayAdapter<RSSItem> adapter = new ArrayAdapter<RSSItem>(this,
	// android.R.layout.simple_list_item_1, feed.getAllItems()) {
		R.layout.item, feed.getAllItems()) {
	    public View getView(int position, View convertView, ViewGroup parent) {
		View row;

		if (null == convertView) {
		    row = mInflater.inflate(R.layout.item, null);
		} else {
		    row = convertView;
		}

		TextView tv = (TextView) row.findViewById(R.id.TextoItem);
		tv.setText(((RSSItem) getItem(position)).getTitle());

		final String desc = ((RSSItem) getItem(position))
			.getDescription();
		ImageView iv = (ImageView) row.findViewById(R.id.ImagenItem);

		if (desc.equals("OK")) {
		    iv.setImageBitmap(BitmapFactory.decodeResource(
			    getResources(), R.drawable.ledgreen));
		} else {
		    iv.setImageBitmap(BitmapFactory.decodeResource(
			    getResources(), R.drawable.ledred));
		}

		return row;

	    }
	};

	itemlist.setAdapter(adapter);

	// itemlist.setOnItemClickListener(this);

	itemlist.setSelection(0);

    }

    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	Log
		.i(tag, "Item clickado! [" + feed.getItem(position).getTitle()
			+ "]");

	Intent itemintent = new Intent(this, ShowDescription.class);

	Bundle b = new Bundle();
	b.putString("title", feed.getItem(position).getTitle());
	b.putString("description", feed.getItem(position).getDescription());
	b.putString("link", feed.getItem(position).getLink());
	b.putString("pubdate", feed.getItem(position).getPubDate());

	itemintent.putExtra("android.intent.extra.INTENT", b);

	startActivityForResult(itemintent, 0);
    }

}