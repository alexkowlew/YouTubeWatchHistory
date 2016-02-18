package com.youtubewatchhistory;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

public class SimpleListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<History> histories;
    ImageLoader imageLoader;


    public SimpleListAdapter(Context context, List<History> histories) {
        this.context = context;
        this.histories = histories;
        imageLoader = AppController.getInstance().getImageLoader(context.getApplicationContext());
    }

    @Override
    public int getCount() {
        return histories.size();
    }

    @Override
    public Object getItem(int position) {
        return histories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_row, null);
        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader(context.getApplicationContext());
        NetworkImageView thumbNail = (NetworkImageView) convertView
                .findViewById(R.id.thumbnail);

        TextView title = (TextView) convertView.findViewById(R.id.videotitle);

        History history = histories.get(position);
        thumbNail.setImageUrl(history.getThumbnailUrl(), imageLoader);
        title.setText(history.getTitle());


        return convertView;
    }
}
