import java.util.Random;
import java.util.concurrent.Semaphore;

public class Consumer extends Thread {
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
                // Random delay between operations
                int t2 = generator.nextInt(t * 1000);
                Thread.sleep(Math.abs(t2));

                // Determine how many slots to consume (1 to k)
                int k2 = generator.nextInt(k) + 1;

                // Acquire k2 full slots - block until available
                full.acquire(k2);

                // Acquire mutex for exclusive access to buffer
                mutex.acquire();

                // Critical section: remove items from buffer
                System.out.println("Consumer removing " + k2 + " items starting at index " + Main.next_out);
                for (int i = 0; i < k2; i++) {
                    int data = buffer[(Main.next_out + i) % n];
                    if (data != 1) {
                        System.out.println("Error: Expected 1 but found " + data + " at position " + ((Main.next_out + i) % n));
                        throw new RuntimeException("Race condition detected! Buffer should contain 1 but found " + data);
                    }
                    buffer[(Main.next_out + i) % n] = 0;  // Mark as empty
                }

                // Update next_out pointer
                Main.next_out = (Main.next_out + k2) % n;

                // Release mutex
                mutex.release();

                // Signal that items have been removed
                empty.release(k2);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Consumer interrupted: " + e.getMessage());
                break;
            }
        }
        System.out.println("Consumer finished");
    }
}
