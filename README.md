# Repository Info

Nella repository sono contenute tre directory:

-   *ProjectFrontend*, che contiene i file relativi al frontend in Angular della web app.

-   *MasterBackend*, che contiene i file relativi al backend (Spring boot) della web app, il quale gestisce l'autenticazione e la connessione con i nodi.

-   *NodeBackend*, che contiene i file relativi ai nodi che gestiscono i dati (Spring boot).


# Introduzione

L'applicazione presentata mira ad offrire un servizio che permette la
condivisione di documenti tramite una rete distribuita.\
Di particolare interesse è il fatto che questi siano memorizzati in più
repliche così da garantirne il più possibile la disponibilità, sia in
termini di accessibilità, dal momento che ogni utente può salvare o
scaricare dal nodo che più gli è congeniale (magari quello più vicino),
sia in termini di \"resistenza ai guasti\", dato che la cancellazione
accidentale (e non) di uno di questi non pregiudica la sua esistenza nel
sistema.

## Tecnologie utilizzate

Per progettare il sistema distribuito in questione sono state usate le
seguenti tecnologie:

-   *Spring boot*, un framework che facilita la creazione applicazioni
    web. In questa sede è stato usato per per sviluppare il lato server
    dell'applicazione ed anche il programma in esecuzione sui nodi così
    da permettere loro di ricevere richieste, fornire i risultati ed
    interfacciarsi con i dati.

-   *Angular*, un framework per lo sviluppo del frontend in applicazioni
    web.

-   *Amazon Simple Storage Service (Amazon S3)*, un servizio di
    archiviazione di oggetti offerto da AWS. E' stato usato per
    memorizzare i documenti e gestire la loro replicazione.

-   *Amazon Cognito*, un servizio di registrazione e accesso degli
    utenti per applicazioni web e mobili. E' stato usato per gestire
    l'autenticazione.

-   *Amazon Virtual Private Cloud (Amazon VPC)*, un servizio che
    permette la creazione e la gestione di reti virtuali.

-   *Amazon Elastic Compute Cloud (Amazon EC2)*, un servizio che
    permette la creazione e la gestione di macchine virtuali su cloud.
    E' stato usato per istanziare le macchine necessarie allo sviluppo
    del sistema.

# Architettura

L'architettura del sistema software in questione si basa su alcuni
componenti principali e l'uso di alcuni dei servizi offerti da Amazon
AWS:

-   *Nodi di archiviazione dati*, sono le macchine che hanno il compito
    di interfacciarsi con i dati.

-   *Nodo backend*, è il nodo su cui viene eseguito il backend
    dell'applicazione ed inoltre è colui il quale ha il compito di
    gestire le richieste ai vari nodi e l'autenticazione.

-   *Nodo frontend*, è il nodo su cui viene eseguito il frontend ed ha
    il compito di interfacciarsi con l'utente e fare le richieste al
    nodo backend.

-   *Cognito User Pool*, è l'identity provider che si occupa
    dell'autenticazione, della creazione degli utente e del rilascio
    degli Access Token.

-   *VPC con relativi Security Groups*, sono delle reti virtuali in cui
    sono presenti i nodi, in particolare ne sono state definite due: una
    per proteggere il nodo frontend, infatti il Security Group associato
    mantiene aperte solo le porte 22 (per ssh) e 80 (per HTTP), ed una
    per proteggere il nodo backend ed i nodi di archiviazione dati, in
    cui sono aperte solo le porte 22 ed 8080 per la comunicazione.

-   *EC2 Instance*, nella presente architettura rappresentano le
    macchine virtuali che sono servite per creare i nodi.

-   *S3 Bucket*, è un contenitore di oggetti, usato per memorizzare i
    file associati ad ogni nodo.

