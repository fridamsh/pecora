package msh.frida.mapapp.Other;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import msh.frida.mapapp.Models.Observation;
import msh.frida.mapapp.R;

/**
 * Created by Frida on 09/11/2017.
 */

public class HistoryItemObservationArrayAdapter extends ArrayAdapter<Observation> {

    private final Context context;
    private final List<Observation> values;

    public HistoryItemObservationArrayAdapter(Context context, List<Observation> values) {
        super(context, R.layout.history_item_observation_layout, values);
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
            view = inflater.inflate(R.layout.history_item_observation_layout, null);
            final HistoryItemObservationArrayAdapter.ViewHolder viewHolder = new HistoryItemObservationArrayAdapter.ViewHolder();
            viewHolder.text = (TextView) view.findViewById(R.id.label_number);
            viewHolder.text2 = (TextView) view.findViewById(R.id.label_type);
            viewHolder.text3 = (TextView) view.findViewById(R.id.label_details);

            view.setTag(viewHolder);

        } else {
            view = convertView;
        }
        HistoryItemObservationArrayAdapter.ViewHolder holder = (HistoryItemObservationArrayAdapter.ViewHolder) view.getTag();
        holder.text.setText(Integer.toString(position+1));
        holder.text2.setText("Type: " + values.get(position).getTypeOfObservation());
        holder.text3.setText("Detaljer: " + values.get(position).getDetails());
        return view;
    }
}
