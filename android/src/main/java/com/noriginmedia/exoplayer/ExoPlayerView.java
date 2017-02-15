/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.noriginmedia.exoplayer;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.ResizeMode;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.util.Assertions;

import com.noriginmedia.react.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.List;

/**
 * A high level view for {@link SimpleExoPlayer} media playbacks.
 * <p>
 * A SimpleExoPlayerView can be customized by setting attributes (or calling corresponding methods),
 * overriding the view's layout file or by specifying a custom view layout file, as outlined below.
 *
 * <h3>Attributes</h3>
 * The following attributes can be set on a SimpleExoPlayerView when used in a layout XML file:
 * <p>
 * <ul>
 *   <li><b>{@code resize_mode}</b> - Controls how video and album art is resized within the view.
 *       Valid values are {@code fit}, {@code fixed_width}, {@code fixed_height} and {@code fill}.
 *       <ul>
 *         <li>Corresponding method: {@link #setResizeMode(int)}</li>
 *         <li>Default: {@code fit}</li>
 *       </ul>
 *   </li>
 *   <li><b>{@code surface_type}</b> - The type of surface view used for video playbacks. Valid
 *       values are {@code surface_view}, {@code texture_view} and {@code none}. Using {@code none}
 *       is recommended for audio only applications, since creating the surface can be expensive.
 *       Using {@code surface_view} is recommended for video applications.
 *       <ul>
 *         <li>Corresponding method: None</li>
 *         <li>Default: {@code surface_view}</li>
 *       </ul>
 *   </li>
 *   <li><b>{@code player_layout_id}</b> - Specifies the id of the layout to be inflated. See below
 *       for more details.
 *       <ul>
 *         <li>Corresponding method: None</li>
 *         <li>Default: {@code R.id.exo_simple_player_view}</li>
 *       </ul>
 * </ul>
 *
 * <h3>Overriding the layout file</h3>
 * To customize the layout of SimpleExoPlayerView throughout your app, or just for certain
 * configurations, you can define {@code exo_simple_player_view.xml} layout files in your
 * application {@code res/layout*} directories. These layouts will override the one provided by the
 * ExoPlayer library, and will be inflated for use by SimpleExoPlayerView. The view identifies and
 * binds its children by looking for the following ids:
 * <p>
 * <ul>
 *   <li><b>{@code exo_content_frame}</b> - A frame whose aspect ratio is resized based on the video
 *       or album art of the media being played, and the configured {@code resize_mode}. The video
 *       surface view is inflated into this frame as its first child.
 *       <ul>
 *         <li>Type: {@link AspectRatioFrameLayout}</li>
 *       </ul>
 *   </li>
 *   <li><b>{@code exo_shutter}</b> - A view that's made visible when video should be hidden. This
 *       view is typically an opaque view that covers the video surface view, thereby obscuring it
 *       when visible.
 *       <ul>
 *        <li>Type: {@link View}</li>
 *       </ul>
 *   </li>
 *   <li><b>{@code exo_subtitles}</b> - Displays subtitles.
 *       <ul>
 *        <li>Type: {@link SubtitleView}</li>
 *       </ul>
 *   </li>
 *   <li><b>{@code exo_artwork}</b> - Displays album art.
 *       <ul>
 *        <li>Type: {@link ImageView}</li>
 *       </ul>
 *   </li>
 * </ul>
 * <p>
 * All child views are optional and so can be omitted if not required, however where defined they
 * must be of the expected type.
 *
 * <h3>Specifying a custom layout file</h3>
 * Defining your own {@code exo_simple_player_view.xml} is useful to customize the layout of
 * SimpleExoPlayerView throughout your application. It's also possible to customize the layout for a
 * single instance in a layout file. This is achieved by setting the {@code player_layout_id}
 * attribute on a SimpleExoPlayerView. This will cause the specified layout to be inflated instead
 * of {@code exo_simple_player_view.xml} for only the instance on which the attribute is set.
 */
@TargetApi(16)
public class ExoPlayerView extends FrameLayout {

  private final AspectRatioFrameLayout contentFrame;
  private final View surfaceView;
  private final SubtitleView subtitleView;
  private final ComponentListener componentListener;

  private SimpleExoPlayer player;

  public ExoPlayerView(Context context) {
    this(context, null);
  }

