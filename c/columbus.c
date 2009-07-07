#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <math.h>

int MAX_TESTS = 1; // the maximum allowable number of concurrent tests
double MAX_OVERHEAD = 1; // the maximum allowable overhead

int verbose = 0; // whether or not to print debugging statements

volatile int total_tests = 0; // tracks how many tests have been run
volatile int curr_tests = 0; // tracks the number of currently-executing tests

int initialized = 0; // whether or not the values have been initialized

volatile double total_time = 0; // total time for which the program has been running
volatile double test_time = 0; // excess amount of time incurred by running tests
volatile double start_time = 0; // starting time
volatile double overhead = 0; // measures the overhead caused by running tests

int should_run_test(char* function)
{
  // initialize if needed
  if (initialized == 0)
  {
    struct timeval t1;
    gettimeofday (&t1, NULL);
    start_time = t1.tv_sec + (t1.tv_usec/1000000.0);
    if (verbose) printf("System start_time %f\n", start_time);
    initialized = -1;
  }

  // only run a test if there aren't too many running already
  if (curr_tests < MAX_TESTS)
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
    curr_tests++;
    total_tests++;
    if (verbose) printf("OKAY TO RUN A TEST %d\n", total_tests);
    if (verbose) fflush(stdout);
    return 1;
  }
  else 
  {
    if (verbose) printf("TOO MANY TESTS %d\n", curr_tests);
    if (verbose) fflush(stdout);
    return 0;
  }
}
