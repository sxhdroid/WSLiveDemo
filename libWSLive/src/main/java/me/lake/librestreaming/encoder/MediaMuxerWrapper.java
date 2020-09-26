package me.lake.librestreaming.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

import me.lake.librestreaming.BuildConfig;

public class MediaMuxerWrapper {
	private static final boolean DEBUG = BuildConfig.DEBUG;
	private static final String TAG = "MediaMuxerWrapper";

    private static final SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

	private String mOutputPath;
	// API >= 18
	private final MediaMuxer mMediaMuxer;
	private int mEncoderCount, mStatredCount;
	private boolean mIsStarted;
	private MediaEncoder mVideoEncoder, mAudioEncoder;

	/**
	 * Constructor
	 * @param outputPath output file path
	 * @throws IOException
	 */
	public MediaMuxerWrapper(String outputPath) throws IOException {
		if (outputPath == null) {
			try {
				this.mOutputPath = getCaptureFile(Environment.DIRECTORY_MOVIES, ".mp4").getAbsolutePath();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		} else  {
			this.mOutputPath = outputPath;
		}
		mMediaMuxer = new MediaMuxer(mOutputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
		mEncoderCount = mStatredCount = 0;
		mIsStarted = false;
	}

	public String getOutputPath() {
		return mOutputPath;
	}

	public void prepare() throws IOException {
		if (mVideoEncoder != null) {
			mVideoEncoder.prepare();
		}
		if (mAudioEncoder != null) {
			mAudioEncoder.prepare();
		}
	}

	public void startRecording() {
		if (mVideoEncoder != null) {
			mVideoEncoder.startRecording();
		}
		if (mAudioEncoder != null) {
			mAudioEncoder.startRecording();
		}
	}

	public void stopRecordingSegment() {
		if (mVideoEncoder != null) {
			mVideoEncoder.stopRecording();
		}
		if (mAudioEncoder != null) {
			mAudioEncoder.stopRecording();
		}
	}

	public void stopRecording() {
		if (mVideoEncoder != null) {
			mVideoEncoder.stopRecording();
		}
		mVideoEncoder = null;
		if (mAudioEncoder != null) {
			mAudioEncoder.stopRecording();
		}
		mAudioEncoder = null;
	}

	public synchronized boolean isStarted() {
		return mIsStarted;
	}

//**********************************************************************
//**********************************************************************
	/**
	 * assign encoder to this calss. this is called from encoder.
	 * @param encoder instance of MediaVideoEncoder or MediaAudioEncoder
	 */
	/*package*/ void addEncoder(final MediaEncoder encoder) {
		if (encoder instanceof MediaVideoEncoder) {
			if (mVideoEncoder != null) {
				throw new IllegalArgumentException("Video encoder already added.");
			}
			mVideoEncoder = encoder;
		} else if (encoder instanceof MediaAudioEncoder) {
			if (mAudioEncoder != null) {
				throw new IllegalArgumentException("Video encoder already added.");
			}
			mAudioEncoder = encoder;
		} else {
			throw new IllegalArgumentException("unsupported encoder");
		}
		mEncoderCount = (mVideoEncoder != null ? 1 : 0) + (mAudioEncoder != null ? 1 : 0);
	}

	/**
	 * request start recording from encoder
	 * @return true when muxer is ready to write
	 */
	/*package*/ synchronized boolean start() {
		if (DEBUG) {
			Log.v(TAG,  "start:");
		}
		mStatredCount++;
		if ((mEncoderCount > 0) && (mStatredCount == mEncoderCount)) {
			mMediaMuxer.start();
			mIsStarted = true;
			notifyAll();
			if (DEBUG) {
				Log.v(TAG,  "MediaMuxer started:");
			}
		}
		return mIsStarted;
	}

	/**
	 * request stop recording from encoder when encoder received EOS
	*/
	/*package*/ synchronized void stop() {
		if (DEBUG) {
			Log.v(TAG,  "stop:mStatredCount=" + mStatredCount);
		}
		mStatredCount--;
		if ((mEncoderCount > 0) && (mStatredCount <= 0)) {
			mMediaMuxer.stop();
			mMediaMuxer.release();
			mIsStarted = false;
			if (DEBUG) {
				Log.v(TAG,  "MediaMuxer stopped:");
			}
		}
	}

	/**
	 * assign encoder to muxer
	 * @param format
	 * @return minus value indicate error
	 */
	/*package*/ synchronized int addTrack(final MediaFormat format) {
		if (mIsStarted) {
			throw new IllegalStateException("muxer already started");
		}
		final int trackIx = mMediaMuxer.addTrack(format);
		if (DEBUG) {
			Log.i(TAG, "addTrack:trackNum=" + mEncoderCount + ",trackIx=" + trackIx + ",format=" + format);
		}
		return trackIx;
	}

	/**
	 * write encoded data to muxer
	 * @param trackIndex
	 * @param byteBuf
	 * @param bufferInfo
	 */
	/*package*/ synchronized void writeSampleData(final int trackIndex, final ByteBuffer byteBuf, final MediaCodec.BufferInfo bufferInfo) {
		if (mStatredCount > 0) {
			mMediaMuxer.writeSampleData(trackIndex, byteBuf, bufferInfo);
		}
	}

//**********************************************************************
//**********************************************************************
    /**
     * generate output file
     * @param type Environment.DIRECTORY_MOVIES / Environment.DIRECTORY_DCIM etc.
     * @param ext .mp4(.m4a for audio) or .png
     * @return return null when this app has no writing permission to external storage.
     */
    private File getCaptureFile(final String type, final String ext) {
		final File dir = Environment.getExternalStoragePublicDirectory(type);
		if (dir.exists() || dir.mkdir()) {
			if (dir.canWrite()) {
				return new File(dir, getDateTimeString() + ext);
			}
		}
    	return null;
    }

    /**
     * get current date and time as String
     * @return
     */
    private static String getDateTimeString() {
    	final GregorianCalendar now = new GregorianCalendar();
    	return mDateTimeFormat.format(now.getTime());
    }
}
