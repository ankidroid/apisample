# MusicIntervals2Anki
MusicIntervals2Anki is an Android app used to create and consolidate flashcards for ear training. It is based on two types of exercize: interval identification and interval comparison.

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
- first note duration (optional)

The app allows using video files as an audio source for notes being added. In such a case, the audio will be extracted before adding it to the Anki collection.

Under the hood, the app also manages two additional fields: smaller and larger sound files. These fields serve as links to related notes - ones with the same parameters but having respectively smaller and larger intervals by one semitone. These fields are filled automatically and are used in interval comparison cards.

### Batch adding

One of the most useful features of the app is the ability to add notes in bulk. Users are able to specify a set of intervals, select multiple sound files, and add the whole set of intervals to Anki in one go. 

In order to specify the set of intervals being added, users can select any combination of notes, octaves, and intervals. The number of sould files must be equal to the number of intervals in the specified set. For example, the user can select 2 notes: D and F#; then select 3 octaves: 1, 2 and 6; and select 1 interval: m3. The total number of intervals in this set is 2x3x1 = 6. 

The specified sound files, when sorted alphabetically, should follow a specific order. They should be sorted first by octave, then by note, then by interval. In this way the program will know which sould file corresponds to which interval.

*add filename dialog screenshot here*

### Searching

On top of being used for setting the attrubutes of added notes, all of the inputs are also used as filters when searching for existing notes. The search count is displayed in real-time.

### Audio capturing

media

### Integrity check

relations, duplicates, validation

### Configuration

default model updating

## Installation

Users can install the app by getting an APK from [the release section](https://github.com/lwp-emelnik/musicintervals2anki/releases).

dev env

## Contributing

TDD
