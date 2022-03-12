/*
Autor: Polina Lebedeva (xlebed12)
Nazev: Program resici Santa Claus problem pro predmet IOS (proj2.c)
*/

//Pouzite knihovny
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <semaphore.h>
#include <time.h>
#include <sys/wait.h>
#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/shm.h>

//Definice pouzitych globalnych promennych
#define SIZE 1024
#define KEY 0x1234

FILE* soubor;

typedef struct {
    int sd_pocitadlo;
    int counter; //promenna pro pocitani skritku
    int sob_counter; //promenna pro pocitani sobu
    int kontrola_sobu; //kontrolujeme, co vsichni sobi jsou doma
    int santa_help; //zjistujeme, jesli mame Santu pustit spat
    int akt_skr; //pocitame skritky, co jsou aktivni
    int konec; //nastuvime, pokud procesy maji skoncit
} SharedMemory;

int shmid;
SharedMemory* memory;

typedef struct {
    sem_t skr_sem; //Semafor pro cekani skritku
    sem_t sob_sem; //Semafor pro cekani sobu
    sem_t sd_prom_sem; //Semafor pro pristup ke sdilene promenne
    sem_t counter_sem; //Skritky pocitadlo semafor
    sem_t sob_count_sem; //soby pocitadlo semafor
    sem_t santa_sem; //semafor pro santu
    sem_t santa_soby;
    sem_t brana;
    sem_t santa_pom;
} Semaphores;
int sem_id;
Semaphores* moje_sem;

//Pomocne funkce:
int init ();
void uninit ();
int m_rand (int h_mez, int d_mez);
//Funkce pro reseni problemu a vytvareni potrebnych procesu
void Santa_creator ();
void Santa_sleep ();
void Santa_help ();
void Santa_hint ();
void Santa_vanoce ();
void Skritek_creator (int NE, int TE);
void Sob_creator (int NR, int TR);

//Hlavni funkce
int main (int argc, char** argv) {
    //Inicializace pameti, semaforu a otevreni souboru pro zapis
    init ();
    Santa_creator (); 

    //Kontrola vstupu i parsovani pr. radky
    if (argc>5) {
        perror ("Zbytecny vstup!");
        exit (1);
    }
    else if (argc<5) {
        perror ("Chybejici vstup!");
        exit (1);
    }

    int NE=atoi(argv[1]); //pocet skritku 0<NE<1000
    int NR=atoi(argv[2]); //pocet sobu 0<NR<20
    int TE=atoi(argv[3]); //cas pro samostatnou praci skritku0<=TE<=1000
    int TR=atoi(argv[4]); //cas navraceni z dovolene pro soba 0<=RE<=1000.
    //Kontrola rozsahu prommenych
    if (NE<0 || NE>1000) {
        perror ("Chybny vstup pro skritky!");
        exit (1);
    }
    if (NR<0 || NR>20){
        perror ("Chybny vstup pro soby!");
        exit (1);
    }
    if (TE<0 || TE>1000) {
        perror ("Chybne casy pro skritky!");
        exit (1);
    }
    if (TR<0 || TR>1000) {
        perror ("Chybne casy pro soby!");
        exit (1);
    }
    Skritek_creator (NE, TE);    
    Sob_creator (NR, TR);    
        
    //Cekaci smycka pro dokonceni vsech procesu
    for (int i=0; i<NE+NR+1; i++) { //Pockame na konec vsech elfu, sobu a Santy
        wait(NULL);
    }

    //Uklizeni sdilene pameti, zruseni semaforu a zavreni souboru pro zapis
    uninit ();
    return 0;
}

