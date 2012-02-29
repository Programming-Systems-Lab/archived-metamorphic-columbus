// pipe.c
// This example shows interprocess communication (IPC) using a pipe
// This is called the "pipe and fork" approach

// Typical approach:
// create a pipe using the system call "pipe"
// create a child process using "fork"
// use system calls "read" and "write" to read/write from/to the pipe

// when a pipe is created in the parent process, two file descriptors
// are created; we need to use them to access the pipe
// fd[0] and fd[1]
// fd[0] is opened for reading, fd[1] is opened for writing.

// when the child process is created, the child also has two
// file descriptors fd[0] and fd[1] (total of 4 are available)
// fd[0] is opened for reading, fd[1] is opened for writing.


// In this example: Parent writes to the pipe, child reads from the pipe
// (Other combinations are also possible)

#include <stdio.h>

int main()
{
        printf("Starting the pipe.c program\n\n");

        // working variables for reading data from the pipe
        int n; /* store the number of characters read */
        char buf[100]; /* store the characters read */

        // pipe needs an integer array of size 2
        int fd[2];

        printf("Create the pipe\n\n");
        pipe(fd); /* fd[0] read; fd[1] write */

        printf("Create the child process using fork\n");

        if ( fork() == 0 ) /* create child, test return value */
        {
                close(fd[1]); /* close write end - not needed in this example */
                printf("child is running...\n");
                printf("about to read from the pipe\n");

                n = read( fd[0], buf, 100); /* child reads from pipe */

                printf("after reading from the pipe\n");
                printf("the value of n is: %d\n",n);
                printf("the value in the buffer is: %s\n\n", buf);
        }
        else /* parent*/
        {
                close(fd[0]); /* close read end - not needed for this example */
                printf("parent is running...\n");
                printf("about to write to the pipe\n");

		int i = 90;
		char buf[3];
		sprintf(buf, "%d", i);
                //write( fd[1], "Writing to pipe", 100); /* parent writes to pipe */
                write( fd[1], buf, 3); /* parent writes to pipe */

                printf("after writing to the pipe\n\n");
        }

        printf("Ending the c program\n");

}
