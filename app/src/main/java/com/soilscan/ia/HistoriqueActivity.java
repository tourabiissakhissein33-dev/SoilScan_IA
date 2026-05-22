package com.soilscan.ia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import java.util.List;

public class HistoriqueActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique);

        List<ResultatSol> liste = HistoriqueManager.charger(this);
        RecyclerView rv = findViewById(R.id.recyclerHistorique);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new AdapterHistorique(liste));

        TextView tvVide = findViewById(R.id.tvVide);
        tvVide.setVisibility(liste.isEmpty() ? View.VISIBLE : View.GONE);

        findViewById(R.id.btnVider).setOnClickListener(v -> {
            HistoriqueManager.vider(this);
            liste.clear();
            rv.getAdapter().notifyDataSetChanged();
            tvVide.setVisibility(View.VISIBLE);
        });

        findViewById(R.id.btnRetour).setOnClickListener(v -> finish());
    }

    static class AdapterHistorique extends
            RecyclerView.Adapter<AdapterHistorique.VH> {

        private final List<ResultatSol> liste;
        AdapterHistorique(List<ResultatSol> l) { this.liste = l; }

        @Override
        public VH onCreateViewHolder(ViewGroup p, int t) {
            View v = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_historique, p, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int i) {
            ResultatSol r = liste.get(i);
            h.tvClasse.setText(r.getEmoji() + " " +
                    r.classe.replace("_", " "));
            h.tvNomFr.setText(r.nomFr);
            h.tvDate.setText(r.dateAnalyse);
            h.tvConf.setText(r.confiancePct);
            h.indicateur.setBackgroundColor(r.getCouleur());
        }

        @Override public int getItemCount() { return liste.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvClasse, tvNomFr, tvDate, tvConf;
            View indicateur;
            VH(View v) {
                super(v);
                tvClasse    = v.findViewById(R.id.tvClasse);
                tvNomFr     = v.findViewById(R.id.tvNomFr);
                tvDate      = v.findViewById(R.id.tvDate);
                tvConf      = v.findViewById(R.id.tvConf);
                indicateur  = v.findViewById(R.id.indicateur);
            }
        }
    }
}