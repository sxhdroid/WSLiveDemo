package me.lake.librestreaming.ws;

import android.content.Context;
import android.hardware.Camera;

import me.lake.librestreaming.model.RESConfig;
import me.lake.librestreaming.model.Size;

import static me.lake.librestreaming.ws.StreamConfig.AVOptionsHolder.DEFAULT_FILTER_MODE;
import static me.lake.librestreaming.ws.StreamConfig.AVOptionsHolder.DEFAULT_RENDER_MODE;

/**
 * Created by WangShuo on 2017/6/11.
 */

public class StreamConfig {

    public static class AVOptionsHolder {

        public static final int DEFAULT_CAMERA_INDEX = Camera.CameraInfo.CAMERA_FACING_FRONT;

        public static final int DEFAULT_FILTER_MODE = RESConfig.FilterMode.HARD;

        public static final int DEFAULT_RENDER_MODE = RESConfig.RenderingMode.OpenGLES;

        public static final int DEFAULT_PREVIEW_WIDTH = 1920;

        public static final int DEFAULT_PREVIEW_HEIGHT = 1080;

        public static final int DEFAULT_VIDEO_WIDTH = 1920;

        public static final int DEFAULT_VIDEO_HEIGHT = 1080;

        public static final int DEFAULT_VIDEO_BITRATE = 600 * 1024;

        public static final int DEFAULT_VIDEO_FPS = 24;

        public static final int DEFAULT_VIDEO_GOP = 1;

    }

    public static RESConfig build(Context context, StreamAVOption option) {
        RESConfig res = RESConfig.obtain();
        res.setFilterMode(DEFAULT_FILTER_MODE);
        res.setRenderingMode(DEFAULT_RENDER_MODE);
        res.setTargetPreviewSize(new Size(option.previewWidth,option.previewHeight));
        res.setTargetVideoSize(new Size(option.videoWidth, option.videoHeight));
        res.setBitRate(option.videoBitrate);
        res.setVideoFPS(option.videoFramerate);
        res.setVideoGOP(option.videoGOP);
        res.setDefaultCamera(option.cameraIndex);
        res.setRtmpAddr(option.streamUrl);
        return res;
    }


}
