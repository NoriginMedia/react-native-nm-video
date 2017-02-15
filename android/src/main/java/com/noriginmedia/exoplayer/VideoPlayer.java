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

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Pair;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * An activity that plays media using {@link SimpleExoPlayer}.
 */
public class VideoPlayer {

    private static final String TAG = VideoPlayer.class.getSimpleName();

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final CookieManager DEFAULT_COOKIE_MANAGER;
    private static final TrackSelection.Factory FIXED_FACTORY = new FixedTrackSelection.Factory();

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private Handler mainHandler;
    private Timeline.Window window;
    private EventLogger eventLogger;
    private ExoPlayerView simpleExoPlayerView;

    private DataSource.Factory mediaDataSourceFactory;
    private SimpleExoPlayer player;

    private DefaultTrackSelector trackSelector;
    private Context mContext;
    private String userAgent;


    public VideoPlayer(ExoPlayerView simpleExoPlayerView, Context context) {
        mContext = context;
        userAgent = Util.getUserAgent(mContext, "ExoVideoPlayer");
        mediaDataSourceFactory = buildDataSourceFactory(true);
        mainHandler = new Handler();
        window = new Timeline.Window();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }
        this.simpleExoPlayerView = simpleExoPlayerView;
    }

    private UUID getDrmUuid(String typeString) {
        if (!TextUtils.isEmpty(typeString)) {
            switch (typeString.toLowerCase()) {
                case "widevine":
                    return C.WIDEVINE_UUID;
                case "playready":
                    return C.PLAYREADY_UUID;
                default:
                    try {
                        return UUID.fromString(typeString);
                    } catch (RuntimeException e) {
                    }
            }
        }
        return null;
    }

    public void initializePlayer(ExoPlayer.EventListener listener, String drmScheme, String drmLicenseUrl, String[] keyRequestPropertiesArray) {
        if (player == null) {
            DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
            if (drmScheme != null) {
                Map<String, String> keyRequestProperties;
                if (keyRequestPropertiesArray == null || keyRequestPropertiesArray.length < 2) {
                    keyRequestProperties = null;
                } else {
                    keyRequestProperties = new HashMap<>();
                    for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
                        keyRequestProperties.put(keyRequestPropertiesArray[i],
                                keyRequestPropertiesArray[i + 1]);
                    }
                }
                try {
                    drmSessionManager = buildDrmSessionManager(getDrmUuid(drmScheme), drmLicenseUrl,
                            keyRequestProperties);
                } catch (UnsupportedDrmException e) {
                    e.printStackTrace();
                    return;
                }
            }

            @SimpleExoPlayer.ExtensionRendererMode int extensionRendererMode = SimpleExoPlayer.EXTENSION_RENDERER_MODE_OFF;

            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveVideoTrackSelection.Factory(BANDWIDTH_METER);
            trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
            player = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector, new DefaultLoadControl(),
                    drmSessionManager, extensionRendererMode);

            player.addListener(listener);

            eventLogger = new EventLogger(trackSelector);
            player.addListener(eventLogger);
            player.setAudioDebugListener(eventLogger);
            player.setVideoDebugListener(eventLogger);
            player.setMetadataOutput(eventLogger);

            simpleExoPlayerView.setPlayer(player);
            player.setPlayWhenReady(false);
        }
    }

    public void preparePlayer(String stream) {
        MediaSource mediaSource = buildMediaSource(Uri.parse(stream), null);
        player.setPlayWhenReady(true);
        player.prepare(mediaSource);
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = Util.inferContentType(!TextUtils.isEmpty(overrideExtension) ? "." + overrideExtension
                : uri.getLastPathSegment());
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                        mainHandler, eventLogger);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private DrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManager(UUID uuid,
																		   String licenseUrl, Map<String, String> keyRequestProperties) throws UnsupportedDrmException {
        if (Util.SDK_INT < 18) {
            return null;
        }
        HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl,
                buildHttpDataSourceFactory(false), keyRequestProperties);
        return new DefaultDrmSessionManager<>(uuid,
                FrameworkMediaDrm.newInstance(uuid), drmCallback, null, mainHandler, eventLogger);
    }

    public void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
            trackSelector = null;
            eventLogger = null;
        }
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    /**
     * Returns a new HttpDataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new HttpDataSource factory.
     */
    private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        return buildHttpDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }


    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(mContext, bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
    }

    public boolean isPlayerReady() {
        return player != null;
    }

    public boolean isPlaying() {
        if (isPlayerReady()) {
            boolean play = player.getPlayWhenReady();
            int state = player.getPlaybackState();
            return play && (state == ExoPlayer.STATE_READY || state == ExoPlayer.STATE_BUFFERING);
        }
        return false;
    }

    public boolean isPrepared() {
        if (isPlayerReady()) {
            int state = player.getPlaybackState();
            return(state == ExoPlayer.STATE_READY || state == ExoPlayer.STATE_BUFFERING);
        }
        return false;
    }

    private TrackGroupArray getTrackGroupArray(int type) {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            for (int i = 0; i < mappedTrackInfo.length; i++) {
                TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);
                if (trackGroups.length != 0) {
                    if (player.getRendererType(i) == type) {
                        return trackGroups;
                    }
                }
            }
        }
        return null;
    }

    private TrackSelection getSelectedTrack(int type) {
        TrackSelectionArray trackSelectionArray = player.getCurrentTrackSelections();
        if (trackSelectionArray != null) {
            for (int i = 0; i < trackSelectionArray.length; i++) {
                TrackSelection trackGroup = trackSelectionArray.get(i);
                if (player.getRendererType(i) == type) {
                    return trackGroup;
                }
            }
        }
        return null;
    }

    private int getTrackId(int type) {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            for (int i = 0; i < mappedTrackInfo.length; i++) {
                TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);
                if (trackGroups.length != 0) {
                    if (player.getRendererType(i) == type) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    public void initSelection(int trackType, boolean enabled, String search) {
        if (trackSelector != null) {
            int trackId = getTrackId(trackType);
            Pair info = null;
            if (enabled) {
                info = getSelectionId(trackType, search);
            }
            if (info != null && (int)info.first >= 0 && (int)info.second >= 0) {
                MappingTrackSelector.SelectionOverride override = new MappingTrackSelector.SelectionOverride(FIXED_FACTORY, (int)info.first, (int)info.second);
                trackSelector.setRendererDisabled(trackId, false);
                trackSelector.setSelectionOverride(trackId, trackSelector.getCurrentMappedTrackInfo().getTrackGroups(trackId), override);
            } else {
                trackSelector.setRendererDisabled(trackId, true);
                trackSelector.clearSelectionOverrides(trackId);
            }
        }
    }

    private Pair getSelectionId(int trackId, String search) {
        TrackGroupArray trackGroupArray = trackSelector.getCurrentMappedTrackInfo().getTrackGroups(trackId);
        if (trackGroupArray != null) {
            for (int groupIndex = 0; groupIndex < trackGroupArray.length; groupIndex++) {
                TrackGroup trackGroup = trackGroupArray.get(groupIndex);
                for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
                    String found = buildLanguageString(trackGroup.getFormat(trackIndex));
                    if ((TextUtils.isEmpty(search) && TextUtils.isEmpty(found))
                            || search.equals(found)) {
                        return Pair.create(groupIndex, trackIndex);
                    }
                }
            }
        }
        return null;
    }

    private static String buildLanguageString(Format format) {
        return TextUtils.isEmpty(format.language) || "und".equals(format.language) ? ""
                : format.language;
    }

    public List<String> getAvailableLanguages() {
        ArrayList<String> audios = new ArrayList<String>();
        if (isPlayerReady()) {
            TrackGroupArray trackGroupArray = getTrackGroupArray(C.TRACK_TYPE_AUDIO);
            if (trackGroupArray != null) {
                for (int i = 0; i < trackGroupArray.length; i++) {
                    TrackGroup trackGroup = trackGroupArray.get(i);
                    for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
                        audios.add(buildLanguageString(trackGroup.getFormat(trackIndex)));
                    }
                }
            }
        }

        return audios;
    }

    public List<String> getAvailableSubtitles() {
        ArrayList<String> subs = new ArrayList<String>();
        if (isPlayerReady()) {
            TrackGroupArray trackGroupArray = getTrackGroupArray(C.TRACK_TYPE_TEXT);
            if (trackGroupArray != null) {
                for (int i = 0; i < trackGroupArray.length; i++) {
                    TrackGroup trackGroup = trackGroupArray.get(i);
                    for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
                        subs.add(buildLanguageString(trackGroup.getFormat(trackIndex)));
                    }
                }
            }
        }
        return subs;
    }

    public int getSelectedAudio() {
        int aud = -1;
        if (isPlayerReady()) {
            TrackSelection trackGroup = getSelectedTrack(C.TRACK_TYPE_AUDIO);
            if (trackGroup != null)
                aud = trackGroup.getSelectedIndex();
        }
        return aud;
    }

    public int getSelectedSubtitle() {
        int sub = -1;
        if (isPlayerReady()) {
            TrackSelection trackGroup = getSelectedTrack(C.TRACK_TYPE_TEXT);
            if (trackGroup != null)
                sub = trackGroup.getSelectedIndex();
        }
        return sub;
    }

    public long getCurrentPosition() {
        if (isPlayerReady()) {
            return player.getCurrentPosition();
        }
        return -1;
    }

    public long getDuration() {
        if (isPlayerReady()) {
            return player.getDuration();
        }
        return -1;
    }

    public long getBufferedPercentage() {
        if (isPlayerReady()) {
            return player.getBufferedPercentage();
        }
        return -1;
    }

    public void stop() {
        if (isPlayerReady())
            player.stop();
    }

    public void setPlayWhenReady(boolean playWhenReady) {
        if (isPlayerReady()) {
            player.setPlayWhenReady(playWhenReady);
        }
    }

    public void seekTo(long seekTo) {
        if (isPlayerReady()) {
            player.seekTo(seekTo);
        }
    }
}
