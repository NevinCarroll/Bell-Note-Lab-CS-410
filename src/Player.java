public class Player implements Runnable {
    private static final int NUM_TURNS = 5;

    BellNote state;

    // TODO Player tells conductor when they are done

    private final Note note;
    private final Thread t;
    private volatile boolean running;
    private boolean myTurn;
    private int turnCount;

    Player(Note note) {
        this.note = note;
        turnCount = 1;
        t = new Thread(this, String.valueOf(note));
        t.start();
    }

    public void stopPlayer() {
        running = false;
    }

    public void giveTurn() {
        synchronized (this) {
            if (myTurn) {
                throw new IllegalStateException("Attempt to give a turn to a player who's hasn't completed the current turn");
            }
            myTurn = true;
            notify();
            while (myTurn) {
                try {
                    wait();
                } catch (InterruptedException ignored) {}
            }
        }
    }

    public void run() {
        running = true;
        synchronized (this) {
            do {
                // Wait for my turn
                while (!myTurn) {
                    try {
                        wait();
                    } catch (InterruptedException ignored) {}
                }

                // My turn!
                doTurn();
                turnCount++;

                // Done, complete turn and wakeup the waiting process
                myTurn = false;
                notify();
            } while (running);
        }
    }

    private void doTurn() {
        System.out.println("Player[" + note.name() + "] taking turn " + turnCount);
    }
}