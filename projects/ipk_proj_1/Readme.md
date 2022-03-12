Projekt do předmětu IPK:
Autor: Polina Lebedeva (xlebed12);

Stručný popis projektu:
    Server je vytvořen v jazyce C a komunikuje prostřednictvím protokolu HHTP. Podle požádavku vrácí informace o systému: domenové jméno, CPU a aktualní zátěž. Při implementaci jsou použité socket knihovny pro vytvaření spojení serveru jako například: <sys/socket.h> a <netinet/in.h>.
Způsob spuštění projektu:
    Překladá se pomoci příkazu ¨make¨, a může být spuštěn příkazem ¨make test¨. Zároveň může být spuštěn jako ./hinfosvc [číslo portu] a v druhém terminálu s příkazem curl http://localhost:12345/hostname | curl http://localhost:12345/cpu-name | curl http://localhost:12345/load. 
Příklady použití projektu:
    .\hinfosvc 12345 & curl http://localhost:12345/hostname | curl http://localhost:12345/cpu-name | curl http://localhost:12345/load
  


