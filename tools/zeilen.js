// Zählt die Zeilen Code des Bots und trägt sie in die App ein.
// Aufruf: node tools/zeilen.js <Pfad zum Bot-Ordner>
const fs = require("fs");
const path = require("path");
const wurzel = process.argv[2] ? path.resolve(process.argv[2]) : path.join(__dirname, "..");
const LABELS = {
  "bot.js": "Bot-Kern",
  "games.js": "Spiele",
  "extras.js": "Statistik, Schicksal, Versicherung",
  "autos.js": "Autos und Sprit",
  "sync.js": "Datenabgleich",
  "ki/index.js": "Kim (KI)",
};
const zaehle = (f) => fs.readFileSync(f, "utf8").split("\n").length - (fs.readFileSync(f, "utf8").endsWith("\n") ? 1 : 0);
const dateien = [];
for (const n of fs.readdirSync(wurzel)) if (n.endsWith(".js")) dateien.push(n);
if (fs.existsSync(path.join(wurzel, "ki"))) for (const n of fs.readdirSync(path.join(wurzel, "ki"))) if (n.endsWith(".js")) dateien.push("ki/" + n);
const liste = dateien.map((n) => ({ name: n, label: LABELS[n] || n, zeilen: zaehle(path.join(wurzel, n)) }));
const app = path.join(__dirname, "..", "app/src/main");
const appDateien = [["assets/index.html", "App-Oberfläche"], ["java/de/thebot/befehle/MainActivity.java", "Android-App"]];
for (const [rel, label] of appDateien) liste.push({ name: rel.split("/").pop(), label, zeilen: zaehle(path.join(app, rel)) });
liste.sort((a, b) => b.zeilen - a.zeilen);
const html = path.join(app, "assets/index.html");
let t = fs.readFileSync(html, "utf8");
const neu = "/*CODE*/" + JSON.stringify(liste) + "/*END*/";
if (!t.includes("/*CODE*/")) throw new Error("Marker fehlt");
t = t.replace(/\/\*CODE\*\/[\s\S]*?\/\*END\*\//, () => neu);
fs.writeFileSync(html, t);
console.log(liste.map((x) => `${x.name}: ${x.zeilen}`).join("\n"), "\nSumme:", liste.reduce((s, x) => s + x.zeilen, 0));