/*
    @param Funkce nic nepotrebuje
    @return Funkce vraci 0, pokud vsechno probehlo spravne

    Funkce otevira soubor pro vypis, inicializuje pouzivanou pamet a semafory.
*/ 
int init() {
    soubor=fopen("proj2.out", "w");
    setvbuf(soubor, NULL, _IONBF, 1024);
    //osetreni chyb
    if (soubor==NULL) {
        perror ("Chyba otevreni souboru!\n");
        exit (1);
    }
    if ((shmid=shmget(KEY, SIZE, 0644 | IPC_CREAT))==-1) {
    perror ("shmget shared memory error!");
    exit (1);
    }
    else {
        memory=shmat(shmid, NULL, 0);
        if (memory==(void*)-1) {
        perror ("shamt shared memory error!");
        exit (1);
        }
        else {
        memory->sd_pocitadlo=0;
        memory->kontrola_sobu=0;
        memory->santa_help=0;
        memory->konec=0;
        }
    }
    
    if ((sem_id=shmget(0x1243, SIZE, 0644 | IPC_CREAT))==-1) {
        perror ("shmget semaphores error!");
        exit (1);
    }
    else {
        moje_sem=shmat (sem_id, NULL, 0);
        if (moje_sem==(void*)-1) {
            perror ("shmat semaphores error!");
            exit (1);
        }
        else {
            sem_init (&moje_sem->skr_sem, 1, 0); //Nastavime hodnotu na 0
            sem_init (&moje_sem->sob_sem, 1, 0); //Nechame semafor na 0
            sem_init (&moje_sem->sd_prom_sem, 1, 1); //Nastavime hodnotu na 1    
            sem_init (&moje_sem->counter_sem, 1, 1); //Semafor pro pocirani skritku
            sem_init (&moje_sem->sob_count_sem, 1, 1);  //Semafor pro pocitani sobu
            sem_init (&moje_sem->santa_sem, 1, 0); //Semafor pro santu na 0
            sem_init (&moje_sem->santa_soby, 1, 0); //semafor pro Soby, uvolnujisi santu
            sem_init (&moje_sem->brana, 1, 1);
            sem_init (&moje_sem->santa_pom, 1, 0);
        }
    }
    return 0;
}

/*
    @param Funcke nic nepotrebuje
    @return Funkce nic nevraci

    Funkce zavira soubor, uvolnuje sdilenou pamet a semafory
*/
void uninit() {
    fclose (soubor);
    fflush (soubor);
    if (soubor==NULL){
        perror ("Chyba zavreni souboru!\n");
        exit (1);
    }
    if (shmdt(memory)==-1) {
        perror ("shmdt shared memory error!");
        exit (-1);
    }
    if (shmctl (shmid, IPC_RMID, 0)==-1) {
        perror ("shmctl shared memory error!");
        exit (1);
    }
    sem_destroy (&moje_sem->skr_sem);
    sem_destroy (&moje_sem->sob_sem);
    sem_destroy (&moje_sem->sd_prom_sem);
    sem_destroy (&moje_sem->counter_sem);
    sem_destroy (&moje_sem->sob_count_sem);
    sem_destroy (&moje_sem->santa_sem);
    sem_destroy (&moje_sem->santa_soby);
    sem_destroy (&moje_sem->brana);
    sem_destroy (&moje_sem->santa_pom);
    if (shmdt(moje_sem)==-1) {
        perror ("shmdt semaphores error");
        exit (-1);
    }
    if (shmctl (sem_id, IPC_RMID, 0)==-1) {
        perror ("shmctl semaphores error");
        exit (1);
    }
}

