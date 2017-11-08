package msh.frida.mapapp.Other;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import msh.frida.mapapp.Models.HikeModel;
import msh.frida.mapapp.R;

public class HistoryArrayAdapter extends ArrayAdapter<HikeModel> {

    private final Context context;
    private final List<HikeModel> values;

    public HistoryArrayAdapter(Context context, List<HikeModel> values) {
        super(context, R.layout.history_listview_layout, values);
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
        View view;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            view = inflater.inflate(R.layout.history_listview_layout, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) view.findViewById(R.id.label);
            viewHolder.text2 = (TextView) view.findViewById(R.id.label2);
            viewHolder.text3 = (TextView) view.findViewById(R.id.label3);

            view.setTag(viewHolder);

        } else {
            view = convertView;
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.text.setText(values.get(position).getName());
        holder.text2.setText(values.get(position).getTitle());
        holder.text3.setText(values.get(position).getWeatherState());
        return view;
    }
}