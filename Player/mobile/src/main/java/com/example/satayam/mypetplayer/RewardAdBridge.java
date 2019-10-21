package com.example.satayam.mypetplayer;

import android.content.Context;

public class RewardAdBridge {
    private Context mcontext = null;
    RewardAd rewardAd = null;
    private RewardAdBridge(){}
    public static RewardAdBridge rewardAdBridge = null;
    public static RewardAdBridge getRewardAdBridge(){
        if(rewardAdBridge == null) {
            rewardAdBridge = new RewardAdBridge();
        }
        return rewardAdBridge;
    }
    public void loadRewardAd(Context context){
        if(rewardAd == null)
            rewardAd = new RewardAd(context);
    }

    public void loadVideo(String filepath, String filename){
        rewardAd.loadVideo(filepath, filename);
    }

    public void resume(){
        rewardAd.resume();
    }

    public void pause(){
        rewardAd.pause();
    }

    public void destory(){
        rewardAd.destory();
    }
}
