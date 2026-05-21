 # SoilScan_IA — Classification des Profils Pédologiques

![Python](https://img.shields.io/badge/Python-3.12-blue)
![PyTorch](https://img.shields.io/badge/PyTorch-2.1-orange)
![FastAPI](https://img.shields.io/badge/FastAPI-0.110-green)
![Android](https://img.shields.io/badge/Android-Java-brightgreen)
![Accuracy](https://img.shields.io/badge/Accuracy-97.12%25-success)

[![Open In Colab](https://colab.research.google.com/assets/colab-badge.svg)](https://colab.research.google.com/github/tourabiissakhissein33-dev/SoilScan_IA/blob/main/notebook/SoilScan_IA.ipynb)

> Application mobile Android de classification des sols par intelligence artificielle.

**Sujet 27** — Pipeline IA complet · Interface mobile · Backend FastAPI

---

## 📊 Résultats

| Métrique | Valeur |
|---|---|
| **Accuracy test** | **97.12%** |
| Accuracy validation | 96.99% |
| Dataset | 5096 images — 7 classes |
| Modèle | ResNet-50 fine-tuné (layer4 + fc) |
| Époques | 25 |
| Source | ai4a-lab (Kaggle) |

## 🌱 Classes détectées

| Classe | Nom français | pH | Texture |
|---|---|---|---|
| Alluvial_Soil | Sol Alluvial | 6.5–7.5 | Limono-argileuse |
| Arid_Soil | Sol Aride | 7.5–9.0 | Sableuse |
| Black_Soil | Sol Noir | 7.2–8.5 | Argileuse lourde |
| Laterite_Soil | Sol Latéritique | 4.5–6.0 | Argilo-sableuse |
| Mountain_Soil | Sol de Montagne | 4.5–6.5 | Limono-sableuse |
| Red_Soil | Sol Rouge | 5.0–6.5 | Sablo-argileuse |
| Yellow_Soil | Sol Jaune | 4.5–5.5 | Limono-argileuse |

## 🏗️ Architecture

## 🚀 Lancement rapide

### Backend (Google Colab)

1. Ouvre le notebook dans Colab (bouton ci-dessus)
2. **Runtime → Modifier le type de runtime → T4 GPU**
3. Exécute **Cellule A** (restauration depuis Drive)
4. Exécute **Cellule B** (lancement serveur + ngrok)
5. Copie l'URL ngrok affichée

### Android Studio

1. Ouvre `AndroidStudioProjects/SoilScanIA`
2. Dans `Config.java` :
```java
   public static final String BASE_URL = "https://NOUVELLE-URL.ngrok-free.app";
```
3. **Build → Run** sur ton téléphone

## 🛠️ Stack technique

**IA & Backend**
- Python 3.12
- PyTorch 2.1 + torchvision
- ResNet-50 (ImageNet pré-entraîné)
- FastAPI + Uvicorn
- ngrok (tunnel public)

**Mobile**
- Android Java (API 24+)
- OkHttp3 4.12.0
- Glide 4.16.0
- Material Design

**Dataset**
- ai4a-lab/comprehensive-soil-classification-datasets (Kaggle)
- 5096 images, 7 classes

## 📁 Structure du projet

## 📝 Notes importantes

### Fichier de poids

Le fichier `resnet50_best.pth` (~100 MB) n'est **pas inclus** dans ce dépôt (trop lourd pour GitHub).

**Deux options :**
1. Générer en exécutant le notebook complet (cellules 1→10)
2. Télécharger depuis Google Drive (lien privé)

### URL ngrok

L'URL change à chaque session Colab. Pense à mettre à jour `Config.java` après chaque redémarrage.

## 📱 Captures d'écran

### Interface mobile
[Ajouter screenshots ici]

### Résultats entraînement
[Ajouter courbes + matrice confusion]

## 🎯 Fonctionnalités

- ✅ Classification 7 types de sols
- ✅ Confiance de prédiction
- ✅ Propriétés pédologiques (pH, texture, usage)
- ✅ Historique des analyses
- ✅ Interface Android Material Design
- ✅ Backend API REST
- ✅ Notebook reproductible

## 📖 Utilisation

### Tester l'API (Swagger UI)

Une fois le serveur lancé, accède à la doc interactive :

### Exemple de requête

```bash
curl -X POST "https://VOTRE-URL.ngrok-free.app/predict" \
  -H "accept: application/json" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@sol_rouge.jpg"
```

## 🔬 Méthodologie

1. **Dataset** : Comprehensive Soil Classification (Kaggle)
2. **Split** : 70% train / 15% val / 15% test (stratifié)
3. **Augmentation** : Flip, rotation, brightness, HSV, bruit
4. **Modèle** : ResNet-50 ImageNet → fine-tuning layer4 + nouvelle tête fc
5. **Loss** : CrossEntropyLoss pondérée par classe
6. **Optimizer** : AdamW (lr=1e-4, weight_decay=1e-4)
7. **Scheduler** : OneCycleLR (max_lr=1e-3)
8. **Époques** : 25

## 🎓 Auteur

**Tourabiissa Khissein**  
Projet IA — Sujet 27 : Classification pédologique par IA

---

⭐ N'hésite pas à star ce projet si tu le trouves utile !
