package com.ichi2.apisample;

import org.junit.Test;
import org.mockito.internal.stubbing.answers.ThrowsExceptionClass;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
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

    final static String[] ALL_NOTES = new String[]{
            "C", "C#",
            "D", "D#",
            "E",
            "F", "F#",
            "G", "G#",
            "A", "A#",
            "B",
    };
    final static String[] ALL_OCTAVES = new String[]{"1", "2", "3", "4", "5", "6"};

    final static String defaultNote = ALL_NOTES[1]; // C#
    final static String defaultOctave = ALL_NOTES[2]; // 3
    final static String defaultStartNote = defaultNote + defaultOctave; // C#3
    final static String note2 = ALL_NOTES[1]; // C#
    final static String octave2 = ALL_NOTES[1]; // 2
    final static String startNote2 = note2 + octave2; // C#2
    final static String intervalMin3 = MusInterval.Fields.Interval.VALUES[3]; //m3

    @Test
    @SuppressWarnings("unchecked")
    public void checkExistence_NoSuchStartingNote() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map[].class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .intervals(new String[]{intervalMin3})
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

        final String interval = "m2";
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
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map[].class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{interval})
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

        final String interval = "m2";
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
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map[].class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{interval})
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertTrue(mi.existsInAnki());
        assertEquals(1, mi.getExistingNotesCount());
        assertEquals(1, mi.getExistingMarkedNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkExistence_StartingNoteExistsIgnoreSpaces() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        final String interval = "m2";
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
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map[].class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{interval})
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

        final String interval = "m2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map[].class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{interval})
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

        final String interval = "m2";
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
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map[].class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{"/test2"})
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{interval})
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertTrue(mi.existsInAnki());
        assertEquals(1, mi.getExistingNotesCount());
        assertEquals(0, mi.getExistingMarkedNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkExistence_EmptyModel_shouldFail() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>(); // no notes at all

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map[].class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(ALL_NOTES)
                .octaves(ALL_OCTAVES)
                .intervals(MusInterval.Fields.Interval.VALUES)
                .build();

        assertFalse(mi.existsInAnki());
        assertEquals(0, mi.getExistingNotesCount());
        assertEquals(0, mi.getExistingMarkedNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkExistence_NonEmptyModel_shouldSucceed() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>(); // at least one note
        Map<String, String> item1 = new HashMap<>();
        item1.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        existingNotesData.add(item1);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map[].class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(ALL_NOTES)
                .octaves(ALL_OCTAVES)
                .intervals(MusInterval.Fields.Interval.VALUES)
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
                .notes(ALL_NOTES)
                .octaves(ALL_OCTAVES)
                .intervals(MusInterval.Fields.Interval.VALUES)
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
        final String interval = "m2";
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
                .sounds(new String[]{sound})
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(direction)
                .timing(timing)
                .intervals(new String[]{interval})
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
        final String interval = "m2";
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
                .sounds(new String[]{sound})
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(direction)
                .timing(timing)
                .intervals(new String[]{interval})
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
        final String interval = "m3";
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
                .sounds(new String[]{sound})
                .notes(new String[]{note2})
                .octaves(new String[]{octave2})
                .direction(direction)
                .timing(timing)
                .intervals(new String[]{interval})
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
        final String interval = "m3";
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
                .sounds(new String[]{sound})
                .notes(new String[]{note2})
                .octaves(new String[]{octave2})
                .direction(direction)
                .timing(timing)
                .intervals(new String[]{interval})
                .tempo(tempo)
                .instrument(instrument)
                .build();

        MusInterval mi2 = mi.addToAnki(); // should not throw any exception

        // everything should be the same, except "sound" field
        assertFalse(Arrays.equals(mi.sounds, mi2.sounds));
        assertArrayEquals(mi.notes, mi2.notes);
        assertArrayEquals(mi.octaves, mi2.octaves);
        assertEquals(mi.direction, mi2.direction);
        assertEquals(mi.timing, mi2.timing);
        assertArrayEquals(mi.intervals, mi2.intervals);
        assertEquals(mi.tempo, mi2.tempo);
        assertEquals(mi.instrument, mi2.instrument);
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
                .sounds(new String[]{sound})
                .notes(new String[]{note2})
                .octaves(new String[]{octave2})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{intervalMin3})
                .tempo("90")
                .instrument("violin")
                .build();

        MusInterval mi2 = mi.addToAnki(); // should not throw any exception

        assertNotEquals(0, mi2.sounds.length);
        assertFalse(Arrays.equals(new String[]{sound}, mi2.sounds));
        String addedSound = mi2.sounds[0];
        assertTrue(addedSound.startsWith("[sound:"));
        assertTrue(addedSound.endsWith(".m4a]"));
        assertEquals("[sound:" + newSound + "]", addedSound);
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
                .sounds(new String[]{sound})
                .notes(new String[]{note2})
                .octaves(new String[]{octave2})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{intervalMin3})
                .tempo("90")
                .instrument("violin")
                .build();

        MusInterval mi2 = mi.addToAnki(); // should not throw any exception

        assertNotEquals(0, mi2.sounds.length);
        assertFalse(Arrays.equals(new String[]{sound}, mi2.sounds));
        String addedSound = mi2.sounds[0];
        assertTrue(addedSound.startsWith("[sound:"));
        assertTrue(addedSound.endsWith(".mp3]"));
        assertEquals("[sound:" + newSound + "]", addedSound);
    }

    @Test(expected = MusInterval.UnexpectedSoundsAmountException.class)
    public void add_NoSoundSpecified_ShouldFail() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{}) // should not be empty on adding
                .notes(ALL_NOTES)
                .octaves(ALL_OCTAVES)
                .intervals(MusInterval.Fields.Interval.VALUES)
                .build();

        mi.addToAnki(); // should throw exception
    }

    @Test(expected = MusInterval.NoteNotSelectedException.class)
    public void create_NoNoteSpecified_ShouldFail() throws MusInterval.Exception {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{"/path/to/file"})
                .notes(new String[]{})
                .build(); // should throw exception
    }

    @Test(expected = MusInterval.OctaveNotSelectedException.class)
    public void create_NoOctaveSpecified_ShouldFail() throws MusInterval.Exception {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{"/path/to/file"})
                .notes(new String[]{defaultNote})
                .octaves(new String[]{})
                .build(); // should throw exception
    }

    @Test(expected = MusInterval.IntervalNotSelectedException.class)
    public void create_NoIntervalSpecified_ShouldFail() throws MusInterval.Exception {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{"/path/to/file"})
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .intervals(new String[]{}) // should throw exception
                .build();
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
                .sounds(new String[]{sound})
                .notes(new String[]{note2})
                .octaves(new String[]{octave2})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{intervalMin3})
                .tempo("90")
                .instrument("violin")
                .build();

        MusInterval mi2 = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{sound})
                .notes(new String[]{note2})
                .octaves(new String[]{octave2})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{intervalMin3})
                .tempo("90")
                .instrument("violin")
                .build();

        MusInterval mi1_2 = mi1.addToAnki();
        MusInterval mi2_2 = mi2.addToAnki();

        assertFalse(Arrays.equals(mi1_2.sounds, mi2_2.sounds));
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
                .sounds(new String[]{sound})
                .notes(new String[]{note2})
                .octaves(new String[]{octave2})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{intervalMin3})
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
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map[].class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .intervals(new String[]{intervalMin3})
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
        final String interval = "m2";
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
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map[].class));

        // Marking failure
        doReturn(0).when(helper).addTagToNote(noteId, " marked ");

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(direction)
                .timing(timing)
                .intervals(new String[]{interval})
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
        final String interval = "m2";
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
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map[].class));

        // Marked successfully
        doReturn(1).when(helper).addTagToNote(noteId, " marked ");

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(direction)
                .timing(timing)
                .intervals(new String[]{interval})
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
        final String interval = "m2";
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
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map[].class));

        // Marked successfully
        doReturn(1).when(helper).addTagToNote(noteId1, " marked ");
        doReturn(1).when(helper).addTagToNote(noteId2, " tag1 marked ");

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{})
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(direction)
                .timing(timing)
                .intervals(new String[]{interval})
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
        final String interval = "m2";
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
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map[].class));

        // Marked successfully
        doReturn(1).when(helper).addTagToNote(noteId, " some tags benchmarked marked_as_red marked ");

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(direction)
                .timing(timing)
                .intervals(new String[]{interval})
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
        final String interval = "m2";
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
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(Map[].class));

        // Marked successfully
        doReturn(1).when(helper).addTagToNote(noteId, " marked ");

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(direction)
                .timing(timing)
                .intervals(new String[]{interval})
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
    public void create_withOnlyHelperAndNoteAndOctaveAndInterval_shouldBeOk() throws MusInterval.ValidationException {
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(any(String.class));
        doReturn(null).when(helper).findDeckIdByName(any(String.class));

        final String[] notes = new String[]{"F"};
        final String[] octaves = new String[]{"4"};
        final String[] intervals = new String[]{"TT"};

        MusInterval mi = new MusInterval.Builder(helper)
                .notes(notes)
                .octaves(octaves)
                .intervals(intervals)
                .build();

        assertArrayEquals(notes, mi.notes);
        assertArrayEquals(octaves, mi.octaves);
        assertArrayEquals(intervals, mi.intervals);
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
        final String interval = "m2";
        final String tempo = "80";
        final String instrument = "guitar";

        MusInterval mi = new MusInterval.Builder(helper)
                .model(modelName)
                .deck(deckName)
                .sounds(new String[]{sound})
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(direction)
                .timing(timing)
                .intervals(new String[]{interval})
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertEquals(modelName, mi.modelName);
        assertEquals(deckName, mi.deckName);
        assertArrayEquals(new String[]{sound}, mi.sounds);
        assertArrayEquals(new String[]{defaultNote}, mi.notes);
        assertArrayEquals(new String[]{defaultOctave}, mi.octaves);
        assertEquals(direction, mi.direction);
        assertEquals(timing, mi.timing);
        assertArrayEquals(new String[]{interval}, mi.intervals);
        assertEquals(tempo, mi.tempo);
        assertEquals(instrument, mi.instrument);
    }

    @Test
    public void create_MultipleBuilders_shouldNotAffectEachOther() throws MusInterval.ValidationException {
        final long modelId = new Random().nextLong();
        final String[] notes1 = new String[]{"C"};
        final String[] octaves1 = new String[]{"2"};
        final String[] intervals1 = new String[]{"m2"};
        final String[] notes2 = new String[]{"C"};
        final String[] octaves2 = new String[]{"3"};
        final String[] intervals2 = new String[]{"m3"};

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(any(String.class));
        doReturn(null).when(helper).findDeckIdByName(any(String.class));

        MusInterval.Builder builder1 = new MusInterval.Builder(helper)
                .notes(notes1)
                .octaves(octaves1)
                .intervals(intervals1);

        MusInterval.Builder builder2 = new MusInterval.Builder(helper)
                .notes(notes2)
                .octaves(octaves2)
                .intervals(intervals2);

        MusInterval mi1 = builder1.build();
        MusInterval mi2 = builder2.build();

        assertArrayEquals(notes1, mi1.notes);
        assertArrayEquals(octaves1, mi1.octaves);
        assertArrayEquals(intervals1, mi1.intervals);
        assertArrayEquals(notes2, mi2.notes);
        assertArrayEquals(octaves2, mi2.octaves);
        assertArrayEquals(intervals2, mi2.intervals);
    }

    @Test(expected = MusInterval.TempoValueException.class)
    public void create_InvalidTempo_shouldFail() throws MusInterval.ValidationException {
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(any(String.class));
        doReturn(null).when(helper).findDeckIdByName(any(String.class));

        final String tempo = "999999"; // incorrect

        new MusInterval.Builder(helper)
                .notes(ALL_NOTES)
                .octaves(ALL_OCTAVES)
                .intervals(MusInterval.Fields.Interval.VALUES)
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
                .notes(ALL_NOTES)
                .octaves(ALL_OCTAVES)
                .intervals(MusInterval.Fields.Interval.VALUES)
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
                .notes(ALL_NOTES)
                .octaves(ALL_OCTAVES)
                .intervals(MusInterval.Fields.Interval.VALUES)

                .tempo(tempo)
                .build();

        tempo = "     90    "; // also should be correct

        new MusInterval.Builder(helper)
                .notes(ALL_NOTES)
                .octaves(ALL_OCTAVES)
                .intervals(MusInterval.Fields.Interval.VALUES)
                .tempo(tempo)
                .build();
    }

    @Test
    @SuppressWarnings("unchecked")
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

        final MusInterval[] musIntervals = new MusInterval[MusInterval.Fields.Interval.VALUES.length];
        for (int i = 0; i < musIntervals.length; i++) {
            String interval = MusInterval.Fields.Interval.VALUES[i];
            String sound = String.format("%s.mp3", interval);
            musIntervals[i] = new MusInterval.Builder(helper)
                    .model(defaultModelName)
                    .deck(defaultDeckName)
                    .sounds(new String[]{sound})
                    .notes(new String[]{defaultNote})
                    .octaves(new String[]{defaultOctave})
                    .direction(MusInterval.Fields.Direction.ASC)
                    .timing(MusInterval.Fields.Timing.MELODIC)
                    .intervals(new String[]{interval})
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
                    String sound = mi.sounds[0];
                    Map<String, String> data;
                    try {
                        data = mi.getCollectedDataSet()[0];
                    } catch (Throwable e) {
                        data = new HashMap<>();
                    }
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

        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                int idx = (int) (long) invocation.getArgument(1);
                Map<String, String> data = new HashMap<>((Map<String, String>) invocation.getArgument(2));
                String startNote = data.get(MusInterval.Fields.START_NOTE);
                String note = startNote.substring(0, startNote.length() - 1);
                String octave = String.valueOf(startNote.charAt(startNote.length() - 1));
                MusInterval updated = new MusInterval.Builder(helper)
                        .model(defaultModelName)
                        .deck(defaultDeckName)
                        .sounds(new String[]{data.get(MusInterval.Fields.SOUND)})
                        .sounds_smaller(new String[]{data.get(MusInterval.Fields.SOUND_SMALLER)})
                        .sounds_larger(new String[]{data.get(MusInterval.Fields.SOUND_LARGER)})
                        .notes(new String[]{note})
                        .octaves(new String[]{octave})
                        .direction(data.get(MusInterval.Fields.DIRECTION))
                        .timing(data.get(MusInterval.Fields.TIMING))
                        .intervals(new String[]{data.get(MusInterval.Fields.INTERVAL)})
                        .tempo(data.get(MusInterval.Fields.TEMPO))
                        .instrument(data.get(MusInterval.Fields.INSTRUMENT))
                        .build();
                musIntervalsAdded.set(idx, updated);
                return true;
            }
        }).when(helper).updateNote(eq(modelId), any(Long.class), any(Map.class));

        musIntervalsAdded.add(musIntervals[0].addToAnki());
        assertArrayEquals(new String[]{""}, musIntervalsAdded.get(0).soundsSmaller);
        assertArrayEquals(new String[]{""}, musIntervalsAdded.get(0).soundsLarger);
        for (int i = 1; i < musIntervals.length; i++) {
            musIntervalsAdded.add(musIntervals[i].addToAnki());
            assertArrayEquals(musIntervalsAdded.get(i - 1).sounds, musIntervalsAdded.get(i).soundsSmaller);
            assertArrayEquals(musIntervalsAdded.get(i).sounds, musIntervalsAdded.get(i - 1).soundsLarger);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
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

        final MusInterval[] musIntervals = new MusInterval[MusInterval.Fields.Interval.VALUES.length];
        for (int i = 0; i < musIntervals.length; i++) {
            String interval = MusInterval.Fields.Interval.VALUES[i];
            String sound = String.format("%s.mp3", interval);
            musIntervals[i] = new MusInterval.Builder(helper)
                    .model(defaultModelName)
                    .deck(defaultDeckName)
                    .sounds(new String[]{sound})
                    .notes(new String[]{defaultNote})
                    .octaves(new String[]{defaultOctave})
                    .direction(MusInterval.Fields.Direction.ASC)
                    .timing(MusInterval.Fields.Timing.MELODIC)
                    .intervals(new String[]{interval})
                    .tempo("90")
                    .instrument(String.format("instrument%d", i)) // different instruments
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
                    String sound = mi.sounds[0];
                    Map<String, String> data;
                    try {
                        data = mi.getCollectedDataSet()[0];
                    } catch (Throwable e) {
                        data = new HashMap<>();
                    }
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

        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                int idx = (int) (long) invocation.getArgument(1);
                Map<String, String> data = new HashMap<>((Map<String, String>) invocation.getArgument(2));
                String startNote = data.get(MusInterval.Fields.START_NOTE);
                String note = startNote.substring(0, startNote.length() - 1);
                String octave = String.valueOf(startNote.charAt(startNote.length() - 1));
                MusInterval updated = new MusInterval.Builder(helper)
                        .model(defaultModelName)
                        .deck(defaultDeckName)
                        .sounds(new String[]{data.get(MusInterval.Fields.SOUND)})
                        .sounds_smaller(new String[]{data.get(MusInterval.Fields.SOUND_SMALLER)})
                        .sounds_larger(new String[]{data.get(MusInterval.Fields.SOUND_LARGER)})
                        .notes(new String[]{note})
                        .octaves(new String[]{octave})
                        .direction(data.get(MusInterval.Fields.DIRECTION))
                        .timing(data.get(MusInterval.Fields.TIMING))
                        .intervals(new String[]{data.get(MusInterval.Fields.INTERVAL)})
                        .tempo(data.get(MusInterval.Fields.TEMPO))
                        .instrument(data.get(MusInterval.Fields.INSTRUMENT))
                        .build();
                musIntervalsAdded.set(idx, updated);
                return true;
            }
        }).when(helper).updateNote(eq(modelId), any(Long.class), any(Map.class));

        for (MusInterval musInterval : musIntervals) {
            musIntervalsAdded.add(musInterval.addToAnki());
            assertArrayEquals(new String[]{}, musInterval.soundsSmaller);
            assertArrayEquals(new String[]{}, musInterval.soundsLarger);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
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
                    .sounds(new String[]{sound})
                    .notes(new String[]{defaultNote})
                    .octaves(new String[]{defaultOctave})
                    .direction(MusInterval.Fields.Direction.ASC)
                    .timing(MusInterval.Fields.Timing.MELODIC)
                    .intervals(new String[]{interval})
                    .tempo("90")
                    .instrument("violin")
                    .build();
        }
        final MusInterval musIntervalSmaller = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{"intervalSmaller.mp3"})
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{intervalSmaller})
                .tempo("90")
                .instrument("violin")
                .build();
        final MusInterval musIntervalLarger = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{"intervalLarger.mp3"})
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{intervalLarger})
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
                    String sound = mi.sounds[0];
                    Map<String, String> data;
                    try {
                        data = mi.getCollectedDataSet()[0];
                    } catch (Throwable e) {
                        data = new HashMap<>();
                    }
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

        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                int idx = (int) (long) invocation.getArgument(1);
                Map<String, String> data = new HashMap<>((Map<String, String>) invocation.getArgument(2));
                String startNote = data.get(MusInterval.Fields.START_NOTE);
                String note = startNote.substring(0, startNote.length() - 1);
                String octave = String.valueOf(startNote.charAt(startNote.length() - 1));
                MusInterval updated = new MusInterval.Builder(helper)
                        .model(defaultModelName)
                        .deck(defaultDeckName)
                        .sounds(new String[]{data.get(MusInterval.Fields.SOUND)})
                        .sounds_smaller(new String[]{data.get(MusInterval.Fields.SOUND_SMALLER)})
                        .sounds_larger(new String[]{data.get(MusInterval.Fields.SOUND_LARGER)})
                        .notes(new String[]{note})
                        .octaves(new String[]{octave})
                        .direction(data.get(MusInterval.Fields.DIRECTION))
                        .timing(data.get(MusInterval.Fields.TIMING))
                        .intervals(new String[]{data.get(MusInterval.Fields.INTERVAL)})
                        .tempo(data.get(MusInterval.Fields.TEMPO))
                        .instrument(data.get(MusInterval.Fields.INSTRUMENT))
                        .build();
                musIntervalsAdded.set(idx, updated);
                return true;
            }
        }).when(helper).updateNote(eq(modelId), any(Long.class), any(Map.class));

        for (MusInterval musInterval : musIntervals) {
            musIntervalsAdded.add(musInterval.addToAnki());
        }
        MusInterval musIntervalSmallerAdded = musIntervalSmaller.addToAnki();
        assertArrayEquals(musIntervalsAdded.getLast().sounds, musIntervalSmallerAdded.soundsLarger);
        MusInterval musIntervalLargerAdded = musIntervalLarger.addToAnki();
        assertArrayEquals(musIntervalsAdded.getLast().sounds, musIntervalLargerAdded.soundsSmaller);
    }

    @Test
    @SuppressWarnings("unchecked")
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
                .sounds(new String[]{"musInterval.mp3"})
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{interval})
                .tempo("90")
                .instrument("violin")
                .build();
        final MusInterval[] musIntervalsSmaller = new MusInterval[2];
        for (int i = 0; i < musIntervalsSmaller.length; i++) {
            String sound = String.format("musIntervalSmaller%d.mp3", i);
            musIntervalsSmaller[i] = new MusInterval.Builder(helper)
                    .model(defaultModelName)
                    .deck(defaultDeckName)
                    .sounds(new String[]{sound})
                    .notes(new String[]{defaultNote})
                    .octaves(new String[]{defaultOctave})
                    .direction(MusInterval.Fields.Direction.ASC)
                    .timing(MusInterval.Fields.Timing.MELODIC)
                    .intervals(new String[]{intervalSmaller})
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
                    .sounds(new String[]{sound})
                    .notes(new String[]{defaultNote})
                    .octaves(new String[]{defaultOctave})
                    .direction(MusInterval.Fields.Direction.ASC)
                    .timing(MusInterval.Fields.Timing.MELODIC)
                    .intervals(new String[]{intervalLarger})
                    .tempo("90")
                    .instrument("violin")
                    .build();
        }

        final LinkedList<MusInterval> musIntervalsAdded = new LinkedList<>();

        doAnswer(new Answer<LinkedList<Map<String, String>>>() {
            @Override
            public LinkedList<Map<String, String>> answer(InvocationOnMock invocation) {
                Map<String, String> inputData = new HashMap<>((Map<String, String>) invocation.getArgument(1));
                LinkedList<Map<String, String>> result = new LinkedList<>();
                for (int i = 0; i < musIntervalsAdded.size(); i++) {
                    MusInterval mi = musIntervalsAdded.get(i);
                    String sound = mi.sounds[0];
                    Map<String, String> data;
                    try {
                        data = mi.getCollectedDataSet()[0];
                    } catch (Throwable e) {
                        data = new HashMap<>();
                    }
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

        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                int idx = (int) (long) invocation.getArgument(1);
                Map<String, String> data = new HashMap<>((Map<String, String>) invocation.getArgument(2));
                String startNote = data.get(MusInterval.Fields.START_NOTE);
                String note = startNote.substring(0, startNote.length() - 1);
                String octave = String.valueOf(startNote.charAt(startNote.length() - 1));
                MusInterval updated = new MusInterval.Builder(helper)
                        .model(defaultModelName)
                        .deck(defaultDeckName)
                        .sounds(new String[]{data.get(MusInterval.Fields.SOUND)})
                        .sounds_smaller(new String[]{data.get(MusInterval.Fields.SOUND_SMALLER)})
                        .sounds_larger(new String[]{data.get(MusInterval.Fields.SOUND_LARGER)})
                        .notes(new String[]{note})
                        .octaves(new String[]{octave})
                        .direction(data.get(MusInterval.Fields.DIRECTION))
                        .timing(data.get(MusInterval.Fields.TIMING))
                        .intervals(new String[]{data.get(MusInterval.Fields.INTERVAL)})
                        .tempo(data.get(MusInterval.Fields.TEMPO))
                        .instrument(data.get(MusInterval.Fields.INSTRUMENT))
                        .build();
                musIntervalsAdded.set(idx, updated);
                return true;
            }
        }).when(helper).updateNote(eq(modelId), any(Long.class), any(Map.class));


        musIntervalsAdded.add(musInterval.addToAnki());
        for (MusInterval value : musIntervalsSmaller) {
            MusInterval musIntervalSmallerAdded = value.addToAnki();
            assertArrayEquals(musIntervalSmallerAdded.sounds, musIntervalsAdded.getFirst().soundsSmaller);
        }
        for (MusInterval value : musIntervalsLarger) {
            MusInterval musIntervalLargerAdded = value.addToAnki();
            assertArrayEquals(musIntervalLargerAdded.sounds, musIntervalsAdded.getFirst().soundsLarger);
        }
    }

    @Test(expected = MusInterval.UnexpectedSoundsAmountException.class)
    public void add_BatchIncorrectNumberOfSounds_shouldFail() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);

        new MusInterval.Builder(helper)
                .sounds(new String[]{"/path/to/file.mp3"})
                .notes(ALL_NOTES)
                .octaves(ALL_OCTAVES)
                .intervals(MusInterval.Fields.Interval.VALUES)
                .build().addToAnki();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void add_Batch_ShouldCorrectlyAssignSoundFiles() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return invocation.getArgument(0);
            }
        }).when(helper).addFileToAnkiMedia(any(String.class));
        final ArrayList<Map<String, String>> addedNotesData = new ArrayList<>();
        doAnswer(new Answer<Long>() {
            private long noteId = 1;

            @Override
            public Long answer(InvocationOnMock invocation) {
                Map<String, String> data = invocation.getArgument(2);
                addedNotesData.add(data);
                return noteId++;
            }
        }).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        final int permutations = ALL_NOTES.length * ALL_OCTAVES.length * MusInterval.Fields.Interval.VALUES.length;
        String[] sounds = new String[permutations];
        for (int i = 0; i < permutations; i++) {
            sounds[i] = String.format("/path/to/file%d.mp3", i);
        }

        new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(sounds)
                .notes(ALL_NOTES)
                .octaves(ALL_OCTAVES)
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(MusInterval.Fields.Interval.VALUES)
                .tempo("90")
                .instrument("violin")
                .build()
                .addToAnki();

        assertEquals(permutations, addedNotesData.size());
        int i = 0;
        for (String octave : ALL_OCTAVES) {
            for (String note : ALL_NOTES) {
                for (String interval : MusInterval.Fields.Interval.VALUES) {
                    Map<String, String> data = addedNotesData.get(i);
                    assertEquals(data.get(MusInterval.Fields.SOUND), String.format("[sound:%s]", sounds[i]));
                    assertEquals(data.get(MusInterval.Fields.START_NOTE), note + octave);
                    assertEquals(data.get(MusInterval.Fields.INTERVAL), interval);
                    i++;
                }
            }
        }
    }
}
