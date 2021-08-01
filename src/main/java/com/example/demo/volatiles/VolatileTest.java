package com.example.demo.volatiles;

/**
 * https://stackoverflow.com/questions/68599358/volatile-variables-does-not-work-as-expected
 */
public class VolatileTest {
  private static volatile int i = 0;
  private static volatile int i1 = 0;

  public static void main(String[] args) throws InterruptedException {
    Thread t = new Thread(VolatileTest::writerThread);
    Thread t1 = new Thread(VolatileTest::readerThread);
    t.start();
    t1.start();
    t.join();
    t1.join();
  }

  public static void writerThread() {
    for (int j = 0; j < 100000; j++) {
      i = j;
      i1 = j;
    }
  }

  public static void readerThread() {
    int myI, myI1;
    for (int j = 0; j < 100000; j++) {
      //
      myI = i;
      myI1 = i1;
      if (myI1 > myI) {
        System.out.println(myI + " < " + myI1);
      }
    }
  }
}