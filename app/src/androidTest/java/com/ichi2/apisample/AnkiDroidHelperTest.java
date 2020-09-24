package com.ichi2.apisample;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.CursorWrapper;
import android.database.MatrixCursor;
import android.test.mock.MockContentResolver;
import android.test.mock.MockContext;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ichi2.anki.FlashCardsContract;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;


@RunWith(AndroidJUnit4.class)
public class AnkiDroidHelperTest {
    @Test
    public void isApiAvailableFalse() {
        ProviderInfo pi = new ProviderInfo();
        pi.packageName = null;

        PackageManager packageManager = mock(PackageManager.class);
        when(packageManager.resolveContentProvider(FlashCardsContract.AUTHORITY, 0))
                .thenReturn(pi);

        Context context = mock(MockContext.class);
        when(context.getPackageManager())
                .thenReturn(packageManager);

        assertFalse(AnkiDroidHelper.isApiAvailable(context));
    }

    @Test
    public void isApiAvailableTrue() {
        ProviderInfo pi = new ProviderInfo();
        pi.packageName = "AnkiDroid Flashcards";

        PackageManager packageManager = mock(PackageManager.class);
        when(packageManager.resolveContentProvider(FlashCardsContract.AUTHORITY, 0))
                .thenReturn(pi);

        Context context = mock(MockContext.class);
        when(context.getPackageManager())
                .thenReturn(packageManager);

        assertTrue(AnkiDroidHelper.isApiAvailable(context));
    }

//    @Test
//    public void findDeckNotExists() {
//        ContentResolver resolver = mock(MockContentResolver.class);
//        when(resolver.query(FlashCardsContract.Deck.CONTENT_ALL_URI, null, null, null, null))
//                .thenReturn(null);
//
//        SharedPreferences db = mock(SharedPreferences.class);
//
//        Context context = mock(MockContext.class);
//        when(context.getApplicationContext())
//                .thenReturn(context);
//        when(context.getContentResolver())
//                .thenReturn(resolver);
//        when(context.getSharedPreferences("com.ichi2.anki.api.decks", Context.MODE_PRIVATE))
//                .thenReturn(db);
//
//        AnkiDroidHelper mAnkiDroid = new AnkiDroidHelper(context);
//        assertNull(mAnkiDroid.findDeckIdByName("WrongDeck"));
//    }
//
//    @Test
//    public void findDeckExists() {
//        MatrixCursor decks = new MatrixCursor(new String[] {FlashCardsContract.Deck.DECK_ID, FlashCardsContract.Deck.DECK_NAME});
//        decks.newRow()
//            .add(FlashCardsContract.Deck.DECK_ID, 1)
//            .add(FlashCardsContract.Deck.DECK_NAME, "CorrectDeck");
//
//        ContentResolver resolver = mock(MockContentResolver.class);
//        doReturn(null)
//                .when(resolver)
//                .acquireUnstableProvider(FlashCardsContract.Deck.CONTENT_ALL_URI);
//        when(resolver.query(FlashCardsContract.Deck.CONTENT_ALL_URI, null, null, null)).thenReturn(new CursorWrapper(decks));
//
//        SharedPreferences db = mock(SharedPreferences.class);
//
//        Context context = mock(MockContext.class);
//        when(context.getApplicationContext())
//                .thenReturn(context);
//        when(context.getContentResolver())
//                .thenReturn(resolver);
//        when(context.getSharedPreferences("com.ichi2.anki.api.decks", Context.MODE_PRIVATE))
//                .thenReturn(db);
//
//        AnkiDroidHelper mAnkiDroid = new AnkiDroidHelper(context);
//        assertNull(mAnkiDroid.findDeckIdByName("WrongDeck"));
//        //assertEquals(1, mAnkiDroid.findDeckIdByName("CorrectDeck").longValue());
//    }
//
//    @Test
//    public void findModelNotExists() {
//        ContentResolver resolver = mock(MockContentResolver.class);
//        when(resolver.query(FlashCardsContract.Model.CONTENT_URI, null, null, null, null))
//                .thenReturn(null);
//
//        SharedPreferences db = mock(SharedPreferences.class);
//        when(db.getLong(AnkiDroidConfig.MODEL_NAME, -1L))
//                .thenReturn(-1L);
//
//        Context context = mock(MockContext.class);
//        when(context.getApplicationContext())
//                .thenReturn(context);
//        when(context.getContentResolver())
//                .thenReturn(resolver);
//        when(context.getSharedPreferences("com.ichi2.anki.api.decks", Context.MODE_PRIVATE))
//                .thenReturn(db);
//
//        AnkiDroidHelper mAnkiDroid = new AnkiDroidHelper(context);
//        assertNull(mAnkiDroid.findModelIdByName(AnkiDroidConfig.MODEL_NAME, AnkiDroidConfig.FIELDS.length));
//    }
//
//    @Test
//    public void findModelExists() {
//        MatrixCursor decks = new MatrixCursor(new String[] {FlashCardsContract.Deck.DECK_ID, FlashCardsContract.Deck.DECK_NAME});
//        decks.newRow()
//                .add(FlashCardsContract.Deck.DECK_ID, 1)
//                .add(FlashCardsContract.Deck.DECK_NAME, "CorrectDeck");
//
//        ContentResolver resolver = mock(MockContentResolver.class);
//        when(resolver.query(FlashCardsContract.Model.CONTENT_URI, null, null, null, null))
//                .thenReturn(new CursorWrapper(decks));
//
//        SharedPreferences db = mock(SharedPreferences.class);
//        when(db.getLong(AnkiDroidConfig.MODEL_NAME, -1L))
//                .thenReturn(-1L);
//
//        Context context = mock(MockContext.class);
//        when(context.getApplicationContext())
//                .thenReturn(context);
//        when(context.getContentResolver())
//                .thenReturn(resolver);
//        when(context.getSharedPreferences("com.ichi2.anki.api.decks", Context.MODE_PRIVATE))
//                .thenReturn(db);
//
//        AnkiDroidHelper mAnkiDroid = new AnkiDroidHelper(context);
//        assertNull(mAnkiDroid.findModelIdByName(AnkiDroidConfig.MODEL_NAME, AnkiDroidConfig.FIELDS.length));
//    }
//
//    private AnkiDroidHelper mAnkiDroid;
//
//    @Before
//    public void setup() {
//        mAnkiDroid = new AnkiDroidHelper(InstrumentationRegistry.getInstrumentation().getTargetContext());
//    }
//
//    @Test
//    public void isApiAvailableTrue() {
//        assertTrue(AnkiDroidHelper.isApiAvailable(InstrumentationRegistry.getInstrumentation().getTargetContext()));
//    }
//
//    @Test
//    public void findDeckNotExists() {
//        assertNull(mAnkiDroid.findDeckIdByName("WrongDeck"));
//    }
//
//    @Test
//    public void findDeckExists() {
//        InstrumentationRegistry.getInstrumentation().getTargetContext().getContentResolver().notifyChange(FlashCardsContract.Deck.CONTENT_ALL_URI, new ContentObserver(null) {
//            int a = 1;
//        });
//        assertNull(mAnkiDroid.findDeckIdByName("GoodDeck"));
//    }
}
