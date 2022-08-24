package de.jockels.open;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils.SimpleStringSplitter;

class DeviceDiv extends Device {
	private String mLabel, mName;
	private boolean mAvailable, mWriteable;
	


	DeviceDiv(SimpleStringSplitter sp) {
		mLabel = sp.next().trim();
		mMountPoint = sp.next().trim();
		updateState();
	}
	

	public boolean isAvailable() { return mAvailable; }


	public boolean isWriteable() { return mWriteable; }

	
	protected void updateState() {
		File f = new File(mMountPoint);
		setName(f.getName()); // letzter Teil des Pfads
		if (mAvailable = f.isDirectory() && f.canRead()) { // ohne canRead() klappts z.B. beim Note2 nicht
			mSize = Size.getSpace(f); 
			mWriteable = f.canWrite();
			// Korrektur, falls in /mnt/sdcard gemountet (z.B. Samsung)
			if (mMountPoint.startsWith(Environment2.mPrimary.mMountPoint) && mSize.equals(Environment2.mPrimary.mSize)) 
				mAvailable = mWriteable = false;
		} else 
			mWriteable = false;
		
	}

	public final String getLabel() { return mLabel; }

	
	public String getName() { return mName; }
	protected final void setName(String name) { mName = name; }

	
	public boolean isRemovable() { return true; }

	public File getCacheDir(Context ctx) { return getFilesDirLow(ctx, "/cache"); }

	
	public File getFilesDir(Context ctx) { return getFilesDirLow(ctx, "/files"); }
	
	
	public File getFilesDir(Context ctx, String s) { return getFilesDirLow(ctx, s); }

	
	public File getPublicDirectory(String s) { 
		if (s!=null && !s.startsWith("/")) s = "/" + s;
		return new File(getMountPoint() + s);
	}


	public String getState() {
		if (mAvailable)
			return mWriteable ? Environment.MEDIA_MOUNTED : Environment.MEDIA_MOUNTED_READ_ONLY;
		else 
			return Environment.MEDIA_REMOVED;
	}

}
