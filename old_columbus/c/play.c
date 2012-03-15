#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <math.h>

int main()
{
  struct timeval f;
  gettimeofday(&f, NULL);
  long f1 = f.tv_sec;
  printf("f1 %d\n", f1);
  long f2 = f.tv_usec;
  printf("f2 %d\n", f2);

  double f3 = f1 + (f2/1000000.0);
  printf("f3 %f\n", f3);

  f3 += 1.0;
  printf("f3 %f\n", f3);


  int* p = malloc(sizeof(int) * 1000);
  int i = 0; 
  for (i = 0; i < 1000; i++)
    p[i] = rand();
  for (i = 0; i < 10; i++)
    printf("p[%d] = %d\n", i, p[i]);



  /*
    struct timeval t1;
    gettimeofday (&t1, NULL);
    long t = t1.tv_usec;
    printf("TIME %d\n", t);
    long tt = t / 100;
    printf("TIME %d\n", tt);
    double start_time = (double) tt / 10.0;
    printf("start_time %f\n", start_time);

    double q = ((double)(t / 100)) / 10.0;
    printf("q %f\n", q);

    
    int i = 0;
    for (i = 0; i < 10; i++) {
      struct timeval t2;
      gettimeofday (&t2, NULL);
      long now = t2.tv_usec;
      now = now / 100;
      double time = (double) now / 10.0;
      printf("time %f\n", time);
      
      double total_time = (time - start_time);
      
      printf("total time %f\n", total_time);
    }
    */
    return 0;

}
