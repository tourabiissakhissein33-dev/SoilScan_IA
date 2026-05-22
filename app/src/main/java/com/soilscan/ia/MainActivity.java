package com.soilscan.ia;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.google.android.material.button.MaterialButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // ── Vues ─────────────────────────────────────────────────────────────────
    private ImageView    imageView;
    private MaterialButton btnCamera, btnGalerie, btnAnalyser;
    private ImageButton  btnHistorique;
    private ProgressBar  progressBar;
    private TextView     tvStatut;

    // ── État ──────────────────────────────────────────────────────────────────
    private File fichierImage;
    private Uri  uriImage;

    // ── Launchers ─────────────────────────────────────────────────────────────
    private final ActivityResultLauncher<Uri> lanceurCamera =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicture(),
                    succes -> { if (succes) afficherImage(uriImage); }
            );

    private final ActivityResultLauncher<String> lanceurGalerie =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            uriImage = uri;
                            afficherImage(uri);
                        }
                    }
            );

    private final ActivityResultLauncher<String[]> lanceurPermission =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {}
            );

    // ── Cycle de vie ──────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation des vues
        imageView     = findViewById(R.id.imageView);
        btnCamera     = findViewById(R.id.btnCamera);
        btnGalerie    = findViewById(R.id.btnGalerie);
        btnAnalyser   = findViewById(R.id.btnAnalyser);
        btnHistorique = findViewById(R.id.btnHistorique);  // ImageButton ✅
        progressBar   = findViewById(R.id.progressBar);
        tvStatut      = findViewById(R.id.tvStatut);

        // Demande des permissions
        demanderPermissions();

        // Listeners
        btnCamera.setOnClickListener(v -> ouvrirCamera());
        btnGalerie.setOnClickListener(v -> lanceurGalerie.launch("image/*"));
        btnAnalyser.setOnClickListener(v -> analyserImage());
        btnHistorique.setOnClickListener(v ->
                startActivity(new Intent(this, HistoriqueActivity.class)));
    }

    // ── Permissions ───────────────────────────────────────────────────────────
    private void demanderPermissions() {
        lanceurPermission.launch(new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_MEDIA_IMAGES
        });
    }

    // ── Caméra ────────────────────────────────────────────────────────────────
    private void ouvrirCamera() {
        try {
            fichierImage = creerFichierImage();
            uriImage = FileProvider.getUriForFile(
                    this,
                    "com.soilscan.ia.fileprovider",
                    fichierImage
            );
            lanceurCamera.launch(uriImage);
        } catch (IOException e) {
            Toast.makeText(this, "Erreur caméra : " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private File creerFichierImage() throws IOException {
        String timestamp = new SimpleDateFormat(
                "yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("SOIL_" + timestamp, ".jpg", dir);
    }

    // ── Affichage image ───────────────────────────────────────────────────────
    private void afficherImage(Uri uri) {
        imageView.setImageURI(uri);
        btnAnalyser.setEnabled(true);
        tvStatut.setText("✅ Image prête — appuie sur Analyser");

        // Cache le placeholder
        View placeholder = findViewById(R.id.layoutPlaceholder);
        if (placeholder != null) placeholder.setVisibility(View.GONE);
    }

    // ── Analyse ───────────────────────────────────────────────────────────────
    private void analyserImage() {
        if (uriImage == null) {
            Toast.makeText(this, "Sélectionne une image d'abord",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // UI — chargement
        progressBar.setVisibility(View.VISIBLE);
        btnAnalyser.setEnabled(false);
        btnCamera.setEnabled(false);
        btnGalerie.setEnabled(false);
        tvStatut.setText("⏳ Analyse en cours...");

        try {
            File fileTemp = uriVersFile(uriImage);

            ApiClient.predire(fileTemp, new ApiClient.Callback() {

                @Override
                public void onSuccess(ResultatSol resultat) {
                    resultat.cheminImage = uriImage.toString();
                    HistoriqueManager.sauvegarder(MainActivity.this, resultat);

                    runOnUiThread(() -> {
                        // UI — succès
                        progressBar.setVisibility(View.GONE);
                        btnCamera.setEnabled(true);
                        btnGalerie.setEnabled(true);
                        tvStatut.setText("✅ Analyse terminée !");

                        // Navigation vers ResultatActivity
                        Intent intent = new Intent(
                                MainActivity.this, ResultatActivity.class);
                        intent.putExtra("resultat", resultat);
                        intent.putExtra("imageUri", uriImage.toString());
                        startActivity(intent);
                    });
                }

                @Override
                public void onError(String erreur) {
                    runOnUiThread(() -> {
                        // UI — erreur
                        progressBar.setVisibility(View.GONE);
                        btnAnalyser.setEnabled(true);
                        btnCamera.setEnabled(true);
                        btnGalerie.setEnabled(true);
                        tvStatut.setText("❌ Erreur : " + erreur);
                        Toast.makeText(MainActivity.this,
                                "❌ " + erreur, Toast.LENGTH_LONG).show();
                    });
                }
            });

        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            btnAnalyser.setEnabled(true);
            btnCamera.setEnabled(true);
            btnGalerie.setEnabled(true);
            tvStatut.setText("❌ Erreur : " + e.getMessage());
        }
    }

    // ── Utilitaires ───────────────────────────────────────────────────────────
    private File uriVersFile(Uri uri) throws IOException {
        InputStream is  = getContentResolver().openInputStream(uri);
        File temp       = File.createTempFile("upload", ".jpg", getCacheDir());
        FileOutputStream fos = new FileOutputStream(temp);
        byte[] buffer   = new byte[4096];
        int n;
        while ((n = is.read(buffer)) != -1) fos.write(buffer, 0, n);
        fos.close();
        is.close();
        return temp;
    }
}