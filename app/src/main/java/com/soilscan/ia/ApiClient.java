package com.soilscan.ia;

import okhttp3.*;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    // ⚠️ REMPLACE PAR TON URL NGROK
    private static final String BASE_URL =
            "https://unison-shredding-disown.ngrok-free.dev";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    public interface Callback {
        void onSuccess(ResultatSol resultat);
        void onError(String erreur);
    }

    public static void predire(File imageFile, Callback callback) {
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", imageFile.getName(),
                        RequestBody.create(imageFile,
                                MediaType.parse("image/jpeg")))
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "/predict")
                .post(body)
                .addHeader("ngrok-skip-browser-warning", "true")
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Erreur réseau : " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String json = response.body().string();
                    JSONObject obj = new JSONObject(json);

                    if (obj.getBoolean("succes")) {
                        ResultatSol r = new ResultatSol();
                        r.classe     = obj.getString("classe");
                        r.nomFr      = obj.getString("nom_fr");
                        r.confiance  = obj.getDouble("confiance");
                        r.confiancePct = obj.getString("confiance_pct");
                        r.dateAnalyse  = obj.getString("date_analyse");

                        JSONObject props = obj.getJSONObject("proprietes");
                        r.ph         = props.getString("ph");
                        r.texture    = props.getString("texture");
                        r.usage      = props.getString("usage");
                        r.drainage   = props.getString("drainage");
                        r.matiereOrg = props.getString("matiere_org");

                        callback.onSuccess(r);
                    } else {
                        callback.onError(obj.getString("erreur"));
                    }
                } catch (Exception e) {
                    callback.onError("Erreur parsing : " + e.getMessage());
                }
            }
        });
    }
}