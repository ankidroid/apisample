package com.ichi2.apisample;

import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.LinkedList;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.not;

import static org.mockito.Mockito.*;


@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    private View decorView;

    @Test
    public void addDataError0() {
        // Prepare mock data and pass it into the activity
        activityScenarioRule.getScenario().onActivity(new ActivityScenario.ActivityAction<MainActivity>() {
            @Override
            public void perform(MainActivity activity) {
                AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
                when(helper.findDeckIdByName(AnkiDroidConfig.DECK_NAME)).thenReturn(null);
                when(helper.findModelIdByName(AnkiDroidConfig.MODEL_NAME, AnkiDroidConfig.FIELDS.length)).thenReturn(null);
                // can not save deck due to some reason
                when(helper.addNewDeck(AnkiDroidConfig.DECK_NAME)).thenReturn(null);
                // can not save model due to some reason
                when(helper.addNewCustomModel(AnkiDroidConfig.MODEL_NAME, AnkiDroidConfig.FIELDS, AnkiDroidConfig.CARD_NAMES,
                        AnkiDroidConfig.QFMT, AnkiDroidConfig.AFMT, AnkiDroidConfig.CSS, null, null)).thenReturn(null);
                activity.setAnkiDroidHelper(helper);
            }
        });

        // Type texts
        onView(withId(R.id.inputQuestion))
                .perform(clearText(), typeText("Hello"), closeSoftKeyboard());
        onView(withId(R.id.inputQuestion))
                .check(matches(withText("Hello")));

        onView(withId(R.id.inputAnswer))
                .perform(clearText(), typeText("World"), closeSoftKeyboard());
        onView(withId(R.id.inputAnswer))
                .check(matches(withText("World")));

        // Press the button
        onView(withId(R.id.actionAddToAnki)).perform(click());

        // Check if toast message appeared
        onView(withText(R.string.card_add_fail))
                .inRoot(withDecorView(not(decorView)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void addDataError1() {
        // Prepare mock data and pass it into the activity
        activityScenarioRule.getScenario().onActivity(new ActivityScenario.ActivityAction<MainActivity>() {
            @Override
            public void perform(MainActivity activity) {
                long deckId = 1000L;
                long modelId = 2000L;

                AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
                when(helper.findDeckIdByName(AnkiDroidConfig.DECK_NAME)).thenReturn(deckId);
                when(helper.findModelIdByName(AnkiDroidConfig.MODEL_NAME, AnkiDroidConfig.FIELDS.length)).thenReturn(modelId);
                // Fields list empty for some reason
                when(helper.getFieldList(modelId)).thenReturn(null);
                activity.setAnkiDroidHelper(helper);
            }
        });

        // Type texts
        onView(withId(R.id.inputQuestion))
                .perform(clearText(), typeText("Hello"), closeSoftKeyboard());
        onView(withId(R.id.inputQuestion))
                .check(matches(withText("Hello")));

        onView(withId(R.id.inputAnswer))
                .perform(clearText(), typeText("World"), closeSoftKeyboard());
        onView(withId(R.id.inputAnswer))
                .check(matches(withText("World")));

        // Press the button
        onView(withId(R.id.actionAddToAnki)).perform(click());

        // Check if toast message appeared
        onView(withText(R.string.card_add_fail))
                .inRoot(withDecorView(not(decorView)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void addDataAlreadyExists() {
        // Prepare mock data and pass it into the activity
        activityScenarioRule.getScenario().onActivity(new ActivityScenario.ActivityAction<MainActivity>() {
            @Override
            public void perform(MainActivity activity) {
                // Needed for the toast messages checking
                decorView = activity.getWindow().getDecorView();

                long deckId = 1000L;
                long modelId = 2000L;

                AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
                when(helper.findDeckIdByName(AnkiDroidConfig.DECK_NAME)).thenReturn(deckId);
                when(helper.findModelIdByName(AnkiDroidConfig.MODEL_NAME, AnkiDroidConfig.FIELDS.length)).thenReturn(modelId);
                when(helper.getFieldList(modelId)).thenReturn(AnkiDroidConfig.FIELDS);
                doAnswer(new Answer() {
                    public Object answer(InvocationOnMock invocation) {
                        LinkedList<String[]> fields = invocation.getArgument(0);
                        fields.clear(); // delete card data (as duplicated data)
                        return null;
                    }
                }).when(helper).removeDuplicates(any(LinkedList.class), any(LinkedList.class), eq(modelId));

                activity.setAnkiDroidHelper(helper);
            }
        });

        // Type texts
        onView(withId(R.id.inputQuestion))
                .perform(clearText(), typeText("Hello"), closeSoftKeyboard());
        onView(withId(R.id.inputQuestion))
                .check(matches(withText("Hello")));

        onView(withId(R.id.inputAnswer))
                .perform(clearText(), typeText("World"), closeSoftKeyboard());
        onView(withId(R.id.inputAnswer))
                .check(matches(withText("World")));

        // Press the button
        onView(withId(R.id.actionAddToAnki)).perform(click());

        // Check if toast message appeared
        onView(withText(R.string.card_exists))
                .inRoot(withDecorView(not(decorView)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void addDataSuccess() {
        // Prepare mock data and pass it into the activity
        activityScenarioRule.getScenario().onActivity(new ActivityScenario.ActivityAction<MainActivity>() {
            @Override
            public void perform(MainActivity activity) {
                // Needed for the toast messages checking
                decorView = activity.getWindow().getDecorView();

                long deckId = 1000L;
                long modelId = 2000L;

                AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
                when(helper.findDeckIdByName(AnkiDroidConfig.DECK_NAME)).thenReturn(deckId);
                when(helper.findModelIdByName(AnkiDroidConfig.MODEL_NAME, AnkiDroidConfig.FIELDS.length)).thenReturn(modelId);
                when(helper.getFieldList(modelId)).thenReturn(AnkiDroidConfig.FIELDS);
                // don't delete anything
                doNothing().when(helper).removeDuplicates(any(LinkedList.class), any(LinkedList.class), eq(modelId));
                when(helper.addNotes(eq(modelId), eq(deckId), any(LinkedList.class), any(LinkedList.class))).thenReturn(1);

                activity.setAnkiDroidHelper(helper);
            }
        });

        // Type texts
        onView(withId(R.id.inputQuestion))
                .perform(clearText(), typeText("Hello"), closeSoftKeyboard());
        onView(withId(R.id.inputQuestion))
                .check(matches(withText("Hello")));

        onView(withId(R.id.inputAnswer))
                .perform(clearText(), typeText("World"), closeSoftKeyboard());
        onView(withId(R.id.inputAnswer))
                .check(matches(withText("World")));

        // Press the button
        onView(withId(R.id.actionAddToAnki)).perform(click());

        // Check if toast message appeared
        onView(withText(R.string.item_added))
                .inRoot(withDecorView(not(decorView)))
                .check(matches(isDisplayed()));
    }
}
