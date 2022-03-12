//Author: Polina Lebedeva (xlebed12)

#include<stdlib.h>
#include<string.h>
#include<unistd.h>
#include<stdio.h>
#include<sys/socket.h>
#include<netinet/in.h>
#include<signal.h>
#define SIZE 256

//Custom functions for execution requests from klient//
const char* find_cpu ();
const char* find_hostname ();
const char* find_cpu_name ();

int main (int argc, char* argv[]) {
    char buffer [SIZE];
    int my_port;
    const char* hostname;
    const char* cpu_name;
    const char* cpu_load;
    switch (argc) {
        case 2:
            my_port = atoi (argv[1]);
        break;

        default:
            fprintf (stderr, "Too few arguments, you have to put number of port\n");
        exit (1);
    }

    //Creating socket//
    int sock = socket (AF_INET, SOCK_STREAM, IPPROTO_TCP);
    if (sock == -1) {
        //Error handling//
        fprintf (stderr, "Error during the execution of socket");
        close (sock);
        exit (1);
    }
    int option = 1;
    socklen_t optlen = sizeof(option);
    setsockopt(sock, SOL_SOCKET, SO_REUSEADDR|SO_REUSEPORT, &option, optlen);
    struct sockaddr_in adress;
    adress.sin_family = AF_INET;
    adress.sin_port = htons (my_port);
    adress.sin_addr.s_addr = INADDR_ANY;
    socklen_t addrlen = sizeof (adress);

    memset(adress.sin_zero, '\0', sizeof (adress.sin_zero));

    if ((bind (sock, (struct sockaddr *) &adress, addrlen)) == -1) {
        //Error handling//
        fprintf (stderr, "Error during bind");
        exit (1);
    }
    if ((listen (sock, 5)) == -1) { 
        //Error handling//
        fprintf (stderr, "Error during listening");
        exit (1);
    }
    int new_socket;
    char* token; 
    char response [1000];
    while (1) { 
        new_socket = accept (sock, (struct sockaddr*)&adress, &addrlen);
        if (new_socket == -1) {
            //Error handling//
            perror ("Error during creating client socket");
            exit (1);
        }
        if (recvfrom(new_socket, buffer, SIZE, 0, (struct sockaddr *) &adress, &addrlen) == -1) {
            //Error handling//
            fprintf(stderr, "Error during reading");
            exit (1);
        }

        //GET http://servername:port/hostname |cpu-name | load//
        //Parsing arguments for request//
        if ((token = strtok(buffer, " ")) != NULL && strcmp (token, "GET") == 0) {
            if ((token = strtok (NULL, " ")) != NULL) {
                if (strcmp (token, "/hostname") == 0) {
                    hostname = find_hostname ();
                    sprintf (response, "HTTP/1.1 200 OK\r\nContent-Type: text/plain;\r\n\r\n %s\n", hostname);
                    sendto (new_socket, response, strlen (response), 0, (struct sockaddr *) &adress, addrlen);
                }
                else if (strcmp (token, "/cpu-name") == 0) {
                    cpu_name = find_cpu_name ();
                    sprintf (response, "HTTP/1.1 200 OK\r\nContent-Type: text/plain;\r\n\r\n %s", cpu_name);
                    sendto (new_socket, response, strlen (response), 0, (struct sockaddr *) &adress, addrlen);
                }
                else if (strcmp (token, "/load") == 0) {
                    cpu_load = find_cpu ();
                    sprintf (response, "HTTP/1.1 200 OK\r\nContent-Type: text/plain;\r\n\r\n %s%%", cpu_load);
                    //FIXME % on another line
                    sendto (new_socket, response, strlen (response), 0, (struct sockaddr *) &adress, addrlen);
                }   
                else {
                    write (new_socket, "400 Bad Request", 16);
                }    
            }
        }
    close (new_socket);
    }
}

const char* find_hostname () {
    static char host_n[15];
    FILE* hostname = fopen ("/proc/sys/kernel/hostname", "r");
    //Control, if file was opened successfully//
    if (hostname == NULL) {
        fprintf (stderr, "Cannot open the file");
        fclose (hostname);
        exit (1);
    }
    while (fgets (host_n, 15, hostname) != NULL) {
        return host_n;
    }
    fclose (hostname);
}

const char* find_cpu_name () {
    FILE* file;
    static char path[50];

    file = popen ("cat /proc/cpuinfo | grep 'model name' | head -n 1 | awk -F': ' '{print $2}' ", "r");
    if (file == NULL) {
        //Error handling//
        fprintf (stderr, "Error during execution popen");
        pclose (file);
        exit (1);
    }
    while (fgets(path, 50, file) != NULL) {
        return path;
    }
    pclose (file);
}

const char* find_cpu () {
    FILE *file;
    static char result[1024];

    file = popen("top -bn 2 -d 0.01 | grep '^%Cpu' | tail -n 1 | gawk '{print $2+$4+$6}'", "r");
    if (file == NULL) {
        //Error handling//
        fprintf (stderr, "Error during execution popen");
        pclose (file);
        exit (1);
    }
    while (fgets(result, sizeof(result), file)) {
        return result;
    }
    pclose(file);
}
