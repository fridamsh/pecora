package msh.frida.mapapp.Other;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import msh.frida.mapapp.Models.HikeModel;
import msh.frida.mapapp.R;

public class SimpleArrayAdapter extends ArrayAdapter<HikeModel> {

    private final Context context;
    private final List<HikeModel> values;

    public SimpleArrayAdapter(Context context, List<HikeModel> values) {
        super(context, R.layout.listviewlayoyt, values);
        this.context = context;
        this.values = values;
    }

    static class ViewHolder {
        protected TextView text;
        protected TextView text2;
        protected TextView text3;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            view = inflater.inflate(R.layout.listviewlayoyt, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) view.findViewById(R.id.label);
            viewHolder.text2 = (TextView) view.findViewById(R.id.label2);
            viewHolder.text3 = (TextView) view.findViewById(R.id.label3);

            view.setTag(viewHolder);

        } else {
            view = convertView;
            //((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.text.setText(values.get(position).getName());
        holder.text2.setText(values.get(position).getTitle());
        holder.text3.setText(values.get(position).getWeatherState());
        return view;
    }

    /*@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.listviewlayoyt, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        TextView textView2 = (TextView) rowView.findViewById(R.id.label2);
        TextView textView3 = (TextView) rowView.findViewById(R.id.label3);
        //ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        textView.setText(values[position]);
        textView2.setText(values[position+1]);
        textView3.setText(values[position+2]);
        //imageView.setImageResource(R.drawable.icon_sheep);

        return rowView;
    }*/
}