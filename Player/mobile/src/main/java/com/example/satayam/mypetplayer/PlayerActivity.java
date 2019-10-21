/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.example.satayam.mypetplayer;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.mapzen.speakerbox.Speakerbox;
import com.masterwok.opensubtitlesandroid.OpenSubtitlesUrlBuilder;
import com.masterwok.opensubtitlesandroid.models.OpenSubtitleItem;
import com.masterwok.opensubtitlesandroid.services.OpenSubtitlesService;
import com.obsez.android.lib.filechooser.ChooserDialog;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.OnItemClickListener;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.extra.Scale;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.ads.reward.RewardedVideoAd;

/**
 * A fullscreen activity to play audio or video streams.
 */
public class PlayerActivity extends AppCompatActivity {

  // bandwidth meter to measure and estimate bandwidth
  private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

  private SimpleExoPlayer player;
  private PlayerView playerView;

  private long playbackPosition;
  private int currentWindow;
  private boolean playWhenReady = true;

  private String filepath;
  private String filetitle;
  private String completeSubtitles;

  public static final int ANDROID_BUILD_GINGERBREAD = 9;

  public ArrayList<DialogList> dialogLists = new ArrayList<DialogList>();
  public ArrayList<DialogList> dialogLoadCaptionLists = new ArrayList<DialogList>();
  public ArrayList<DialogList> dialogSpeedLists = new ArrayList<DialogList>();
  public ArrayList<DialogList> dialogOnOffLists = new ArrayList<DialogList>();
  public ArrayList<SubtitleDialogList> subtitleLoadingList = new ArrayList<SubtitleDialogList>();
  public ArrayList<SubtitleDialogList> subtitleTitleList = new ArrayList<SubtitleDialogList>();
  public ArrayList<SubtitleDialogList> subtitleManualTitleList = new ArrayList<SubtitleDialogList>();
  public SubtitleDialogAdapter subtitleLoadingDialogAdapter = null;

  public DialogPlus subtitleLoadingDialog = null;
  public DialogPlus subtitleTitleDialog = null;

  private DBHelper dbHelper = DBHelper.getDBHelper();
  private DefaultDataSourceFactory dataSourceFactory;
  private MediaSource mediaSource;
  private MediaSource videoSource;
  private DefaultTrackSelector trackSelector;
  private DefaultTrackSelector.Parameters trackSelectorParameters;

  private  OpenSubtitlesService mOpenSubtitlesService;
  private OpenSubtitlesUrlBuilder mOpenSubtitlesUrlBuilder;
  private OpenSubtitleItem[] mSubtitleList = null;
  private String mManualSearchTitle = null;

  //add enum
  public int bottomSheetMenuClickedPosition = -1;
  public int captionLoadMenuClickedPosition = -1;
  public int playbackSpeedSelectedPosition = 3;//NORMAL
  public  int loopPlayPosition = 1; //OFF
  float playbackSpeedOptions[] = {0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f};
  String playbackSpeedOptionsStrings[] = {"0.25x", "0.5x", "0.75x", "Normal", "1.25x", "1.5x", "1.75x", "2x"};
  String onOffOptionsStrings[] = {"On", "Off"};

  public static HashSet<String> ignoreList = new HashSet<String>(Arrays.asList("to","of","in","for","on","with","at","by","from","up","about","into","over","after","the","and","a","that","i","it","not","he","as","you","this","but","his","they","her","she","or","an","will","my","one","all","would","there","their","be","have","do","is","i"));
  public static int updatedItemsInAdapter = 0;
  public static int numberOfRequests = 0;
  public static final String URL_DICT = "https://googledictionaryapi.eu-gb.mybluemix.net/?define=";

  private RecyclerView worddefRecyclerView;
  //private ShimmerRecyclerViewX worddefRecyclerView;
  private WorddefinitionAdapter worddefAdapter;
  private List<Worddefinition> worddefList;
  private List<WordMeanings> wordmeaningList;
  private RelativeLayout worddefMainContentRL;
  private Speakerbox mSpeakerBox;

