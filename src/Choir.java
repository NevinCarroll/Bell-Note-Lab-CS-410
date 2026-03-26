import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * A conductor will play a song given by a note sheet. For each note, the conductor will point to the player than needs
 * player there note, they will play there note, then the conductor will go on to the next player until the song is
 * finished.
 */
public class Choir {

    // Mary had a little lamb
    private final List<BellNote> song = new ArrayList<>();

    private Map<Note, Player> players = new HashMap<>();

    private final Conductor conductor; // TODO might remove
    private final AudioFormat af;

    /**
     * Set up conductor and audio format
     * @param af How the audio is to be interpreted and played
     */
    Choir(AudioFormat af) {
        this.conductor = new Conductor();
        this.af = af;
    }


    /**
     * Reads the given file and stores the notes and note lengths into the song list, that will be played later.
     * @param filename Name of file to be read
     * @return Whether an errors occurred when reading note sheet
     */
    private boolean loadNoteSheet(String filename) {
        // Read file into memory
        BufferedReader noteReader = null;
        try {
            noteReader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filename);
            return false;
        }

        String line; // Line to read
        int lineNum = 0; // Index to locate line errors
        boolean hasError = false; // Store every line with an error

        // Run while there are no more lines to read
        while (true) {
            lineNum++;
            try {
                if ((line = noteReader.readLine()) == null) break; // Break out of loop when at final line
            } catch (IOException e) {
                System.err.println("Error reading note: " + lineNum); // I/O error reading line
                hasError = true;  // State that there was an error while reading
                continue;
            }

            String[] parts = line.trim().split(" "); // Split line into multiple parts

            // If line is not in two parts, then it is invalid
            if (parts.length != 2) {
                System.err.println("Invalid amount of parts at line " + lineNum + ". Need to be two parts separated by one space.");
                hasError = true;
                continue;
            }

            // Temp variables to hold parts of the string
            String noteString = parts[0];
            int noteLengthString = 0;

            // Parse second part of line to see if it is an integer, if not, then line has error
            try {
                noteLengthString = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid note length at line " + lineNum + ". Need to be an integer");
                hasError = true;
                continue;
            }

            // Note of line
            Note note = null;

            // Try to get note value from enum
            // Enum.valueOf() TODO
            for (Note n : Note.values()) {
                if (n.name().equals(noteString)) {
                    note = n;
                    break;
                }
            }

            // If note is not a note value in the enum, then the line has error
            if (note == null) {
                System.err.println("Invalid note at line " + lineNum + ": " + noteString);
                hasError = true;
                continue;
            }

            // Note length
            NoteLength noteLength;

            // Convert int into NoteLength value, it no value exists, then line has error
            switch (noteLengthString) {
                case 1:
                    noteLength = NoteLength.WHOLE;
                    break;
                case 2:
                    noteLength = NoteLength.HALF;
                    break;
                case 4:
                    noteLength = NoteLength.QUARTER;
                    break;
                case 8:
                    noteLength = NoteLength.EIGHTH;
                    break;
                default:
                    System.err.println("Invalid note length at line " + lineNum + ": " + noteLengthString);
                    hasError = true;
                    continue;
            }

            // Make new bell note if everything has gone fine and add it into song
            BellNote newNote = new BellNote(note, noteLength);
            song.add(newNote);
        }

        // Return stating whether there was an error
        return hasError;
    }

    /**
     * TODO Finish
     * Create a player corresponding to each note in the song
     */
    void createPlayers() {
        final int songNotes = 8;

        for (Note note : Note.values()) {
//            players.put();
        }
    }

    /**
     * TODO Implement multithreading
     * Play song that was read from the text file
     */
    void playSong() {

        /* TODO While playing, go to the current note that needs to be played, notify that player, have him
        * play that note, wait until done, then play the next note after they are done, do this until all
        * */
        try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
            line.open();
            line.start();

            // Play every note in song
            for (BellNote bn: song) {
                playNote(line, bn);
            }
            line.drain();
        } catch (LineUnavailableException e) {
            System.err.println("Error while playing song");
        }
    }

    /**
     * Play note given on the line
     * @param line Writes audio to output
     * @param bn Note and length to be played
     */
    private void playNote(SourceDataLine line, BellNote bn) {
        final int ms = Math.min(bn.length.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
        final int length = Note.SAMPLE_RATE * ms / 1000;
        line.write(bn.note.sample(), 0, length);
        line.write(Note.REST.sample(), 0, 50);
    }

    /**
     * Get notes in songs, to only create players that are for the song
     */
    private void getNotesInSong() {

    }

    public static void main(String[] args) {
        // Set up how notes will be played
        final AudioFormat af = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
        Choir choir = new Choir(af);
        boolean hasError = choir.loadNoteSheet(args[0]); // Load song and check if note sheet is valid

        // Play if there were no errors, else print that there was an error
        if (hasError) {
            System.err.println("Error loading note sheet: " + args[0]);
        } else {
            choir.playSong();
        }

    }
}