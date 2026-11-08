# THE Bot App

Befehls-Übersicht für den THE Bot als Android-App (WebView). Die fertige APK liegt unter [Releases](../../releases/latest), direkter Download: `THE-Bot.apk`.

Die App prüft beim Öffnen im Hintergrund, ob es eine neuere Version gibt.

## Web-Version (iPhone und Browser)
`docs/index.html` ist dieselbe Seite wie in der Android-App und wird von `node tools/zeilen.js` automatisch mitgezogen.
Mit GitHub Pages (Einstellungen, Pages, Quelle `main` und Ordner `/docs`) ist sie im Browser erreichbar.
iPhone-Nutzer öffnen den Link in Safari und wählen „Zum Home-Bildschirm“.

## Werbung und Material
Der Ordner [`werbung`](werbung) enthält:
- `THEBot-Werbevideo.mp4`: Werbevideo, Hochformat, mit Musik, ohne Stimme
- `THEBot-App-Seite.html`: die App-Seite als eigenständige Datei, zum Herunterladen und im Browser Öffnen (immer aktuell ist `docs/index.html`)
- `Supporter-Testfragen.txt` und `Supporter-Setup-Fragen.txt`: Fragenlisten zum Testen künftiger Supporter