/*
    @param Dolni mez
    @param Horni mez
    @return Funkce vraci randomne cislo ze zadaneho intervalu

    Funkce vezme dolni a horni mez a vrati randomne cislo z intervalu
*/
int m_rand (int d_mez, int h_mez) {
    srand (time (0));
    int r_cislo=(rand()%(h_mez-d_mez+1))+d_mez;
    return r_cislo;
}
/*
    @param nic
    @return nic

    Funkce vytvari proces santa a kontroluje dal jeho aktivitu
*/
void Santa_creator () {
    pid_t santa;
    santa=fork ();
    if (santa<0) {
        perror ("Chyba pri vytvareni Santy!");
        exit (1);
    }
    else if (santa==0) {
        for (;;) {
            Santa_sleep ();
            sem_wait (&moje_sem->santa_sem); 
            if (memory->kontrola_sobu==1) {
                break;
            }
            Santa_help ();
            sem_wait (&moje_sem->santa_sem);
            sem_wait (&moje_sem->santa_pom);
        }
        Santa_hint ();
        sem_post (&moje_sem->brana);
        sem_wait (&moje_sem->santa_soby);
        Santa_vanoce ();
        memory->konec=1;
        exit (0);
    }   
}
/*
    @param nic
    @return nic

   Funkce se pouziva pro vypis radku
*/
void Santa_sleep () {
    sem_wait (&moje_sem->sd_prom_sem);
        memory->sd_pocitadlo++;
        fprintf (soubor, "%d: Santa: going to sleep\n", memory->sd_pocitadlo);         
    sem_post (&moje_sem->sd_prom_sem);
}
/*
    @param nic
    @return nic

   Funkce se pouziva pro vypis radku
*/
void Santa_help () {
    sem_wait (&moje_sem->sd_prom_sem);
        memory->sd_pocitadlo++;
        fprintf (soubor, "%d: Santa: helping elves\n", memory->sd_pocitadlo);    
    sem_post (&moje_sem->sd_prom_sem);
        sem_post (&moje_sem->skr_sem);
        sem_post (&moje_sem->skr_sem);
        sem_post (&moje_sem->skr_sem);    
}
/*
    @param nic
    @return nic

   Funkce se pouziva pro vypis radku
*/
void Santa_hint () {
    sem_wait (&moje_sem->sd_prom_sem);
        memory->sd_pocitadlo++;
        fprintf (soubor, "%d: Santa: closing workshop\n",memory->sd_pocitadlo);     
    sem_post (&moje_sem->sd_prom_sem);
    sem_post (&moje_sem->sob_sem); 
}
/*
    @param nic
    @return nic

   Funkce se pouziva pro vypis radku
*/
void Santa_vanoce () {
    sem_wait (&moje_sem->sd_prom_sem);
        memory->sd_pocitadlo++;
        fprintf (soubor, "%d: Santa: Christmas started\n", memory->sd_pocitadlo);
    sem_post (&moje_sem->sd_prom_sem);    
    if (memory->akt_skr!=0 && memory->kontrola_sobu==1) {
        for (int i=0; i<memory->akt_skr; i++) {
            sem_post (&moje_sem->skr_sem);
        }   
    }
}
/*
    @param NE - pocet skritku
    @param TE - cas samostatne prace skritku
    @return nic

   Funkce se pouziva pro vytvareni potrebneho mnozstvi skritku a pro dalsi simulaci
*/
void Skritek_creator (int NE, int TE) {
    int elfID;
    memory->counter=0; //skritky chteji pomoc
    for (int i=0; i<NE; i++) {         
        if ((elfID=fork())<0) {
            perror ("Chyba pri vytvareni elfu");
            exit (1);
        }
        if (elfID==0) {
            sem_wait (&moje_sem->sd_prom_sem);
                memory->sd_pocitadlo++;
                fprintf (soubor, "%d: Elf %d: started\n", memory->sd_pocitadlo, i+1);            
            sem_post (&moje_sem->sd_prom_sem);

            for (;;) {
            usleep (m_rand(0, TE)); //simulace samostatne prace
            sem_wait (&moje_sem->sd_prom_sem);
                memory->sd_pocitadlo++;
                fprintf (soubor, "%d: Elf %d: need help\n", memory->sd_pocitadlo,  i+1);     
            sem_post (&moje_sem->sd_prom_sem);

            if (memory->konec==1) {
                sem_post(&moje_sem->santa_pom);
                break;
            }
            
            sem_wait (&moje_sem->counter_sem);
                memory->counter++;
                memory->akt_skr++;
            sem_post (&moje_sem->counter_sem);

            if (memory->counter==3) {
                sem_wait (&moje_sem->counter_sem); 
                    memory->counter=0; //vynulujeme counter
                    sem_post (&moje_sem->santa_sem); 
                sem_post (&moje_sem->counter_sem);                 
            }

            // if (memory->konec==1) {
            //     sem_post (&moje_sem->santa_pom);
            //     break;
            // }
            //puts ("skr pred semaforem");
            sem_wait (&moje_sem->skr_sem); //zaradim skritka do fronty
            //printf ("%d elfy\n", memory->akt_skr);
            if (memory->konec==1) {
                sem_post (&moje_sem->santa_pom);
                break;  
            } 
            else {
                sem_wait (&moje_sem->sd_prom_sem);
                memory->sd_pocitadlo++;
                   fprintf (soubor, "%d: Elf %d: get help\n", memory->sd_pocitadlo, i+1);
                    memory->santa_help++;
                    if (memory->santa_help==3) {
                        sem_post (&moje_sem->santa_sem); //pustime santu spat
                        sem_post (&moje_sem->santa_pom); 
                        memory->santa_help=0;
                    }
                sem_post (&moje_sem->sd_prom_sem);
                }
            }

            sem_wait (&moje_sem->sd_prom_sem);
                memory->sd_pocitadlo++;
                memory->akt_skr--;
                fprintf (soubor, "%d: Elf %d: taking holidays\n", memory->sd_pocitadlo, i+1);                
                sem_post (&moje_sem->sd_prom_sem);
            exit (0);
        }
    }
}

