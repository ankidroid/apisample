package com.ichi2.apisample;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionProvider;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.ichi2.anki.api.AddContentApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = "AnkiDroidApiSample";
    private static final String MID = "ankidroid.mid";
    private static final String DID = "ankidroid.did";

    private ListView mListView;
    private ArrayList<HashMap<String, String>> mListData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Create the example data
        mListData = AnkiDroidConfig.getExampleData();
        // Setup the ListView containing the example data
        mListView = (ListView) findViewById(R.id.main_list);
        mListView.setAdapter(new SimpleAdapter(this, mListData, R.layout.word_layout,
                Arrays.copyOfRange(AnkiDroidConfig.FIELDS, 0, 3),
                new int[]{R.id.word_item, R.id.word_item_reading, R.id.word_item_translation}));
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        // When an item is long-pressed the ListSelectListener will make a Contextual Action Bar with Share icon
        mListView.setMultiChoiceModeListener(new ListSelectListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Inner class that handles the contextual action bar that appears when an item is long-pressed in the ListView
     */
    class ListSelectListener implements AbsListView.MultiChoiceModeListener {

        @Override
        public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
            // Set the subtitle on the action bar to show how many items are selected
            int numItemsChecked = mListView.getCheckedItemCount();
            String subtitle = getResources().getString(R.string.n_items_selected, numItemsChecked);
            mode.setSubtitle(subtitle);
        }

        @Override
        public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
            // Inflate the menu resource while holds the contextual action bar actions
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.action_mode_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
            // Don't need to do anything here
            return false;
        }

        @Override
        public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
            // This is called when the contextual action bar buttons are pressed
            switch (item.getItemId()) {
                case R.id.share_data_button:
                    /** Use AnkiDroid provider if installed, otherwise use ACTION_SEND intent
                        If you don't need to share with any apps other than AnkiDroid, you can completely replace
                        this code block with the code in AnkiDroidActionProvider.onMenuItemClick()
                     **/
                    if (!AddContentApi.isInstalled(MainActivity.this)) {
                        // Only 1 piece of text is supported by the ACTION_SEND intent, so take first entry
                        shareViaSendIntent(getSelectedData().get(0));
                    } else {
                        // Use AnkiDroidActionProvider to handle the click event if the provider is installed
                        item.setActionProvider(new AnkiDroidActionProvider(MainActivity.this, getSelectedData()));
                    }
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(android.view.ActionMode mode) {
            // Don't need to do anything here
        }
    }

    ArrayList<HashMap<String, String>> getSelectedData() {
        // Extract the selected data
        SparseBooleanArray checked = mListView.getCheckedItemPositions();
        ArrayList<HashMap<String, String>> selectedData = new ArrayList<>();
        for (int i=0;i<checked.size();i++) {
            if (checked.valueAt(i)) {
                selectedData.add(mListData.get(checked.keyAt(i)));
            }
        }
        return selectedData;
    }

    /**
     * Inner class which implements the dropdown menu on the Share button of the Contextual Action Bar
     */
    class AnkiDroidActionProvider extends ActionProvider implements
            MenuItem.OnMenuItemClickListener {

        static final int ANKIDROID_INSTANT_ADD = 0;
        static final int ALL_APPS = 1;
        ArrayList<HashMap<String, String>> mSelectedData;


        /**
         * Creates a new instance.
         *
         * @param context Context for accessing resources.
         */
        public AnkiDroidActionProvider(Activity context, ArrayList<HashMap<String, String>> selectedData) {
            super(context);
            mSelectedData = selectedData;
        }

        @Override
        public View onCreateActionView() {
            // Just return null for a simple dropdown menu
            return null;
        }

        @Override
        public boolean hasSubMenu() {
            // If the AnkiDroid ContentProvider is installed then show it in a submenu, otherwise no need for submenu
            return AddContentApi.isInstalled(MainActivity.this);
        }

        @Override
        public void onPrepareSubMenu(SubMenu subMenu) {
            // Generate the submenu when the system asks for it
            subMenu.clear();
            PackageManager manager = getApplicationContext().getPackageManager();
            Resources res = getApplicationContext().getResources();

            // Add AnkiDroid "instant add" to the menu
            try {
                ApplicationInfo appInfo = manager.getApplicationInfo(AddContentApi.getPackageName(MainActivity.this),0);
                String label = manager.getApplicationLabel(appInfo) + " " + res.getString(R.string.instant_add);
                subMenu.add(0, ANKIDROID_INSTANT_ADD, ANKIDROID_INSTANT_ADD, label)
                        .setIcon(appInfo.loadIcon(manager))
                        .setOnMenuItemClickListener(this);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(MainActivity.LOG_TAG, "AnkiDroid app could not be found");
            }
            // Add other apps here if it's advantageous for the user to be able to access them with one click
            // You could also get rid of the "more" item and just add the apps that support SEND directly to submenu

            // Add a "more" item to show more items if there are too many
            subMenu.add(0, ALL_APPS, ALL_APPS, res.getString(R.string.more_items))
                    .setIcon(R.mipmap.ic_launcher)
                    .setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            // Handle when the submenu items are clicked
            if (item.getItemId() == ANKIDROID_INSTANT_ADD) {
                // Add all data using AnkiDroid provider
                configureAndSendToAnkiDroid(getSelectedData());
            } else if (item.getItemId() == ALL_APPS) {
                // If the user presses "more" then switch to the stock Android intent selector (can only send 1 card)
                shareViaSendIntent(mSelectedData.get(0));
            }
            return true;
        }
    }

    /**
     * Send a simple front / back flashcard via the ACTION_SEND intent
     * @param data
     */
    private void shareViaSendIntent(HashMap<String, String> data) {
        // Use ShareCompat so that the sending app info is correctly included in the share intent
        Activity context = MainActivity.this;
        Intent shareIntent = ShareCompat.IntentBuilder.from(context)
                .setType("text/plain")
                .setText(data.get(AnkiDroidConfig.BACK_SIDE_KEY))
                .setSubject(data.get(AnkiDroidConfig.FRONT_SIDE_KEY))
                .getIntent();
        if (shareIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(shareIntent);
        }
    }

    private void configureAndSendToAnkiDroid(final ArrayList<HashMap<String, String>> data) {
        // Get api instance
        final AddContentApi api = new AddContentApi(MainActivity.this);
        // Check if a valid model ID and deck ID have been stored in preferences
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        Long mid = prefs.getLong(MID, -1);
        Long did = prefs.getLong(DID, -1);
        // Try to find by name if they weren't stored in preferences (will work if the user hasn't changed the names)
        if (mid == -1) {
            mid = api.findModelId(AnkiDroidConfig.MODEL_NAME, AnkiDroidConfig.FIELDS.length);
        }
        if (did == -1) {
            did = api.getDeckId(AnkiDroidConfig.DECK_NAME);
        }
        final boolean addModel = api.getModelName(mid) == null;
        final boolean addDeck = api.getDeckName(did) == null;

        if (addModel || addDeck) {
            // If a valid mid and did could not be found then add a new model and deck to AnkiDroid
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Dialog);
            builder.setMessage(R.string.confirm_add_models);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Long did = null;
                    if (addDeck) {
                        try {
                            did = api.addNewDeck(AnkiDroidConfig.DECK_NAME);
                            prefs.edit().putLong(DID, did).commit();
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "Error adding deck "+AnkiDroidConfig.DECK_NAME+" to AnkiDroid via API", e);
                            Toast.makeText(MainActivity.this, R.string.deck_add_fail, Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                    if (addModel) {
                        try {
                            Long mid = api.addNewModel(AnkiDroidConfig.MODEL_NAME, AnkiDroidConfig.FIELDS,
                                    AnkiDroidConfig.CARD_NAMES, AnkiDroidConfig.QFMT, AnkiDroidConfig.AFMT,
                                    AnkiDroidConfig.CSS, did);
                            prefs.edit().putLong(MID, mid).commit();
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "Error adding model " + AnkiDroidConfig.MODEL_NAME + " to AnkiDroid via API", e);
                            Toast.makeText(MainActivity.this, R.string.model_add_fail, Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                    addCardsToAnkiDroid(data);
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
        } else {
            // No need to add
            addCardsToAnkiDroid(data);
        }
    }

    /**
     * Add flashcards to AnkiDroid. A valid model ID must be given or an exception will be thrown.
     * Call configureAndSendToAnkiDroid() first to ensure that the model and deck are setup properly.
     * @param data
     */
    private void addCardsToAnkiDroid(ArrayList<HashMap<String, String>> data) {
        Resources res = getResources();
        // Get api instance
        AddContentApi api = new AddContentApi(MainActivity.this);
        // Get mid and did from prefs
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        Long mid = prefs.getLong(MID, -1);
        Long did = prefs.getLong(DID, -1);
        if (mid == -1 || did == -1) {
            throw new IllegalArgumentException("mid and did not saved in shared preferences");
        }
        // Add cards
        int added = 0;
        for (HashMap<String, String> hm: data) {
            String[] fieldNames = api.getFieldList(mid);
            if (fieldNames == null) {
                throw new IllegalArgumentException("Specified model doesn't exist");
            }
            // Build a field map accounting for the fact that the user could have changed the fields in the model
            String[] flds = new String[fieldNames.length];
            for (int i = 0; i < flds.length; i++) {
                // Fill up the fields one-by-one up until either all fields are filled or we run out of fields to send
                if (i < AnkiDroidConfig.FIELDS.length) {
                    flds[i] = hm.get(AnkiDroidConfig.FIELDS[i]);
                }
            }
            try {
                api.addNewNote(mid, did, flds, AnkiDroidConfig.TAGS);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception adding cards to AnkiDroid", e);
                Toast.makeText(MainActivity.this, R.string.card_add_fail, Toast.LENGTH_LONG).show();
                return;
            }
            added++;
        }
        Toast.makeText(MainActivity.this, res.getString(R.string.n_items_added, added), Toast.LENGTH_LONG).show();
    }
}