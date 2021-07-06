package com.ichi2.apisample.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.preference.PreferenceDialogFragmentCompat;

import com.ichi2.apisample.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MappingDialogFragment extends PreferenceDialogFragmentCompat {
    private String[] keys;
    private ArrayList<String> values;

    private Map<String, Spinner> keySpinners;

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        Context context = getContext();

        MappingPreference preference = (MappingPreference) getPreference();
        Map<String, String> persistedMapping = preference.getPersistedMapping();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                values
        );

        TableLayout table = view.findViewById(R.id.layoutEntries);
        keySpinners = new HashMap<>();
        for (final String key : keys) {
            TableRow row = new TableRow(context);
            row.setOrientation(LinearLayout.HORIZONTAL);

            TextView label = new TextView(context);
            label.setText(key);
            row.addView(label);

            Spinner spinner = new Spinner(context);
            spinner.setAdapter(adapter);
            String value = persistedMapping.getOrDefault(key, "");
            int position = values.indexOf(value);
            spinner.setSelection(position);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    String value = (String) adapterView.getItemAtPosition(i);
                    for (Map.Entry<String, Spinner> keySpinner : keySpinners.entrySet()) {
                        String _key = keySpinner.getKey();
                        Spinner spinner = keySpinner.getValue();
                        String _value = (String) spinner.getSelectedItem();
                        if (!key.equals(_key) && value.equals(_value)) {
                            spinner.setSelection(0);
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            keySpinners.put(key, spinner);
            row.addView(spinner);

            table.addView(row);
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (!positiveResult) {
            return;
        }
        Map<String, String> mapping = new HashMap<>();
        for (Map.Entry<String, Spinner> fieldSpinner : keySpinners.entrySet()) {
            String key = fieldSpinner.getKey();
            Spinner spinner = fieldSpinner.getValue();
            String value = (String) spinner.getSelectedItem();
            mapping.put(key, value);
        }
        MappingPreference preference = (MappingPreference) getPreference();
        preference.persistMapping(mapping);
    }

    public static MappingDialogFragment newInstance(String key, String[] keys, String[] values) {
        final MappingDialogFragment fragment = new MappingDialogFragment();

        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        fragment.keys = keys;
        fragment.values = new ArrayList<>(Arrays.asList(values));
        fragment.values.add(0, "");

        return fragment;
    }
}