/*
    @param NR - pocet sobu
    @param TR - cas na dovolene
    @return nic

   Funkce se pouziva pro vytvareni potrebneho mnozstvi sobu a pro dalsi kontrolu 
   a simulaci procesu
   */
void Sob_creator (int NR, int TR) {
    int rdID;
    memory->sob_counter=0;
    for (int i=0; i<NR; i++) {
        rdID=fork();
        if (rdID<0) { 
            perror("Chyba pri vytvareni sobu!");
            exit (1);
        }
        else if (rdID==0) {
            sem_wait (&moje_sem->sd_prom_sem);
                memory->sd_pocitadlo++;
                fprintf (soubor, "%d: RD %d: rstarted\n", memory->sd_pocitadlo,  i+1);                        
            sem_post (&moje_sem->sd_prom_sem);
            usleep (m_rand(TR/2, TR)); //simulace prace
            sem_wait (&moje_sem->sd_prom_sem);
                memory->sd_pocitadlo++;
                fprintf (soubor, "%d: RD %d: return home\n", memory->sd_pocitadlo, i+1);              
            sem_post (&moje_sem->sd_prom_sem);     
            sem_wait (&moje_sem->sob_count_sem);     
                memory->sob_counter++;
            sem_post (&moje_sem->sob_count_sem);

            if (memory->sob_counter==NR) {
                sem_post (&moje_sem->santa_sem);
                sem_wait (&moje_sem->brana);
                memory->kontrola_sobu=1;
            }
        
            sem_wait (&moje_sem->sob_sem); //zaradim soba do fronty
            sem_post (&moje_sem->sob_sem);
            sem_wait(&moje_sem->sd_prom_sem);
                memory->sd_pocitadlo++;
                fprintf (soubor, "%d: RD %d: get hitched\n",memory->sd_pocitadlo, i+1);                    
            sem_post(&moje_sem->sd_prom_sem);
            sem_wait (&moje_sem->sob_count_sem);
                memory->sob_counter--;
                if (memory->sob_counter==0) {
                    sem_post (&moje_sem->santa_soby);
                }
            sem_post (&moje_sem->sob_count_sem);
            exit (0);
        }
    }
}
