package com.bedrankarakoc.mobilesentinel;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class LogAdapter extends ArrayAdapter<LogPacket> implements View.OnClickListener {

    private ArrayList<LogPacket> dataSet;
    Context mContext;


    private static class ViewHolder {
        TextView txtName;
        ImageView info;
    }


    public LogAdapter(ArrayList<LogPacket> data, Context context) {
        super(context, R.layout.row_packet, data);
        this.dataSet = data;
        this.mContext = context;


    }


    @Override
    public void onClick(View v) {

        int position = (Integer) v.getTag();
        Object object = getItem(position);
        LogPacket logPacket = (LogPacket) object;
        switch (v.getId()) {
            case R.id.item_info:
                Intent intent = new Intent(mContext, PacketViewerActivity.class);
                intent.putExtra("content", logPacket.getPacketContent());
                intent.putExtra("packetName", logPacket.getPacketName());
                mContext.startActivity(intent);

                break;
        }

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LogPacket logPacket = getItem(position);

        ViewHolder viewHolder;


        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_packet, parent, false);
            viewHolder.info = (ImageView) convertView.findViewById(R.id.item_info);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.version_heading);


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        viewHolder.info.setOnClickListener(this);
        viewHolder.info.setTag(position);
        assert logPacket != null;
        viewHolder.txtName.setText(logPacket.getPacketName());

        return convertView;
    }
}