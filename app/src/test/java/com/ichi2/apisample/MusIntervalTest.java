package com.ichi2.apisample;

import org.junit.Test;
import org.mockito.internal.stubbing.answers.ThrowsExceptionClass;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class MusIntervalTest {

    final static String defaultDeckName = "Music intervals";
    final static String defaultModelName = "Music.interval";
    final static String defaultStartNote = "C#3";
    final static String startNote2 = "C#2";

    @Test
    @SuppressWarnings("unchecked")
    public void checkExistence_NoStartingNotes() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .build();

        assertFalse(mi.existsInAnki());
        assertEquals(0, mi.getExistingNotesCount());
        assertEquals(0, mi.getExistingMarkedNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkExistence_NoSuchStartingNote() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .start_note(defaultStartNote)
                .build();

        assertFalse(mi.existsInAnki());
        assertEquals(0, mi.getExistingNotesCount());
        assertEquals(0, mi.getExistingMarkedNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkExistence_StartingNoteExists() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        final String interval = "min2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        item1.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.ASC);
        item1.put(MusInterval.Fields.TIMING, MusInterval.Fields.Timing.MELODIC);
        item1.put(MusInterval.Fields.INTERVAL, interval);
        item1.put(MusInterval.Fields.TEMPO, tempo);
        item1.put(MusInterval.Fields.INSTRUMENT, instrument);
        existingNotesData.add(item1);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .start_note(defaultStartNote)
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .interval(interval)
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertTrue(mi.existsInAnki());
        assertEquals(1, mi.getExistingNotesCount());
        assertEquals(0, mi.getExistingMarkedNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkExistence_StartingNoteExistsAlreadyMarked() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        final String interval = "min2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        item1.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.ASC);
        item1.put(MusInterval.Fields.TIMING, MusInterval.Fields.Timing.MELODIC);
        item1.put(MusInterval.Fields.INTERVAL, interval);
        item1.put(MusInterval.Fields.TEMPO, tempo);
        item1.put(MusInterval.Fields.INSTRUMENT, instrument);
        item1.put("tags", " marked ");
        existingNotesData.add(item1);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .start_note(defaultStartNote)
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .interval(interval)
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertTrue(mi.existsInAnki());
        assertEquals(1, mi.getExistingNotesCount());
        assertEquals(1, mi.getExistingMarkedNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkExistence_StartingNoteExistsIgnoreCase() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        final String interval = "min2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        item1.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.ASC);
        item1.put(MusInterval.Fields.TIMING, MusInterval.Fields.Timing.MELODIC);
        item1.put(MusInterval.Fields.INTERVAL, interval);
        item1.put(MusInterval.Fields.TEMPO, tempo);
        item1.put(MusInterval.Fields.INSTRUMENT, instrument);
        existingNotesData.add(item1);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .start_note(defaultStartNote.toLowerCase()) // case should be ignored
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .interval(interval.toUpperCase()) // case should be ignored
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertTrue(mi.existsInAnki());
        assertEquals(1, mi.getExistingNotesCount());
        assertEquals(0, mi.getExistingMarkedNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkExistence_StartingNoteExistsIgnoreSpaces() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        final String interval = "min2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        item1.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.ASC);
        item1.put(MusInterval.Fields.TIMING, MusInterval.Fields.Timing.MELODIC);
        item1.put(MusInterval.Fields.INTERVAL, interval);
        item1.put(MusInterval.Fields.TEMPO, tempo);
        item1.put(MusInterval.Fields.INSTRUMENT, instrument);
        existingNotesData.add(item1);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .start_note(defaultStartNote)
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .interval(interval + " ")
                .tempo("  ") // should be trimmed
                .instrument(" " + instrument + " ") // should be trimmed
                .build();

        assertTrue(mi.existsInAnki());
        assertEquals(1, mi.getExistingNotesCount());
        assertEquals(0, mi.getExistingMarkedNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkExistence_StartingNoteExistsWithDifferentOtherFields() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        final String interval = "min2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .start_note(defaultStartNote)
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .interval(interval)
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertFalse(mi.existsInAnki());
        assertEquals(0, mi.getExistingNotesCount());
        assertEquals(0, mi.getExistingMarkedNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkExistence_StartingNoteExistsRegardlessOfSound() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        final String interval = "min2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        item1.put(MusInterval.Fields.SOUND, "/test1");  // sound field does not matter
        item1.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.ASC);
        item1.put(MusInterval.Fields.TIMING, MusInterval.Fields.Timing.MELODIC);
        item1.put(MusInterval.Fields.INTERVAL, interval);
        item1.put(MusInterval.Fields.TEMPO, tempo);
        item1.put(MusInterval.Fields.INSTRUMENT, instrument);
        existingNotesData.add(item1);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sound("/test2")
                .start_note(defaultStartNote)
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .interval(interval)
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertTrue(mi.existsInAnki());
        assertEquals(1, mi.getExistingNotesCount());
        assertEquals(0, mi.getExistingMarkedNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkExistence_withOnlyHelperAndEmptyModel_shouldFail() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>(); // no notes at all

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .build();

        assertFalse(mi.existsInAnki());
        assertEquals(0, mi.getExistingNotesCount());
        assertEquals(0, mi.getExistingMarkedNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkExistence_withOnlyHelperAndNonEmptyModel_shouldSucceed() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>(); // at least one note
        Map<String, String> item1 = new HashMap<>();
        item1.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        existingNotesData.add(item1);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .build();

        assertTrue(mi.existsInAnki());
        assertEquals(1, mi.getExistingNotesCount());
        assertEquals(0, mi.getExistingMarkedNotesCount());
    }

    @Test(expected = MusInterval.CreateDeckException.class)
    public void add_NoSuchDeckCantCreate() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        // model ok
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        // can't create deck for some reason
        doReturn(null).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(null).when(helper).addNewDeck(defaultDeckName);

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .build();

        mi.addToAnki();
    }

    @Test(expected = MusInterval.AddToAnkiException.class)
    @SuppressWarnings("unchecked")
    public void add_NoSuchDeck_CardShouldNotBeCreated() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        final String sound = "/path/to/file.m4a";
        final String newSound = "music_interval_12345.m4a";
        final String direction = MusInterval.Fields.Direction.ASC;
        final String timing = MusInterval.Fields.Timing.MELODIC;
        final String interval = "min2";
        final String tempo = "80";
        final String instrument = "guitar";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        // model ok
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        // create deck
        doReturn(null).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(deckId).when(helper).addNewDeck(defaultDeckName);
        doNothing().when(helper).storeDeckReference(defaultDeckName, deckId);

        doReturn(newSound).when(helper).addFileToAnkiMedia(sound);
        doReturn(null).when(helper).findNotes(eq(modelId), any(Map.class));
        doAnswer(new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocation) {
                // Check passed arguments
                Map<String, String> data = invocation.getArgument(2);
                assertTrue(data.containsKey(MusInterval.Fields.SOUND));
                assertEquals("[sound:" + newSound + "]", data.get(MusInterval.Fields.SOUND));
                assertTrue(data.containsKey(MusInterval.Fields.START_NOTE));
                assertEquals(defaultStartNote, data.get(MusInterval.Fields.START_NOTE));
                assertTrue(data.containsKey(MusInterval.Fields.DIRECTION));
                assertEquals(direction, data.get(MusInterval.Fields.DIRECTION));
                assertTrue(data.containsKey(MusInterval.Fields.TIMING));
                assertEquals(timing, data.get(MusInterval.Fields.TIMING));
                assertTrue(data.containsKey(MusInterval.Fields.INTERVAL));
                assertEquals(interval, data.get(MusInterval.Fields.INTERVAL));
                assertTrue(data.containsKey(MusInterval.Fields.TEMPO));
                assertEquals(tempo, data.get(MusInterval.Fields.TEMPO));
                assertTrue(data.containsKey(MusInterval.Fields.INSTRUMENT));
                assertEquals(instrument, data.get(MusInterval.Fields.INSTRUMENT));

                Set<String> tags = invocation.getArgument(3);
                assertNull(tags);

                // can't create note for some reason
                return null;
            }

        }).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sound(sound)
                .start_note(defaultStartNote)
                .direction(direction)
                .timing(timing)
                .interval(interval)
                .tempo(tempo)
                .instrument(instrument)
                .build();

        mi.addToAnki();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void add_NoSuchDeck_DeckShouldBeCreated() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final String sound = "/path/to/file.m4a";
        final String newSound = "music_interval_12345.m4a";
        final String direction = MusInterval.Fields.Direction.ASC;
        final String timing = MusInterval.Fields.Timing.MELODIC;
        final String interval = "min2";
        final String tempo = "80";
        final String instrument = "guitar";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        // model ok
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        // create deck
        doReturn(null).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(deckId).when(helper).addNewDeck(defaultDeckName);
        doNothing().when(helper).storeDeckReference(defaultDeckName, deckId);

        doReturn(newSound).when(helper).addFileToAnkiMedia(sound);

        doAnswer(new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocation) {
                // Check passed arguments
                Map<String, String> data = invocation.getArgument(2);
                assertTrue(data.containsKey(MusInterval.Fields.SOUND));
                assertEquals("[sound:" + newSound + "]", data.get(MusInterval.Fields.SOUND));
                assertTrue(data.containsKey(MusInterval.Fields.START_NOTE));
                assertEquals(defaultStartNote, data.get(MusInterval.Fields.START_NOTE));
                assertTrue(data.containsKey(MusInterval.Fields.DIRECTION));
                assertEquals(direction, data.get(MusInterval.Fields.DIRECTION));
                assertTrue(data.containsKey(MusInterval.Fields.TIMING));
                assertEquals(timing, data.get(MusInterval.Fields.TIMING));
                assertTrue(data.containsKey(MusInterval.Fields.INTERVAL));
                assertEquals(interval, data.get(MusInterval.Fields.INTERVAL));
                assertTrue(data.containsKey(MusInterval.Fields.TEMPO));
                assertEquals(tempo, data.get(MusInterval.Fields.TEMPO));
                assertTrue(data.containsKey(MusInterval.Fields.INSTRUMENT));
                assertEquals(instrument, data.get(MusInterval.Fields.INSTRUMENT));

                Set<String> tags = invocation.getArgument(3);
                assertNull(tags);

                // successful note creation
                return noteId;
            }

        }).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sound(sound)
                .start_note(defaultStartNote)
                .direction(direction)
                .timing(timing)
                .interval(interval)
                .tempo(tempo)
                .instrument(instrument)
                .build();

        mi.addToAnki(); // should not throw any exception
    }

    @Test
    @SuppressWarnings("unchecked")
    public void add_AllFieldsAreSet_NoteShouldBeCreated() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final String sound = "/path/to/file.m4a";
        final String newSound = "music_interval_12345.m4a";
        final String direction = MusInterval.Fields.Direction.ASC;
        final String timing = MusInterval.Fields.Timing.MELODIC;
        final String interval = "min3";
        final String tempo = "90";
        final String instrument = "violin";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        // existing model
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        // existing deck
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);

        doReturn(newSound).when(helper).addFileToAnkiMedia(sound);

        doAnswer(new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocation) {
                // Check passed arguments
                Map<String, String> data = invocation.getArgument(2);
                assertTrue(data.containsKey(MusInterval.Fields.SOUND));
                assertEquals("[sound:" + newSound + "]", data.get(MusInterval.Fields.SOUND));
                assertTrue(data.containsKey(MusInterval.Fields.START_NOTE));
                assertEquals(startNote2, data.get(MusInterval.Fields.START_NOTE));
                assertTrue(data.containsKey(MusInterval.Fields.DIRECTION));
                assertEquals(direction, data.get(MusInterval.Fields.DIRECTION));
                assertTrue(data.containsKey(MusInterval.Fields.TIMING));
                assertEquals(timing, data.get(MusInterval.Fields.TIMING));
                assertTrue(data.containsKey(MusInterval.Fields.INTERVAL));
                assertEquals(interval, data.get(MusInterval.Fields.INTERVAL));
                assertTrue(data.containsKey(MusInterval.Fields.TEMPO));
                assertEquals(tempo, data.get(MusInterval.Fields.TEMPO));
                assertTrue(data.containsKey(MusInterval.Fields.INSTRUMENT));
                assertEquals(instrument, data.get(MusInterval.Fields.INSTRUMENT));

                Set<String> tags = invocation.getArgument(3);
                assertNull(tags);

                // successful note creation
                return noteId;
            }

        }).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sound(sound)
                .start_note(startNote2)
                .direction(direction)
                .timing(timing)
                .interval(interval)
                .tempo(tempo)
                .instrument(instrument)
                .build();

        mi.addToAnki(); // should not throw any exception
    }

    @Test
    @SuppressWarnings("unchecked")
    public void add_AllFieldsAreSet_NewMusicIntervalShouldBeCreated() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final String sound = "/path/to/file.m4a";
        final String newSound = "music_interval_12345.m4a";
        final String direction = MusInterval.Fields.Direction.ASC;
        final String timing = MusInterval.Fields.Timing.MELODIC;
        final String interval = "min3";
        final String tempo = "90";
        final String instrument = "violin";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(newSound).when(helper).addFileToAnkiMedia(sound);
        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sound(sound)
                .start_note(startNote2)
                .direction(direction)
                .timing(timing)
                .interval(interval)
                .tempo(tempo)
                .instrument(instrument)
                .build();

        MusInterval mi2 = mi.addToAnki(); // should not throw any exception

        // everything should be the same, except "sound" field
        assertNotEquals(mi.sound, mi2.sound);
        assertEquals(mi.startNote, mi2.startNote);
        assertEquals(mi.direction, mi2.direction);
        assertEquals(mi.timing, mi2.timing);
        assertEquals(mi.interval, mi2.interval);
        assertEquals(mi.tempo, mi2.tempo);
        assertEquals(mi.instrument, mi2.instrument);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void add_AllFieldsAreSet_startNoteShouldBeFixedToUpperCase() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final String sound = "/path/to/file.m4a";
        final String newSound = "music_interval_12345.m4a";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(newSound).when(helper).addFileToAnkiMedia(sound);
        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sound(sound)
                .start_note(startNote2.toLowerCase())
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .interval("min3")
                .tempo("90")
                .instrument("violin")
                .build();

        MusInterval mi2 = mi.addToAnki(); // should not throw any exception

        // c#2 should be fixed to C#2
        assertEquals(mi.startNote.toUpperCase(), mi2.startNote);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void add_AllFieldsAreSet_SoundFieldShouldBeProper() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final String sound = "/path/to/file.m4a";
        final String newSound = "music_interval_12345.m4a";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(newSound).when(helper).addFileToAnkiMedia(sound);
        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sound(sound)
                .start_note(startNote2)
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .interval("min3")
                .tempo("90")
                .instrument("violin")
                .build();

        MusInterval mi2 = mi.addToAnki(); // should not throw any exception

        assertFalse(mi2.sound.isEmpty());
        assertNotEquals(sound, mi2.sound);
        assertTrue(mi2.sound.startsWith("[sound:"));
        assertTrue(mi2.sound.endsWith(".m4a]"));
        assertEquals("[sound:" + newSound + "]", mi2.sound);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void add_AllFieldsAreSet_SoundFieldShouldBeProper2() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final String sound = "/path/to/file.mp3";
        final String newSound = "music_interval_12345.mp3";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(newSound).when(helper).addFileToAnkiMedia(sound);
        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sound(sound)
                .start_note(startNote2)
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .interval("min3")
                .tempo("90")
                .instrument("violin")
                .build();

        MusInterval mi2 = mi.addToAnki(); // should not throw any exception

        assertFalse(mi2.sound.isEmpty());
        assertNotEquals(sound, mi2.sound);
        assertTrue(mi2.sound.startsWith("[sound:"));
        assertTrue(mi2.sound.endsWith(".mp3]"));
        assertEquals("[sound:" + newSound + "]", mi2.sound);
    }

    @Test(expected = MusInterval.MandatoryFieldEmptyException.class)
    public void add_NoSoundSpecified_ShouldFail() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sound("") // should not be empty on adding
                .build();

        mi.addToAnki(); // should throw exception
    }

    @Test(expected = MusInterval.MandatoryFieldEmptyException.class)
    public void add_NoStartNoteSpecified_ShouldFail() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sound("/path/to/file")
                .start_note("")// should not be empty on adding
                .build();

        mi.addToAnki(); // should throw exception
    }

    @Test(expected = MusInterval.MandatoryFieldEmptyException.class)
    public void add_NoIntervalSpecified_ShouldFail() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sound("/path/to/file")
                .start_note(defaultStartNote)
                .interval("") // should not be empty on adding
                .build();

        mi.addToAnki(); // should throw exception
    }

    @Test
    @SuppressWarnings("unchecked")
    public void add_TheSameSoundFileName2Times_shouldCreate2DifferentSoundFilesInAnki() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final String sound = "/path/to/file.mp3";
        final String newSound1 = "music_interval_12345.mp3";
        final String newSound2 = "music_interval_23456.mp3";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);

        doAnswer(new Answer<String>() {
            private int count = 0;

            @Override
            public String answer(InvocationOnMock invocation) {
                if (count++ == 1)
                    return newSound1;

                return newSound2;
            }
        }).when(helper).addFileToAnkiMedia(sound);

        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        MusInterval mi1 = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sound(sound)
                .start_note(startNote2)
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .interval("min3")
                .tempo("90")
                .instrument("violin")
                .build();

        MusInterval mi2 = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sound(sound)
                .start_note(startNote2)
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .interval("min3")
                .tempo("90")
                .instrument("violin")
                .build();

        MusInterval mi1_2 = mi1.addToAnki();
        MusInterval mi2_2 = mi2.addToAnki();

        assertNotEquals(mi1_2.sound, mi2_2.sound);
    }

    @Test(expected = MusInterval.SoundAlreadyAddedException.class)
    @SuppressWarnings("unchecked")
    public void add_SoundFieldContainsBrackets_shouldFail() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final String sound = "[sound:/path/to/file.mp3]";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sound(sound)
                .start_note(startNote2)
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .interval("min3")
                .tempo("90")
                .instrument("violin")
                .build();

        mi.addToAnki(); // should throw exception
    }

    @Test(expected = MusInterval.NoteNotExistsException.class)
    @SuppressWarnings("unchecked")
    public void markExistingNote_NoteNotExists() throws MusInterval.NoteNotExistsException, AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>(); // empty

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .start_note(defaultStartNote)
                .build();

        assertEquals(0, mi.getExistingNotesCount());
        assertEquals(0, mi.markExistingNotes());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void markExistingNote_MarkNoteFailure() throws MusInterval.NoteNotExistsException, AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final String direction = MusInterval.Fields.Direction.ASC;
        final String timing = MusInterval.Fields.Timing.MELODIC;
        final String interval = "min2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put("id", Long.toString(noteId));
        item1.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        item1.put(MusInterval.Fields.SOUND, "/test1");  // sound field does not matter
        item1.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.ASC);
        item1.put(MusInterval.Fields.TIMING, MusInterval.Fields.Timing.MELODIC);
        item1.put(MusInterval.Fields.INTERVAL, interval);
        item1.put(MusInterval.Fields.TEMPO, tempo);
        item1.put(MusInterval.Fields.INSTRUMENT, instrument);
        existingNotesData.add(item1);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map.class));

        // Marking failure
        doReturn(0).when(helper).addTagToNote(noteId, " marked ");

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .start_note(defaultStartNote)
                .direction(direction)
                .timing(timing)
                .interval(interval)
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertEquals(1, mi.getExistingNotesCount());
        assertEquals(0, mi.markExistingNotes());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void markExistingNote_MarkNoteSuccess() throws MusInterval.NoteNotExistsException, AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final String direction = MusInterval.Fields.Direction.ASC;
        final String timing = MusInterval.Fields.Timing.MELODIC;
        final String interval = "min2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put("id", Long.toString(noteId));
        item1.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        item1.put(MusInterval.Fields.SOUND, "/test1");  // sound field does not matter
        item1.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.ASC);
        item1.put(MusInterval.Fields.TIMING, MusInterval.Fields.Timing.MELODIC);
        item1.put(MusInterval.Fields.INTERVAL, interval);
        item1.put(MusInterval.Fields.TEMPO, tempo);
        item1.put(MusInterval.Fields.INSTRUMENT, instrument);
        existingNotesData.add(item1);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map.class));

        // Marked successfully
        doReturn(1).when(helper).addTagToNote(noteId, " marked ");

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .start_note(defaultStartNote)
                .direction(direction)
                .timing(timing)
                .interval(interval)
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertEquals(1, mi.getExistingNotesCount());
        assertEquals(1, mi.markExistingNotes());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void markExistingNote_MarkTwoNoteSuccess() throws MusInterval.NoteNotExistsException, AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        final long noteId1 = new Random().nextLong();
        final long noteId2 = new Random().nextLong();

        final String direction = MusInterval.Fields.Direction.ASC;
        final String timing = MusInterval.Fields.Timing.MELODIC;
        final String interval = "min2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put("id", Long.toString(noteId1));
        item1.put(MusInterval.Fields.SOUND, "/test1");  // sound field does not matter
        item1.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        item1.put(MusInterval.Fields.DIRECTION, direction);
        item1.put(MusInterval.Fields.TIMING, timing);
        item1.put(MusInterval.Fields.INTERVAL, interval);
        item1.put(MusInterval.Fields.TEMPO, tempo);
        item1.put(MusInterval.Fields.INSTRUMENT, instrument);
        existingNotesData.add(item1);
        Map<String, String> item2 = new HashMap<>();
        item2.put("id", Long.toString(noteId2));
        item2.put(MusInterval.Fields.SOUND, "/test2");  // sound field does not matter
        item2.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        item2.put(MusInterval.Fields.DIRECTION, direction);
        item2.put(MusInterval.Fields.TIMING, timing);
        item2.put(MusInterval.Fields.INTERVAL, interval);
        item2.put(MusInterval.Fields.TEMPO, tempo);
        item2.put(MusInterval.Fields.INSTRUMENT, instrument);
        item2.put("tags", " tag1 ");
        existingNotesData.add(item2);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map.class));

        // Marked successfully
        doReturn(1).when(helper).addTagToNote(noteId1, " marked ");
        doReturn(1).when(helper).addTagToNote(noteId2, " tag1 marked ");

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sound("")
                .start_note(defaultStartNote)
                .direction(direction)
                .timing(timing)
                .interval(interval)
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertEquals(2, mi.getExistingNotesCount());
        assertEquals(2, mi.markExistingNotes());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void markExistingNote_MarkNoteWithTagsSuccess() throws MusInterval.NoteNotExistsException, AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final String direction = MusInterval.Fields.Direction.ASC;
        final String timing = MusInterval.Fields.Timing.MELODIC;
        final String interval = "min2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put("id", Long.toString(noteId));
        item1.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        item1.put(MusInterval.Fields.SOUND, "/test1");  // sound field does not matter
        item1.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.ASC);
        item1.put(MusInterval.Fields.TIMING, MusInterval.Fields.Timing.MELODIC);
        item1.put(MusInterval.Fields.INTERVAL, interval);
        item1.put(MusInterval.Fields.TEMPO, tempo);
        item1.put(MusInterval.Fields.INSTRUMENT, instrument);
        item1.put("tags", " some tags benchmarked marked_as_red ");
        existingNotesData.add(item1);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map.class));

        // Marked successfully
        doReturn(1).when(helper).addTagToNote(noteId, " some tags benchmarked marked_as_red marked ");

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .start_note(defaultStartNote)
                .direction(direction)
                .timing(timing)
                .interval(interval)
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertEquals(1, mi.getExistingNotesCount());
        assertEquals(1, mi.markExistingNotes());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void markExistingNote_MarkAlreadyMarkedNoteSuccess() throws MusInterval.NoteNotExistsException, AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final String direction = MusInterval.Fields.Direction.ASC;
        final String timing = MusInterval.Fields.Timing.MELODIC;
        final String interval = "min2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put("id", Long.toString(noteId));
        item1.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        item1.put(MusInterval.Fields.SOUND, "/test1");  // sound field does not matter
        item1.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.ASC);
        item1.put(MusInterval.Fields.TIMING, MusInterval.Fields.Timing.MELODIC);
        item1.put(MusInterval.Fields.INTERVAL, interval);
        item1.put(MusInterval.Fields.TEMPO, tempo);
        item1.put(MusInterval.Fields.INSTRUMENT, instrument);
        item1.put("tags", " marked ");
        existingNotesData.add(item1);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map.class));

        // Marked successfully
        doReturn(1).when(helper).addTagToNote(noteId, " marked ");

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .start_note(defaultStartNote)
                .direction(direction)
                .timing(timing)
                .interval(interval)
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertEquals(1, mi.getExistingNotesCount());
        assertEquals(0, mi.markExistingNotes());
    }

    @Test(expected = NullPointerException.class)
    public void create_withNoHelper_shouldThrowException() throws MusInterval.ValidationException {
        new MusInterval.Builder(null).build();
    }

    @Test
    public void create_withOnlyHelper_shouldBeOk() throws MusInterval.ValidationException {
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(any(String.class));
        doReturn(null).when(helper).findDeckIdByName(any(String.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .build();

        assertNotEquals("", mi.modelName);
        assertNotEquals("", mi.deckName);
        assertEquals("", mi.sound);
        assertEquals("", mi.startNote);
        assertEquals("", mi.direction);
        assertEquals("", mi.timing);
        assertEquals("", mi.interval);
        assertEquals("", mi.tempo);
        assertEquals("", mi.instrument);
    }

    @Test
    public void create_withOnlyHelperAndModelAndDeck_shouldBeOk() throws MusInterval.ValidationException {
        final long modelId = new Random().nextLong();
        final String modelName = "Model name";
        final String deckName = "Deck name";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(modelName);
        doReturn(null).when(helper).findDeckIdByName(deckName);

        MusInterval mi = new MusInterval.Builder(helper)
                .model(modelName)
                .deck(deckName)
                .build();

        assertEquals(modelName, mi.modelName);
        assertEquals(deckName, mi.deckName);
    }

    @Test
    public void create_withOnlyHelperAndStartNote_shouldBeOk() throws MusInterval.ValidationException {
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(any(String.class));
        doReturn(null).when(helper).findDeckIdByName(any(String.class));

        final String startNote = "F4";

        MusInterval mi = new MusInterval.Builder(helper)
                .start_note(startNote)
                .build();

        assertEquals(startNote, mi.startNote);
    }

    @Test
    public void create_withOnlyHelperAndInterval_shouldBeOk() throws MusInterval.ValidationException {
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(any(String.class));
        doReturn(null).when(helper).findDeckIdByName(any(String.class));

        final String interval = "min2";

        MusInterval mi = new MusInterval.Builder(helper)
                .interval(interval)
                .build();

        assertEquals(interval, mi.interval);
    }

    @Test
    public void create_withAllFields_shouldBeOk() throws MusInterval.ValidationException {
        final long modelId = new Random().nextLong();
        final String modelName = "Model name";
        final String deckName = "Deck name";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(modelName);
        doReturn(null).when(helper).findDeckIdByName(deckName);

        final String sound = "/path/to/file";
        final String direction = MusInterval.Fields.Direction.ASC;
        final String timing = MusInterval.Fields.Timing.MELODIC;
        final String interval = "min2";
        final String tempo = "80";
        final String instrument = "guitar";

        MusInterval mi = new MusInterval.Builder(helper)
                .model(modelName)
                .deck(deckName)
                .sound(sound)
                .start_note(defaultStartNote)
                .direction(direction)
                .timing(timing)
                .interval(interval)
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertEquals(modelName, mi.modelName);
        assertEquals(deckName, mi.deckName);
        assertEquals(sound, mi.sound);
        assertEquals(defaultStartNote, mi.startNote);
        assertEquals(direction, mi.direction);
        assertEquals(timing, mi.timing);
        assertEquals(interval, mi.interval);
        assertEquals(tempo, mi.tempo);
        assertEquals(instrument, mi.instrument);
    }

    @Test
    public void create_MultipleBuilders_shouldNotAffectEachOther() throws MusInterval.ValidationException {
        final long modelId = new Random().nextLong();
        final String startNote1 = "C2";
        final String startNote2 = "C3";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(any(String.class));
        doReturn(null).when(helper).findDeckIdByName(any(String.class));

        MusInterval.Builder builder1 = new MusInterval.Builder(helper)
                .start_note(startNote1);

        MusInterval.Builder builder2 = new MusInterval.Builder(helper)
                .start_note(startNote2);

        MusInterval mi1 = builder1.build();
        MusInterval mi2 = builder2.build();

        assertEquals(startNote1, mi1.startNote);
        assertEquals(startNote2, mi2.startNote);
    }

    @Test(expected = MusInterval.StartNoteSyntaxException.class)
    public void create_InvalidNoteValue_shouldFail() throws MusInterval.ValidationException {
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(any(String.class));
        doReturn(null).when(helper).findDeckIdByName(any(String.class));

        final String startNote = "123"; // incorrect

        new MusInterval.Builder(helper)
                .start_note(startNote)
                .build();
    }

    @Test
    public void create_ValidNoteValue_shouldBeOk() throws MusInterval.ValidationException {
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(any(String.class));
        doReturn(null).when(helper).findDeckIdByName(any(String.class));

        final String startNote = " F7 "; // correct

        new MusInterval.Builder(helper)
                .start_note(startNote)
                .build();
    }

    @Test(expected = MusInterval.TempoValueException.class)
    public void create_InvalidTempo_shouldFail() throws MusInterval.ValidationException {
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(any(String.class));
        doReturn(null).when(helper).findDeckIdByName(any(String.class));

        final String tempo = "999999"; // incorrect

        new MusInterval.Builder(helper)
                .tempo(tempo)
                .build();
    }

    @Test(expected = NumberFormatException.class)
    public void create_NotNumericTempo_shouldFail() throws MusInterval.ValidationException {
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(any(String.class));
        doReturn(null).when(helper).findDeckIdByName(any(String.class));

        final String tempo = "asdf"; // incorrect

        new MusInterval.Builder(helper)
                .tempo(tempo)
                .build();
    }

    @Test
    public void create_CorrectTempo_shouldBeOk() throws MusInterval.ValidationException {
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(any(String.class));
        doReturn(null).when(helper).findDeckIdByName(any(String.class));

        String tempo = "80"; // correct

        new MusInterval.Builder(helper)
                .tempo(tempo)
                .build();

        tempo = "     90    "; // also should be correct

        new MusInterval.Builder(helper)
                .tempo(tempo)
                .build();
    }

    @Test
    public void add_SimilarIntervals_shouldCreateLinks() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return invocation.getArgument(0);
            }
        }).when(helper).addFileToAnkiMedia(any(String.class));
        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        final MusInterval[] musIntervals = new MusInterval[MusInterval.Fields.Interval.VALUES.length - 1];
        for (int i = 0; i < musIntervals.length; i++) {
            String interval = MusInterval.Fields.Interval.VALUES[i + 1];
            String sound = String.format("%s.mp3", interval);
            musIntervals[i] = new MusInterval.Builder(helper)
                    .model(defaultModelName)
                    .deck(defaultDeckName)
                    .sound(sound)
                    .start_note(defaultStartNote)
                    .direction(MusInterval.Fields.Direction.ASC)
                    .timing(MusInterval.Fields.Timing.MELODIC)
                    .interval(interval)
                    .tempo("90")
                    .instrument("violin")
                    .build();
        }

        final ArrayList<MusInterval> musIntervalsAdded = new ArrayList<>();

        doAnswer(new Answer<LinkedList<Map<String, String>>>() {
            @Override
            public LinkedList<Map<String, String>> answer(InvocationOnMock invocation) {
                Map<String, String> inputData = new HashMap<>((Map<String, String>) invocation.getArgument(1));
                LinkedList<Map<String, String>> result = new LinkedList<>();
                for (int i = 0; i < musIntervalsAdded.size(); i++) {
                    MusInterval mi = musIntervalsAdded.get(i);
                    String sound = mi.sound;
                    Map<String, String> data = mi.getCollectedData();
                    data.remove(MusInterval.Fields.SOUND);
                    data.remove(MusInterval.Fields.SOUND_SMALLER);
                    data.remove(MusInterval.Fields.SOUND_LARGER);
                    if (inputData.equals(data)) {
                        data.put(MusInterval.Fields.SOUND, sound);
                        data.put("id", String.valueOf(i));
                        result.add(data);
                    } else {
                        data.put(MusInterval.Fields.SOUND, sound);
                    }
                }
                return result;
            }
        }).when(helper).findNotes(eq(modelId), any(Map.class));

        doAnswer(new Answer() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                int idx = (int)(long) invocation.getArgument(1);
                Map<String, String> data = new HashMap<>((Map<String, String>) invocation.getArgument(2));
                MusInterval updated = new MusInterval.Builder(helper)
                        .model(defaultModelName)
                        .deck(defaultDeckName)
                        .sound(data.get(MusInterval.Fields.SOUND))
                        .sound_smaller(data.get(MusInterval.Fields.SOUND_SMALLER))
                        .sound_larger(data.get(MusInterval.Fields.SOUND_LARGER))
                        .start_note(data.get(MusInterval.Fields.START_NOTE))
                        .direction(data.get(MusInterval.Fields.DIRECTION))
                        .timing(data.get(MusInterval.Fields.TIMING))
                        .interval(data.get(MusInterval.Fields.INTERVAL))
                        .tempo(data.get(MusInterval.Fields.TEMPO))
                        .instrument(data.get(MusInterval.Fields.INSTRUMENT))
                        .build();
                musIntervalsAdded.set(idx, updated);
                return true;
            }
        }).when(helper).updateNote(eq(modelId), any(Long.class), any(Map.class));

        musIntervalsAdded.add(musIntervals[0].addToAnki());
        assertEquals("", musIntervalsAdded.get(0).soundSmaller);
        assertEquals("", musIntervalsAdded.get(0).soundLarger);
        for (int i = 1; i < musIntervals.length; i++) {
            musIntervalsAdded.add(musIntervals[i].addToAnki());
            assertEquals(musIntervalsAdded.get(i - 1).sound, musIntervalsAdded.get(i).soundSmaller);
            assertEquals(musIntervalsAdded.get(i).sound, musIntervalsAdded.get(i - 1).soundLarger);
        }
    }

    @Test
    public void add_DifferentIntervals_shouldNotCreateLinks() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return invocation.getArgument(0);
            }
        }).when(helper).addFileToAnkiMedia(any(String.class));
        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        final MusInterval[] musIntervals = new MusInterval[MusInterval.Fields.Interval.VALUES.length - 1];
        final LinkedList<Map<String, String>> findMockResult = new LinkedList<>();
        for (int i = 0; i < musIntervals.length; i++) {
            String interval = MusInterval.Fields.Interval.VALUES[i + 1];
            String sound = String.format("%s.mp3", interval);
            musIntervals[i] = new MusInterval.Builder(helper)
                    .model(defaultModelName)
                    .deck(defaultDeckName)
                    .sound(sound)
                    .start_note(defaultStartNote)
                    .direction(MusInterval.Fields.Direction.ASC)
                    .timing(MusInterval.Fields.Timing.MELODIC)
                    .interval(interval)
                    .tempo("90")
                    .instrument(String.format("instrument%d", i)) // different instruments
                    .build();
            findMockResult.add(musIntervals[i].getCollectedData(sound));
        }

        final ArrayList<MusInterval> musIntervalsAdded = new ArrayList<>();

        doAnswer(new Answer<LinkedList<Map<String, String>>>() {
            @Override
            public LinkedList<Map<String, String>> answer(InvocationOnMock invocation) {
                Map<String, String> inputData = new HashMap<>((Map<String, String>) invocation.getArgument(1));
                LinkedList<Map<String, String>> result = new LinkedList<>();
                for (int i = 0; i < musIntervalsAdded.size(); i++) {
                    MusInterval mi = musIntervalsAdded.get(i);
                    String sound = mi.sound;
                    Map<String, String> data = mi.getCollectedData();
                    data.remove(MusInterval.Fields.SOUND);
                    data.remove(MusInterval.Fields.SOUND_SMALLER);
                    data.remove(MusInterval.Fields.SOUND_LARGER);
                    if (inputData.equals(data)) {
                        data.put(MusInterval.Fields.SOUND, sound);
                        data.put("id", String.valueOf(i));
                        result.add(data);
                    } else {
                        data.put(MusInterval.Fields.SOUND, sound);
                    }
                }
                return result;
            }
        }).when(helper).findNotes(eq(modelId), any(Map.class));

        doAnswer(new Answer() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                int idx = (int)(long) invocation.getArgument(1);
                Map<String, String> data = new HashMap<>((Map<String, String>) invocation.getArgument(2));
                MusInterval updated = new MusInterval.Builder(helper)
                        .model(defaultModelName)
                        .deck(defaultDeckName)
                        .sound(data.get(MusInterval.Fields.SOUND))
                        .sound_smaller(data.get(MusInterval.Fields.SOUND_SMALLER))
                        .sound_larger(data.get(MusInterval.Fields.SOUND_LARGER))
                        .start_note(data.get(MusInterval.Fields.START_NOTE))
                        .direction(data.get(MusInterval.Fields.DIRECTION))
                        .timing(data.get(MusInterval.Fields.TIMING))
                        .interval(data.get(MusInterval.Fields.INTERVAL))
                        .tempo(data.get(MusInterval.Fields.TEMPO))
                        .instrument(data.get(MusInterval.Fields.INSTRUMENT))
                        .build();
                musIntervalsAdded.set(idx, updated);
                return true;
            }
        }).when(helper).updateNote(eq(modelId), any(Long.class), any(Map.class));

        for (int i = 0; i < musIntervals.length; i++) {
            musIntervalsAdded.add(musIntervals[i].addToAnki());
            assertEquals("", musIntervals[i].soundSmaller);
            assertEquals("", musIntervals[i].soundLarger);
        }
    }

    @Test
    public void add_SimilarIntervalToDuplicates_shouldCreateLinkToLatest() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return invocation.getArgument(0);
            }
        }).when(helper).addFileToAnkiMedia(any(String.class));
        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        final String interval = MusInterval.Fields.Interval.VALUES[2];
        final String intervalSmaller = MusInterval.Fields.Interval.VALUES[1];
        final String intervalLarger = MusInterval.Fields.Interval.VALUES[3];

        final MusInterval[] musIntervals = new MusInterval[2];
        for (int i = 0; i < musIntervals.length; i++) {
            String sound = String.format("musInterval%d.mp3", i);
            musIntervals[i] = new MusInterval.Builder(helper)
                    .model(defaultModelName)
                    .deck(defaultDeckName)
                    .sound(sound)
                    .start_note(defaultStartNote)
                    .direction(MusInterval.Fields.Direction.ASC)
                    .timing(MusInterval.Fields.Timing.MELODIC)
                    .interval(interval)
                    .tempo("90")
                    .instrument("violin")
                    .build();
        }
        final MusInterval musIntervalSmaller = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sound("intervalSmaller.mp3")
                .start_note(defaultStartNote)
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .interval(intervalSmaller)
                .tempo("90")
                .instrument("violin")
                .build();
        final MusInterval musIntervalLarger = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sound("intervalLarger.mp3")
                .start_note(defaultStartNote)
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .interval(intervalLarger)
                .tempo("90")
                .instrument("violin")
                .build();

        final LinkedList<MusInterval> musIntervalsAdded = new LinkedList<>();

        doAnswer(new Answer<LinkedList<Map<String, String>>>() {
            @Override
            public LinkedList<Map<String, String>> answer(InvocationOnMock invocation) {
                Map<String, String> inputData = new HashMap<>((Map<String, String>) invocation.getArgument(1));
                LinkedList<Map<String, String>> result = new LinkedList<>();
                for (int i = 0; i < musIntervalsAdded.size(); i++) {
                    MusInterval mi = musIntervalsAdded.get(i);
                    String sound = mi.sound;
                    Map<String, String> data = mi.getCollectedData();
                    data.remove(MusInterval.Fields.SOUND);
                    data.remove(MusInterval.Fields.SOUND_SMALLER);
                    data.remove(MusInterval.Fields.SOUND_LARGER);
                    if (inputData.equals(data)) {
                        data.put(MusInterval.Fields.SOUND, sound);
                        data.put("id", String.valueOf(i));
                        result.add(data);
                    } else {
                        data.put(MusInterval.Fields.SOUND, sound);
                    }
                }
                return result;
            }
        }).when(helper).findNotes(eq(modelId), any(Map.class));

        doAnswer(new Answer() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                int idx = (int)(long) invocation.getArgument(1);
                Map<String, String> data = new HashMap<>((Map<String, String>) invocation.getArgument(2));
                MusInterval updated = new MusInterval.Builder(helper)
                        .model(defaultModelName)
                        .deck(defaultDeckName)
                        .sound(data.get(MusInterval.Fields.SOUND))
                        .sound_smaller(data.get(MusInterval.Fields.SOUND_SMALLER))
                        .sound_larger(data.get(MusInterval.Fields.SOUND_LARGER))
                        .start_note(data.get(MusInterval.Fields.START_NOTE))
                        .direction(data.get(MusInterval.Fields.DIRECTION))
                        .timing(data.get(MusInterval.Fields.TIMING))
                        .interval(data.get(MusInterval.Fields.INTERVAL))
                        .tempo(data.get(MusInterval.Fields.TEMPO))
                        .instrument(data.get(MusInterval.Fields.INSTRUMENT))
                        .build();
                musIntervalsAdded.set(idx, updated);
                return true;
            }
        }).when(helper).updateNote(eq(modelId), any(Long.class), any(Map.class));

        for (int i = 0; i < musIntervals.length; i++) {
            musIntervalsAdded.add(musIntervals[i].addToAnki());
        }
        MusInterval musIntervalSmallerAdded = musIntervalSmaller.addToAnki();
        assertEquals(musIntervalsAdded.getLast().sound, musIntervalSmallerAdded.soundLarger);
        MusInterval musIntervalLargerAdded = musIntervalLarger.addToAnki();
        assertEquals(musIntervalsAdded.getLast().sound, musIntervalLargerAdded.soundSmaller);
    }

    @Test
    public void add_DuplicateSimilarInterval_shouldUpdateLink() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return invocation.getArgument(0);
            }
        }).when(helper).addFileToAnkiMedia(any(String.class));
        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        final String interval = MusInterval.Fields.Interval.VALUES[2];
        final String intervalSmaller = MusInterval.Fields.Interval.VALUES[1];
        final String intervalLarger = MusInterval.Fields.Interval.VALUES[3];

        final MusInterval musInterval = new MusInterval.Builder(helper)
                    .model(defaultModelName)
                    .deck(defaultDeckName)
                    .sound("musInterval.mp3")
                    .start_note(defaultStartNote)
                    .direction(MusInterval.Fields.Direction.ASC)
                    .timing(MusInterval.Fields.Timing.MELODIC)
                    .interval(interval)
                    .tempo("90")
                    .instrument("violin")
                    .build();
        final MusInterval[] musIntervalsSmaller = new MusInterval[2];
        for (int i = 0; i < musIntervalsSmaller.length; i++) {
            String sound = String.format("musIntervalSmaller%d.mp3", i);
            musIntervalsSmaller[i] = new MusInterval.Builder(helper)
                    .model(defaultModelName)
                    .deck(defaultDeckName)
                    .sound(sound)
                    .start_note(defaultStartNote)
                    .direction(MusInterval.Fields.Direction.ASC)
                    .timing(MusInterval.Fields.Timing.MELODIC)
                    .interval(intervalSmaller)
                    .tempo("90")
                    .instrument("violin")
                    .build();
        }
        final MusInterval[] musIntervalsLarger = new MusInterval[2];
        for (int i = 0; i < musIntervalsLarger.length; i++) {
            String sound = String.format("musIntervalLarger%d", i);
            musIntervalsLarger[i] = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sound(sound)
                .start_note(defaultStartNote)
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .interval(intervalLarger)
                .tempo("90")
                .instrument("violin")
                .build();}

        final LinkedList<MusInterval> musIntervalsAdded = new LinkedList<>();

        doAnswer(new Answer<LinkedList<Map<String, String>>>() {
            @Override
            public LinkedList<Map<String, String>> answer(InvocationOnMock invocation) {
                Map<String, String> inputData = new HashMap<>((Map<String, String>) invocation.getArgument(1));
                LinkedList<Map<String, String>> result = new LinkedList<>();
                for (int i = 0; i < musIntervalsAdded.size(); i++) {
                    MusInterval mi = musIntervalsAdded.get(i);
                    String sound = mi.sound;
                    Map<String, String> data = mi.getCollectedData();
                    data.remove(MusInterval.Fields.SOUND);
                    data.remove(MusInterval.Fields.SOUND_SMALLER);
                    data.remove(MusInterval.Fields.SOUND_LARGER);
                    if (inputData.equals(data)) {
                        data.put(MusInterval.Fields.SOUND, sound);
                        data.put("id", String.valueOf(i));
                        result.add(data);
                    } else {
                        data.put(MusInterval.Fields.SOUND, sound);
                    }
                }
                return result;
            }
        }).when(helper).findNotes(eq(modelId), any(Map.class));

        doAnswer(new Answer() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                int idx = (int)(long) invocation.getArgument(1);
                Map<String, String> data = new HashMap<>((Map<String, String>) invocation.getArgument(2));
                MusInterval updated = new MusInterval.Builder(helper)
                        .model(defaultModelName)
                        .deck(defaultDeckName)
                        .sound(data.get(MusInterval.Fields.SOUND))
                        .sound_smaller(data.get(MusInterval.Fields.SOUND_SMALLER))
                        .sound_larger(data.get(MusInterval.Fields.SOUND_LARGER))
                        .start_note(data.get(MusInterval.Fields.START_NOTE))
                        .direction(data.get(MusInterval.Fields.DIRECTION))
                        .timing(data.get(MusInterval.Fields.TIMING))
                        .interval(data.get(MusInterval.Fields.INTERVAL))
                        .tempo(data.get(MusInterval.Fields.TEMPO))
                        .instrument(data.get(MusInterval.Fields.INSTRUMENT))
                        .build();
                musIntervalsAdded.set(idx, updated);
                return true;
            }
        }).when(helper).updateNote(eq(modelId), any(Long.class), any(Map.class));


        musIntervalsAdded.add(musInterval.addToAnki());
        for (int i = 0; i < musIntervalsSmaller.length; i++) {
            MusInterval musIntervalSmallerAdded = musIntervalsSmaller[i].addToAnki();
            assertEquals(musIntervalSmallerAdded.sound, musIntervalsAdded.getFirst().soundSmaller);
        }
        for (int i = 0; i < musIntervalsLarger.length; i++) {
            MusInterval musIntervalLargerAdded = musIntervalsLarger[i].addToAnki();
            assertEquals(musIntervalLargerAdded.sound, musIntervalsAdded.getFirst().soundLarger);
        }
    }
}
