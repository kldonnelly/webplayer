package de.jockels.open;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.Log;
import de.jockels.open.pref.DevicesListPreference;



public class Environment2  {
	private static final String TAG = "Environment2";
	private static final boolean DEBUG = true;
	
	private static ArrayList<DeviceDiv> mDeviceList = null;
	private static boolean mExternalEmulated = false;
	private static Device mInternal = null;
	protected static DeviceExternal mPrimary = null;
	private static DeviceDiv mSecondary = null;

	public final static String PATH_PREFIX = "/Android/data/";
	static {
		rescanDevices();
	}


	public static boolean isSecondaryExternalStorageAvailable() {
		return mSecondary!=null && mSecondary.isAvailable();
	}


	public final static boolean isSecondaryExternalStorageRemovable() throws NoSecondaryStorageException {
		if (mSecondary==null) throw new NoSecondaryStorageException();
		return true;
	}
	

	public static File getSecondaryExternalStorageDirectory() throws NoSecondaryStorageException {
		if (mSecondary==null) throw new NoSecondaryStorageException();
		return mSecondary.getFile(); 
	}


	public static String getSecondaryExternalStorageState() throws NoSecondaryStorageException {
		if (mSecondary==null) throw new NoSecondaryStorageException();
		return mSecondary.getState();
	}


	public static File getSecondaryExternalStoragePublicDirectory(String s) throws NoSecondaryStorageException {
		if (mSecondary==null) throw new NoSecondaryStorageException();
		if (s==null) throw new IllegalArgumentException("s darf nicht null sein");
		return mSecondary.getPublicDirectory(s);
	}
	

	public static File getSecondaryExternalFilesDir(Context context, String s) throws NoSecondaryStorageException {
		if (mSecondary==null) throw new NoSecondaryStorageException();
		if (context==null) throw new IllegalArgumentException("context darf nicht null sein");
		return mSecondary.getFilesDir(context, s);
	}
	
	
	public static File getSecondaryExternalCacheDir(Context context) throws NoSecondaryStorageException {
		if (mSecondary==null) throw new NoSecondaryStorageException();
		if (context==null) throw new IllegalArgumentException("context darf nicht null sein");
		return mSecondary.getCacheDir(context);
	}
	

/*	
	 * Implementiert sind: {@link #getCardDirectory()}, 
	 * {@link #getCardPublicDirectory(String)}, {@link #getCardStorageState()} , 
	 * {@link #getCardCacheDir(Context)}, {@link #getCardFilesDir(Context, String)}.
*/
	public static File getCardDirectory() {
		if (isSecondaryExternalStorageAvailable())
			try {return getSecondaryExternalStorageDirectory();} 
			catch (NoSecondaryStorageException e) {throw new RuntimeException("NoSecondaryException trotz Available"); }
		else
			return Environment.getExternalStorageDirectory();
	}

	public static File getCardPublicDirectory(String dir) {
		if (isSecondaryExternalStorageAvailable())
			try {return getSecondaryExternalStoragePublicDirectory(dir);} 
			catch (NoSecondaryStorageException e) {throw new RuntimeException("NoSecondaryException trotz Available"); }
		else
			return mPrimary.getPublicDirectory(dir);
	}

	public static String getCardState() {
		if (isSecondaryExternalStorageAvailable())
			try {return getSecondaryExternalStorageState();} 
			catch (NoSecondaryStorageException e) {throw new RuntimeException("NoSecondaryException trotz Available"); }
		else
			return Environment.getExternalStorageState();
	}

	public static File getCardCacheDir(Context ctx) {
		if (isSecondaryExternalStorageAvailable())
			try {return getSecondaryExternalCacheDir(ctx);} 
			catch (NoSecondaryStorageException e) {throw new RuntimeException("NoSecondaryException trotz Available"); }
		else
			return mPrimary.getCacheDir(ctx);
	}

	public static File getCardFilesDir(Context ctx, String dir) {
		if (isSecondaryExternalStorageAvailable())
			try {return getSecondaryExternalFilesDir(ctx, dir);} 
			catch (NoSecondaryStorageException e) {throw new RuntimeException("NoSecondaryException trotz Available"); }
		else
			return mPrimary.getFilesDir(ctx, dir);
	}


	public static boolean isExternalStorageEmulated() {
		return mExternalEmulated; 
	}


