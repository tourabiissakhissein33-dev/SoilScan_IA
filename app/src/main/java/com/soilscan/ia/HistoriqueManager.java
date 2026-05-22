package com.soilscan.ia;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class HistoriqueManager {

    private static final String PREFS = "soilscan_historique";
    private static final String KEY   = "analyses";
    private static final int    MAX   = 50;

    public static void sauvegarder(Context ctx, ResultatSol r) {
        try {
            SharedPreferences prefs =
                    ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            JSONArray arr = new JSONArray(
                    prefs.getString(KEY, "[]"));

            JSONObject obj = new JSONObject();
            obj.put("classe",     r.classe);
            obj.put("nomFr",      r.nomFr);
            obj.put("confiance",  r.confiance);
            obj.put("ph",         r.ph);
            obj.put("texture",    r.texture);
            obj.put("usage",      r.usage);
            obj.put("drainage",   r.drainage);
            obj.put("matiereOrg", r.matiereOrg);
            obj.put("date",       r.dateAnalyse);
            obj.put("image",      r.cheminImage);

            // Insertion en tête
            JSONArray nouveau = new JSONArray();
            nouveau.put(obj);
            for (int i = 0; i < Math.min(arr.length(), MAX - 1); i++)
                nouveau.put(arr.get(i));

            prefs.edit().putString(KEY, nouveau.toString()).apply();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static List<ResultatSol> charger(Context ctx) {
        List<ResultatSol> liste = new ArrayList<>();
        try {
            SharedPreferences prefs =
                    ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            JSONArray arr = new JSONArray(prefs.getString(KEY, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                ResultatSol r = new ResultatSol();
                r.classe     = obj.getString("classe");
                r.nomFr      = obj.getString("nomFr");
                r.confiance  = obj.getDouble("confiance");
                r.confiancePct = r.confiance + "%";
                r.ph         = obj.getString("ph");
                r.texture    = obj.getString("texture");
                r.usage      = obj.getString("usage");
                r.drainage   = obj.getString("drainage");
                r.matiereOrg = obj.getString("matiereOrg");
                r.dateAnalyse = obj.getString("date");
                r.cheminImage = obj.optString("image", "");
                liste.add(r);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return liste;
    }

    public static void vider(Context ctx) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().remove(KEY).apply();
    }
}