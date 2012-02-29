#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <math.h>

char* foo(int);
char* _foo(int);

extern int should_run_test(char*);
extern int curr_tests;
extern int total_tests;
extern long test_time;
struct timeval t1, t2;

int main()
{
  foo(50);

  printf("TOTAL TESTS %d\n", total_tests);
}


char* _foo(int x)
{
  char *ptr = malloc(x * sizeof(char));
  if (x % 2 == 0) ptr[0] = 'a'; else ptr[0] = 'c';
  ptr[1] = 'b';
  return ptr;
}

char* foo(int x)
{
  gettimeofday (&t1, NULL);

  // if no test is to be run, then just call the original
  if (should_run_test("foo") == 0) return _foo(x); // *************

  // the original invocation of the function
  char* result = _foo(x);

  //int fd[2];
  //pipe(fd);

  int pid = fork();
  signal(SIGCHLD, SIG_IGN);

  if (pid == 0)
  {
    // wait for the result
    //close(fd[1]); // close the "write" end 
    //char result[100];
    //read(fd[0], result, 100);
    //printf("ready to go: %s\n", result);
    //close(fd[0]);

    // now invoke the test
    if (testFoo(x, result) == 0) // ****************** function name & conversion func
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

    // send a message over the pipe that the original method is done
    //close(fd[0]); // close the "read" end
    //char buf[20];
    //sprintf(buf, "%d", result); // **************** how to write the result to the buffer
    //write(fd[1], buf, 20);
    //close(fd[1]);

    // update the counter to indicate that this test is done
    //curr_tests--;

    // return the result and carry on
    return result;
  }
  
  return NULL;

}

int testFoo(int x, char* result)
{
  printf("result %d\n", result);
  printf("*result %d\n", *result);
  printf("result[0] %c.\n", result[0]);

  char* p = _foo(2 * x);
  printf("p %d\n", p);
  printf("*p %d\n", *p);
  printf("p[0] %c.\n", p[0]);

  if (p[0] != result[0])
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