![Architettura](https://github.com/larena14/ReplicationProject/blob/ac67555e433840dd34377e1ed428d98ca46cfe9b/readme_images/architettura.jpg)


# Nodi di archiviazione dati

## Amazon S3

Come detto in precedenza è un servizio di archiviazione offerto da AWS,
in questo scenario è stato usato per memorizzare i file.\
Ad ogni nodo di archiviazione dati è stato associato un S3 Bucket, con
il quale può interfacciarsi sia per ottenere i file che per caricarli.\
E' stata scelta questa soluzione poichè questo servizio riesce a gestire
le repliche in modo efficiente ed automatizzato, oltretutto con un costo
ragionevole.\
La configurazione di ogni bucket è la seguente:

-   *Bucket versioning - abilitato*, il controllo delle versioni serve
    per mantenere più versioni di un oggetto nello stesso bucket. E'
    utile per preservare, recuperare e ripristinare ogni versione di
    ogni oggetto archiviato nel bucket e quindi per ripristinare
    eventuali azioni involontarie e/o malevole. Inoltre è necessario che
    sia abilitato per la gestione delle repliche.

-   *Encryption type - Server-side encryption with Amazon S3 managed
    keys (SSE-S3)*, con la server-side encryption, Amazon S3 cifra un
    oggetto prima di salvarlo su disco e lo decifra quando lo si
    scarica.

-   *Block all public access - On*, l'accesso pubblico agli oggetti è
    vietato, possono farlo solo entità autorizzate.

-   *Replication Rules*, sono regole che gestiscono la replicazione
    degli oggetti. In questo frangente si è scelto di replicare l'intero
    bucket (ossia ogni oggetto che viene caricato). Per quanto riguarda
    i bucket di destinazione delle repliche la scelta è ricaduta su
    tutti gli altri buckets dato che, per presentare il sistema in
    questione, sono stati istanziati solo tre nodi e quindi altrettanti
    buckets. In presenza di un numero maggiore di nodi si potrebbe
    pensare di impostarle in modo diverso. La classe di storage scelta,
    invece, è *S3 Standard* che offre un'archiviazione di oggetti con
    durabilità, disponibilità e prestazioni elevate per i dati a cui si
    accede con maggiore frequenza.

## Implementazione del server - *Spring boot*

Come già detto anche in precedenza, questi nodi hanno il compito di
interfacciarsi con i dati e quindi con i bucket di S3. Per far ciò è
stato usato il framework *Spring boot* così da creare un'istanza di un
server Java, in particolare è opportuno dire che questi nodi sono in
ascolto sulla porta 8090. Anche se, di fatto, quella aperta nel Security
Group è la 8080, questi possono essere comunque raggiunti grazie
all'ausilio dei *Private IPs*, questione che verrà approfondita in
seguito.\
Per comunicare con S3 direttamente da Spring è stata utilizzata la SDK
AWS e le sue librerie. In più è stata generata una *Access Key* (formata
da AccessKey e SecretAccessKey) così da poter inviare le richieste ad
AWS direttamente dal codice.\
Di seguito vengono presentati due dei punti cardine
dell'implementazione: la classe di servizio che si occupa della gestione
dei dati su S3 e il controller delle Rest API esportate da ogni nodo.

# Nodo Backend

## Amazon Cognito

In questa sezione viene presentato il meccanismo di autenticazione
usato, grazie all'ausilio di Amazon Cognito.\
E' stata creata una *User Pool* con la seguente configurazione:

-   *Required attributes - E-mail*, l'attributo richiesto per la
    registrazione di un utente è l'email.

-   *App client - Replication client*, si specifica quale sarà il client
    che usufruirà di questa user pool.

-   *Authentication flows*, vengono usati per implementare gli scenari
    dell'applicazione che richiedono token. Sono stati abilitati:

    -   *ALLOW_ADMIN_USER_PASSWORD_AUTH*

    -   *ALLOW_CUSTOM_AUTH*

    -   *ALLOW_REFRESH_TOKEN_AUTH*

    -   *ALLOW_USER_PASSWORD_AUTH*

Grazie alla SDK AWS e alle librerie del servizio Amazon Cognito è stato
possibile gestire il tutto nell'implementazione del nodo backend, il
quale può creare nuovi utenti, cambiarne la password, richiedere token e
verificarne la validità.\
Nel seguito viene riportata la classe di servizio che si occupa di
interfacciarsi con Cognito.


## Implementazione del backend - *Spring boot*

Questo nodo non ha solo il compito di gestire l'autenticazione, ma anche
quello di gestire le richieste provenienti dal frontend e contattare i
nodi di archiviazione dati per fornire dei risultati. Tutto ciò avviene
mediante l'uso di richieste HTTP:

1.  L'utente sceglie l'azione da compiere tramite l'interfaccia grafica
    fornita dal frontend.

2.  Il frontend invia una richiesta HTTP sulla porta 8080 al backend.

3.  Il backend processa la richiesta.

4.  Il backend invia la richiesta sulla porta 8090 ai nodi di
    archiviazione.

5.  I nodi di archiviazione forniscono le risposte.

6.  Il backend processa le risposte.

7.  Il backend invia la risposta finale al frontend.

8.  Il frontend mostra i risultati ottenuti all'utente.

Come è facilmente intuibile dallo schema, il backend gioca un ruolo
fondamentale poichè è il punto di snodo tra la richiesta dell'utente e
quelle da inviare ai nodi. Inoltre ha anche il compito di capire quali
sono i nodi online e come instradare le richieste stesse.\
Di seguito viene riportato il Rest controller che si occupa di ciò e la
classe di servizio che, sfruttando sempre SDK AWS, gestisce la
risoluzione degli indirizzi IP dei nodi e verifica se effettivamente
sono online oppure sono irragiungibili.


# Nodo Frontend

## Implementazione - *Angular*

Angular, come framework frontend basato su TypeScript, consente di
creare un'architettura robusta e scalabile per l'applicazioni web.\
La struttura del frontend in questione è basata su alcuni component che
vanno a definire l'interfaccia grafica vera e propria, implementati
tramite l'uso di HTML e CSS e una logica di business dietro ognuno di
loro, implementata in TypeScript. Di particolare interesse però è la
classe di servizio che si occupa di inviare le richieste al backend, la
quale ha il compito di formare le stesse e gestire le risposte così da
mapparle in componenti grafici con i quali l'utente può interagire.\
Di seguito l'implementazione.

## Alcuni screenshot dell'applicazione

![Homepage](https://github.com/larena14/ReplicationProject/blob/ac67555e433840dd34377e1ed428d98ca46cfe9b/readme_images/homepage.png)

![Login](https://github.com/larena14/ReplicationProject/blob/ac67555e433840dd34377e1ed428d98ca46cfe9b/readme_images/login.png)

![Signup](https://github.com/larena14/ReplicationProject/blob/ac67555e433840dd34377e1ed428d98ca46cfe9b/readme_images/signin.png)

![Dashboard](https://github.com/larena14/ReplicationProject/blob/ac67555e433840dd34377e1ed428d98ca46cfe9b/readme_images/dashboard.png)

# Amazon VPC e Security Groups

## Amazon Virtual Private Cloud

Come già detto in precedenza Amazon Virtual Private Cloud (VPC) è un
servizio che consente di creare un ambiente cloud isolato all'interno di
AWS (Amazon Web Services). In pratica, una VPC offre un controllo
completo sulla configurazione di una rete virtuale all'interno
dell'infrastruttura di AWS, consentendo di definire e personalizzare
l'ambiente di rete, compresi indirizzi IP, tabelle di routing e
connessioni di rete.\
In particolare si possono andare a definire e gestire:

-   *Subnet*, ossia un intervallo di indirizzi IP all'interno di una
    VPC, consentendo così di segmentare la rete per isolare risorse e
    applicazioni.

-   *Internet Gateway*, che consente alle risorse all'interno di una
    subnet pubblica di comunicare con Internet.

-   *Route Tables*, ossia le tabelle di routing definiscono le regole
    per instradare il traffico all'interno della VPC.

-   *Security Groups*, in pratica un meccanismo fondamentale per
    controllare il traffico in ingresso e in uscita verso le istanze EC2
    all'interno di una VPC. Funzionano come un firewall virtuale,
    consentendo di specificare le regole di sicurezza basate su
    protocolli, porte e indirizzi IP.

In questo scenario specifico sono state create due VPC: una per isolare
il Nodo Backend e i Nodi di archiviazione dei dati ed un'altra per il
Nodo Frontend.\
Nel seguito viene presentata la configurazione della prima VPC citata.

![Configurazione VPC](https://github.com/larena14/ReplicationProject/blob/ac67555e433840dd34377e1ed428d98ca46cfe9b/readme_images/vpc.png)

## Security Groups

I security group sono essenziali per garantire la sicurezza delle
risorse all'interno di una VPC su AWS e sono strettamente legate alle
istanze EC2, poichè le regole di sicurezza specificate nel Security
Group si applicano all'istanza stessa.

Si possono andare a definire *regole di Inbound e Outbound* ossia regole
per il traffico in entrata e in uscita. Le regole di ingresso
specificano quali tipi di traffico sono consentiti di accedere alle
risorse, mentre le regole di uscita controllano quali tipi di traffico
possono lasciare le risorse. In più è possibile specificare protocolli
(come TCP, UDP, ICMP) e porte per consentire o negare il traffico.\
Di seguito vengono riportate le configurazioni dei Security Groups usati
per isolare i nodi presenti nelle VPC.

![image](https://github.com/larena14/ReplicationProject/blob/ac67555e433840dd34377e1ed428d98ca46cfe9b/readme_images/sg_node.png)

![Inbound e Outbound Rules per la VPC che si occupa dei nodi](https://github.com/larena14/ReplicationProject/blob/ac67555e433840dd34377e1ed428d98ca46cfe9b/readme_images/sg_node2.png)

![image](https://github.com/larena14/ReplicationProject/blob/ac67555e433840dd34377e1ed428d98ca46cfe9b/readme_images/sg_fe.png)

![Inbound e Outbound Rules per la VPC che si occupa del
frontend](https://github.com/larena14/ReplicationProject/blob/ac67555e433840dd34377e1ed428d98ca46cfe9b/readme_images/sg_fe2.png)

\
Infine è doveroso precisare che, seppur il server dei nodi di
archiviazione dati sia in ascolto sulla porta 8090 e questa dalla
configurazione presentata sopra sembri chiusa, la comunicazione può
comunque avvenire in quanto il nodo backend usa i *Private IPs* delle
istanze di EC2, i quali possono essere visti come \"trusted\" e quindi
non soggetti alle regole di cui sopra.

# Deploy

Per quanto riguarda il deploy, sono state create delle istanze di EC2 di
tipo *t2.micro* aventi come sistema operativo *Amazon Linux*, in seguito
sono state assegnate alle relative VPC e sono anche stati impostati i
Security Groups trattati nella sezione precedente. In più è stata
associata ad ognuno di loro una coppia di chiavi per permettere la
connessione con ssh.\
Per i nodi di archiviazione e quello backend:

1.  E' stato installato Java.

2.  E' stato trasferito su ogni macchina il file *jar* contenente
    l'eseguibile del server.

3.  E' stato creato un servizio. Di seguito è riportato un esempio di
    servizio, in particolare quello creato per il nodo backend, ma anche
    tutti gli altri anno la stessa impostazione.

    ``` {.bash language="bash"}
    [Unit]
      Description=My Spring Application
      After=syslog.target

      [Service]
      User=ec2-user
      ExecStart=/usr/bin/java -jar /home/ec2-user/Backend.jar
      SuccessExitStatus=143

      [Install]
      WantedBy=multi-user.target
    ```

4.  E' stato avviato il servizio e reso possibile l'avvio automatico
    tramite l'uso dei seguenti comandi.

    ``` {.bash language="bash"}
    sudo systemctl daemon-reload
      sudo systemctl start spring-app.service
      sudo systemctl enable spring-app.service
    ```

Per il nodo frontend invece:

1.  E' stato installato nginx.

2.  Sono stati trasferiti su ogni macchina i files rigurdanti il build
    dell'applicazione in Angular.

3.  E' stato modificato il file di configurazione di nginx, così da
    specificare i file da usare e su quale porta mettere in ascolto il
    web server.

    ``` {.bash language="bash"}
    user nginx;
      worker_processes auto;
      error_log /var/log/nginx/error.log notice;
      pid /run/nginx.pid;
      include /usr/share/nginx/modules/*.conf;

      events {
          worker_connections 1024;
      }

      http {
          log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                            '$status $body_bytes_sent "$http_referer" '
                            '"$http_user_agent" "$http_x_forwarded_for"';

          access_log  /var/log/nginx/access.log  main;

          sendfile            on;
          tcp_nopush          on;
          keepalive_timeout   65;
          types_hash_max_size 4096;

          include             /etc/nginx/mime.types;
          default_type        application/octet-stream;

          include /etc/nginx/conf.d/*.conf;

          server {
              listen       80 default_server;
              listen       [::]:80 default_server;
              root         /var/www/html/project-frontend/browser/;

              index index.html index.htm index.nginx-debian.html;

              server_name _;
              location / {
                      try_files $uri $uri/ /index.html;
              }
          }
      }
    ```

4.  E' stato riavviato il servizio riguardante nginx tramite l'uso del
    seguente comando.

    ``` {.bash language="bash"}
    sudo systemctl restart nginx.service
    ```
