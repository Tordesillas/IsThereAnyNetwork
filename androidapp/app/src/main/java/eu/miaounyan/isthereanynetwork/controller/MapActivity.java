package eu.miaounyan.isthereanynetwork.controller;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eu.miaounyan.isthereanynetwork.R;
import eu.miaounyan.isthereanynetwork.model.RankItem;
import eu.miaounyan.isthereanynetwork.model.SignalStrength;
import eu.miaounyan.isthereanynetwork.service.isthereanynetwork.IsThereAnyNetwork;
import eu.miaounyan.isthereanynetwork.service.isthereanynetwork.IsThereAnyNetworkService;
import eu.miaounyan.isthereanynetwork.view.MapAdapter;
import eu.miaounyan.isthereanynetwork.view.RankingListAdapter;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MapActivity extends AppCompatActivity {
    private List<RankItem> operators;
    private RankingListAdapter rankingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        /* Network */
        IsThereAnyNetwork isThereAnyNetwork = new IsThereAnyNetwork();
        IsThereAnyNetworkService isThereAnyNetworkService = isThereAnyNetwork.connect();

        /* Network colors */
        isThereAnyNetworkService.getNetworkMap(isThereAnyNetwork.defaultParams())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Integer>>() {
                    @Override
                    public void onSubscribe(Disposable d) {}

                    @Override
                    public void onNext(List<Integer> integers) {
                        loadMap(integers);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(this.getClass().getName(), "Error: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {}
                });

        /* Operator ranking */
        createListOperators();
        isThereAnyNetworkService.getOperatorRanking(isThereAnyNetwork.defaultParams())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Map<String, Double>>() {
            @Override
            public void onSubscribe(Disposable d) {}

            @Override
            public void onNext(Map<String, Double> stringIntegerMap) {
                loadOperators(stringIntegerMap);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(this.getClass().getName(), "Error: " + e.getMessage());
            }

            @Override
            public void onComplete() {}
        });
    }

    private void loadOperators(Map<String, Double> operatorMap) {
        operators.clear();
        for (Map.Entry<String, Double> operator : operatorMap.entrySet()) {
            operators.add(new RankItem(operator.getKey(), operator.getValue()));
        }
        rankingAdapter.notifyDataSetChanged();
    }

    private void createListOperators() {
        operators = new ArrayList<>();
        rankingAdapter = new RankingListAdapter(this, operators);
        ((ListView) findViewById(R.id.ranking)).setAdapter(rankingAdapter);
    }

    private void loadMap(List<Integer> networkMap) {
        List<Integer> colors = new LinkedList<>();

        for (int networkNumber : networkMap) {
            colors.add(SignalStrength.values()[networkNumber].getColor());
        }

        createGrid(colors);
    }

    private void createGrid(List<Integer> colors) {
        GridView grid = findViewById(R.id.grid);
        grid.setNumColumns((int)Math.sqrt(colors.size()));

        MapAdapter ma = new MapAdapter(this, colors);
        grid.setAdapter(ma);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
