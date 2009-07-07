#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <math.h>
#define __USE_GNU
#include <sched.h>

long sum(int);
long _sum(int);

extern int should_run_test(char*);
extern int curr_tests;
extern int total_tests;
extern double total_time;
extern double test_time;
struct timeval t1, t2;
double function_time = 0;

int main()
{
  // sets the CPU affinity to processor #0
  cpu_set_t mask;
  CPU_ZERO(&mask);
  CPU_SET(0, &mask);
  sched_setaffinity(0, sizeof(mask), &mask);

  long i = 0;
  
  for (i = 0; i < 2; i++)
    {
      sum(1000);
    }
  
  printf("TOTAL TESTS %d\n", total_tests);
  printf("total time %f\n", total_time);
  printf("test time %f\n", test_time);
  printf("function time %f\n", function_time);
}

long _sum(int N)
{
  /*
  long sum = 0;
  int i;
  for (i = 0; i <= N; i++) sum += i;
  return sum;
  */

  struct timeval t;
  gettimeofday(&t, NULL);
  double s = t.tv_sec + (t.tv_usec/1000000.0);
  
  int* p = malloc(sizeof(int) * N);
  int i = 0; 
  for (i = 0; i < N; i++)
    p[i] = rand();

  // BUBBLE SORT
  int x = 0, y = 0, holder = 0;
  for(x = 0; x < N; x++)
    for(y = 0; y < N-1; y++)
      if(p[y] > p[y+1]) {
        holder = p[y+1];
        p[y+1] = p[y];
        p[y] = holder;
      }
  free(p);

  gettimeofday(&t, NULL);
  double e = t.tv_sec + (t.tv_usec/1000000.0);
  printf("function time %f\n", (e-s));
  function_time += e - s;

  return N;
  
}


long sum(int N)
{
  // call the original method
  long result = _sum(N);

  // this is for measuring the overhead
  gettimeofday (&t1, NULL);
  double start_time = t1.tv_sec + (t1.tv_usec/1000000.0);

  if (should_run_test("sum") != 0) 
  {
    int pid = fork();
    signal(SIGCHLD, SIG_IGN);
    
    if (pid == 0)
    {    
      // sets the CPU affinity to processor #1
      cpu_set_t mask;
      CPU_ZERO(&mask);
      CPU_SET(1, &mask);
      sched_setaffinity(0, sizeof(mask), &mask);
      
      // now invoke the test
      if (testSum(N, result) == 0)
      {
	// probably include N and result, of course
	printf("testSum ERROR!\n");
      }
      else
      {
	//printf("testSum %d passed\n", total_tests);
      }
    		    
      // kill the process
      exit(0);
    }
    else
    {

      // update the counter to indicate that this test is done
      curr_tests--;
    }
  }

  // update of test time
  gettimeofday (&t2, NULL);
  double end_time = t2.tv_sec + (t2.tv_usec/1000000.0);
  test_time += (end_time - start_time);
  
  return result;

}

int testSum(int N, long result)
{
  // printf("starting test: %d\n", result);
  /*
  if (_sum(2 * N) == 2 * result + N*N)
  {
    return 1;
  }
  else return 0;
  */
  if (_sum(2 * N) == 2 * result) return 1;
  else return 0;
}
