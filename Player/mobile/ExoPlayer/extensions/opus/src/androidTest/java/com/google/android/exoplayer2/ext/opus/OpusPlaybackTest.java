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
package com.google.android.exoplayer2.ext.opus;

import static androidx.test.InstrumentationRegistry.getContext;
import static org.junit.Assert.fail;

import android.content.Context;
import android.net.Uri;
import android.os.Looper;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.extractor.mkv.MatroskaExtractor;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Playback tests using {@link LibopusAudioRenderer}. */
@RunWith(AndroidJUnit4.class)
public class OpusPlaybackTest {

  private static final String BEAR_OPUS_URI = "asset:///bear-opus.webm";

  @Before
  public void setUp() {
    if (!OpusLibrary.isAvailable()) {
      fail("Opus library not available.");
    }
  }

  @Test
  public void testBasicPlayback() throws Exception {
    playUri(BEAR_OPUS_URI);
  }

  private void playUri(String uri) throws Exception {
    TestPlaybackRunnable testPlaybackRunnable =
        new TestPlaybackRunnable(Uri.parse(uri), getContext());
    Thread thread = new Thread(testPlaybackRunnable);
    thread.start();
    thread.join();
    if (testPlaybackRunnable.playbackException != null) {
      throw testPlaybackRunnable.playbackException;
    }
  }

  private static class TestPlaybackRunnable implements Player.EventListener, Runnable {

    private final Context context;
    private final Uri uri;

    private ExoPlayer player;
    private ExoPlaybackException playbackException;

    public TestPlaybackRunnable(Uri uri, Context context) {
      this.uri = uri;
      this.context = context;
    }

    @Override
    public void run() {
      Looper.prepare();
      LibopusAudioRenderer audioRenderer = new LibopusAudioRenderer();
      DefaultTrackSelector trackSelector = new DefaultTrackSelector();
      player = ExoPlayerFactory.newInstance(new Renderer[] {audioRenderer}, trackSelector);
      player.addListener(this);
      MediaSource mediaSource =
          new ExtractorMediaSource.Factory(
                  new DefaultDataSourceFactory(context, "ExoPlayerExtOpusTest"))
              .setExtractorsFactory(MatroskaExtractor.FACTORY)
              .createMediaSource(uri);
      player.prepare(mediaSource);
      player.setPlayWhenReady(true);
      Looper.loop();
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
      playbackException = error;
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      if (playbackState == Player.STATE_ENDED
          || (playbackState == Player.STATE_IDLE && playbackException != null)) {
        player.release();
        Looper.myLooper().quit();
      }
    }

  }

}
