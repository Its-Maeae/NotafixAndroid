# Notafix Android App — Vollständige Installationsanleitung

## Übersicht

Die App ist eine Capacitor-WebView-App. Das bedeutet:
- Die App öffnet `https://notafix.mooo.com` in einer nativen Android-Shell
- Dein Raspberry Pi ist der Server (Next.js + Datenbank)
- Die App funktioniert solange der Pi erreichbar ist

---

## Was du brauchst

- Windows/Mac/Linux PC
- Node.js 18+ (https://nodejs.org)
- Android Studio (https://developer.android.com/studio)
- Android-Handy mit aktiviertem USB-Debugging ODER ein Emulator
- Dein Raspberry Pi läuft mit Notafix auf notafix.mooo.com

---

## Schritt 1: Node.js prüfen

```bash
node --version   # sollte v18 oder höher sein
npm --version
```

---

## Schritt 2: Android Studio installieren

1. Lade Android Studio von https://developer.android.com/studio herunter
2. Installiere es mit Standard-Einstellungen
3. Beim ersten Start: "Standard" Setup wählen, alles installieren lassen
4. Warte bis der SDK-Download fertig ist (kann 10–20 Minuten dauern)

### Android SDK Pfad merken:
- Windows: `C:\Users\DEINNAME\AppData\Local\Android\Sdk`
- Mac: `~/Library/Android/sdk`
- Linux: `~/Android/Sdk`

### Umgebungsvariable setzen (Windows):
1. Suche "Umgebungsvariablen" im Startmenü
2. "Neue" Systemvariable: Name `ANDROID_HOME`, Wert = dein SDK-Pfad
3. In der PATH-Variable hinzufügen: `%ANDROID_HOME%\platform-tools`

---

## Schritt 3: Projekt einrichten

Entpacke `notafix-android.zip` in einen Ordner, z.B. `C:\Projekte\notafix-android\`

```bash
cd notafix-android
npm install
```

---

## Schritt 4: Android-Plattform hinzufügen

```bash
npx cap add android
```

Das erstellt einen `android/` Ordner mit dem kompletten Android-Projekt.

---

## Schritt 5: Projekt synchronisieren

```bash
npx cap sync android
```

---

## Schritt 6: App-Icon und Splash Screen (optional aber empfohlen)

Lege eine `icon.png` (1024x1024px) und `splash.png` (2732x2732px) in den Ordner.

Dann mit dem Capacitor Assets Tool:
```bash
npm install -g @capacitor/assets
npx capacitor-assets generate --android
```

---

## Schritt 7: In Android Studio öffnen

```bash
npx cap open android
```

Android Studio öffnet sich automatisch mit dem Projekt.

### Warte bis Android Studio fertig ist:
- Unten rechts siehst du einen Fortschrittsbalken ("Gradle sync")
- Warte bis dieser fertig ist (2–5 Minuten beim ersten Mal)

---

## Schritt 8: App auf dein Handy laden

### Handy vorbereiten:
1. Einstellungen → Über das Telefon → 7x auf "Build-Nummer" tippen → Entwickleroptionen aktiviert
2. Einstellungen → Entwickleroptionen → USB-Debugging aktivieren
3. Handy per USB an PC anschließen
4. Auf dem Handy "Immer von diesem Computer erlauben" bestätigen

### In Android Studio:
1. Oben in der Toolbar siehst du dein Gerät in der Dropdown-Liste
2. Klicke auf den grünen ▶ Play-Button
3. Die App wird gebaut und auf dein Handy übertragen (2–5 Minuten)

---

## Schritt 9: APK-Datei erstellen (zum Weitergeben)

In Android Studio:
1. Menü → **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
2. Warte bis der Build fertig ist
3. Klicke auf "locate" in der Benachrichtigung unten rechts
4. Die APK liegt in: `android/app/build/outputs/apk/debug/app-debug.apk`

### APK auf Handy installieren ohne Android Studio:
1. APK-Datei per USB/WhatsApp/E-Mail aufs Handy übertragen
2. Datei öffnen → "Aus unbekannten Quellen installieren" erlauben
3. Installieren

---

## Raspberry Pi Setup

Damit die App funktioniert, muss dein Pi erreichbar sein:

### Next.js als Dienst starten (systemd):

SSH auf deinen Pi, dann:

```bash
sudo nano /etc/systemd/system/notafix.service
```

Inhalt:
```ini
[Unit]
Description=Notafix Next.js Server
After=network.target

[Service]
Type=simple
User=pi
WorkingDirectory=/home/pi/notafix
ExecStart=/usr/bin/npm start
Restart=on-failure
RestartSec=10
Environment=NODE_ENV=production
Environment=PORT=3000

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable notafix
sudo systemctl start notafix
sudo systemctl status notafix
```

### HTTPS mit Nginx + Let's Encrypt (PFLICHT für die App):

Die Android App benötigt HTTPS. HTTP reicht nicht.

```bash
sudo apt install nginx certbot python3-certbot-nginx -y

# Nginx Konfiguration
sudo nano /etc/nginx/sites-available/notafix
```

Nginx-Config:
```nginx
server {
    server_name notafix.mooo.com;

    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/notafix /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx

# SSL-Zertifikat holen (kostenlos):
sudo certbot --nginx -d notafix.mooo.com
```

Certbot fragt nach deiner E-Mail und richtet HTTPS automatisch ein.

### Router-Portweiterleitung:
- Port 80 → Raspberry Pi IP → Port 80
- Port 443 → Raspberry Pi IP → Port 443

---

## Häufige Probleme

**"SDK not found"**: ANDROID_HOME Umgebungsvariable nicht gesetzt → Schritt 2 wiederholen

**"Gradle sync failed"**: Internet-Verbindung prüfen, Android Studio neu starten

**App zeigt "Keine Verbindung"**: 
- Ist notafix.mooo.com im Browser erreichbar?
- Hat dein Pi ein gültiges SSL-Zertifikat?
- Prüfe mit: `curl https://notafix.mooo.com`

**"cleartext traffic not permitted"**: HTTP statt HTTPS wird verwendet → SSL einrichten

**App installiert sich nicht (APK)**: Einstellungen → Sicherheit → "Installation aus unbekannten Quellen" für Dateien-App erlauben

---

## Offline-Verhalten

Die App zeigt einen Ladebildschirm wenn kein Internet da ist.
Für echte Offline-Funktionalität (Noten ohne Internet eintragen) müsste
die App um eine lokale SQLite-Datenbank mit Sync-Logik erweitert werden —
das ist ein separates, größeres Projekt.

