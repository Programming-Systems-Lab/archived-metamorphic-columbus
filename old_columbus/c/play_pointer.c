#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <math.h>

int* test(int x)
{
  int *ptr = malloc(sizeof(int));
  *ptr = x;
  return ptr;
}


int main()
{
  /*
  int *p, x = 100;
  p = &x;
  printf("p %d\n", p);
  printf("*p %d\n", *p);
  */

  int *p = test(5);

  int pid = fork();
  if (pid == 0)
    {
      printf("child p %d\n", p);
      printf("child *p %d\n", *p);
      
      int *p1 = test(5);
      printf("child p1 %d\n", p1);
      printf("child *p1 %d\n", *p1);    

      printf("child p %d\n", p);
      printf("child *p %d\n", *p);

      if (*p1 == *p) printf("HOORAY\n");
      else printf("BOO\n");
      fflush(stdout);

    }
  else
    {
      printf("parent p %d\n", p);
      printf("parent *p %d\n", *p);
    }

    return 0;
}



