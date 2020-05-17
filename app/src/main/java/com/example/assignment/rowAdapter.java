package com.example.assignment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.assignment.R;
import com.example.assignment.DetailPost;


import org.w3c.dom.Text;

import java.util.ArrayList;

public class rowAdapter extends ArrayAdapter<DetailPost> {

    private ArrayList<DetailPost> details;
    private Context context;
    private int resource;

    public rowAdapter(Context context, int resource ,ArrayList<DetailPost> details) {
        super(context, resource, details);
        this.context = context;
        this.resource = resource;
        this.details = details;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @Nullable ViewGroup parent) {
        View view = convertView;
        System.out.println("View: " + view);
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(resource, parent, false);
        }
        TextView comment_name = (TextView) view.findViewById(R.id.comment_name);
        TextView comment_context = (TextView) view.findViewById(R.id.comment_context);
        TextView comment_count = (TextView) view.findViewById(R.id.count);


        System.out.println(details.get(position).getComment_owner());
//        comment_name.setText("Testing");
        comment_name.setText(details.get(position).getComment_owner());
        comment_context.setText(details.get(position).getComment_contents());
        String counter = String.valueOf(position + 1);
        comment_count.setText(counter);
        return view;
    }
}