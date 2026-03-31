# Bell Choir

A multithreaded Java application that simulates a bell choir, where each "player" is responsible for performing a musical note in synchronization with a conductor.

## Overview

This project models a bell choir performance using concurrent programming concepts. Each musician (thread) plays a specific note at the correct time, coordinated by a central conductor.

The program demonstrates:

* Multithreading and synchronization
* Real-time audio playback
* Coordination between concurrent processes

## Features

* **Bell Note System**

    * Encapsulates musical notes and durations using `BellNote`, `Note`, and `NoteLength`

* **Multithreaded Players**

    * Each player runs on its own thread
    * Waits for a signal from the conductor before playing

* **Audio Playback**

    * Uses Java’s `SourceDataLine` for sound output
    * Plays generated note samples with precise timing

* **Choir Coordination**

    * A conductor (controller) ensures correct sequencing of notes
    * Supports timing for different note lengths (whole, half, quarter, etc.)

## Project Structure

```
Bell-Choir-CS-410/
├── src/
│   ├── Player.java        # Runnable musician thread
│   ├── BellNote.java      # Combines note + duration
│   ├── Note.java          # Audio sample generation
│   ├── NoteLength.java    # Enum of note durations
│   ├── Choir.java         # Conductor / synchronization logic
│   └── Main.java          # Entry point
├── README.md
```

## How It Works

1. A sequence of `BellNote` objects represents the music.
2. Each `Player` thread is assigned a specific `Note`.
3. The `Choir` (conductor) signals players when to perform.
4. Players:

    * Wait until it’s their turn
    * Play their note via `SourceDataLine`
    * Signal completion
5. The conductor advances through the sequence.

## Key Concepts

### Multithreading

* Each player runs independently using `Runnable`
* Threads are synchronized to prevent overlap and ensure timing accuracy

### Synchronization

* Shared coordination object (Choir/conductor)
* Players wait/notify to ensure correct sequencing

### Audio Processing

* Converts notes into raw audio samples
* Streams audio data to the speaker line

## Requirements

* Java 21 - https://adoptium.net/temurin/releases?version=21&os=any&arch=any
* Apache Ant - https://ant.apache.org/manual/install.html

## Running the Project

Clone the repository:

```bash
git pull https://github.com/NevinCarroll/Bell-Choir-CS-410.git
```

Then compile and run by typing:

```bash
ant run
```

into the terminal. This will play the default song provided, "MaryLamb.txt". If you want to play a custom note track, 
change the note sheet file path by typing:

``` bash
ant run -Dsong=""
```

then placing the file path to the file between the quotes (EX: path/to/file.txt). 


## Educational Purpose

This project was developed for CS 410 and is intended to demonstrate:

* Practical use of threads in Java
* Synchronization patterns (wait/notify)
* Real-time system coordination

## Future Improvements

* GUI for visualizing the choir
* Support for loading songs from files
* Improved timing accuracy using advanced scheduling
* Dynamic number of players

## Author

Nevin Carroll
GitHub: https://github.com/NevinCarroll

## License

This project is for educational use. Add a license if you plan to distribute or reuse.
