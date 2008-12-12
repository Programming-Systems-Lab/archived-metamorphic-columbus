#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <math.h>
#define __USE_GNU
#include <sched.h>

int* sort(int*, int);
int* _sort(int*, int);
int* random_array(int);

extern int should_run_test(char*);
extern int next_cpu();
extern int verbose;
extern int total_tests;
extern double total_time;
extern double test_time;
struct timeval t1, t2;
double function_time = 0;

int main()
{
  int N = 10000;

  long i = 0;
  
  for (i = 0; i < 1; i++)
    {
      int* p = random_array(N);
      int* r = sort(p, N);
      free(p);
      free(r);
    }
  
  printf("TOTAL TESTS %d\n", total_tests);
  printf("total time %f\n", total_time);
  printf("test time %f\n", test_time);
  printf("total function time %f\n", function_time);
}

int* random_array(int N)
{
  int* p = malloc(sizeof(int) * N);
  int i = 0; 
  for (i = 0; i < N; i++)
    p[i] = rand();
  return p;
}

int* _sort(int* p, int N)
{
  /// this is just for testing purposes, it's not actually used in any computation
  struct timeval t;
  gettimeofday(&t, NULL);
  double s = t.tv_sec + (t.tv_usec/1000000.0);

  // create the new array
  int* r = malloc(N * sizeof(int));
  // copy everything from the argument
  int i;
  for (i = 0; i < N; i++) r[i] = p[i];

  // BUBBLE SORT
  int x = 0, y = 0, holder = 0;
  for(x = 0; x < N; x++)
    for(y = 0; y < N-1; y++)
      if(r[y] > r[y+1]) {
        holder = r[y+1];
        r[y+1] = r[y];
        r[y] = holder;
      }

  gettimeofday(&t, NULL);
  double e = t.tv_sec + (t.tv_usec/1000000.0);
  printf("function time %f\n", (e-s));
  function_time += e - s;

  return r;
}


int* sort(int* p, int N)
{
  // this would be the point where we make a backup of anything that is modified
  // by the function, so that we can restore it before calling the test function

  // call the original method
  int* result = _sort(p, N);

  // this is for measuring the overhead
  gettimeofday (&t1, NULL);
  double start_time = t1.tv_sec + (t1.tv_usec/1000000.0);

  if (should_run_test("sort") != 0) 
  {
    // figure out the CPU to assign the test to
    int cpu = next_cpu();

    // fork
    int pid = fork();
    // don't wait for the child to finish
    signal(SIGCHLD, SIG_IGN);
    
    if (pid == 0)
    {    
      // sets the CPU affinity
      cpu_set_t mask;
      CPU_ZERO(&mask);
      CPU_SET(cpu, &mask);
      sched_setaffinity(0, sizeof(mask), &mask);
      
      // now invoke the test
      if (testSort(p, N, result) == 0)
      {
	// probably include N and result, of course
	printf("testSort ERROR!\n");
      }
      else
      {
	if (verbose) printf("testSort %d passed\n", total_tests);
      }
    		    
      // kill the process
      exit(0);
    }
  }

  // update of test time
  gettimeofday (&t2, NULL);
  double end_time = t2.tv_sec + (t2.tv_usec/1000000.0);
  test_time += (end_time - start_time);
  
  return result;

}

int testSort(int* p, int N, int* result)
{
  // printf("starting test: %d\n", result);

  // shuffle
  int i;
  for (i = 0; i < N; i++)
  {
    int swap = rand() % N;
    int temp = p[i];
    p[i] = p[swap];
    p[swap] = temp;
  }

  // sort again
  p = _sort(p, N);

  // compare
  for (i = 0; i < N; i++)
  {
    if (p[i] != result[i])
    {
      free(p);
      return 0;
    }
  }

  free(p);
  free(result);
  return 1;

}
