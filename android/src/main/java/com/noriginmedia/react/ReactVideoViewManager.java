package com.noriginmedia.react;

import com.facebook.react.uimanager.ReactProp;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.noriginmedia.exoplayer.VideoPlayer;

import android.support.annotation.Nullable;

public class ReactVideoViewManager extends SimpleViewManager<ReactVideoView> {

    public static final String REACT_CLASS = "RCTNMVideo";

	private VideoPlayer mVideoPlayer;

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected ReactVideoView createViewInstance(ThemedReactContext themedReactContext) {
		ReactVideoView view = new ReactVideoView(themedReactContext);
		mVideoPlayer = new VideoPlayer(view, view.getContext());
        return view;
    }

	@ReactProp(name = "src")
	public void setSrc(ReactVideoView view, @Nullable String src) {
		mVideoPlayer.preparePlayer(src);
	}

	//TODO add methods for controling the life of the player including creating and destroying it
}
