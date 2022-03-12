#include <stdio.h>
#include <stdlib.h>
#include<string.h>
#include<unistd.h>

FILE *popen(const char *command, const char *mode);
int pclose(FILE *stream);
const char* cpu ();

int main(void) {
    const char* first = cpu ();
    // sleep (10);
    // const char* second = cpu ();
    // float CPU = atof (second) - atof (first);
    printf ("%s\n", first);
    return 0;
}

const char* cpu () {
    FILE *cmd;
    static char result[1024];

    cmd = popen("top -bn 2 -d 0.01 | grep '^%Cpu' | tail -n 1 | gawk '{print $2+$4+$6}'", "r");
    if (cmd == NULL) {
        perror("popen");
        exit(EXIT_FAILURE);
    }
    while (fgets(result, sizeof(result), cmd)) {
        return result;
    }
    pclose(cmd);
}
//grep 'cpu ' /proc/stat | awk '{usage=($2+$4)*100/($2+$4+$5)} END {print usage}'