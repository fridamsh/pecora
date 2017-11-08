package msh.frida.mapapp.Other;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import msh.frida.mapapp.Models.ObservationPoint;
import msh.frida.mapapp.R;

/**
 * Created by Frida on 08/11/2017.
 */

public class HistoryItemArrayAdapter extends ArrayAdapter<ObservationPoint> {

    private final Context context;
    private final List<ObservationPoint> values;

    public HistoryItemArrayAdapter(Context context, List<ObservationPoint> values) {
        super(context, R.layout.history_item_listview_layout, values);
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
            view = inflater.inflate(R.layout.history_item_listview_layout, null);
            final HistoryItemArrayAdapter.ViewHolder viewHolder = new HistoryItemArrayAdapter.ViewHolder();
            viewHolder.text = (TextView) view.findViewById(R.id.label);
            viewHolder.text2 = (TextView) view.findViewById(R.id.label2);
            viewHolder.text3 = (TextView) view.findViewById(R.id.label3);

            view.setTag(viewHolder);

        } else {
            view = convertView;
        }
        HistoryItemArrayAdapter.ViewHolder holder = (HistoryItemArrayAdapter.ViewHolder) view.getTag();
        holder.text.setText(Integer.toString(position+1));
        holder.text2.setText(Integer.toString(values.get(position).getObservationList().size()));
        holder.text3.setText(getTime(values.get(position).getTimeOfObservation()));
        return view;
    }

    private String getTime(Long dateInMillis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dateInMillis);
        String date = c.getTime().toString();
        String[] dateArray = date.split(" ");
        String[] timeArray = dateArray[3].split(":");

        return timeArray[0] + ":" + timeArray[1];
    }
}