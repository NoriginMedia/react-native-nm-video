package com.noriginmedia.react;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.noriginmedia.exoplayer.ExoPlayerView;
import com.noriginmedia.exoplayer.VideoPlayerFragment;

import android.util.Log;
import android.view.View;

public class ReactVideoView extends ExoPlayerView implements ExoPlayer.EventListener {
	private final String LOG_TAG = ReactVideoView.class.getSimpleName();

    public ReactVideoView(ThemedReactContext themedReactContext) {
        super(themedReactContext);
    }

	private void showLoader(boolean on) {
		//TODO
	}

	private void initPlayerPrepared() {
	}

	private void triggerOnPositionChanged() {

	}


	//TODO add methods for passing to react-native the states of the player
	public void notifyPlayerStateChanged(String playerState) {
		WritableMap event = Arguments.createMap();
		event.putString("playerState", playerState);
		ReactContext reactContext = (ReactContext) getContext();
		reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "topChange", event);
	}

	@Override
	public void onTimelineChanged(Timeline timeline, Object manifest) {

	}

	@Override
	public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

	}

	@Override
	public void onLoadingChanged(boolean isLoading) {

	}

	@Override
	public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
//		switch (playbackState) {
//			case ExoPlayer.STATE_BUFFERING:
//				initPlayerPrepared();
//				if (playWhenReady) {
//					mPlayerState = VideoPlayerFragment.PLAYER_STATE.prepared;
//				} else {
//					mPlayerState = VideoPlayerFragment.PLAYER_STATE.paused;
//				}
//				showLoader(true);
//				break;
//			case ExoPlayer.STATE_READY:
//				showLoader(false);
//				initPlayerPrepared();
//				triggerOnPositionChanged();
//				if (playWhenReady) {
//					mPlayerState = VideoPlayerFragment.PLAYER_STATE.playing;
//				} else {
//					mPlayerState = VideoPlayerFragment.PLAYER_STATE.paused;
//				}
//				long pos = mVideoPlayer.getCurrentPosition();
//				break;
//			case ExoPlayer.STATE_ENDED:
//				if (playWhenReady) {
//					mPlayerState = VideoPlayerFragment.PLAYER_STATE.playbackCompleted;
//				} else {
//					mPlayerState = VideoPlayerFragment.PLAYER_STATE.stopped;
//				}
//				break;
//			case ExoPlayer.STATE_IDLE:
//				mPlayerState = VideoPlayerFragment.PLAYER_STATE.initialized;
//				break;
//		}
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
//		mPlayerState = VideoPlayerFragment.PLAYER_STATE.error;
	}

	@Override
	public void onPositionDiscontinuity() {

	}
}
