package com.noriginmedia.exoplayer;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by Petru on 18/01/2017.
 */

//TODO DELETE THIS CLASS, THE FUNCTIONALITY SHOULD BE IN THE ReactVideoView

@SuppressLint("ValidFragment")
public class VideoPlayerFragment extends Fragment implements ExoPlayer.EventListener {
    private static final String LOG_TAG = "VideoPlayerFragment";

    public enum PLAYER_STATE {
        idle, initialized, prepared, preparing, playing, paused, stopped, playbackCompleted, end, error
    };


    protected PLAYER_STATE mPlayerState;

    private boolean onError;

    private long seekTo;

    private View mView, mPlayerView;

    private static Context mContext;
    private static Activity mActivity;

    private int mToBeSeeked;
    private String contentType;

    private VideoPlayer mVideoPlayer;

    private boolean initPlayerPrepared;

    private String mLicenseUrl;
    private String mDrmScheme;

    private boolean isComponentReady;
    protected int mLastVideoWidth, mLastVideoHeight, mLastTop, mLastLeft;
    private String url;

    public VideoPlayerFragment() {
        mPlayerState = PLAYER_STATE.idle;
        Log.d(LOG_TAG, "-_-_-_-_- VAPlayerFragment");
    }

    public VideoPlayerFragment(Activity activity) {
        mPlayerState = PLAYER_STATE.idle;
        mActivity = activity;
        Log.d(LOG_TAG, "-_-_-_-_- VAPlayerFragment (activity)");
    }

    private void preparePlayer(final String url) {
        onError = false;
        seekTo = 0L;
        initPlayerPrepared = false;
        mVideoPlayer.preparePlayer(url);
        mPlayerState = PLAYER_STATE.initialized;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mPlayerState = PLAYER_STATE.idle;
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "_-_ onCreateView Fragment");
        setChannelsList();

