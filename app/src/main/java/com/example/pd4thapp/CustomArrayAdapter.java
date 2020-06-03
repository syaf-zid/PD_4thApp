package com.example.pd4thapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomArrayAdapter extends ArrayAdapter<ReadXMLFile> {
    private Context context;
    private ArrayList<ReadXMLFile> rssInfoList;
    private int resource;
    TextView tvTitle, tvDesc, tvLink, tvPubDate;
    String title, desc, link, pubDate;

    public CustomArrayAdapter(Context context, int resource, ArrayList<ReadXMLFile> objects) {
        super(context, resource, objects);

        this.context = context;
        this.rssInfoList = objects;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(resource, null);

        ReadXMLFile data = rssInfoList.get(position);

        tvTitle = rowView.findViewById(R.id.textViewTitle);
        tvDesc = rowView.findViewById(R.id.textViewDesc);
        tvLink = rowView.findViewById(R.id.textViewLink);
        tvPubDate = rowView.findViewById(R.id.textViewPubDate);

        title = data.getTitle();
        desc = data.getDescription();
        link = data.getLink();
        pubDate = data.getPubDate();

        tvTitle.setText(title);
        tvDesc.setText(desc);
        tvLink.setText(link);
        tvPubDate.setText(pubDate);

        return rowView;
    }
}
