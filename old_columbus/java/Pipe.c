#include <jni.h>
#include <stdio.h>
#include <stdlib.h>

// the stuff for the pipe
int fd[2];

JNIEXPORT jint JNICALL
Java_Pipe_createPipe ( JNIEnv* env, jobject obj ) 
{
  pipe(fd);
  return 0;
}


JNIEXPORT jint JNICALL
Java_Pipe_writePipe ( JNIEnv* env, jobject obj, jlong val ) 
{
  close(fd[0]);
  //printf("About to write\n");
  //printf("val is %d\n", val);
  
  char buf[100]; // assumes the value is less than 100 chars
  sprintf(buf, "%d", val);
  write(fd[1], buf, 100);
  //printf("Wrote to pipe\n");
  close(fd[1]);
  return 0;
}


JNIEXPORT jstring JNICALL
Java_Pipe_readPipe ( JNIEnv* env, jobject obj ) 
{
  close(fd[1]);
  //printf("About to read\n");
  char buf[100];
  int n = read(fd[0], buf, 100);
  //printf("The value of n is %d\n", n);
  //printf("The value in the buffer is %s\n", buf);
  close(fd[0]);
  return (*env)->NewStringUTF(env, buf);
}