  public ExoPlayerView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ExoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    int playerLayoutId = R.layout.exo_simpleplayer_view;
    int resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT;

    LayoutInflater.from(context).inflate(playerLayoutId, this);
    componentListener = new ComponentListener();
    setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

    // Content frame.
    contentFrame = (AspectRatioFrameLayout) findViewById(R.id.exo_content_frame);
    if (contentFrame != null) {
      setResizeModeRaw(contentFrame, resizeMode);
    }

    // Create a surface view and insert it into the content frame, if there is one.
    if (contentFrame != null) {
      ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
      surfaceView = new SurfaceView(context);
      surfaceView.setLayoutParams(params);
      contentFrame.addView(surfaceView, 0);
    } else {
      surfaceView = null;
    }

    // Subtitle view.
    subtitleView = (SubtitleView) findViewById(R.id.exo_subtitles);
    if (subtitleView != null) {
      subtitleView.setUserDefaultStyle();
      subtitleView.setUserDefaultTextSize();
    }

    // Playback control view.
  }

  /**
   * Returns the player currently set on this view, or null if no player is set.
   */
  public SimpleExoPlayer getPlayer() {
    return player;
  }

  /**
   * Set the {@link SimpleExoPlayer} to use. The {@link SimpleExoPlayer#setTextOutput} and
   * {@link SimpleExoPlayer#setVideoListener} method of the player will be called and previous
   * assignments are overridden.
   *
   * @param player The {@link SimpleExoPlayer} to use.
   */
  public void setPlayer(SimpleExoPlayer player) {
    if (this.player == player) {
      return;
    }
    if (this.player != null) {
      this.player.setTextOutput(null);
      this.player.setVideoListener(null);
      this.player.removeListener(componentListener);
      this.player.setVideoSurface(null);
    }
    this.player = player;

    if (player != null) {
      player.setVideoSurfaceView((SurfaceView) surfaceView);
      player.setVideoListener(componentListener);
      player.addListener(componentListener);
      player.setTextOutput(componentListener);
    }
  }

  /**
   * Sets the resize mode.
   *
   * @param resizeMode The resize mode.
   */
  public void setResizeMode(@ResizeMode int resizeMode) {
    Assertions.checkState(contentFrame != null);
    contentFrame.setResizeMode(resizeMode);
  }

  /**
   * Gets the view onto which video is rendered. This is either a {@link SurfaceView} (default)
   * or a {@link TextureView} if the {@code use_texture_view} view attribute has been set to true.
   *
   * @return Either a {@link SurfaceView} or a {@link TextureView}.
   */
  public View getVideoSurfaceView() {
    return surfaceView;
  }


  /**
   * Gets the {@link SubtitleView}.
   *
   * @return The {@link SubtitleView}, or {@code null} if the layout has been customized and the
   *     subtitle view is not present.
   */
  public SubtitleView getSubtitleView() {
    return subtitleView;
  }




  @SuppressWarnings("ResourceType")
  private static void setResizeModeRaw(AspectRatioFrameLayout aspectRatioFrame, int resizeMode) {
    aspectRatioFrame.setResizeMode(resizeMode);
  }

  private final class ComponentListener implements SimpleExoPlayer.VideoListener,
      TextRenderer.Output, ExoPlayer.EventListener {

    // TextRenderer.Output implementation

    @Override
    public void onCues(List<Cue> cues) {
      if (subtitleView != null) {
        subtitleView.onCues(cues);
      }
    }

    // SimpleExoPlayer.VideoListener implementation

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
        float pixelWidthHeightRatio) {
      if (contentFrame != null) {
        float aspectRatio = height == 0 ? 1 : (width * pixelWidthHeightRatio) / height;
        contentFrame.setAspectRatio(aspectRatio);
      }
    }

    @Override
    public void onRenderedFirstFrame() {
    }

    @Override
    public void onTracksChanged(TrackGroupArray tracks, TrackSelectionArray selections) {
    }

    // ExoPlayer.EventListener implementation

    @Override
    public void onLoadingChanged(boolean isLoading) {
      // Do nothing.
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
      // Do nothing.
    }

    @Override
    public void onPositionDiscontinuity() {
      // Do nothing.
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
      // Do nothing.
    }

  }

}