	public static boolean isExternalStorageRemovable() { 
		return mPrimary.isRemovable();
	}


	public static IntentFilter getRescanIntentFilter() {
		if (mDeviceList==null) rescanDevices();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL); // rausgenommen
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED); // wieder eingesetzt
		filter.addAction(Intent.ACTION_MEDIA_REMOVED); // entnommen
		filter.addAction(Intent.ACTION_MEDIA_SHARED); // per USB am PC
		// geht ohne folgendes nicht, obwohl das in der Doku nicht so recht steht
		filter.addDataScheme("file"); 

		/*
		 * die folgenden waren zumindest bei den bisher mit USB getesteten 
		 * Ger�ten nicht notwendig, da diese auch bei USB-Sticks und externen 
		 * SD-Karten die ACTION_MEDIA-Intents abgefeuert haben
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		 */
		return filter;
	}
	

	public static BroadcastReceiver registerRescanBroadcastReceiver(Context context, final Runnable r) {
		if (mDeviceList==null) rescanDevices();
		BroadcastReceiver br = new BroadcastReceiver() {
			@Override public void onReceive(Context context, Intent intent) {
				if (DEBUG) Log.i(TAG, "Storage: "+intent.getAction()+"-"+intent.getData());
				updateDevices();
				if (r!=null) r.run();
			}
		};
		context.registerReceiver(br, getRescanIntentFilter());
		return br;
	}


	public static BroadcastReceiver registerRescanBroadcastReceiver(Context context, final BroadcastReceiver r) {
		if (mDeviceList==null) rescanDevices();
		BroadcastReceiver br = new BroadcastReceiver() {
			@Override public void onReceive(Context context, Intent intent) {
				if (DEBUG) Log.i(TAG, "Storage: "+intent.getAction()+"-"+intent.getData());
				updateDevices();
				if (r!=null) r.onReceive(context, intent);
			}
		};
		context.registerReceiver(br, getRescanIntentFilter());
		return br;
	}


	public static void updateDevices() {
		for (Device i : mDeviceList) {i.updateState();}
		mPrimary.updateState();
	}


	@SuppressLint("NewApi")
	public static void rescanDevices() {
		mDeviceList = new ArrayList<DeviceDiv>(10);
		mPrimary = new DeviceExternal();

		// vold.fstab lesen; TODO bei Misserfolg eine andere Methode
		if (!scanVold("vold.fstab")) scanVold("vold.conf");

    	// zeigen /mnt/sdcard und /data auf denselben Speicher?
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
    		mExternalEmulated = Environment.isExternalStorageEmulated();
    	} else {
    		// vor Honeycom gab es den unified memory noch nicht
    		mExternalEmulated = false; 
    	}

		// Pfad zur zweiten SD-Karte suchen; bisher nur Methode 1 implementiert
		// Methode 1: einfach der erste Eintrag in vold.fstab, ggf. um ein /mnt/sdcard-Doppel bereinigt
		// Methode 2: das erste mit "sd", falls nicht vorhanden das erste mit "ext"
		// Methode 3: das erste verf�gbare
		if (mDeviceList.size()==0) {
			mSecondary = null;
			// TODO Ger�te mit interner SD und Android 2 wie Nexus S
			// if (nexus) mPrimary.setRemovable(false);
		} else {
			mSecondary = mDeviceList.get(0);
			if (mSecondary.getName().contains("usb")) {
				// z.B. HTC One X+
				mSecondary = null;
			} else {
				// jau, SD gefunden
				mSecondary.setName("SD-Card");
				// Hack
				if (mPrimary.isRemovable()) Log.w(TAG, "isExternStorageRemovable overwrite (secondary sd found) auf false");
				mPrimary.setRemovable(false);
			}
		}
	}
	

	private static boolean scanVold(String name) {
		String s, f;
		boolean prefixScan = true; // sdcard-Prefixes
		SimpleStringSplitter sp = new SimpleStringSplitter(' ');
    	try {
    		BufferedReader buf = new BufferedReader(new FileReader(Environment.getRootDirectory().getAbsolutePath()+"/etc/"+name), 2048);
    		s = buf.readLine();
    		while (s!=null) {
    			sp.setString(s.trim());
    			f = sp.next(); // dev_mount oder anderes
        		if ("dev_mount".equals(f)) {
        			DeviceDiv d = new DeviceDiv(sp);
        			
        			if (TextUtils.equals(mPrimary.getMountPoint(), d.getMountPoint())) {
        				// ein wenig Spezialkrams �ber /mnt/sdcard herausfinden
        				
        				// wenn die Gingerbread-Funktion isExternalStorageRemovable nicht da ist, diesen Hinweis nutzen
        				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) 
        					mPrimary.setRemovable(true); 
        					// dann ist auch der Standard-Eintrag removable
        					// eigentlich reicht das hier nicht, denn die vold-Eintr�ge f�r die prim�re SD-Karte sind viel komplexer, 
        					// oft steht da was von non-removable. Doch diese ganzen propriet�ren Klamotten auszuwerden,
        					// w�re viel zu komplex. Ein gangbarer Kompromiss scheint zu sein, sich ab 2.3 einfach auf
        					// isExternalStorageRemovable zu verlassen, was schon oben in Device() gesetzt wird. Bei den
        					// bisher aufgetauchten Ger�ten mit 2.2 wiederum scheint der Hinweis in vold zu klappen.vccfg
        				
        				// z.B. Galaxy Note h�ngt "encryptable_nonremovable" an
        				while (sp.hasNext()) {
        					f = sp.next();
        					if (f.contains("nonremovable")) {
        						mPrimary.setRemovable(false);
        						Log.w(TAG, "isExternStorageRemovable overwrite ('nonremovable') auf false");
        					}
        				}
        				prefixScan = false;
        			} else 
        				// nur in Liste aufnehmen, falls nicht Dupe von /mnt/sdcard
        				mDeviceList.add(d);
        			
        		} else if (prefixScan) {
        			// Weitere Untersuchungen nur, wenn noch vor sdcard-Eintrag
        			// etwas unsauber, da es eigentlich in {} vorkommen muss, was ich hier nicht �berpr�fe
        			
        			if ("discard".equals(f)) {
        				// manche (Galaxy Note) schreiben "discard=disable" vor den sdcard-Eintrag.
        				sp.next(); // "="
        				f = sp.next();
        				if ("disable".equals(f)) {
        					mPrimary.setRemovable(false);
        					Log.w(TAG, "isExternStorageRemovable overwrite ('discard=disable') auf false");
        				} else if ("enable".equals(f)) {
        					// ha, denkste...  bisher habe ich den Eintrag nur bei zwei Handys gefunden, (Galaxy Note, Galaxy Mini 2), und
        					// da stimmte er *nicht*, sondern die Karten waren nicht herausnehmbar.
        					// mPrimary.mRemovable = true;
        					Log.w(TAG, "isExternStorageRemovable overwrite overwrite ('discard=enable'), bleibt auf "+mPrimary.isRemovable());
        				} else
        					Log.w(TAG, "disable-Eintrag unverst�ndlich: "+f);
        			}
        			
        		}
    			s = buf.readLine();
    		}
    		buf.close();
    		Log.v(TAG, name+" gelesen; Ger�te gefunden: "+mDeviceList.size());
    		return true;
    	} catch (Exception e) {
    		Log.e(TAG, "kann "+name+" nicht lesen: "+e.getMessage());
    		return false;
    	}
	}

	public static Device[] getDevices(String key, boolean available, boolean intern, boolean data) {
		if (key!=null) key = key.toLowerCase();
		ArrayList<Device> temp = new ArrayList<Device>(mDeviceList.size()+2);
		if (data) temp.add(getInternalStorage());
		if (intern && ( !available || mPrimary.isAvailable())) temp.add(mPrimary);
		for (Device d : mDeviceList) {
			if ( ((key==null) || d.getName().toLowerCase().contains(key)) && (!available || d.isAvailable()) ) temp.add(d);
		}
		return temp.toArray(new Device[temp.size()]);
	}
	

	public static Device getPrimaryExternalStorage() {
		return mPrimary;
	}
	
	
	public static Device getSecondaryExternalStorage() throws NoSecondaryStorageException {
		if (mSecondary==null) throw new NoSecondaryStorageException();
		return mSecondary;
	}
	
	
	public static Device getInternalStorage() {
		if (mInternal==null) mInternal = new DeviceIntern();
		return mInternal;
	}
	
	
}
