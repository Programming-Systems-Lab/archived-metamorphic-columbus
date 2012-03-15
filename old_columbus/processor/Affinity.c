#include <stdlib.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/sysinfo.h>
#include <sys/mman.h>
#include <unistd.h>
#define  __USE_GNU
#include <sched.h>
#include <ctype.h>
#include <string.h>
#include <jni.h>

/* Create us some pretty boolean types and definitions */
typedef int bool; 	
#define TRUE  1
#define FALSE 0 



JNIEXPORT jint JNICALL 
Java_Affinity_checkAffinityN (JNIEnv* env, jobject obj, jint cpu)
{
  // printf("JNI checking %d", cpu);

  cpu_set_t mask;

  if (sched_getaffinity(0, sizeof(mask), &mask) == -1)
  {
    printf("WARNING: Could not get CPU Affinity, continuing...\n");
  }

  return CPU_ISSET(cpu, &mask);

}



JNIEXPORT jint JNICALL 
Java_Affinity_setAffinityN (JNIEnv* env, jobject obj, jint cpu)
{
  // printf("JNI assigning to %d\n", cpu);

  cpu_set_t mask;
  
  /* CPU_ZERO initializes all the bits in the mask to zero. */ 
  CPU_ZERO( &mask ); 	
  
  /* CPU_SET sets only the bit corresponding to cpu. */
  CPU_SET(cpu, &mask); // this puts this process on the corresponding CPU
  
  /* sched_setaffinity returns 0 in success */
  /* the 0 as the first argument means "this process" */
  return sched_setaffinity( 0, sizeof(mask), &mask );
}
