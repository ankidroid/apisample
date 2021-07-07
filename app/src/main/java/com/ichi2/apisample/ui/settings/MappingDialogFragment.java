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
import java.util.Set;

public class MappingDialogFragment extends PreferenceDialogFragmentCompat {
    private final String[] keys;
    private final int nEntries;
    private final Set<String> disabledKeys;
    private final ArrayList<String> values;
    private final Spinner[] spinners;

    private MappingDialogFragment(String[] keys, String[] values, Set<String> disabledKeys) {
        this.keys = keys;
        this.disabledKeys = disabledKeys;
        this.values = new ArrayList<>(Arrays.asList(values));
        this.values.add(0, "");
        nEntries = keys.length;
        spinners = new Spinner[nEntries];
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        Context context = getContext();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                values
        );

        MappingPreference preference = (MappingPreference) getPreference();
        Map<String, String> mapping = preference.getPersistedMapping();

        int nAvailableEntries = 0;
        ArrayList<String> availableValues = new ArrayList<>(values);
        for (String key : keys) {
            if (!disabledKeys.contains(key)) {
                nAvailableEntries++;
            } else {
                if (mapping.containsKey(key)) {
                    String value = mapping.get(key);
                    if (!value.isEmpty()) {
                        availableValues.remove(value);
                    }
                }
            }
        }
        ArrayAdapter<String> availableAdapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                availableValues
        );
        final Spinner[] availableSpinners = new Spinner[nAvailableEntries];
        int availableIdx = 0;

        TableLayout table = view.findViewById(R.id.layoutEntries);
        for (int idx = 0; idx < nEntries; idx++) {
            String key = keys[idx];

            TableRow row = new TableRow(context);
            row.setOrientation(LinearLayout.HORIZONTAL);

            TextView label = new TextView(context);
            label.setText(key);
            row.addView(label);

            boolean isEnabled = !disabledKeys.contains(key);

            final Spinner spinner = new Spinner(context);
            spinner.setAdapter(isEnabled ? availableAdapter : adapter);
            String value = mapping.getOrDefault(key, "");
            int position = (isEnabled ? availableValues : values).indexOf(value);
            spinner.setSelection(position);
            spinner.setEnabled(isEnabled);
            row.addView(spinner);
            spinners[idx] = spinner;
            if (isEnabled) {
                availableSpinners[availableIdx++] = spinner;
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                        String value = (String) adapterView.getItemAtPosition(position);
                        for (Spinner _spinner : availableSpinners) {
                            String _value = (String) _spinner.getSelectedItem();
                            if (_spinner != spinner && value.equals(_value)) {
                                _spinner.setSelection(0);
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
            }

            table.addView(row);
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (!positiveResult) {
            return;
        }
        Map<String, String> mapping = new HashMap<>();
        for (int i = 0; i < nEntries; i++) {
            String key = keys[i];
            Spinner spinner = spinners[i];
            String value = (String) spinner.getSelectedItem();
            mapping.put(key, value);
        }
        MappingPreference preference = (MappingPreference) getPreference();
        preference.persistMapping(mapping);
    }

    public static MappingDialogFragment newInstance(String key, String[] keys, String[] values, Set<String> disabledKeys) {
        final MappingDialogFragment fragment = new MappingDialogFragment(keys, values, disabledKeys);

        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }
}
