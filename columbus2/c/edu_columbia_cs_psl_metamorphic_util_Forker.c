#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <signal.h>

JNIEXPORT jint JNICALL
Java_edu_columbia_cs_psl_metamorphic_util_Forker_doFork ( JNIEnv* env, jobject obj ) 
{
  // fork this process and get the id
  jint pid = fork();
  
  //printf("pid %i\n", pid);
  
  // so that the parent does not wait for the child to finish,
  // and thus there are no zombie states
  signal(SIGCHLD, SIG_IGN);
  //sigignore();

  // the child will have a pid of 0, but the parent's is unchanged
  return pid;

}


JNIEXPORT void JNICALL
Java_edu_columbia_cs_psl_metamorphic_util_Forker_doExit ( JNIEnv* env, jobject obj ) 
{
  //printf("Buh bye\n");
  exit(0);

}


