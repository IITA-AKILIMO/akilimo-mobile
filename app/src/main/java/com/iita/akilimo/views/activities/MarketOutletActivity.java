package com.iita.akilimo.views.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.util.Strings;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.MarketOutlet;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.interfaces.IVolleyCallback;
import com.iita.akilimo.models.StarchFactory;
import com.iita.akilimo.rest.RestService;
import com.iita.akilimo.utils.CurrencyHelper;
import com.iita.akilimo.utils.Tools;
import com.iita.akilimo.utils.enums.EnumCountries;
import com.iita.akilimo.utils.enums.EnumProduceType;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MarketOutletActivity extends BaseActivity {

    private String LOG_TAG = MarketOutletActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindString(R.string.title_activity_market_outlet)
    String marketOutletTitle;

    @BindView(R.id.factoryTitle)
    AppCompatTextView factoryTitle;

    @BindView(R.id.produceTypeTitle)
    AppCompatTextView produceTypeTitle;

    @BindView(R.id.unitOfSaleTitle)
    AppCompatTextView unitOfSaleTitle;

    @BindView(R.id.unitPriceTitle)
    AppCompatTextView unitPriceTitle;

    @BindView(R.id.rdGari)
    RadioButton rdGari;

    @BindView(R.id.rd_exact_price)
    RadioButton rdExactPrice;

    @BindView(R.id.rdgMarketOutlet)
    RadioGroup rdgMarketOutlet;

    @BindView(R.id.rdgStarchFactories)
    RadioGroup rdgStarchFactories;

    @BindView(R.id.rdgProduceType)
    RadioGroup rdgProduceType;

    @BindView(R.id.rdgUnitOfSale)
    RadioGroup rdgUnitOfSale;

    @BindView(R.id.rdgUnitPrice)
    RadioGroup rdgUnitPrice;

    @BindView(R.id.marketOutletCard)
    CardView marketOutletCard;

    @BindView(R.id.starchFactoryCard)
    CardView starchFactoryCard;

    @BindView(R.id.produceTypeCard)
    CardView produceTypeCard;

    @BindView(R.id.unitOfSaleCard)
    CardView unitOfSaleCard;

    @BindView(R.id.unitPriceCard)
    CardView unitPriceCard;

    @BindView(R.id.btnFinishMarketOutlet)
    MaterialButton btnFinishMarketOutlet;

    CurrencyHelper currencyHelper;
    private String selectedFactory;
    String produceType;
    String unitOfSale;
    String unitOfSaleText;
    double unitPriceUSD = 0.0;
    double unitPriceLocal = 0.0;
    private double minAmountUSD = 5.00;
    private double maxAmountUSD = 500.00;

    String priceText;
    int unitWeight = 0;
    private MarketOutlet marketOutlet;

    private boolean factoryRequired;
    private boolean otherMarketsRequired;
    private boolean dataIsValid;
    private boolean selectionMade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market_outlet);
        ButterKnife.bind(this);
        context = this;

        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(context);
        queue = Volley.newRequestQueue(context);
        currencyHelper = new CurrencyHelper();

        marketOutlet = objectBoxEntityProcessor.getMarketOutlet();
        if (marketOutlet == null) {
            marketOutlet = new MarketOutlet();
        } else {
            selectedFactory = marketOutlet.getStarchFactory();
        }

        initToolbar();
        initComponent();
    }

    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(marketOutletTitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> closeActivity(false));
    }

    @Override
    protected void initComponent() {
        processStarchFactories();

        rdgMarketOutlet.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            factoryRequired = false;
            otherMarketsRequired = false;
            selectionMade = true;
            switch (radioIndex) {
                case R.id.rdFactory:
                    //show the starch factory option
                    hideAll(true);
                    factoryTitle.setVisibility(View.VISIBLE);
                    starchFactoryCard.setVisibility(View.VISIBLE);
                    factoryRequired = true;
                    produceType = null;
                    unitOfSale = null;
                    unitOfSaleText = null;
                    unitPriceUSD = 0.0;
                    unitPriceLocal = 0.0;
                    unitWeight = 0;
                    break;
                case R.id.rdOtherMarket:
                    //so the other market options
                    hideAll(false);
                    produceTypeTitle.setVisibility(View.VISIBLE);
                    produceTypeCard.setVisibility(View.VISIBLE);
                    selectedFactory = "NA";
                    otherMarketsRequired = true;
                    break;
            }
        });

        rdgStarchFactories.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            int radioButtonId = radioGroup.getCheckedRadioButtonId();
            dataIsValid = false;
            if (radioButtonId > -1) {
                RadioButton radioButton = findViewById(radioButtonId);
                String itemTagIndex = (String) radioButton.getTag();
                StarchFactory selectedStarchFactory = objectBoxEntityProcessor.getSelectedStarchFactoryByTag(itemTagIndex);
                if (selectedStarchFactory != null) {
                    selectedFactory = selectedStarchFactory.getFactoryName();
                }
                dataIsValid = true;
            }
        });

        rdgProduceType.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            switch (radioIndex) {
                case R.id.rdRoots:
                    produceType = EnumProduceType.ROOTS.produce();
                    break;
                case R.id.rdFlour:
                    produceType = EnumProduceType.FLOUR.produce();
                    break;
                case R.id.rdGari:
                    produceType = EnumProduceType.GARI.produce();
                    break;
            }

            //display the next card
            unitOfSaleTitle.setVisibility(View.VISIBLE);
            unitOfSaleCard.setVisibility(View.VISIBLE);
        });

        rdgUnitOfSale.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            switch (radioIndex) {
                case R.id.rd_per_kg:
                    unitOfSale = "kg";
                    unitOfSaleText = "a 1 kg bag";
                    unitWeight = 1;
                    break;
                case R.id.rd_50_kg_bag:
                    unitOfSale = "50 kg bag";
                    unitOfSaleText = "a 50 kg bag";
                    unitWeight = 50;
                    break;
                case R.id.rd_100_kg_bag:
                    unitOfSale = "100 kg bag";
                    unitOfSaleText = "a 100 kg bag";
                    unitWeight = 100;
                    break;
                case R.id.rd_per_tonne:
                    unitOfSale = "tonne";
                    unitOfSaleText = "1 tonne";
                    unitWeight = 1000;
                    break;
            }

            unitPriceTitle.setVisibility(View.VISIBLE);
            unitPriceCard.setVisibility(View.VISIBLE);
        });

        rdgUnitPrice.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            switch (radioIndex) {
                case R.id.rd_20_30_price:
                    unitPriceUSD = 25; //remains in USD
                    break;
                case R.id.rd_30_50_price:
                    unitPriceUSD = 40; //remains in USD
                    break;
                case R.id.rd_50_100_price:
                    unitPriceUSD = 75; //remains in USD
                    break;
                case R.id.rd_100_150_price:
                    unitPriceUSD = 125; //remains in USD
                    break;
                case R.id.rd_150_200_price:
                    unitPriceUSD = 175; //remains in USD
                    break;
                case R.id.rd_exact_price:
                    dataIsValid = false;
                    showExactPriceDialog();
                    break;
            }
            rdExactPrice.setOnClickListener(view -> {
                dataIsValid = false;
                showExactPriceDialog();
            });
        });

        btnFinishMarketOutlet.setOnClickListener(view -> {

            if (validated() && dataIsValid) {
                unitPriceLocal = currencyHelper.convertToLocalCurrency(unitPriceUSD, currency);
                marketOutlet.setStarchFactory(selectedFactory);
                marketOutlet.setProduceType(produceType);
                marketOutlet.setUnitOfSale(unitOfSale);
                marketOutlet.setUnitPriceUSD(unitPriceUSD);
                marketOutlet.setUnitPriceLocalCurrency(unitPriceLocal);
                marketOutlet.setUnitWeightValue(unitWeight);

                long id = objectBoxEntityProcessor.saveMarketOutlet(marketOutlet);
                if (id > 0) {
                    closeActivity(false);
                }
            } else if (!selectionMade) {
                showCustomWarningDialog("Nothing selected", "You have not made any selection", null);
            }
        });
    }

    private boolean validated() {
        if (factoryRequired) {
            if (Strings.isEmptyOrWhitespace(selectedFactory) || selectedFactory.equalsIgnoreCase("NA")) {
                showCustomWarningDialog("Invalid factory", "Please select a starch factory", "OK");
                return false;
            }
        } else if (otherMarketsRequired) {
            if (Strings.isEmptyOrWhitespace(produceType)) {
                showCustomWarningDialog("Invalid produce type", "Please specify a valid produce type", "OK");
                return false;
            }
            if (Strings.isEmptyOrWhitespace(unitOfSale)) {
                showCustomWarningDialog("Invalid unit of sale", "Please specify a valid unit of sale", "OK");
                return false;
            }

            if ((unitWeight <= 0)) {
                showCustomWarningDialog("Invalid unit weight", "Please specify a valid unit weight", "OK");
                return false;
            }

            if ((unitPriceUSD <= 0)) {
                showCustomWarningDialog("Invalid unit price", "Please specify a valid unit price", "OK");
                return false;
            }

            Double minAmount = currencyHelper.convertCurrency(minAmountUSD, currency);
            Double maxAmount = currencyHelper.convertCurrency(maxAmountUSD, currency);

            if (!(unitPriceUSD >= minAmountUSD) || !(unitPriceUSD <= maxAmountUSD)) {
                String message = String.format("Unit price should be between %s %s and %s %s", minAmount, currency, maxAmount, currency);
                showCustomWarningDialog("Invalid unit price", message, "OK");
                dataIsValid = false;
                return false;
            } else {
                dataIsValid = true;
            }
        }

        return true;
    }

    private void hideAll(boolean clearMarket) {
        if (clearMarket) {
            rdgStarchFactories.clearCheck();
        } else {
            rdgProduceType.clearCheck();
            rdgUnitPrice.clearCheck();
            rdgUnitOfSale.clearCheck();
        }
        if (countryCode.equalsIgnoreCase(EnumCountries.NIGERIA.countryCode())) {
            rdGari.setVisibility(View.VISIBLE);
        } else {
            rdGari.setVisibility(View.GONE);
        }

        factoryTitle.setVisibility(View.GONE);
        starchFactoryCard.setVisibility(View.GONE);

        produceTypeTitle.setVisibility(View.GONE);
        produceTypeCard.setVisibility(View.GONE);

        unitPriceTitle.setVisibility(View.GONE);
        unitPriceCard.setVisibility(View.GONE);

        unitOfSaleTitle.setVisibility(View.GONE);
        unitOfSaleCard.setVisibility(View.GONE);
    }

    private void processStarchFactories() {
        final RestService restService = RestService.getInstance(queue, this);
        final ObjectMapper objectMapper = new ObjectMapper();
        restService.setCountryCode(countryCode);
        restService.setEndpoint("v2/starch-factories");

        restService.getJsonArrList(new IVolleyCallback() {
            @Override
            public void onSuccessJsonString(String jsonStringResult) {
            }

            @Override
            public void onSuccessJsonArr(JSONArray jsonArray) {
                try {
                    List<StarchFactory> starchFactoriesList = objectMapper.readValue(jsonArray.toString(), new TypeReference<List<StarchFactory>>() {
                    });

                    objectBoxEntityProcessor.saveStarchFactories(starchFactoriesList);
                    addFactoriesRadioButtons(starchFactoriesList);
                } catch (Exception ex) {
                    Log.i(LOG_TAG, String.format("Error reading list :%s", ex.getMessage()));
                }
            }

            @Override
            public void onSuccessJsonObject(JSONObject jsonObject) {

            }

            @Override
            public void onError(VolleyError volleyError) {
                String error = Tools.parseNetworkError(volleyError).getMessage();
                if (error != null) {
                    Snackbar.make(marketOutletCard, error, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void addFactoriesRadioButtons(@NotNull List<StarchFactory> starchFactoryList) {


        rdgStarchFactories.removeAllViews();

        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int dimension = (int) this.getResources().getDimension(R.dimen.spacing_medium);
        for (StarchFactory factory : starchFactoryList) {

            String radioLabel = factory.getFactoryLabel();
            String factoryNameCountry = factory.getFactoryNameCountry();
            if (!radioLabel.equalsIgnoreCase("NA")) {
                RadioButton radioButton = new RadioButton(this);
                radioButton.setId(View.generateViewId());
                radioButton.setTag(factoryNameCountry);
//            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.label_text_size));


                params.setMargins(0, 0, 0, dimension);
                radioButton.setLayoutParams(params);

                radioButton.setText(radioLabel);

                rdgStarchFactories.addView(radioButton);

                if (factory.getFactoryName().equals(selectedFactory)) {
                    Log.i(LOG_TAG, "The factory matches " + radioButton.getId());
                    radioButton.setChecked(true);
                }
            }
        }

    }

    private void showExactPriceDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_unit_price);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final EditText etUnitPrice = dialog.findViewById(R.id.etUnitPrice);
        etUnitPrice.setText(priceText);

        dialog.findViewById(R.id.bt_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.bt_submit).setOnClickListener(v -> {
            priceText = etUnitPrice.getText().toString().trim();
            if (Strings.isEmptyOrWhitespace(priceText)) {
                dataIsValid = false;
                Toast.makeText(context, "Please enter valid amount", Toast.LENGTH_SHORT).show();
                return;
            } else {
                unitPriceUSD = currencyHelper.convertToUSD(Double.parseDouble(priceText), currency);
            }
            dialog.dismiss();
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

}
