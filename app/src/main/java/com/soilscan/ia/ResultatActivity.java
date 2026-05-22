package com.soilscan.ia;

import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.bumptech.glide.Glide;

public class ResultatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultat);

        ResultatSol r;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            r = getIntent().getSerializableExtra("resultat", ResultatSol.class);
        } else {
            r = (ResultatSol) getIntent().getSerializableExtra("resultat");
        }
        String imageUri = getIntent().getStringExtra("imageUri");

        if (r == null) { finish(); return; }

        // Image analysée
        ImageView imgSol = findViewById(R.id.imgSol);
        Glide.with(this).load(Uri.parse(imageUri)).into(imgSol);

        // Résultat principal
        TextView tvClasse    = findViewById(R.id.tvClasse);
        TextView tvNomFr     = findViewById(R.id.tvNomFr);
        TextView tvConfiance = findViewById(R.id.tvConfiance);
        CardView cardHeader  = findViewById(R.id.cardHeader);

        tvClasse.setText(r.getEmoji() + "  " + r.classe.replace("_", " "));
        tvNomFr.setText(r.nomFr);
        tvConfiance.setText(r.confiancePct);
        cardHeader.setCardBackgroundColor(r.getCouleur());

        // Propriétés
        ((TextView) findViewById(R.id.tvPh)).setText(r.ph);
        ((TextView) findViewById(R.id.tvTexture)).setText(r.texture);
        ((TextView) findViewById(R.id.tvUsage)).setText(r.usage);
        ((TextView) findViewById(R.id.tvDrainage)).setText(r.drainage);
        ((TextView) findViewById(R.id.tvMatiereOrg)).setText(r.matiereOrg);
        ((TextView) findViewById(R.id.tvDate)).setText(r.dateAnalyse);

        // Barre de confiance
        ProgressBar barreConfiance = findViewById(R.id.barreConfiance);
        barreConfiance.setProgress((int) r.confiance);

        // Bouton retour
        findViewById(R.id.btnRetour).setOnClickListener(v -> finish());
    }
}