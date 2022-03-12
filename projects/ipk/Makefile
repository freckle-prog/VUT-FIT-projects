#CFLAGS=-std=c99 -Wall -Wextra -g
#CC=gcc
.PHONY: clean

all: ipk

ipk: hinfosvc.o 
	$(CC) $(CFLAGS) -o hinfosvc hinfosvc.o

hinfosvc.o: hinfosvc.c
	$(CC) $(CFLAGS) -c hinfosvc.c

test: hinfosvc
	./hinfosvc 8080 & curl http://localhost:8080/hostname

clean: 
	rm *.o


