#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <math.h>

int* foo(int);
int* _foo(int);

extern int should_run_test(char*);
extern int curr_tests;
extern int total_tests;
extern long test_time;
struct timeval t1, t2;

int main()
{
  foo(55);

  printf("TOTAL TESTS %d\n", total_tests);
}


int* _foo(int x)
{
  int *ptr = malloc(sizeof(int));
  if (x == 0) *ptr = 3; else *ptr = x;
  return ptr;
}

int* foo(int x)
{
  // if no test is to be run, then just call the original
  if (should_run_test("foo") == 0) return _foo(x); // *************

  gettimeofday (&t1, NULL);

  int fd[2];
  pipe(fd);

  int pid = fork();
  signal(SIGCHLD, SIG_IGN);

  if (pid == 0)
  {
    // wait for the result
    close(fd[1]); // close the "write" end 
    char result[100];
    read(fd[0], result, 100);
    //printf("ready to go: %s\n", result);
    close(fd[0]);

    // now invoke the test
    if (testFoo(x, atoi(result)) == 0) // ****************** function name & conversion func
    {
      // probably include N and result, of course
      printf("ERROR!\n");
    }
    else
    {
      printf("testFoo %d passed\n", total_tests);
    }
		    
    // kill the process
    exit(0);
  }
  else
  {
    gettimeofday (&t2, NULL);
    //double time = (t2.tv_usec - t1.tv_usec)/1000000.0;
    //printf("time to fork = %f\n", time);
    test_time += (t2.tv_usec - t1.tv_usec)/1000000.0;

    // run the "original" invocation of the method
    int* result = _foo(x); // ******************
    printf("RESULT %d\n", result);
    printf("*RESULT %d\n", *result);

    // send a message over the pipe that the original method is done
    close(fd[0]); // close the "read" end
    char buf[20];
    sprintf(buf, "%d", result); // **************** how to write the result to the buffer
    write(fd[1], buf, 20);
    close(fd[1]);

    // update the counter to indicate that this test is done
    curr_tests--;

    // return the result and carry on
    return result;
  }
  
  return NULL;

}

int testFoo(int x, int* result)
{
  printf("result %d\n", result);
  printf("*result %d\n", *result);

  int* p = _foo(0);
  printf("p %d\n", p);
  printf("*p %d\n", *p);
  fflush(stdout);

  if (*p != *result)
  {
    printf("boo \n");
    return 0;
  }
  else
    {
      printf("hooray!\n");
      return 1;
    }
}
