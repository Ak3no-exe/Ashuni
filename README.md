# Ashuni Android

Projet Android qui ouvre `https://ashuni.lol/` dans une WebView Android, avec prise en charge du plein écran et de la navigation retour.

> **À savoir :** le jeu et ses menus sont chargés depuis le site officiel. Ce projet n'embarque pas les fichiers propriétaires du jeu. Une connexion Internet est nécessaire et le fonctionnement dépend de la disponibilité et de la compatibilité du site.

## Générer l'APK avec GitHub Actions

1. Crée un dépôt GitHub et envoie **tout le contenu de ce dossier** à la racine du dépôt (ne mets pas le dossier `AshuniAndroid` dans un deuxième dossier du même nom).
2. Ouvre l'onglet **Actions** du dépôt et active les workflows si GitHub le demande.
3. Le workflow **Build Android APK** démarre automatiquement à chaque push sur `main` ou `master`. Tu peux aussi le lancer avec **Run workflow**.
4. Ouvre l'exécution terminée, descends jusqu'à **Artifacts**, puis télécharge `Ashuni-debug-apk`.
5. Décompresse l'archive téléchargée : elle contient `app-debug.apk`. Transfère-la sur ton téléphone et autorise l'installation depuis cette source si Android le demande.

Le workflow installe Java 17, Gradle 8.9 et Android SDK Platform 35 puis compile `assembleDebug`.

## Compilation locale

Prérequis : Java 17, Gradle 8.9 et Android SDK Platform 35 / Build Tools 35.0.0.

À la racine du projet :

```bash
gradle assembleDebug
```

APK générée : `app/build/outputs/apk/debug/app-debug.apk`.

## Limites

- Il s'agit d'un APK de type WebView, pas d'une copie hors ligne du jeu.
- Le jeu, les cartes, les réglages et le chronomètre disponibles dépendent du site chargé.
- L'APK debug est destinée aux tests. Pour une distribution publique, configure une signature de version release et vérifie les droits sur les marques et contenus utilisés.
