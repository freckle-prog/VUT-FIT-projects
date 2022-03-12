#include<stdlib.h>
#include<string.h>
#include<unistd.h>
#include<stdio.h>
#include<sys/socket.h>
#include<netinet/in.h>
#include <errno.h>
float Idle_calc ();
float Non_Idle_calc ();

int main () {
    float curr_idle = Idle_calc ();
    float curr_non = Non_Idle_calc ();
    float curr_total = curr_idle + curr_non;
    sleep (1);
    float next_idle = Idle_calc ();
    float next_non = Non_Idle_calc ();
    float next_total = next_idle + next_non;

    float totald = next_total - curr_total;
    float idled = next_idle - curr_idle;
    float CPU = (totald - idled) / totald;
    printf ("%f\n", CPU);
   
   return(0);
}

//Calculate Idle
float Idle_calc () {
    char* token;
    char delim[2] = " ";
    char buff[255];
    int i = 1;
    float idle, iowait = 0;

    //Read the first line of /proc/stat
    FILE* cpu_stat = fopen ("/proc/stat", "r");
        //Control, if file was opened successfully
    if (cpu_stat == NULL) {
        fprintf (stderr, "Cannot open the file");
        fclose (cpu_stat);
        exit (1);
    }
    fgets (buff, 254, cpu_stat);
    token = strtok (buff, delim);
    while (token!=NULL) {
        token = strtok (NULL, delim);
        if (i == 4) {
            idle = atoi (token);
        }
        if (i == 5) {
            iowait = atoi (token);
        }
        i++; 
    }
    float Idle = idle + iowait;
    fclose (cpu_stat);
    return Idle;
}

//Calculate NonIdle
float Non_Idle_calc () {
    char* token;
    char delim[2] = " ";
    char buff[255];
    int i = 1;
    float user, nice, sys, irq, softirq, steal = 0;

    //Read the first line of /proc/stat
    FILE* cpu_stat = fopen ("/proc/stat", "r");
        //Control, if file was opened successfully
    if (cpu_stat == NULL) {
        fprintf (stderr, "Cannot open the file");
        fclose (cpu_stat);
        exit (1);
    }
    fgets (buff, 254, cpu_stat);
    token = strtok (buff, delim);
    while (token!=NULL) {
        token = strtok (NULL, delim);
        if (i == 1) {
            user = atoi(token);
        }
        if (i == 2) {
            nice = atoi (token);
        }
        if (i == 3) {
            sys = atoi (token);
        }
        if (i == 6) {
            irq = atoi (token);
        }
        if (i == 7) {
            softirq = atoi (token);
        }
        if (i == 8) {
            steal = atoi (token);
        }
        i++; 
    }
    float NonIdle = user + nice + sys + irq + softirq + steal;
    fclose (cpu_stat);
    return NonIdle;
}

