package com.ichi2.apisample;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    public static final String LOG_TAG = "AnkiDroidApiSample";
    private static final int AD_PERM_REQUEST = 0;

    private ListView mListView;
    private List<Map<String, String>> mListData;
    private AnkiDroidHelper mAnkiDroid;

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
        // Create instance of helper class
        mAnkiDroid = new AnkiDroidHelper(this);
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == AD_PERM_REQUEST && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            addCardsToAnkiDroid(getSelectedData());
        } else {
            Toast.makeText(MainActivity.this, R.string.permission_denied, Toast.LENGTH_LONG).show();
        }
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
            if (item.getItemId() == R.id.share_data_button) {
                /* Use AnkiDroid provider if installed, otherwise use ACTION_SEND intent
                 * If you don't need to share with any apps other than AnkiDroid, you can completely replace
                 * this code block with the code in AnkiDroidActionProvider.onMenuItemClick()
                 */
                if (AnkiDroidHelper.isApiAvailable(MainActivity.this)) {
                    // Use AnkiDroidActionProvider to handle the click event if the provider is installed
                    item.setActionProvider(new AnkiDroidActionProvider(MainActivity.this, getSelectedData()));
                } else {
                    // Only 1 piece of text is supported by the ACTION_SEND intent, so take first entry
                    shareViaSendIntent(getSelectedData().get(0));
                }
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void onDestroyActionMode(android.view.ActionMode mode) {
            // Don't need to do anything here
        }
    }

    List<Map<String, String>> getSelectedData() {
        // Extract the selected data
        SparseBooleanArray checked = mListView.getCheckedItemPositions();
        List<Map<String, String>> selectedData = new ArrayList<>();
        for (int i = 0; i < checked.size(); i++) {
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
        List<Map<String, String>> mSelectedData;


        /**
         * Creates a new instance.
         *
         * @param context Context for accessing resources.
         */
        public AnkiDroidActionProvider(Activity context, List<Map<String, String>> selectedData) {
            super(context);
            mSelectedData = selectedData;
        }

        @Override
        @Deprecated
        public View onCreateActionView() {
            // Just return null for a simple dropdown menu
            return null;
        }

        @Override
        public boolean hasSubMenu() {
            // If the AnkiDroid ContentProvider is installed then show it in a submenu, otherwise no need for submenu
            return AddContentApi.getAnkiDroidPackageName(MainActivity.this) != null;
        }

        @Override
        public void onPrepareSubMenu(SubMenu subMenu) {
            // Generate the submenu when the system asks for it
            subMenu.clear();
            PackageManager manager = getApplicationContext().getPackageManager();
            Resources res = getApplicationContext().getResources();
            // Add AnkiDroid "instant add" to the menu
            try {
                ApplicationInfo appInfo = manager.getApplicationInfo(AddContentApi.getAnkiDroidPackageName(MainActivity.this), 0);
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
                // Request permission to access API if required
                if (mAnkiDroid.shouldRequestPermission()) {
                    mAnkiDroid.requestPermission(MainActivity.this, AD_PERM_REQUEST);
                    return true;
                }
                // Add all data using AnkiDroid provider
                addCardsToAnkiDroid(mSelectedData);
            } else if (item.getItemId() == ALL_APPS) {
                // If the user presses "more" then switch to the stock Android intent selector (can only send 1 card)
                shareViaSendIntent(mSelectedData.get(0));
            }
            return true;
        }
    }

    /**
     * Send a simple front / back flashcard via the ACTION_SEND intent
     */
    private void shareViaSendIntent(Map<String, String> data) {
        // Use ShareCompat so that the sending app info is correctly included in the share intent
        Activity context = MainActivity.this;
        Intent shareIntent = new ShareCompat.IntentBuilder(context)
                .setType("text/plain")
                .setText(data.get(AnkiDroidConfig.BACK_SIDE_KEY))
                .setSubject(data.get(AnkiDroidConfig.FRONT_SIDE_KEY))
                .getIntent();
        if (shareIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(shareIntent);
        }
    }

    /**
     * get the deck id
     *
     * @return might be null if there was a problem
     */
    private Long getDeckId() {
        Long did = mAnkiDroid.findDeckIdByName(AnkiDroidConfig.DECK_NAME);
        if (did == null) {
            did = mAnkiDroid.getApi().addNewDeck(AnkiDroidConfig.DECK_NAME);
            mAnkiDroid.storeDeckReference(AnkiDroidConfig.DECK_NAME, did);
        }
        return did;
    }

    /**
     * get model id
     *
     * @return might be null if there was an error
     */
    private Long getModelId() {
        Long mid = mAnkiDroid.findModelIdByName(AnkiDroidConfig.MODEL_NAME, AnkiDroidConfig.FIELDS.length);
        if (mid == null) {
            mid = mAnkiDroid.getApi().addNewCustomModel(AnkiDroidConfig.MODEL_NAME, AnkiDroidConfig.FIELDS,
                    AnkiDroidConfig.CARD_NAMES, AnkiDroidConfig.QFMT, AnkiDroidConfig.AFMT, AnkiDroidConfig.CSS, getDeckId(), null);
            mAnkiDroid.storeModelReference(AnkiDroidConfig.MODEL_NAME, mid);
        }
        return mid;
    }

    /**
     * Use the instant-add API to add flashcards directly to AnkiDroid.
     *
     * @param data List of cards to be added. Each card has a HashMap of field name / field value pairs.
     */
    private void addCardsToAnkiDroid(final List<Map<String, String>> data) {
        Long deckId = getDeckId();
        Long modelId = getModelId();
        if ((deckId == null) || (modelId == null)) {
            // we had an API error, report failure and return
            Toast.makeText(MainActivity.this, getResources().getString(R.string.card_add_fail), Toast.LENGTH_LONG).show();
            return;
        }
        String[] fieldNames = mAnkiDroid.getApi().getFieldList(modelId);
        if (fieldNames == null) {
            // we had an API error, report failure and return
            Toast.makeText(MainActivity.this, getResources().getString(R.string.card_add_fail), Toast.LENGTH_LONG).show();
            return;
        }
        // Build list of fields and tags
        LinkedList<String[]> fields = new LinkedList<>();
        LinkedList<Set<String>> tags = new LinkedList<>();
        for (Map<String, String> fieldMap : data) {
            // Build a field map accounting for the fact that the user could have changed the fields in the model
            String[] flds = new String[fieldNames.length];
            for (int i = 0; i < flds.length; i++) {
                // Fill up the fields one-by-one until either all fields are filled or we run out of fields to send
                if (i < AnkiDroidConfig.FIELDS.length) {
                    flds[i] = fieldMap.get(AnkiDroidConfig.FIELDS[i]);
                }
            }
            tags.add(AnkiDroidConfig.TAGS);
            fields.add(flds);
        }
        // Remove any duplicates from the LinkedLists and then add over the API
        mAnkiDroid.removeDuplicates(fields, tags, modelId);
        int added = mAnkiDroid.getApi().addNotes(modelId, deckId, fields, tags);
        if (added != 0) {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.n_items_added, added), Toast.LENGTH_LONG).show();
        } else {
            // API indicates that a 0 return value is an error
            Toast.makeText(MainActivity.this, getResources().getString(R.string.card_add_fail), Toast.LENGTH_LONG).show();
        }
    }
}