  private void createLoadCaptionOptions(){
      dialogLoadCaptionLists.add(new DialogList(R.drawable.ic_cloud, "Cloud Automatically", true, false, ""));
      dialogLoadCaptionLists.add(new DialogList(R.drawable.ic_keyboard, "Cloud Manually", true, false, ""));
      dialogLoadCaptionLists.add(new DialogList(R.drawable.ic_smartphone, "Internal Storage", true, false, ""));
  }
  private void createBottomSheetOptions() {
      dialogLists.add(new DialogList(R.drawable.ic_subtitles_black_24dp, "Captions", true, false, ""));
      dialogLists.add(new DialogList(R.drawable.ic_slow_motion_video_black_24dp, "Playback Speed", true, true, playbackSpeedOptionsStrings[playbackSpeedSelectedPosition]));
      dialogLists.add(new DialogList(R.drawable.ic_loop_white_24dp, "Loop", true, true, "Off"));
  }
  private void createBottomSheetPlaybackSpeedOptions() {
     for(int i = 0; i < 8; i++){
         dialogSpeedLists.add(new DialogList(R.drawable.ic_done_black_24dp , playbackSpeedOptionsStrings[i], playbackSpeedSelectedPosition == i, false, ""));
     }
  }
  private void createBottomSheetOnOffOptions(){
      dialogOnOffLists.add(new DialogList(R.drawable.ic_done_black_24dp, "On", loopPlayPosition == 0, false, ""));
      dialogOnOffLists.add(new DialogList(R.drawable.ic_done_black_24dp, "Off", loopPlayPosition == 1, false, ""));
  }
  private void launchDictionaryService(String subtitle){
      prepareWorddefList(subtitle);
      showVocabList();
  }
  private WordMeanings parseJson(JSONArray response){
      WordMeanings wdef = new WordMeanings();
      String word = null, phonetic = null, result = null;
      try {
          Log.d("PlayerActiva", "parseJson:"+response.toString());
          JSONObject obj = response.getJSONObject(0);
          word = (String)obj.get("word");
          wdef.setWord(word);
          try {
              phonetic = (String) obj.get("phonetic");
              wdef.setPhonetic(phonetic);
          }catch (Exception e){}

          JSONObject meaningObj = (JSONObject)obj.get("meaning");
          Iterator iterator = meaningObj.keys();
          while(iterator.hasNext()){
              String partsofspeech = (String) iterator.next();
              JSONArray definition = (JSONArray) meaningObj.get(partsofspeech);
              for(int i = 0; i < definition.length(); i++) {
                  JSONObject def = definition.getJSONObject(i);
                  String wordDef = def.getString("definition");
                  wdef.setMeaning(partsofspeech, wordDef);
                  String wordExample = null;
                  try{
                      wordExample = def.getString("example");
                  }
                  catch (Exception e){}
                  Log.d("PlayerActiva", "word: "+word+"\t wordDef: "+wordDef+"\t"+"wordExample: "+wordExample);
              }
          }
      }
      catch (Exception e){}
      return wdef;
  }
  private void makeJsonArryReq(final String word) {
     JsonArrayRequest req = new JsonArrayRequest(URL_DICT+word,
             new Response.Listener<JSONArray>() {
                 @Override
                 public void onResponse(JSONArray response) {
                     Log.d("PlayerActiva", response.toString());
                     if(updatedItemsInAdapter <= numberOfRequests) {
                         Worddefinition wdef = worddefList.get(updatedItemsInAdapter++);
                         wdef.setDidload(true);
                         WordMeanings wordResult = parseJson(response);
                         wdef.setWord(wordResult.getWord());
                         wdef.setDefinition(wordResult.getFirstMeaning() != null ? wordResult.getFirstMeaning() : "Sorry Definition Not Found !!!");
                         if(wdef.getDefinition() != "Sorry Definition Not Found !!!") {
                             dbHelper.addWordDefinition(word, wdef.getWord(), wdef.getDefinition());
                             wordmeaningList.add(wordResult);
                             worddefAdapter.notifyDataSetChanged();
                         }
                     }
                 }
             }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
             Log.d("PlayerActiva", "Error: " + error.getMessage() +" word:"+word +" numberOfRequests"+numberOfRequests);
             if(numberOfRequests == 1){
                 Worddefinition wdef = worddefList.get(updatedItemsInAdapter++);
                 wdef.setDidload(true);
                 wdef.setWord("Sorry");
                 wdef.setDefinition("No vocabulary found for current subtitle cue !!!");
                 wdef.setIsSorryCard(true);
                 worddefAdapter.notifyDataSetChanged();
             }else {
                 worddefList.remove(numberOfRequests - 1);
                 numberOfRequests--;
             }
             worddefAdapter.notifyDataSetChanged();
         }
    });

     // Adding request to request queue
      AppController.getInstance().addToRequestQueue(req,"PlayerActiva");
 }
  private void makeJsonArryReqBackground(final String word) {
        JsonArrayRequest req = new JsonArrayRequest(URL_DICT+word,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("PlayerActiva", "MUVENDICT response: "+response.toString());
                        Worddefinition wdef = new Worddefinition();
                        WordMeanings wordResult = parseJson(response);
                        wdef.setWord(wordResult.getWord());
                        wdef.setDefinition(wordResult.getFirstMeaning() != null ? wordResult.getFirstMeaning() : "Sorry Definition Not Found !!!");
                        if(wdef.getDefinition() != "Sorry Definition Not Found !!!") {
                            dbHelper.addWordDefinition(word, wdef.getWord(), wdef.getDefinition());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("PlayerActiva", " MUVENDICT Error: " + error.getMessage() +" word:"+word);
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req,"PlayerActiva");
    }
  public static Set<String> prepareWordList(String subtitle){
        subtitle = subtitle.toLowerCase();
        subtitle = subtitle.replaceAll("[\\t\\n\\r]+"," ");
        subtitle = subtitle.replaceAll(",", " ");
        subtitle = subtitle.replaceAll("\\?", " ");
        subtitle = subtitle.replaceAll("\\.", " ");
        subtitle = subtitle.replaceAll("\\:", " ");
        subtitle = subtitle.replaceAll("\\(", " ");
        subtitle = subtitle.replaceAll("\\)", " ");
        subtitle = subtitle.replaceAll("\\[", " ");
        subtitle = subtitle.replaceAll("\\]", " ");
        subtitle = subtitle.replaceAll("\\{", " ");
        subtitle = subtitle.replaceAll("\\}", " ");
        subtitle = subtitle.replaceAll("\\-", " ");
        String[] subtitles = subtitle.trim().split(" ");
        Set<String> modifiedStr = new HashSet<String>();
        for(String str : subtitles)
                modifiedStr.add(str);
        return modifiedStr;
    }
  private void prepareWorddefList(String subtitle){
      worddefList.clear();
      wordmeaningList.clear();
      Set<String> splitStr = prepareWordList(subtitle);
      Worddefinition a = null;
      for(int i = 0; i < splitStr.size(); i++) {
          a = new Worddefinition("", "", true, false);
          worddefList.add(a);
      }

      updatedItemsInAdapter = 0;
      numberOfRequests = splitStr.size();

      worddefAdapter.notifyDataSetChanged();
      Iterator<String> i = splitStr.iterator();
      while (i.hasNext()) {
          String word = i.next();
          String definition = dbHelper.getDefinition(word);
          if(definition == null)
              makeJsonArryReq(word);
          else{
              if(updatedItemsInAdapter <= numberOfRequests) {
                  Worddefinition wdef = worddefList.get(updatedItemsInAdapter++);
                  wdef.setDidload(true);
                  WordMeanings wordResult = null;
                  wdef.setWord(word);
                  wdef.setDefinition(definition);
                  wordmeaningList.add(wordResult);
                  worddefAdapter.notifyDataSetChanged();
              }
          }
      }
  }
  /**
     * RecyclerView item decoration - give equal margin around grid item
     */
  public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }
  /**
     * Converting dp to pixel
     */
  private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
  private boolean isPlaying() {
      return player != null
              && player.getPlaybackState() != Player.STATE_ENDED
              && player.getPlaybackState() != Player.STATE_IDLE
              && player.getPlayWhenReady();
  }
  private void createWorddefLayout(){
      worddefRecyclerView = (RecyclerView) findViewById(R.id.worddef_recycler_view);
      worddefList = new ArrayList<>();
      wordmeaningList = new ArrayList<>();
      worddefAdapter = new WorddefinitionAdapter(this, worddefList, mSpeakerBox);

      worddefMainContentRL = (RelativeLayout) findViewById(R.id.worddef_main_content);
      RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
      worddefRecyclerView.setLayoutManager(mLayoutManager);
      worddefRecyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(10), true));
      worddefRecyclerView.setItemAnimator(new DefaultItemAnimator());
      worddefRecyclerView.setAdapter(worddefAdapter);
  }
  private void createOpensubtitlesService(){
      mOpenSubtitlesService = new OpenSubtitlesService();
      mOpenSubtitlesUrlBuilder = new OpenSubtitlesUrlBuilder();
  }
  private void createSpeakerBox(){
      mSpeakerBox = new Speakerbox(getApplication());
  }
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_player);

    Log.d("PlayerActiva","onCreate");
    playerView = findViewById(R.id.video_view);
    createSpeakerBox();
    createBottomSheetOptions();
    createBottomSheetPlaybackSpeedOptions();
    createBottomSheetOnOffOptions();
    createLoadCaptionOptions();
    createWorddefLayout();
    createOpensubtitlesService();
  }
  @Override
  public void onStart() {
    super.onStart();
    Intent myIntent = getIntent(); // gets the previously created intent
    filepath = myIntent.getStringExtra("filepath");
    filetitle = myIntent.getStringExtra("filetitle");
    if (Util.SDK_INT > 23) {
      Log.d("PlayerActiva","onStart");
      initializePlayer(filepath, filetitle);
    }
  }
  @Override
  public void onResume() {
    super.onResume();
    hideSystemUi();
    if ((Util.SDK_INT <= 23 || player == null)) {
      Log.d("PlayerActiva","onResume");
      initializePlayer(filepath, filetitle);
    }
  }
  @Override
  public void onPause() {
    super.onPause();
    if (Util.SDK_INT <= 23) {
      Log.d("PlayerActiva","onPause");
      releasePlayer();
    }
  }
  @Override
  public void onStop() {
    super.onStop();
    if (Util.SDK_INT > 23) {
      Log.d("PlayerActiva","onStop");
      releasePlayer();
    }
  }
  private void initializePlayer(String pfilepath, String pfiletitle) {
    if (player == null) {
      TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory();
      DefaultRenderersFactory renderersFactory =
                new DefaultRenderersFactory(this, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);

      trackSelector = new DefaultTrackSelector(trackSelectionFactory);
      trackSelectorParameters = new DefaultTrackSelector.ParametersBuilder().build();
      trackSelector.setParameters(trackSelectorParameters);
      DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
      player =
                ExoPlayerFactory.newSimpleInstance(
                        /* context= */ this, renderersFactory, trackSelector, drmSessionManager);
      playerView.setPlayer(player);
      player.setPlayWhenReady(playWhenReady);
      player.seekTo(currentWindow, playbackPosition);

      TextView tview = (TextView)findViewById(R.id.videotitle);
      tview.setText(pfiletitle);
    }
    mediaSource = buildMediaSource(Uri.parse(pfilepath));
    player.prepare(mediaSource, true, false);
    //avoid screen going off
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    //closed captions unline animation
    final View transitionView = (View) findViewById(R.id.videoclosedcaptionenable);
    findViewById(R.id.videoclosedcaption).setOnClickListener(new VisibleToggleClickListener() {
         @Override
         protected void changeVisibility(boolean visible) {
             TransitionManager.beginDelayedTransition((ViewGroup)transitionView.getParent(), new Scale());
             transitionView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
             playerView.setSubtitleVisibility(visible);
         }
    });

    player.addListener(new Player.EventListener(){
        @Override
        public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {}

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {}

        @Override
        public void onLoadingChanged(boolean isLoading) {}

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (!isPlaying()) {
                // paused, get the sutitle text now;
                String subtitle = player.getSubtitle();
                Log.d("PlayerActiva", "onPlayerStateChanged: player paused; subtitle:"+subtitle);
                if(subtitle.length() != 0) {
                    launchDictionaryService(subtitle);
                }
            } else {
                hideVocabList();
            }
        }
        @Override
        public void onRepeatModeChanged(int repeatMode) {}
        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {}
        @Override
        public void onPlayerError(ExoPlaybackException error) {}
        @Override
        public void onPositionDiscontinuity(int reason) {}
        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {}
        @Override
        public void onSeekProcessed() {}
    });
  }
  public void showVocabList(){
      // Slide animation move in;
      if(worddefMainContentRL.getVisibility() == View.INVISIBLE) {
          findViewById(R.id.worddef_content_main).setVisibility(View.VISIBLE);
          Animation leftSwipe = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slideleft);
          worddefMainContentRL.startAnimation(leftSwipe);
          worddefMainContentRL.setVisibility(View.VISIBLE);
      }
  }
  public void hideVocabList(){
      if(worddefMainContentRL.getVisibility() == View.VISIBLE) {
          // Slide animation move out;
          Animation rightSwipe = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slideright);
          worddefMainContentRL.startAnimation(rightSwipe);
          worddefMainContentRL.setVisibility(View.INVISIBLE);
      }
  }
  public void closeVocabList(View v){
      Log.d("PlayerActiva", "closeVocabList:");
      hideVocabList();
  }
  public void onClickVocabLinearLayout(View v){
    Log.d("PlayerActiva", "onClickVocabRelativeLayout:");
  }
  private void releasePlayer() {
    if (player != null) {
      playbackPosition = player.getCurrentPosition();
      currentWindow = player.getCurrentWindowIndex();
      playWhenReady = player.getPlayWhenReady();
      player.release();
      player = null;
    }
  }
  private MediaSource buildMediaSource(Uri uri) {
    dataSourceFactory = new DefaultDataSourceFactory(this, "en");
    videoSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
    return videoSource;
  }
  public void addSubtitlesToMedia(Uri uri){
      Log.d("PlayerActiva", uri.toString());
      Format subtitleFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, C.SELECTION_FLAG_FORCED, null);
      //MediaSource subtitleSource = new SingleSampleMediaSource(uri, dataSourceFactory, subtitleFormat, C.TIME_UNSET);
      long duration = player.getDuration();
      Log.d("PlayerActiva", "addSubtitlesToMedia: "+duration);
      MediaSource subtitleSource = new SingleSampleMediaSource.Factory(dataSourceFactory).createMediaSource(uri, subtitleFormat, duration);
      mediaSource = new MergingMediaSource(videoSource, subtitleSource);
      player.prepare(mediaSource, false, false);
      //change cc button with red underlinew
      findViewById(R.id.videoclosedcaptionenable).setVisibility(View.VISIBLE);

      //Have to check if any better way is there.
      new Timer().schedule(new TimerTask() {
          @Override
          public void run() {
              // this code will be executed after 2 seconds
              String completeSubtitles = player.getCompleteSubtitles();
              new SubtitleLoadAsyncTask().execute(completeSubtitles);
              Log.d("PlayerActiva", "completeSubtitles: " + completeSubtitles);
          }
      }, 2000);
  }
  
  @SuppressLint("InlinedApi")
  private void hideSystemUi() {
    playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
        | View.SYSTEM_UI_FLAG_FULLSCREEN
        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
  }
  public void onClickShareVideo(View view)
  {
      Log.d("PlayerActiva","onClickShareVideo");
      hideVocabList();
      Intent sharingIntent = new Intent(Intent.ACTION_SEND);
      Uri screenshotUri = Uri.parse(filepath);

      sharingIntent.setType("video/*");
      sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
      startActivity(Intent.createChooser(sharingIntent, "Complete action using"));
  }
  private void prepareSubtitleData() {
        for(int i = 0; i < mSubtitleList.length; i++){
            String subFileName = mSubtitleList[i].getSubFileName();
            subtitleLoadingList.add(new SubtitleDialogList(subFileName));
        }
    }
  private class DownloadSubtitlesForVideo extends AsyncTask<OpenSubtitleItem, Integer, Long> {
        private File downloadLocation = null, downloadedFile = null;
        protected Long doInBackground(OpenSubtitleItem... subtitleItem) {
            try {
                downloadLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                downloadedFile = new File(downloadLocation, subtitleItem[0].getSubFileName());
                mOpenSubtitlesService.downloadSubtitle(
                        getApplicationContext()
                        , subtitleItem[0]
                        , Uri.fromFile(downloadedFile));
            }
            catch(Exception e){
                Log.e("PlayerActiva", "Failed to download subtitles");
            }
            long listLen = 0;
            return listLen;
        }

        protected void onProgressUpdate(Integer... progress) {}

        protected void onPostExecute(Long result) {
            addSubtitlesToMedia(Uri.parse("file:///"+downloadedFile.getAbsolutePath()));
            Toast.makeText(PlayerActivity.this, "Syncing Downloaded Caption", Toast.LENGTH_SHORT).show();
        }
    }
  private class GetSubtitlesForVideo extends AsyncTask<String, Integer, Long> {
        protected Long doInBackground(String... string) {
            try {
                if(string[0].length() != 0)
                    mSubtitleList =  mOpenSubtitlesService.search(OpenSubtitlesService.TemporaryUserAgent, mOpenSubtitlesUrlBuilder
                            .query(string[0])
                            .subLanguageId(string[1])
                            .build());
                else
                    mSubtitleList = null;
            }
            catch(Exception e){}
            long listLen = mSubtitleList != null ? mSubtitleList.length : 0;
            return listLen;
        }

        protected void onProgressUpdate(Integer... progress) {}

        protected void onPostExecute(Long result) {
            TextView subtitlesHeader = (TextView) findViewById(R.id.subtitles_loading_header);
            final EditText searchManually = (EditText) findViewById(R.id.search_manually);
            View view = findViewById(R.id.dots);
            if(view != null)
                view.setVisibility(View.GONE);
            if(mSubtitleList != null && mSubtitleList.length != 0) {
                prepareSubtitleData();
                subtitlesHeader.setText("Select Caption");
                if (subtitleLoadingDialogAdapter != null)
                    subtitleLoadingDialogAdapter.notifyDataSetChanged();
            } else {
                subtitlesHeader.setText("No Captions Found");
                searchManually.setVisibility(View.VISIBLE);
                searchManually.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                            mManualSearchTitle = searchManually.getText().toString();
                            subtitleLoadingDialog.dismiss();
                            hideSystemUi();
                            return true;
                        }
                        return false;
                    }
                });
            }
        }
    }
  public void loadCaptionsFromWeb(String titleMedia){
      subtitleLoadingList.clear();
      subtitleLoadingDialogAdapter = new SubtitleDialogAdapter(this, subtitleLoadingList);
      subtitleLoadingDialog = DialogPlus.newDialog(PlayerActivity.this)
              .setHeader(R.layout.subtitles_header)
              .setAdapter(subtitleLoadingDialogAdapter)
              .setOnItemClickListener(new OnItemClickListener() {
                  @Override
                  public void onItemClick(@NonNull DialogPlus dialog, @NonNull Object item, @NonNull View view, int position) {
                      OpenSubtitleItem downloadItem =  mSubtitleList[position];
                      Toast.makeText(PlayerActivity.this, "Downloading Caption", Toast.LENGTH_SHORT).show();
                      new DownloadSubtitlesForVideo().execute(downloadItem);
                      dialog.dismiss();
                  }
              })
              .setOnDismissListener(new OnDismissListener() {
                  @Override
                  public void onDismiss(@NonNull DialogPlus dialog) {
                      if(mManualSearchTitle != null) {
                          String searchTitle = mManualSearchTitle;
                          mManualSearchTitle = null;
                          loadCaptionsFromWeb(searchTitle);
                      }
                  }
              })
              .create();
      subtitleLoadingDialog.show();
      // have get subtitle language from settings. currently hardcoded to en;
      new GetSubtitlesForVideo().execute(titleMedia, "eng");
  }
  public void searchCloudManually(){
     subtitleLoadingDialogAdapter = new SubtitleDialogAdapter(this, subtitleManualTitleList);
     DialogPlus dialog = DialogPlus.newDialog(this)
             .setHeader(R.layout.subtitles_manual_search)
             .setAdapter(subtitleLoadingDialogAdapter)
             .setOnDismissListener(new OnDismissListener() {
                 @Override
                 public void onDismiss(@NonNull DialogPlus dialog) {
                     hideSystemUi();
                     if(mManualSearchTitle != null) {
                         String searchTitle = mManualSearchTitle;
                         mManualSearchTitle = null;
                         loadCaptionsFromWeb(searchTitle);
                     }
                 }
             })
             .create();
     dialog.show();
     final EditText searchManually = (EditText) findViewById(R.id.search_title_manually);
     searchManually.setOnEditorActionListener(new TextView.OnEditorActionListener() {
         @Override
         public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
             if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                 mManualSearchTitle = searchManually.getText().toString();
                 dialog.dismiss();
                 return true;
             }
             return false;
         }
     });
 }
  public void onClickOptions(View view){
    Log.d("PlayerActiva","onClickOptions");
    hideVocabList();
    final DialogAdapter dialogAdapter = new DialogAdapter(this, dialogLists);
    DialogPlus dialog = DialogPlus.newDialog(this)
            .setAdapter(dialogAdapter)
            .setOnItemClickListener(new OnItemClickListener(){
                @Override
                public void onItemClick(DialogPlus dialog, Object item, View view, int position){
                    Log.d("PlayerActiva","onItemClick");
                    if(position == 0){
                        bottomSheetMenuClickedPosition = 0;
                        dialog.dismiss();
                    }else if(position == 1){
                        bottomSheetMenuClickedPosition = 1;
                        dialog.dismiss();
                    }else if(position == 2){
                        bottomSheetMenuClickedPosition = 2;
                        dialog.dismiss();
                    }
                }
            })
            .setOnDismissListener(new OnDismissListener(){
                @Override
                public void onDismiss(DialogPlus dialog) {
                    Log.d("PlayerActiva","onDismiss "+filepath);
                    if(bottomSheetMenuClickedPosition == 0) {
                        final DialogAdapter loadCaptionsAdapter = new DialogAdapter(PlayerActivity.this, dialogLoadCaptionLists);
                        DialogPlus captionLoadDialog = DialogPlus.newDialog(PlayerActivity.this)
                                .setHeader(R.layout.captions_header)
                                .setAdapter(loadCaptionsAdapter)
                                .setOnItemClickListener(new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(@NonNull DialogPlus dialog, @NonNull Object item, @NonNull View view, int position) {
                                        if(position == 0){
                                            captionLoadMenuClickedPosition = 0;
                                            dialog.dismiss();
                                        } else if(position == 1){
                                            captionLoadMenuClickedPosition = 1;
                                            dialog.dismiss();
                                        } else if(position == 2){
                                            captionLoadMenuClickedPosition = 2;
                                            dialog.dismiss();
                                        }
                                    }
                                })
                                .setOnDismissListener(new OnDismissListener() {
                                    @Override
                                    public void onDismiss(@NonNull DialogPlus dialog) {
                                        if(captionLoadMenuClickedPosition == 0){
                                            // load captions from the web;
                                            loadCaptionsFromWeb(filetitle);
                                        }
                                        else if(captionLoadMenuClickedPosition == 1) {
                                            // search cloud manually;
                                            searchCloudManually();
                                        }
                                        else if(captionLoadMenuClickedPosition == 2){
                                            // launch filechooser.
                                            new ChooserDialog().with(PlayerActivity.this)
                                                    .withFilter(false, false, "srt", "ssa", "xml","dfxp","ttml","vtt")
                                                    .withStartFile(new File(filepath).getParent())
                                                    .withResources(R.string.select_caption, R.string.title_choose, R.string.dialog_cancel)
                                                    .withFileIconsRes(false, R.drawable.ic_insert_drive_file, R.drawable.ic_folder_white)
                                                    .withChosenListener(new ChooserDialog.Result() {
                                                        @Override
                                                        public void onChoosePath(String path, File pathFile) {
                                                            addSubtitlesToMedia(Uri.parse("file://"+path));
                                                            Toast.makeText(PlayerActivity.this, "Syncing Caption", Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .withOnDismissListener(new DialogInterface.OnDismissListener(){
                                                        @Override
                                                        public void onDismiss(DialogInterface dialog) {
                                                            hideSystemUi();
                                                        }
                                                    })
                                                    .build()
                                                    .show();
                                        }
                                        captionLoadMenuClickedPosition = -1;
                                    }
                                })
                                .create();
                        captionLoadDialog.show();
                    }
                    else if(bottomSheetMenuClickedPosition == 1) {
                        // launch playback speed.
                        final DialogAdapter dialogSpeedAdapter = new DialogAdapter(PlayerActivity.this, dialogSpeedLists);
                        DialogPlus speeddialog = DialogPlus.newDialog(PlayerActivity.this)
                                .setAdapter(dialogSpeedAdapter)
                                .setOnItemClickListener(new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                                        Log.d("PlayerActiva", dialogSpeedLists.get(position).getOptionName());
                                        //Toast.makeText(getApplicationContext(), dialogSpeedLists.get(position).getOptionName(), Toast.LENGTH_LONG).show();
                                        //set playback speed here;
                                        float newPlaybackSpeed = 1.0f;
                                        switch (position) {
                                            case 0 : newPlaybackSpeed = playbackSpeedOptions[0];break;
                                            case 1 : newPlaybackSpeed = playbackSpeedOptions[1];break;
                                            case 2 : newPlaybackSpeed = playbackSpeedOptions[2];break;
                                            case 3 : newPlaybackSpeed = playbackSpeedOptions[3];break;
                                            case 4 : newPlaybackSpeed = playbackSpeedOptions[4];break;
                                            case 5 : newPlaybackSpeed = playbackSpeedOptions[5];break;
                                            case 6 : newPlaybackSpeed = playbackSpeedOptions[6];break;
                                            case 7 : newPlaybackSpeed = playbackSpeedOptions[7];break;
                                        }
                                        dialogLists.get(1).setmOptionPlaybackSpeed(playbackSpeedOptionsStrings[position]);
                                        dialogSpeedLists.get(playbackSpeedSelectedPosition).setmOptionSlected(false);
                                        playbackSpeedSelectedPosition = position;
                                        dialogSpeedLists.get(playbackSpeedSelectedPosition).setmOptionSlected(true);

                                        player.setPlaybackParameters(new PlaybackParameters(newPlaybackSpeed));
                                        dialog.dismiss();
                                    }
                                })
                                .create();
                        speeddialog.show();
                    }else if(bottomSheetMenuClickedPosition == 2){
                        final DialogAdapter dialogOnOffAdapter = new DialogAdapter(PlayerActivity.this, dialogOnOffLists);
                        DialogPlus onOffdialog = DialogPlus.newDialog(PlayerActivity.this)
                                .setAdapter(dialogOnOffAdapter)
                                .setOnItemClickListener(new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                                        Log.d("PlayerActiva", dialogOnOffLists.get(position).getOptionName());
                                        //Toast.makeText(getApplicationContext(), dialogSpeedLists.get(position).getOptionName(), Toast.LENGTH_LONG).show();
                                        //set playback speed here;
                                        boolean onOffOption = false;
                                        switch (position) {
                                            case 0 : onOffOption = true;break;
                                            case 1 : onOffOption = false;break;
                                        }
                                        dialogLists.get(2).setmOptionPlaybackSpeed(onOffOptionsStrings[position]);
                                        dialogOnOffLists.get(loopPlayPosition).setmOptionSlected(false);
                                        loopPlayPosition = position;
                                        dialogOnOffLists.get(loopPlayPosition).setmOptionSlected(true);
                                        if(onOffOption)
                                            player.setRepeatMode(Player.REPEAT_MODE_ONE);
                                        else
                                            player.setRepeatMode(Player.REPEAT_MODE_OFF);
                                        dialog.dismiss();
                                    }
                                })
                                .create();
                        onOffdialog.show();
                    }
                    bottomSheetMenuClickedPosition = -1;
                }
            })
            .create();
    dialog.show();
  }
  public void onClickBack(View view){
      Log.d("PlayerActiva","onClickBack");
      super.onBackPressed();
      finish();
  }
  public class SubtitleLoadAsyncTask extends AsyncTask<String, Void, Integer> {
        String completeSubstring;
        @Override
        protected void onPreExecute() {}

        @Override
        protected Integer doInBackground(String...params) {
            Integer result = 0;
            try {
                completeSubstring = (String) params[0];
                Set<String> splitStr = prepareWordList(completeSubstring);
                Iterator<String> i = splitStr.iterator();
                while (i.hasNext()) {
                    String word = i.next();
                    if(dbHelper.getDefinition(word) == null) {
                        Log.d("PlayerActiva", "MUVENDICT preload word: " + word);
                        makeJsonArryReqBackground(word);
                    }
                }
                result =1;
            }catch (Exception e) {
                e.printStackTrace();
                result =0;
            }
            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == 1) {
                if (result == 1) {
                } else {
                    //Log.e(TAG, "Failed to fetch data!");
                }
            }
        }
    }
}