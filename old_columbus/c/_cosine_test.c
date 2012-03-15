#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <math.h>

double cosine(double);
double _cosine(double);

extern int should_run_test(char*);
extern int curr_tests;
extern int total_tests;
extern long test_time;
struct timeval t1, t2;

int main()
{
  cosine(2.5);

  printf("TOTAL TESTS %d\n", total_tests);
}


double _cosine(double angle)
{
  return cos(angle);
}

double cosine(double angle)
{
  // if no test is to be run, then just call the original
  if (should_run_test("cosine") == 0) return _cosine(angle); // *************

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
    if (testCosine(angle, strtod(result, NULL)) == 0) // ****************** function name & conversion func
    {
      // probably include N and result, of course
      printf("ERROR!\n");
    }
    else
    {
      printf("testCosine %d passed\n", total_tests);
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
    double result = _cosine(angle); // ******************

    // send a message over the pipe that the original method is done
    close(fd[0]); // close the "read" end
    char buf[20];
    sprintf(buf, "%f", result); // **************** how to write the result to the buffer
    write(fd[1], buf, 20);
    close(fd[1]);

    // update the counter to indicate that this test is done
    curr_tests--;

    // return the result and carry on
    return result;
  }
  
  return -1;

}

int testCosine(double angle, double result)
{
  double result2 = _cosine(angle);
  if (result2 - result < 0.1 && result - result2 < 0.1)
    {
      return 1;
    }
  else 
    {
      printf("result: %f\n", result);
      printf("cosine: %f\n", _cosine(angle));
      return 0;
    }
}
