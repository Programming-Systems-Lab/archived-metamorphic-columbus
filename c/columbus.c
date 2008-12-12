#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <math.h>
#define __USE_GNU
#include <sched.h>

int CPUS = 4; // the number of CPUs available on this machine
double MAX_OVERHEAD = 1; // the maximum allowable overhead

int ENABLE = 1; // whether or not to enable testing
int verbose = 1; // whether or not to print debugging statements

volatile int total_tests = 0; // tracks how many tests have been run

int initialized = 0; // whether or not the values have been initialized

volatile double total_time = 0; // total time for which the program has been running
volatile double test_time = 0; // excess amount of time incurred by running tests
volatile double start_time = 0; // starting time
volatile double overhead = 0; // measures the overhead caused by running tests
volatile int test_cpu = 0; // the CPU to which to assign the next test - start counting at 0

int should_run_test(char* function)
{
  // if disabled
  if (ENABLE == 0) return 0;

  // initialize if needed
  if (initialized == 0)
  {
    struct timeval t1;
    gettimeofday (&t1, NULL);
    start_time = t1.tv_sec + (t1.tv_usec/1000000.0);
    if (verbose) printf("System start_time %f\n", start_time);
    initialized = -1;

    // sets the CPU affinity to processor #0
    cpu_set_t mask;
    CPU_ZERO(&mask);
    CPU_SET(0, &mask);
    sched_setaffinity(0, sizeof(mask), &mask);
  }

  // TODO: put in some sort of check about max number of tests
  if (ENABLE)
  {
    if (verbose) printf("\n");

    // figure out the time for which the program has been running
    struct timeval t2;
    gettimeofday (&t2, NULL);
    double now = t2.tv_sec + (t2.tv_usec/1000000.0);
    total_time = now - start_time;

    if (verbose) printf("total time %f\n", total_time);
    if (verbose) printf("test time %f\n", test_time);

    overhead = test_time / (total_time - test_time);
    if (verbose) printf("overhead %f\n", overhead);

    if (verbose) fflush(stdout);

    // TODO: make this faster
    if (overhead >= MAX_OVERHEAD * 0.99)
    {
      if (verbose) printf("TOO MUCH OVERHEAD %f\n", overhead);
      if (verbose) fflush(stdout);
      return 0;
    }

    // made it here, okay to run a test
    total_tests++;
    if (verbose) printf("OKAY TO RUN A TEST %d\n", total_tests);
    if (verbose) fflush(stdout);
    return 1;
  }
  else 
  {
    if (verbose) printf("DISABLED\n");
    if (verbose) fflush(stdout);
    return 0;
  }
}

/*
 * Determines which CPU the test should run on.
 */
int next_cpu()
{
  // increment the counter
  test_cpu++;
  // if we've hit the limit, start counting at 1, 
  // since the main process is on 0
  if (test_cpu == CPUS) test_cpu = 1;
  if (verbose) printf("ASSIGNING TO CPU %d\n", test_cpu);
  return test_cpu;
}
