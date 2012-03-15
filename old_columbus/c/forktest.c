#include <stdio.h>
#include <time.h>
#include <stdlib.h>

int main()
{
  struct timeval t;
  gettimeofday(&t, NULL);
  double s = t.tv_sec + (t.tv_usec/1000000.0);

  int pid = fork();

  if (pid == 0)
  {
    struct timeval t1;
    gettimeofday(&t1, NULL);
    double e = t1.tv_sec + (t1.tv_usec/1000000.0);

    printf("child %f\n", (e-s));
  }
  else
  {
    struct timeval t1;
    gettimeofday(&t1, NULL);
    double e = t1.tv_sec + (t1.tv_usec/1000000.0);

    printf("parent %f\n", (e-s));
  }

}
