# Message Broadcast

Message Broadcast sendet in einem einstellbaren Intervall eine Nachricht an alle, die die
erforderlichen Berechtigungen haben. Die Nachrichten werden in einer Datenbank gespeichert und beim
Start synchronisiert.

## Datenbank einrichten

Vor dem ersten Start des Plugins muss die Datenbank angelegt werden. Der hierfür erforderliche SQL
Befehl ist der folgende, der auch als `.sql` Datei vorhanden ist:

``
CREATE TABLE `message_broadcast`
(
`uuid`       VARCHAR(36)   NOT NULL,
`sortId`     INT           NOT NULL,
`content`    VARCHAR(1000) NOT NULL,
`author`     VARCHAR(16)   NOT NULL,
`createdAt`  FLOAT         NOT NULL,
`permission` VARCHAR(100),
PRIMARY KEY (`uuid`)
);
``

## Message Broadcasts verwalten

Message Broadcasts können über den ``/messagebroadcast`` Befehl verwaltet werden.
``/messagebroadcast`` setzt folgende Permission voraus: `messagebroadcast.command.messagebroadcast`

<br>

``/messagebroadcast add <permission> <content>``

Hiermit wird ein neuer Eintrag in der Datenbank angelegt.
Über die Berechtigung kann man z.B. Broadcasts für jeden Spieler individuell ausschalten.

<br>

``/messagebroadcast edit <uuid> <content>``

Der Edit Befehl updatet die Nachricht in der Datenbank.

<br>

``/messagebroadcast delete <uuid>``

Die angegebene Nachricht wird aus der Datenbank gelöscht.

<br>

``/messagebroadcast list``

Es wird eine Liste angezeigt mit den geladenen Nachrichten.

<br>

`/messagebroadcast sort`

Es öffnet sich ein sortiertes Inventar mit allen Nachrichten als Item. Nun soll die Reihenfolge
geändert werden. Beim Schließen wird die
Sortierung der Items im Inventar übernommen und in der Datenbank aktualisiert.

<br>
<br>

Es wird empfohlen den `add` und `edit` Befehl für komplexe Nachrichten in der Konsole zu verwenden,
da hier keine Begrenzung der Eingabelänge besteht.

Die maximale Textlänge ist 1000 Zeichen.