        return mView;
    }

    private void setChannelsList() {
    }

    @Override
    public void onAttach(Activity activity) {
        this.mActivity = activity;
        super.onAttach(activity);
    }

    @Override
    public void onAttach(Context context) {
        this.mContext = context;
        super.onAttach(context);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayerState = PLAYER_STATE.end;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initializePlayer() {
        isComponentReady = true;
        mVideoPlayer.initializePlayer(this, mDrmScheme, mLicenseUrl, null);
    }

    @Override
    public void onStart() {
        super.onStart();
        initializePlayer();
    }

    private void releasePlayer() {
        cmdStop();
        mVideoPlayer.releasePlayer();
        mPlayerState = PLAYER_STATE.end;
    }


    @Override
    public void onPause() {
        super.onPause();
        cmdPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        releasePlayer();
    }

    public void show() {
        Log.d(LOG_TAG, "_-_-_-_ showPlayerFragment ");
        if (mView != null) {
            mView.setVisibility(View.VISIBLE);
        }
    }

    public void hide() {
        Log.d(LOG_TAG, "_-_-_-_ hidePlayerFragment");
        if (mView != null) {
            showLoader(false);
            mView.setVisibility(View.INVISIBLE);
        }
    }

    public void showLoader(boolean onOff) {
        Log.d(LOG_TAG, "---___--- show loader:" + onOff + " mHeight:" + mLastVideoHeight + " mWidth:"
                + mLastVideoWidth);
        if (mLastVideoHeight > 0 && mLastVideoHeight > 0) {
        } else {
        }
    }

    protected ExoplayerVideoViewFacade getViewAdapter() {
        return new ExoplayerVideoViewFacade(mContext, mVideoPlayer, this);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(LOG_TAG, "onConfigurationChanged");
        resizeVideo(newConfig, false);
    }

    private void resizeVideo(Configuration config, boolean fullscreen) {
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();
        boolean softkeys = false;
        resizeVideo(config, fullscreen, screenWidth, screenHeight, true, softkeys);
    }

    private void resizeVideo(Configuration config, boolean fullscreen, int screenWidth, int screenHeight, boolean isPortrait, boolean softkeys) {
        if (mView == null || mPlayerView == null)
            return;

        int displayWidth = screenWidth;
        int displayHeight = screenHeight;
        int videoWidth = 0;
        int videoHeight = 0;
        int left = 0;
        int top = 0;

        videoWidth = displayWidth;
        videoHeight = displayHeight;

        Log.d(LOG_TAG, "_-_ resizingVideo... vH:" + videoHeight + " vW:" + videoWidth + " mLastH:" + mLastVideoHeight
                + " mLastW:" + mLastVideoWidth);
        int vTop, vLeft;

        if (videoWidth > 0 && videoHeight > 0 && !fullscreen) {
            if (displayHeight - videoHeight > 0) {
                vTop = top + (displayHeight - videoHeight) / 2;
            } else {
                vTop = top;
            }

            if (displayWidth - videoWidth > 0) {
                vLeft = left + (displayWidth - videoWidth) / 2;
            } else {
                vLeft = left;
            }
        } else {
            vTop = 0;
            vLeft = 0;
        }

        if (mLastVideoWidth != videoWidth || mLastVideoHeight != videoHeight
                || vTop != mLastTop || vLeft != mLastLeft) {
            RelativeLayout.LayoutParams paramsV;

            if (videoWidth > 0 && videoHeight > 0) {
                paramsV = new RelativeLayout.LayoutParams(videoWidth, videoHeight);
                paramsV.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                paramsV.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

            }
            else {
                paramsV = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
                        RelativeLayout.LayoutParams.FILL_PARENT);
                paramsV.addRule(RelativeLayout.CENTER_IN_PARENT);
            }
            mLastTop = paramsV.topMargin = vTop;
            mLastLeft = paramsV.leftMargin = vLeft;
            Log.d(LOG_TAG, "_-_ resizedVideo tm:" + paramsV.topMargin + " tl: " + paramsV.leftMargin + " w:"
                    + paramsV.width + " h:" + paramsV.height + " fullscreen:" + fullscreen);

            mPlayerView.setLayoutParams(paramsV);
            mView.requestLayout();

            mLastVideoHeight = videoHeight;
            mLastVideoWidth = videoWidth;
        }

        boolean fullView = (displayWidth >= screenWidth && displayHeight >= screenHeight);
    }

    public void setVideoData(String url) {
        this.url = url;
            Log.i(LOG_TAG, " --- setVideoData --- [vod = "
                    + url + "] position:"
                    + 0);
            mToBeSeeked = 0;
    }


    private void applyKonftikiDrm() {
        String mLicenseUrl = null;
        String mDrmScheme = null;

        if (!TextUtils.isEmpty(mLicenseUrl) && !TextUtils.isEmpty(mDrmScheme)
                && (!mLicenseUrl.equals(this.mLicenseUrl) || !mDrmScheme.equals(this.mDrmScheme))) {
            Log.v("Konftiki", "mLicenseUrl in ExoPlayerFragment: " + mLicenseUrl);
            Log.v("Konftiki", "mDrmScheme in ExoPlayerFragment: " + mDrmScheme);
            this.mLicenseUrl = mLicenseUrl;
            this.mDrmScheme = mDrmScheme;
            releasePlayer();
            initializePlayer();
        }
    }


    public void cmdStartPlaying() {
        applyKonftikiDrm();
        preparePlayer(url);
    }

    public boolean cmdStop() {
        if (mVideoPlayer.isPlayerReady()) {

            mPlayerState = PLAYER_STATE.stopped;

            mVideoPlayer.stop();
        }
        return true;
    }

    public boolean cmdPause() {
        if (mVideoPlayer.isPlayerReady()) {
            mVideoPlayer.setPlayWhenReady(false);
            mPlayerState = PLAYER_STATE.paused;
        }
        return true;
    }



    public boolean cmdPlay() {
        if (mVideoPlayer.isPlayerReady() && !mVideoPlayer.isPlaying()) {
            mVideoPlayer.setPlayWhenReady(true);
            triggerOnPositionChanged();
        }
        return false;
    }

    public boolean cmdToggle() {
        if (mVideoPlayer.isPlayerReady()) {
          cmdPause();
          showLoader(false);
        } else {
          cmdPlay();
        }
        return true;
    }

    public boolean cmdSeekToPosition(int position, boolean timeshifting, boolean isFinal) {
        if (mVideoPlayer.isPlayerReady()) {
            if (mVideoPlayer.isPrepared()) {
                long duration = mVideoPlayer.getDuration();

                Log.d(LOG_TAG, "_-_ onPlayerSeekToPosition: duration:"
                        + duration);
                mVideoPlayer.seekTo(position);
                showLoader(true);
            } else {
                mToBeSeeked = position;
            }
        }
        return false;
    }


    public void cmdReset() {
        if (isAdded()) {
            mLastVideoWidth = -1;
            mLastVideoHeight = -1;
            mLastTop = -1;
            mLastLeft = -1;
            Log.v(LOG_TAG, "onCmdReset");
            resizeVideo(getResources().getConfiguration(), false);
        }
    }


    // ExoPlayer.EventListener implementation

    @Override
    public void onLoadingChanged(boolean isLoading) {
        // Do nothing.
    }

    private void initPlayerPrepared() {
        if (!initPlayerPrepared) {
            long duration = mVideoPlayer.getDuration();

            Log.d(LOG_TAG, "_-_ onPrepared: mToBeSeeked: " + mToBeSeeked + " duration:");

            if (mToBeSeeked >= 0) {
                mVideoPlayer.seekTo(mToBeSeeked);
                Log.d(LOG_TAG, "_-_ seeking to the: " + mToBeSeeked);
            }


            triggerOnPositionChanged();

            mToBeSeeked = 0;
            initPlayerPrepared = true;
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                initPlayerPrepared();
                if (playWhenReady) {
                    mPlayerState = PLAYER_STATE.prepared;
                } else {
                    mPlayerState = PLAYER_STATE.paused;
                }
                showLoader(true);
                break;
            case ExoPlayer.STATE_READY:
                showLoader(false);
                initPlayerPrepared();
                triggerOnPositionChanged();
                if (playWhenReady) {
                    mPlayerState = PLAYER_STATE.playing;
                } else {
                    mPlayerState = PLAYER_STATE.paused;
                }
                long pos = mVideoPlayer.getCurrentPosition();
                break;
            case ExoPlayer.STATE_ENDED:
                if (playWhenReady) {
                    mPlayerState = PLAYER_STATE.playbackCompleted;
                } else {
                    mPlayerState = PLAYER_STATE.stopped;
                }
                break;
            case ExoPlayer.STATE_IDLE:
                mPlayerState = PLAYER_STATE.initialized;
                break;
        }
    }

    @Override
    public void onPositionDiscontinuity() {
        // Do nothing.
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        String errorString = null;
        if (e.type == ExoPlaybackException.TYPE_RENDERER) {
            Exception cause = e.getRendererException();
            if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                // Special case for decoder initialization failures.
                MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                        (MediaCodecRenderer.DecoderInitializationException) cause;
                if (decoderInitializationException.decoderName == null) {
                    errorString = decoderInitializationException.getCause().toString();
                } else {
                    errorString = decoderInitializationException.toString();
                }
            }
        }
        if (errorString != null) {
            Log.d(LOG_TAG, "Exo VideoPlayer" + errorString);
        }
        mPlayerState = PLAYER_STATE.error;
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
    }

    private int getLastPosition() {
        if (mToBeSeeked > 0
                && mPlayerState != PLAYER_STATE.paused
                && mPlayerState != PLAYER_STATE.playing
                && mPlayerState != PLAYER_STATE.stopped
                && mPlayerState != PLAYER_STATE.end) {
            return mToBeSeeked;
        }
        return (int)mVideoPlayer.getCurrentPosition();
    }

    private static final int START_SENDING_ONPOSTIONCHANGED = 1;

    private void triggerOnPositionChanged() {
        Message msg = mHandler.obtainMessage(START_SENDING_ONPOSTIONCHANGED);
        mHandler.removeMessages(START_SENDING_ONPOSTIONCHANGED);
        mHandler.sendMessageDelayed(msg, 1000);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mPlayerState == PLAYER_STATE.playing || mPlayerState == PLAYER_STATE.paused) {
                Message newMsg = mHandler.obtainMessage(START_SENDING_ONPOSTIONCHANGED);
                mHandler.removeMessages(START_SENDING_ONPOSTIONCHANGED);
                sendMessageDelayed(newMsg, 1000);
            }
        }
    };

    public PLAYER_STATE getPlayerState() {
        return this.mPlayerState;
    }
}
