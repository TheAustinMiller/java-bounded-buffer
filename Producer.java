import java.util.Random;
import java.util.concurrent.Semaphore;

public class Producer extends Thread {
    // These should match your Main class static variables
    public void run() {
        Random generator = Main.generator;
        int[] buffer = Main.buffer;
        int n = Main.n;
        int k = Main.k;
        int t = Main.t;
        Semaphore mutex = Main.m;
        Semaphore empty = Main.empty;
        Semaphore full = Main.full;

        long now = System.currentTimeMillis();
        long end = now + 90000;

        while (System.currentTimeMillis() < end) {
            try {
                // Determine how many slots to fill (1 to k)
                int k1 = generator.nextInt(k) + 1;

                // Acquire k1 empty slots - block until available
                empty.acquire(k1);

                // Acquire mutex for exclusive access to buffer
                mutex.acquire();

                // Critical section: add items to buffer
                System.out.println("Producer adding " + k1 + " items starting at index " + Main.next_in);
                for (int i = 0; i < k1; i++) {
                    buffer[(Main.next_in + i) % n] = 1;  // Mark as filled
                }

                // Update next_in pointer
                Main.next_in = (Main.next_in + k1) % n;

                // Release mutex
                mutex.release();

                // Signal that items have been added
                full.release(k1);

                // Random delay between operations
                int t1 = generator.nextInt(t * 1000);
                Thread.sleep(Math.abs(t1));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Producer interrupted: " + e.getMessage());
                break;
            }
        }
        System.out.println("Producer finished");
    }
}
