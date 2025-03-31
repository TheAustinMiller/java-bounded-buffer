/*
    AUSTIN MILLER
    Program 2 - Semaphores/Bounded Buffer Problem
    03 08 2022
    Bounded buffer problem solve that ends after 90 seconds!
    Without P and V my race conditions were as follows: n = 10, k = 7, t = 5
 */


import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Main {
    public static final int n = 30; // Size of buffer
    public static final int k = 7; // Max slots messed with
    public static final int t = 5; // Max SECS (lol) between operations
    public static int[] buffer = new int[n];
    public static int next_in = 0;
    public static int next_out = 0;
    public static Random generator = new Random();
    public static Semaphore m = new Semaphore(1);
    public static Semaphore empty = new Semaphore(n);
    public static Semaphore full = new Semaphore(0);

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < n; i++) {
            buffer[i] = 0;
        }
        Producer p = new Producer();
        Consumer c = new Consumer();

        Thread prod = new Thread(p);
        Thread cons = new Thread(c);

        prod.start();
        cons.start();

        long now = System.currentTimeMillis();
        long end = now + 90000;
        while (System.currentTimeMillis() < end) {
            for (int i = 0; i < n; i++) {
                System.out.print(buffer[i] + " ");
            }
            System.out.println();
            Thread.sleep(2000);
        }

    }

    public static class Producer extends Thread {
        public void run() {
            long now = System.currentTimeMillis();
            long end = now + 90000;
            while (System.currentTimeMillis() < end) {
                empty.tryAcquire();
                m.tryAcquire();

                int k1 = generator.nextInt(k) + 1;
                for (int i = 0; i < k1; i++) {
                    buffer[(next_in + i) % n] = 1;
                }
                next_in = (next_in + k1) % n;


                m.release(1);
                full.release(1);


                int t1 = generator.nextInt((int) t * 1000);
                try {
                    Thread.sleep(Math.abs(t1));
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public static class Consumer extends Thread {
        public void run() {
            long now = System.currentTimeMillis();
            long end = now + 90000;
            while (System.currentTimeMillis() < end) {


                int t2 = generator.nextInt((int) t * 1000);
                try {
                    Thread.sleep(Math.abs(t2));
                } catch (InterruptedException e) {
                }

                full.tryAcquire();
                m.tryAcquire();


                int k2 = generator.nextInt(k) + 1;
                for (int i = 0; i < k2; i++) {
                    int data = buffer[(next_out + i) % n];
                    if (data > 1) {
                        throw new RuntimeException();
                    }
                    buffer[(next_out + i) % n] = 0;
                }
                next_out = (next_out + k2) % n;

                m.release(1);
                empty.release(1);
            }
        }
    }
}

