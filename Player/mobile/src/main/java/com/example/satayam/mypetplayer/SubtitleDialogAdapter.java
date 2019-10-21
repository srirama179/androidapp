package com.example.satayam.mypetplayer;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SubtitleDialogAdapter extends ArrayAdapter<SubtitleDialogList> {

    private static final String LOG_TAG = SubtitleDialogAdapter.class.getSimpleName();


    public SubtitleDialogAdapter(Activity context, ArrayList<SubtitleDialogList> subtitleItems) {
        super(context, 0, subtitleItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.subtitles_row, parent, false);
        }

        // Get the {@link VocabList} object located at this position in the list
        SubtitleDialogList subtitle = getItem(position);
        TextView subtitleTitle = (TextView) listItemView.findViewById(R.id.subtitle_title);
        subtitleTitle.setText(subtitle.getTitle());

        // Return the whole list item layout (containing 2 TextViews and an ImageView)
        // so that it can be shown in the ListView
        notifyDataSetChanged();
        return listItemView;
    }
}