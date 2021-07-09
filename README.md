# MusicIntervals2Anki
MusicIntervals2Anki is an Android app used to create and consolidate flashcards for [ear training](https://en.wikipedia.org/wiki/Ear_training). It is based on two types of exercise: [interval](https://en.wikipedia.org/wiki/Interval_(music)) identification and interval comparison.

## Installation

Users can install the app by getting an APK from [the release section](https://github.com/lwp-emelnik/musicintervals2anki/releases).

For dev environment setup see [contributing](#contributing).

## Features

### Adding notes

The app allows to add notes to AnkiDroid provided the following attributes:
- sound file
- start note and octave
- direction (ascending/descending)
- timing (melodic/harmonic)
- interval
- tempo (beats per minute)
- instrument
- [optional] first note duration

For example, a note with the following parameters:

#### Note 1

| Field          | Value                        |
|----------------|------------------------------|
| sound          | [sound:[sample1.mp3](https://lwp-ygavrilenko.github.io/C4_2_ascending_melodic_Maj3_80_bass-guitar-5-strings-MIDI.mp3)]           |
| note1          | C4                           |
| note1.duration | 2                            |
| direction      | ascending                    |
| timing         | melodic                      |
| interval       | Maj3                         |
| tempo          | 80                           |
| instrument     | bass guitar, 5 strings, MIDI |

Would result in the following interval identification card:

#### Card 1

| Front | [▶️](https://lwp-ygavrilenko.github.io/C4_2_ascending_melodic_Maj3_80_bass-guitar-5-strings-MIDI.mp3) Which interval is it?                                                        |
|-------|--------------------------------------------------------------------------------|
| Back  | Major 3rd<br>C4, ascending, melodic, Maj3, 80BPM, bass guitar, 5 strings, MIDI |

Under the hood, the app also manages two additional fields: smaller and larger sound files. These fields serve as links to related notes - ones with the same parameters but having respectively smaller and larger intervals by one semitone. Smaller & larger sound file fields are filled automatically and used in interval comparison cards.

So if we would to add another note, identical to the [previous one](#note-1), but having `interval` field, say, smaller by one semitone (for Maj3 it would be min3):

#### Note 2

| Field          | Value                        |
|----------------|------------------------------|
| sound          | [sound:[sample2.mp3](https://lwp-ygavrilenko.github.io/C4_2_ascending_melodic_min3_80_bass-guitar-5-strings-MIDI.mp3)]           |
| note1          | C4                           |
| note1.duration | 2                            |
| direction      | ascending                    |
| timing         | melodic                      |
| interval       | min3                         |
| tempo          | 80                           |
| instrument     | bass guitar, 5 strings, MIDI |

, first note's `sound_larger` field now would be filled with second note's `sound` and, conversely, second note's `sound_smaller` would fill with first note's `sound.

As a result, we get two interval comparison cards:

| Front | [▶️](https://lwp-ygavrilenko.github.io/C4_2_ascending_melodic_Maj3_80_bass-guitar-5-strings-MIDI.mp3) [▶️](https://lwp-ygavrilenko.github.io/C4_2_ascending_melodic_min3_80_bass-guitar-5-strings-MIDI.mp3) Which interval is larger?                                            |
|-------|--------------------------------------------------------------------------|
| Back  | C4, ascending, melodic: Maj3 - min3; 80BPM, bass guitar, 5 strings, MIDI |

and

| Front | [▶️](https://lwp-ygavrilenko.github.io/C4_2_ascending_melodic_min3_80_bass-guitar-5-strings-MIDI.mp3) [▶️](https://lwp-ygavrilenko.github.io/C4_2_ascending_melodic_Maj3_80_bass-guitar-5-strings-MIDI.mp3) Which interval is larger?                                            |
|-------|--------------------------------------------------------------------------|
| Back  | C4, ascending, melodic: min3 - Maj3; 80BPM, bass guitar, 5 strings, MIDI |

### Batch adding

One of the most useful features of the app is the ability to add notes in bulk. Users are able to specify a set of music intervals, select multiple sound files, and add the whole set to AnkiDroid in one go. 

In order to specify the set of music intervals being added, users can select any combination of notes, octaves, and intervals. The number of sound files must be equal to the number of entries in the specified set. For example, the user can select 2 notes: D and F#; then select 3 octaves: 1, 2, and 6; and select 1 interval: m3. The total number of intervals in this set is 2x3x1 = 6. 

The specified sound files, when sorted alphabetically, should follow a specific order. They should be sorted first by octave, then by note, then by interval. In this way, the program will know which sound file corresponds to which music interval.

### Searching

On top of being used for setting the attributes of added notes, all of the inputs are also used as filters when searching for existing notes. The search result count is updated and displayed in real-time.

### Integrity check

The integrity check operation can be used to monitor the correctness of saved notes. Its responsibilities are:
- to validate field values based on predefined rules
- search for duplicate notes
- verify the actuality of existing relations
- fill missing relations

After completing the operation, the user will be presented with a report describing the integrity statistics. 

Additionally, both invalid notes and notes with incorrect relations are marked with error tags. The latter applies to notes that contain invalid links as well as notes that point to or are being pointed by inappropriate notes (i. e. invalid or relation condition isn't met).

The error tags utilize hierarchical structure and have the following format:

`mi2a::<issue>[::<field>[::<cause>]]`,

where `issue` is either "invalid", "duplicate", or "suspicious", optional `field` specifies the field that has caused the error, and optional `cause` provides succinct error explanation if needed. 


Integrity check, as well as mark operation, is executed on the result set of the search.

### Audio capturing & extraction

Alongside the option of playing back the selected sound files, the app provides built-in tools to capture device audio in case you want to use other apps to generate sounds.

The app also allows using video files as the audio source for notes being added. In such a case, the audio will be extracted before adding it to the Anki collection.

### Configuration

The app is able to create a default "music interval" note type and keep it up to date with the provided functionality. While this will be enough for most of the users, the app also allows opting out of using the default note type, and provides the ability to switch used Anki note type and configure used fields mapping.

Besides this, the users are given the following settings:
- select the deck for adding notes to
- select whether or not audio/video files should be deleted upon insertion to Anki
- enable logging the app version to added notes in a separate field
- disable marking duplicates with the error tag

## Contributing

If you have any questions or suggestions, do not hesitate to open an [issue](https://github.com/lwp-emelnik/musicintervals2anki/issues).

Pull requests are welcome. The project is built and ran using [TDD](https://en.wikipedia.org/wiki/Test-driven_development). Please make sure to update tests as appropriate before submitting a PR.

## Environment setup

Prerequisites:
- Installed Java SE Development Kit (JDK)
- Installed Android SDK

### Install
```
git clone https://github.com/lwp-emelnik/musicintervals2anki && cd musicintervals2anki
```

### Build
```
./gradlew assembleDebug
```

### Run tests
```
./gradlew test
```
