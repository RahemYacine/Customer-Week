# Ooredoo Customer Week — appli Android (APK)

Sans clé API : l'appli envoie le Master Visual + la photo de l'employé + le prompt
vers l'appli ChatGPT via le partage Android.

## Obtenir l'APK (sans installer Android Studio)
1. Créez un dépôt GitHub privé et poussez tout le contenu de ce dossier (branche `main`).
2. Onglet **Actions** > **Build APK** > attendez la fin (3-6 min).
3. Téléchargez l'artefact **OoredooCustomerWeek-apk** (zip contenant `app-debug.apk`).
4. Installez l'APK sur le téléphone (autoriser les sources inconnues). L'appli ChatGPT doit être installée.

## Ou avec Android Studio
Ouvrez le dossier, puis Build > Build APK(s).

## Interface
`app/src/main/assets/index.html` (contient le Master Prompt, entre les balises `master-prompt`).
