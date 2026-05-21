"""
SoilScan_IA — Backend FastAPI
Classification des sols par ResNet-50 (97.12% accuracy)

Lancer localement :
    uvicorn main:app --reload --port 8000

Avec ngrok (pour Android) :
    1. Lancer uvicorn en arrière-plan
    2. ngrok http 8000
"""

import torch
import torch.nn as nn
import torchvision.models as models
import torchvision.transforms as T
from PIL import Image
from fastapi import FastAPI, File, UploadFile
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from datetime import datetime
import io

# ── Configuration ─────────────────────────────────────────
DEVICE      = "cuda" if torch.cuda.is_available() else "cpu"
CLASSES     = ["Alluvial_Soil", "Arid_Soil", "Black_Soil",
               "Laterite_Soil", "Mountain_Soil", "Red_Soil", "Yellow_Soil"]
NUM_CLASSES = len(CLASSES)
MODEL_PATH  = "resnet50_best.pth"  # À placer dans le même dossier

# ── Informations pédologiques ────────────────────────────
INFO = {
    "Alluvial_Soil": {
        "nom_fr": "Sol Alluvial", "ph": "6.5–7.5", 
        "texture": "Limono-argileuse", "usage": "Riz, blé, canne à sucre",
        "drainage": "Bon à modéré", "matiere_org": "Élevée (2–4%)"
    },
    "Arid_Soil": {
        "nom_fr": "Sol Aride", "ph": "7.5–9.0",
        "texture": "Sableuse", "usage": "Cultures irriguées, dattes",
        "drainage": "Excessif", "matiere_org": "Très faible (<0.5%)"
    },
    "Black_Soil": {
        "nom_fr": "Sol Noir", "ph": "7.2–8.5",
        "texture": "Argileuse lourde", "usage": "Coton, sorgho, tournesol",
        "drainage": "Lent", "matiere_org": "Modérée (1–2%)"
    },
    "Laterite_Soil": {
        "nom_fr": "Sol Latéritique", "ph": "4.5–6.0",
        "texture": "Argilo-sableuse", "usage": "Thé, café, anacarde",
        "drainage": "Bon", "matiere_org": "Faible (<1%)"
    },
    "Mountain_Soil": {
        "nom_fr": "Sol de Montagne", "ph": "4.5–6.5",
        "texture": "Limono-sableuse", "usage": "Forêts, pomme de terre",
        "drainage": "Bon à excessif", "matiere_org": "Élevée (3–8%)"
    },
    "Red_Soil": {
        "nom_fr": "Sol Rouge", "ph": "5.0–6.5",
        "texture": "Sablo-argileuse", "usage": "Arachides, millet, tabac",
        "drainage": "Bon", "matiere_org": "Faible (0.5–1.5%)"
    },
    "Yellow_Soil": {
        "nom_fr": "Sol Jaune", "ph": "4.5–5.5",
        "texture": "Limono-argileuse", "usage": "Thé, agrumes, riz",
        "drainage": "Modéré à lent", "matiere_org": "Faible (0.5–1%)"
    }
}

# ── Chargement du modèle ──────────────────────────────────
def charger_modele():
    """Charge ResNet-50 avec architecture personnalisée"""
    model = models.resnet50(weights=None)
    in_features = model.fc.in_features
    model.fc = nn.Sequential(
        nn.BatchNorm1d(in_features),
        nn.Dropout(0.4),
        nn.Linear(in_features, 512),
        nn.ReLU(),
        nn.Dropout(0.2),
        nn.Linear(512, NUM_CLASSES)
    )
    model.load_state_dict(torch.load(MODEL_PATH, map_location=DEVICE))
    model.to(DEVICE)
    model.eval()
    return model

print(f"🔄 Chargement du modèle...")
resnet = charger_modele()
print(f"✅ Modèle chargé sur {DEVICE}")

# ── Preprocessing ─────────────────────────────────────────
transform = T.Compose([
    T.Resize((224, 224)),
    T.ToTensor(),
    T.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
])

# ── API FastAPI ───────────────────────────────────────────
app = FastAPI(
    title="SoilScan_IA",
    version="1.0.0",
    description="API de classification des sols par IA — ResNet-50 (97.12% accuracy)"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"]
)

@app.get("/")
def accueil():
    """Route racine — info API"""
    return {
        "status": "ok",
        "message": "SoilScan_IA — API opérationnelle ✅",
        "accuracy": "97.12%",
        "classes": CLASSES,
        "endpoints": {
            "prediction": "/predict (POST)",
            "classes": "/classes (GET)",
            "docs": "/docs"
        }
    }

@app.get("/classes")
def liste_classes():
    """Liste toutes les classes disponibles"""
    return {
        "classes": CLASSES,
        "total": NUM_CLASSES,
        "info": {cls: INFO[cls]["nom_fr"] for cls in CLASSES}
    }

@app.post("/predict")
async def predire(file: UploadFile = File(...)):
    """
    Prédit le type de sol à partir d'une image
    
    Args:
        file: Image du sol (JPG, PNG)
    
    Returns:
        JSON avec classe prédite, confiance, propriétés pédologiques
    """
    try:
        # Lecture image
        image = Image.open(io.BytesIO(await file.read())).convert("RGB")
        tensor = transform(image).unsqueeze(0).to(DEVICE)

        # Inférence
        with torch.no_grad():
            logits = resnet(tensor)
            probas = torch.softmax(logits, dim=1)[0].cpu().numpy()

        # Résultats
        resultats = sorted(zip(CLASSES, probas), key=lambda x: -x[1])
        classe_pred = resultats[0][0]
        confiance = float(resultats[0][1]) * 100
        info_sol = INFO.get(classe_pred, {})

        return JSONResponse({
            "succes": True,
            "classe": classe_pred,
            "nom_fr": info_sol.get("nom_fr", classe_pred),
            "confiance": round(confiance, 2),
            "confiance_pct": f"{confiance:.1f}%",
            "proprietes": {
                "ph": info_sol.get("ph"),
                "texture": info_sol.get("texture"),
                "usage": info_sol.get("usage"),
                "drainage": info_sol.get("drainage"),
                "matiere_org": info_sol.get("matiere_org")
            },
            "top3": [
                {"classe": c, "probabilite_pct": f"{p*100:.1f}%"}
                for c, p in resultats[:3]
            ],
            "date_analyse": datetime.now().strftime("%d/%m/%Y %H:%M:%S")
        })

    except Exception as e:
        return JSONResponse(
            {"succes": False, "erreur": str(e)},
            status_code=500
        )

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
