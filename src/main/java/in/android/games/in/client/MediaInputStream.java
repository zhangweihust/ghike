package in.android.games.in.client;

import java.io.IOException;
import java.io.InputStream;

public class MediaInputStream extends InputStream {

	private InputStream mInStream;
	
	private String mName;
	
	private String mMimeType;
	
	private int mContentLength;

	public MediaInputStream(String name, String mimeType, InputStream inStream, int contentLength){
		mInStream = inStream;
		mName = name;
		mMimeType = mimeType;
		mContentLength = contentLength;
	}
	
	public int available() throws IOException {
		return mInStream.available();
	}

	public void close() throws IOException {
		mInStream.close();
	}

	public boolean equals(Object o) {
		return mInStream.equals(o);
	}

	public int hashCode() {
		return mInStream.hashCode();
	}

	public void mark(int readlimit) {
		mInStream.mark(readlimit);
	}

	public boolean markSupported() {
		return mInStream.markSupported();
	}

	public int read() throws IOException {
		return mInStream.read();
	}

	public int read(byte[] buffer, int offset, int length) throws IOException {
		return mInStream.read(buffer, offset, length);
	}

	public int read(byte[] buffer) throws IOException {
		return mInStream.read(buffer);
	}

	public void reset() throws IOException {
		mInStream.reset();
	}

	public long skip(long byteCount) throws IOException {
		return mInStream.skip(byteCount);
	}

	public String toString() {
		return mInStream.toString();
	}
	
	public String getName() {
		return mName;
	}
	
	public String getMimeType() {
		return mMimeType;
	}

	public int getContentLength() {
		return mContentLength;
	}

}
