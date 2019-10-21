package com.example.satayam.mypetplayer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

public class RewardAd implements RewardedVideoAdListener {
    private RewardedVideoAd mRewardedVideoAd;
    private Context mcontext;
    private String mfilepath;
    private String mfilename;

    public RewardAd(Context context){
        mcontext = context;
        createRewardAd();
    }
    private void createRewardAd(){
        MobileAds.initialize(mcontext, "ca-app-pub-3940256099942544~5224354917");
        // Use an activity context to get the rewarded video instance.
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(mcontext);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        Toast.makeText(mcontext, "onRewardedVideoAdLeftApplication", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdClosed() {
        Toast.makeText(mcontext, "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show();
        // Preload the next video ad.
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
        Toast.makeText(mcontext, "onRewardedVideoAdFailedToLoad", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        Toast.makeText(mcontext, "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdOpened() {
        Toast.makeText(mcontext, "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewarded(RewardItem reward) {
        Toast.makeText(mcontext,
                String.format(" onRewarded! play the video now"),
                Toast.LENGTH_SHORT).show();
        //Now play the video.
        playVideo();
    }

    @Override
    public void onRewardedVideoStarted() {
        Toast.makeText(mcontext, "onRewardedVideoStarted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoCompleted() {
        Toast.makeText(mcontext, "onRewardedVideoCompleted", Toast.LENGTH_SHORT).show();
    }

    public void loadRewardedVideoAd(){
        if (!mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                    new AdRequest.Builder().build());
        }
    }

    private void showRewardedVideo() {
        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        }
    }

    private void playVideo(){
        Intent myIntent = new Intent(mcontext, PlayerActivity.class);
        myIntent.putExtra("filepath",mfilepath);
        myIntent.putExtra("filetitle",mfilename);
        Log.d("PlayerActiva","Calling Intent");
        mcontext.startActivity(myIntent);
        mfilename = null;
        mfilepath = null;
    }

    public void loadVideo(String filepath, String filename){
        mfilepath = filepath;
        mfilename = filename;
        if (mRewardedVideoAd.isLoaded() && false) {
            mRewardedVideoAd.show();
        }else{
            loadRewardedVideoAd();
            playVideo();
        }
    }

    public void resume(){
        mRewardedVideoAd.resume(mcontext);
    }

    public void pause(){
        mRewardedVideoAd.pause(mcontext);
    }

    public void destory(){
    }
}
