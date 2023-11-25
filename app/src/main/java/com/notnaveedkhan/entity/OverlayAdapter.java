package com.notnaveedkhan.entity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.notnaveedkhan.R;

import java.util.List;
import java.util.Locale;

public class OverlayAdapter extends BaseAdapter {
    private final Context context;
    private final List<Overlay> overlays;

    public OverlayAdapter(Context context, List<Overlay> overlays) {
        this.overlays = overlays;
        this.context = context;
    }

    @Override
    public int getCount() {
        return overlays.size();
    }

    @Override
    public Object getItem(int i) {
        return overlays.get(i);
    }

    @Override
    public long getItemId(int i) {
        return overlays.get(i).id;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.overlay_list_element_layout, viewGroup, false);
        }
        Overlay member = (Overlay) getItem(i);
        TextView idTextView = view.findViewById(R.id.overlay_id);
        TextView coordinatesTextView = view.findViewById(R.id.coordinates);
        TextView sizeTextView = view.findViewById(R.id.size);
        idTextView.setText(String.valueOf(member.id));
        coordinatesTextView.setText(String.format(Locale.ENGLISH, "x: %d, y: %d, w: %d, h: %d", member.x, member.y, member.width, member.height));
        sizeTextView.setText(String.format(Locale.ENGLISH, "movable: %s", member.movable ? "true" : "false"));
        return view;
    }
}
