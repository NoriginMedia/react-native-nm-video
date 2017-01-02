package com.noriginmedia.react;

import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;

public class ReactVideoViewManager extends SimpleViewManager<ReactVideoView> {

    public static final String REACT_CLASS = "RCTNMVideo";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected ReactVideoView createViewInstance(ThemedReactContext themedReactContext) {
        return new ReactVideoView(themedReactContext);
    }
}
