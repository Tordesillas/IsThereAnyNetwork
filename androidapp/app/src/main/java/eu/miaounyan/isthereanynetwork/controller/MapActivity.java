package eu.miaounyan.isthereanynetwork.controller;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eu.miaounyan.isthereanynetwork.R;
import eu.miaounyan.isthereanynetwork.model.RankItem;
import eu.miaounyan.isthereanynetwork.model.SignalStrength;
import eu.miaounyan.isthereanynetwork.service.isthereanynetwork.IsThereAnyNetwork;
import eu.miaounyan.isthereanynetwork.service.isthereanynetwork.IsThereAnyNetworkParams;
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
    private String operatorFilter = "none";
    private IsThereAnyNetwork isThereAnyNetwork;
    private IsThereAnyNetworkService isThereAnyNetworkService;
    private com.shawnlin.numberpicker.NumberPicker picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        /* Network */
        isThereAnyNetwork = new IsThereAnyNetwork();
        isThereAnyNetworkService = isThereAnyNetwork.connect();

        /* Network colors */
        getMapFromNetwork();

        /* Operator ranking */
        createListOperators();
        getOperatorsFromNetwork();

        /* Picker */
        picker = findViewById(R.id.picker);
        picker.setOnValueChangedListener((numPicker, oldVal, newVal) -> getMapFromNetwork());
        picker.setMinValue(-1);
        picker.setMaxValue(23);
        picker.setValue(-1);
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
            if (networkNumber < 0 || networkNumber >= SignalStrength.values().length) {
                // out of range
                Log.e(this.getClass().getName(), "networkNumber out of range: " + networkNumber);
                continue;
            }
            colors.add(SignalStrength.values()[networkNumber].getColor());
        }

        createGrid(colors);
    }

    private void getMapFromNetwork() {
        isThereAnyNetworkService.getNetworkMap(getRequestParameters(isThereAnyNetwork))
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
    }

    private void createGrid(List<Integer> colors) {
        GridView grid = findViewById(R.id.grid);
        grid.setNumColumns((int)Math.sqrt(colors.size()));

        MapAdapter ma = new MapAdapter(this, colors);
        grid.setAdapter(ma);
    }

    private void getOperatorsFromNetwork() {
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

    public void onOperatorClicked(View view) {
        if (((RadioButton) view).isChecked()) {
            switch(view.getId()) {
                case R.id.operator_bouygues:
                    operatorFilter = "Bouygues Telecom"; break;
                case R.id.operator_orange:
                    operatorFilter = "Orange F"; break;
                case R.id.operator_free:
                    operatorFilter = "Free"; break;
                case R.id.operator_sfr:
                    operatorFilter = "F SFR"; break;
                default:
                    operatorFilter = "none"; break;
            }
            getMapFromNetwork();
        }
    }

    public IsThereAnyNetworkParams getRequestParameters(IsThereAnyNetwork isThereAnyNetwork) {
        IsThereAnyNetworkParams params = isThereAnyNetwork.createParams()
                .withLatitudeGreaterThan(43.595810)
                .withLongitudeGreaterThan(7.032711)
                .withLatitudeLowerThan(43.635890)
                .withLongitudeLowerThan(7.087802);
        if (!"none".equals(operatorFilter)) {
            params.withOperatorName(operatorFilter);
        }
        if (picker != null && picker.getValue() != -1) {
            Log.d("UNICORN", picker.getValue()+"");
            params.withTargetHour(picker.getValue());
        }

        return params;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
