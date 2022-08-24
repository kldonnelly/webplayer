package de.jockels.open;

import java.io.File;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.StatFs;
import android.util.Pair;

public class Size extends Pair<Long,Long> {
	
	private Size(long free, long size) { super(free, size); }


	public long guessSize() {
		if (second==0) return 0;
		long g;
		if (second>1024*1024*1024) g = 1024*1024*1024;
		else if (second>1024*1024) g = 1024*1024;
		else g = 1;
		while (second>g) g *= 2;
		return g;
	}


	@SuppressLint("NewApi")
	public static Size getSpace(File f) {
		if (f!=null) try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				// Gingerbread hat Gr��e/freier Platz im File
				return new Size(f.getUsableSpace(), f.getTotalSpace());
			} else {
				// vor Gingerbread muss der StatFs-Krams ran; wichtig ist die long-Wandlung
				StatFs fs = new StatFs(f.getAbsolutePath());
				return new Size((long)fs.getAvailableBlocks()*fs.getBlockSize(), (long)fs.getBlockCount()*fs.getBlockSize());
			}
		} catch (Exception e) { }
		return new Size(0, 0);
	}
}