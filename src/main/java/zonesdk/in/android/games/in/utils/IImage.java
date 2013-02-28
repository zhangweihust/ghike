package zonesdk.in.android.games.in.utils;

import android.net.Uri;

public interface IImage {
	static final int THUMBNAIL_TARGET_SIZE = 320;
	static final int MINI_THUMB_TARGET_SIZE = 96;
	static final int THUMBNAIL_MAX_NUM_PIXELS = 512 * 384;
	static final int MINI_THUMB_MAX_NUM_PIXELS = 128 * 128;
	static final int UNCONSTRAINED = -1;

	public static final boolean ROTATE_AS_NEEDED = true;
	public static final boolean NO_ROTATE = false;

	public abstract Uri fullSizeImageUri();

}
