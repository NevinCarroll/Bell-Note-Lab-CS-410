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
 * The {@code Choir} class acts as a conductor for a group of {@link Player} objects.
 * <p>
 * It reads a note sheet from a file, assigns each note to the appropriate player,
 * and coordinates playback of the song in sequence.
 * </p>
 * <p>
 * Each {@link Player} is responsible for playing a specific {@link Note}, while the
 * {@code Choir} controls timing and ordering.
 * </p>
 */
public class Choir {

    /** Sequence of notes that make up the song. */
    private final List<BellNote> song = new ArrayList<>();

    /** Mapping of each Note to its corresponding Player. */
    private final Map<Note, Player> players = new HashMap<>();

    /** Shared audio output line used by all players. */
    private final SourceDataLine sourceDataLine;

    /**
     * Constructs a {@code Choir} with the given audio format.
     *
     * @param audioFormat the format used for audio playback
     */
    Choir(AudioFormat audioFormat) {
        SourceDataLine sourceDataLineTemp;
        try {
            sourceDataLineTemp = AudioSystem.getSourceDataLine(audioFormat);
        } catch (LineUnavailableException e) {
            System.err.println("Error while loading source data line.");
            sourceDataLineTemp = null;
        }

        // Assign initialized audio line
        sourceDataLine = sourceDataLineTemp;

        if (sourceDataLine == null) {
            System.err.println("Error while loading source data line.");
            return;
        }

        // Initialize players for each note
        createPlayers();
    }

    /**
     * Reads a note sheet file and loads its contents into the {@code song} list.
     * <p>
     * Each line of the file must contain:
     * <pre>
     * NOTE LENGTH
     * </pre>
     * Example:
     * <pre>
     * C4 4
     * D4 8
     * </pre>
     *
     * @param filename the path to the note sheet file
     * @return {@code true} if any errors occurred while reading or parsing the file;
     *         {@code false} otherwise
     */
    private boolean loadNoteSheet(String filename) {

        BufferedReader noteReader = null;

        // Attempt to open the file
        try {
            noteReader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filename);
            return true;
        }

        String line;
        int lineNum = 0;
        boolean hasError = false;

        // Read file line-by-line
        while (true) {
            lineNum++;
            try {
                if ((line = noteReader.readLine()) == null) break;
            } catch (IOException e) {
                System.err.println("Error reading note at line: " + lineNum);
                hasError = true;
                continue;
            }

            // Split line into components
            String[] parts = line.trim().split(" ");

            // Validate format (must contain exactly 2 parts)
            if (parts.length != 2) {
                System.err.println("Invalid format at line " + lineNum +
                        ". Expected: NOTE LENGTH (EX: A4 2). Ensure there is only one space between note and length.");
                hasError = true;
                continue;
            }

            String noteString = parts[0];
            int noteLengthValue;

            // Parse note length
            try {
                noteLengthValue = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid note length at line " + lineNum +
                        ". Must be an integer: " + parts[1]);
                hasError = true;
                continue;
            }

            // Parse note enum
            Note note;
            try {
                note = Note.valueOf(noteString);
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid note at line " + lineNum + ": " + noteString);
                hasError = true;
                continue;
            }

            // Convert integer to NoteLength enum
            NoteLength noteLength;
            switch (noteLengthValue) {
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
                    System.err.println("Invalid note length at line " + lineNum +
                            ": " + noteLengthValue);
                    hasError = true;
                    continue;
            }

            // Add validated note to song
            song.add(new BellNote(note, noteLength));
        }

        return hasError;
    }

    /**
     * Initializes a {@link Player} for each available {@link Note}.
     */
    private void createPlayers() {
        for (Note note : Note.values()) {
            players.put(note, new Player(note, sourceDataLine));
        }
    }

    /**
     * Starts all player threads.
     */
    private void startPlayers() {
        for (Note note : Note.values()) {
            players.get(note).startThread();
        }
    }

    /**
     * Plays the loaded song by coordinating all players.
     * <p>
     * Each note is handed to the appropriate player, which plays it
     * before the conductor proceeds to the next note.
     * </p>
     */
    void playSong() {
        try {
            sourceDataLine.open();
        } catch (LineUnavailableException e) {
            System.err.println("Error while opening source data line.");
        }

        sourceDataLine.start();

        // Start all players
        startPlayers();

        Player player;

        // Play each note sequentially
        for (BellNote bn : song) {
            player = players.get(bn.getNote());
            player.setNoteLength(bn.getNoteLength());
            player.giveTurn();
            player.waitToStop();
        }

        // Stop all players
        for (Note note : Note.values()) {
            player = players.get(note);
            player.stopThread();
            player.notifyPlayer();
        }

        // Ensure all audio data is played
        sourceDataLine.drain();
    }

    /**
     * Entry point for running the Choir program.
     *
     * @param args command-line arguments (expects a single argument: note sheet file path)
     */
    public static void main(String[] args) {

        // Initialize choir with audio format
        Choir choir = new Choir(new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false));

        // Load note sheet
        boolean hasError = choir.loadNoteSheet(args[0]);

        // Play song if no errors occurred
        if (hasError) {
            System.err.println("Error loading note sheet: " + args[0]);
        } else {
            choir.playSong();
        }
    }
}