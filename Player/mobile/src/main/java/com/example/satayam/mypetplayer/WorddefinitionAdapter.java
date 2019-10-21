package com.example.satayam.mypetplayer;

import android.content.Context;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapzen.speakerbox.Speakerbox;

import java.util.List;

import io.supercharge.shimmerlayout.ShimmerLayout;

/**
 * Created by Ravi Tamada on 18/05/16.
 */
public class WorddefinitionAdapter extends RecyclerView.Adapter<WorddefinitionAdapter.MyViewHolder> {

    private Context mContext;
    private List<Worddefinition> albumList;
    private Speakerbox speakerBox;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView dictwordtitle, dictworddef;
        private ImageView dictwordaudio, dicimportant;
        private ShimmerLayout dictlayoutshimmer;
        private RelativeLayout dictcardview;


        public MyViewHolder(View view) {
            super(view);
            dictwordtitle = (TextView) view.findViewById(R.id.dictwordtitle);
            dictworddef = (TextView) view.findViewById(R.id.dictworddef);
            dictwordaudio = (ImageView) view.findViewById(R.id.dictwordaudio);
            dicimportant = (ImageView) view.findViewById(R.id.dicimportant);
            dictcardview = (RelativeLayout) view.findViewById(R.id.card_view_rl);
            dictlayoutshimmer = (ShimmerLayout) view.findViewById(R.id.shimmer_layout);
        }
    }


    public WorddefinitionAdapter(Context mContext, List<Worddefinition> albumList, Speakerbox speakerBox) {
        this.mContext = mContext;
        this.albumList = albumList;
        this.speakerBox = speakerBox;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.word_definition_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Worddefinition album = albumList.get(position);
        holder.dictwordtitle.setText(album.getWord());
        holder.dictworddef.setText(album.getDefinition());
        if(album.getImportant())
            holder.dicimportant.setVisibility(View.VISIBLE);
        else
            holder.dicimportant.setVisibility(View.INVISIBLE);

        if(album.getDidload() == false) {
            holder.dictlayoutshimmer.setVisibility(View.VISIBLE);
            holder.dictlayoutshimmer.startShimmerAnimation();
            holder.dictcardview.setVisibility(View.GONE);
        }
        else {
            holder.dictlayoutshimmer.stopShimmerAnimation();
            holder.dictlayoutshimmer.setVisibility(View.GONE);
            holder.dictcardview.setVisibility(View.VISIBLE);
        }

        if(album.getIsSorryCard() == true){
            holder.dictwordaudio.setVisibility(View.GONE);
            holder.dicimportant.setVisibility(View.GONE);
        }
        // loading album cover using Glide library
        // Glide.with(mContext).load(album.getThumbnail()).into(holder.thumbnail);

        holder.dictwordaudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //play audio file;
                speakerBox.play(holder.dictwordtitle.getText().toString());
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }
}
