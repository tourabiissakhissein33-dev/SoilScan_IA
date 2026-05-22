package com.soilscan.ia;

import java.io.Serializable;

public class ResultatSol implements Serializable {
    public String classe;
    public String nomFr;
    public double confiance;
    public String confiancePct;
    public String ph;
    public String texture;
    public String usage;
    public String drainage;
    public String matiereOrg;
    public String dateAnalyse;
    public String cheminImage;

    public String getEmoji() {
        switch (classe) {
            case "Alluvial_Soil":  return "🟤";
            case "Arid_Soil":      return "🟡";
            case "Black_Soil":     return "⚫";
            case "Laterite_Soil":  return "🔴";
            case "Mountain_Soil":  return "🪨";
            case "Red_Soil":       return "🧱";
            case "Yellow_Soil":    return "🌕";
            default:               return "🌍";
        }
    }

    public int getCouleur() {
        switch (classe) {
            case "Alluvial_Soil":  return 0xFF8B6914;
            case "Arid_Soil":      return 0xFFD4A017;
            case "Black_Soil":     return 0xFF2C2C2C;
            case "Laterite_Soil":  return 0xFFB22222;
            case "Mountain_Soil":  return 0xFF708090;
            case "Red_Soil":       return 0xFFCD5C5C;
            case "Yellow_Soil":    return 0xFFDAA520;
            default:               return 0xFF1D9E75;
        }
    }
}