# Diese Dateien nach npx cap add android kopieren

Nach dem Ausführen von `npx cap add android` diese Dateien in dein Projekt kopieren:

## strings.xml
Von: `android-overrides/strings.xml`
Nach: `android/app/src/main/res/values/strings.xml`
(Vorhandene Datei ERSETZEN)

## colors.xml  
Von: `android-overrides/colors.xml`
Nach: `android/app/src/main/res/values/colors.xml`
(Vorhandene Datei ERSETZEN)

## network_security_config.xml
Von: `android-overrides/network_security_config.xml`
Nach: `android/app/src/main/res/xml/network_security_config.xml`
(Neu erstellen, Ordner xml/ ggf. zuerst anlegen)

Dann in `android/app/src/main/AndroidManifest.xml` im <application> Tag hinzufügen:
    android:networkSecurityConfig="@xml/network_security_config"
