# Notafix Android App

## Voraussetzungen

- **Android Studio** (Ladrillos Iguana oder neuer): https://developer.android.com/studio
- **Node.js** (bereits installiert für Notafix)
- **JDK 17** (wird mit Android Studio mitgeliefert)
- Dein Raspberry Pi läuft und ist unter `notafix.mooo.com` erreichbar

---

## Schritt-für-Schritt Anleitung

### 1. Abhängigkeiten installieren

```bash
cd notafix-android
npm install
```

### 2. Android-Plattform initialisieren

```bash
npx cap sync android
```

### 3. Projekt in Android Studio öffnen

```bash
npx cap open android
```

Android Studio öffnet sich automatisch mit dem Projekt.

### 4. App bauen und auf Handy installieren

**Option A – USB Debug (empfohlen zum Testen):**
1. Auf dem Handy: Einstellungen → Über das Telefon → Buildnummer 7x tippen → Entwickleroptionen → USB-Debugging aktivieren
2. Handy per USB an PC anschließen
3. In Android Studio oben das Gerät auswählen
4. ▶ Run drücken → App wird installiert und startet

**Option B – APK exportieren:**
1. In Android Studio: Build → Build Bundle(s) / APK(s) → Build APK(s)
2. APK liegt dann in: `android/app/build/outputs/apk/debug/app-debug.apk`
3. Diese Datei per USB/E-Mail auf dein Handy übertragen
4. Auf dem Handy: Einstellungen → Sicherheit → "Unbekannte Quellen" / "Apps aus unbekannten Quellen" erlauben
5. APK-Datei öffnen und installieren

**Option C – Signierte Release APK (für Weitergabe):**
1. Build → Generate Signed Bundle / APK
2. Create new keystore → Daten ausfüllen und merken!
3. Release APK wird erstellt

---

## Offline-Verhalten

Die App lädt `https://notafix.mooo.com` beim Start.

- **Online:** Vollständige Funktionalität, Daten werden live von deinem Raspberry Pi geladen
- **Offline:** Die WebView zeigt die zuletzt geladene Version (Browser-Cache). Noten können nicht gespeichert werden, solange kein Internet besteht.

> **Hinweis zu vollständiger Offline-Unterstützung:** Für echtes Offline-Speichern müsste die Next.js-App um Service Worker + lokale Datenbank (IndexedDB) erweitert werden. Das ist ein separates größeres Feature.

---

## HTTPS auf dem Raspberry Pi einrichten (wichtig!)

Die App erlaubt nur HTTPS-Verbindungen. Stelle sicher, dass dein Raspberry Pi HTTPS unterstützt.

### Mit Caddy (einfachste Methode):

```bash
# Auf dem Raspberry Pi:
sudo apt install caddy

# /etc/caddy/Caddyfile:
notafix.mooo.com {
    reverse_proxy localhost:3000
}

sudo systemctl restart caddy
```

Caddy holt automatisch ein Let's Encrypt Zertifikat.

### Mit nginx + Certbot:

```bash
sudo apt install nginx certbot python3-certbot-nginx
sudo certbot --nginx -d notafix.mooo.com
```

Dann in `/etc/nginx/sites-available/notafix`:
```nginx
server {
    listen 443 ssl;
    server_name notafix.mooo.com;
    
    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_cache_bypass $http_upgrade;
    }
}
```

---

## Notafix als Systemdienst auf dem Pi einrichten

Damit Notafix beim Pi-Start automatisch läuft:

```bash
sudo nano /etc/systemd/system/notafix.service
```

```ini
[Unit]
Description=Notafix Web App
After=network.target

[Service]
Type=simple
User=pi
WorkingDirectory=/home/pi/notafix
Environment=NODE_ENV=production
ExecStart=/usr/bin/npm start
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl enable notafix
sudo systemctl start notafix
```

---

## Häufige Probleme

| Problem | Lösung |
|---|---|
| "NET::ERR_CERT_INVALID" | HTTPS mit gültigem Zertifikat einrichten (Caddy/Certbot) |
| App lädt nicht | Pi läuft? Domain erreichbar? `ping notafix.mooo.com` testen |
| "Unbekannte Quellen" Fehler | Einstellungen → Sicherheit → Installation unbekannter Apps erlauben |
| Gradle Sync schlägt fehl | In Android Studio: File → Invalidate Caches → Restart |
| SDK nicht gefunden | Android Studio → SDK Manager → Android 14 (API 34) installieren |

---

## App-Konfiguration ändern

Alle Einstellungen in `capacitor.config.json`:
- `server.url` – URL deiner Notafix-Instanz
- `appId` – Eindeutige App-ID (nicht ändern nach Installation)
- `appName` – Name der App auf dem Handy

Nach Änderungen: `npx cap sync android` ausführen.
