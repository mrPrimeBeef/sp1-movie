**forståelse af projektet:**

Der er en *buildMain som står for en Run Once, for at oprette databasen. Så er der FuncMain som står for functionaliteten af databasen, altså står den for at arbejde med databasen, hvor buildMain fetch data og bygge den.

I TMDBServices for at hente film bliver der brugt newScheduledThreadPool, ellers oplevede jeg at jeg 429 http respons tilbage(too many requests) - dette stykke er ikke optimeret, men tager dog ikke lang tid.

OBS: hvis man køre BuildMain skal man opdatere hibernateConfig til at create, hvorimod hvis du køre andre steder fra skal den opdateres til "update"

*BuildMain laver: genre -> film -> directors -> actors, under actors skal den så tilføje realtioner hvilket tager lang tid (ca. 1 time hvis der hentes 5 års danske film). (man burde kigge på noget optimering her).
Både Actors og Director bliver hentet med tråde.
Der er også en udkommenteret metode - saveTestDataAsJson(den er lavet på "2 pages" indholde) som har været til for at lave testdata som kunne bruges i dao testsne, hvis den "bruges" på ny skal man formodenligt også opdatere testsne.


arbejdstegning:
https://dgm.sh/?roomId=O0KwwFHk_AnS0aKzqihgx
