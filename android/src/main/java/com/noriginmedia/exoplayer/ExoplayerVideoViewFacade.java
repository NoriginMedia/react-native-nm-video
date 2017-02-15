package com.noriginmedia.exoplayer;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import java.util.List;

public class ExoplayerVideoViewFacade {
	private static final String TAG = "VoVideoViewFacade";

	private VideoPlayer videoPlayer;
	private VideoPlayerFragment mVideoPlayerFragment;
	private AudioManager audioManager;
	private Context context;


	public ExoplayerVideoViewFacade(Context context, VideoPlayer videoPlayer, VideoPlayerFragment videoPlayerFragment) {
		audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		this.context = context;
		this.videoPlayer = videoPlayer;
		this.mVideoPlayerFragment = videoPlayerFragment;
	}

	public int getCurrentPosition() {
		return (int) videoPlayer.getCurrentPosition();
	}

	public int getDuration() {
		return (int) videoPlayer.getDuration();
	}

	public boolean isPlaying() {
		return videoPlayer.isPlaying();
	}

	public int getBufferPercentage() {
		return (int)videoPlayer.getBufferedPercentage();
	}

	public List<String> getAvailableLanguages() {
		return videoPlayer.getAvailableLanguages();
	}


	public List<String> getAvailableSubtitles() {
		return videoPlayer.getAvailableSubtitles();
	}

	public int getSelectedAudio() {
		return videoPlayer.getSelectedAudio();
	}

	public VideoPlayerFragment.PLAYER_STATE getPlayerState() {
		return mVideoPlayerFragment.getPlayerState();
	}

	public int getSelectedSubtitle() {
		int sub = -1;
		sub = videoPlayer.getSelectedSubtitle();
		Log.d(TAG,"-_- Subtitles selected: [" + sub + "] : ");
		return sub;
	}

	public boolean isTimeshiftingSupported() {
		return true;
	}

	public double getVolumePercent() {
		double curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		int max = audioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		return curVolume / max;
	}
}
