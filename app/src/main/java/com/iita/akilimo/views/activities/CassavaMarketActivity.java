package com.iita.akilimo.views.activities;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

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
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.util.Strings;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.CassavaMarketOutlet;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.interfaces.IVolleyCallback;
import com.iita.akilimo.models.StarchFactory;
import com.iita.akilimo.rest.RestService;
import com.iita.akilimo.utils.FireBaseEvents;
import com.iita.akilimo.utils.MathHelper;
import com.iita.akilimo.utils.Tools;
import com.iita.akilimo.utils.enums.EnumCassavaProduceType;
import com.iita.akilimo.utils.enums.EnumUnitOfSale;
import com.iita.akilimo.utils.enums.EnumUnitPrice;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class CassavaMarketActivity extends BaseActivity {

    private String LOG_TAG = CassavaMarketActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindString(R.string.title_activity_cassava_market_outlet)
    String marketOutletTitle;

    @BindView(R.id.factoryTitle)
    AppCompatTextView factoryTitle;
    @BindView(R.id.produceTypeTitle)
    AppCompatTextView produceTypeTitle;
    @BindView(R.id.unitOfSaleTitle)
    AppCompatTextView unitOfSaleTitle;

    @BindView(R.id.rdgMarketOutlet)
    RadioGroup rdgMarketOutlet;

    @BindView(R.id.rdgStarchFactories)
    RadioGroup rdgStarchFactories;

    @BindView(R.id.rdgProduceType)
    RadioGroup rdgProduceType;

    @BindView(R.id.rdgUnitOfSale)
    RadioGroup rdgUnitOfSale;

    @BindView(R.id.marketOutletCard)
    CardView marketOutletCard;

    @BindView(R.id.starchFactoryCard)
    CardView starchFactoryCard;

    @BindView(R.id.produceTypeCard)
    CardView produceTypeCard;

    @BindView(R.id.unitOfSaleCard)
    CardView unitOfSaleCard;

    @BindView(R.id.btnFinish)
    MaterialButton btnFinish;
    @BindView(R.id.btnCancel)
    MaterialButton btnCancel;


    MathHelper mathHelper;
    private String selectedFactory;

    EnumCassavaProduceType enumCassavaProduceType;
    EnumUnitOfSale enumUnitOfSale;
    EnumUnitPrice enumUnitPrice;

    double unitPriceUSD = 0.0;
    double unitPriceLocal = 0.0;
    private double minAmountUSD = 5.00;
    private double maxAmountUSD = 500.00;

    String priceText;
    String unitOfSale;
    private CassavaMarketOutlet cassavaMarketOutlet;

    private boolean factoryRequired;
    private boolean otherMarketsRequired;
    private boolean dataIsValid;
    private boolean exactPriceSelected;
    private boolean selectionMade;
    private boolean dialogOpen;
    private FireBaseEvents fireBaseEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cassava_market);
        ButterKnife.bind(this);
        context = this;

        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(context);
        queue = Volley.newRequestQueue(context);
        mathHelper = new MathHelper();
        fireBaseEvents = FireBaseEvents.newInstance(context);

        cassavaMarketOutlet = objectBoxEntityProcessor.getCassavaMarketOutlet();
        if (cassavaMarketOutlet == null) {
            cassavaMarketOutlet = new CassavaMarketOutlet();
        } else {
            selectedFactory = cassavaMarketOutlet.getStarchFactory();
        }

        MandatoryInfo mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();
        countryCode = mandatoryInfo.getCountryCode();
        currency = mandatoryInfo.getCurrency();

        initToolbar();
        initComponent();
    }

    @Override
    protected void onResume() {
        super.onResume();

        List<StarchFactory> starchFactoriesList = objectBoxEntityProcessor.getStarchFactories(countryCode);
        addFactoriesRadioButtons(starchFactoriesList);
        cassavaMarketOutlet = objectBoxEntityProcessor.getCassavaMarketOutlet();

        if (cassavaMarketOutlet != null) {
            boolean sfRequired = cassavaMarketOutlet.isStarchFactoryRequired();
            priceText = String.valueOf(cassavaMarketOutlet.getExactPrice());
            rdgMarketOutlet.check(sfRequired ? R.id.rdFactory : R.id.rdOtherMarket);
            if (!sfRequired) {
                enumCassavaProduceType = cassavaMarketOutlet.getEnumCassavaProduceType();
                enumUnitOfSale = cassavaMarketOutlet.getEnumUnitOfSale();
                enumUnitPrice = cassavaMarketOutlet.getEnumUnitPrice();
                unitOfSale = enumUnitOfSale.unitOfSale();
                if (enumCassavaProduceType == EnumCassavaProduceType.ROOTS) {
                    rdgProduceType.check(R.id.rdRoots);
                }

                switch (enumUnitOfSale) {
                    case UNIT_ONE_KG:
                        rdgUnitOfSale.check(R.id.rd_per_kg);
                        break;
                    case UNIT_FIFTY_KG:
                        rdgUnitOfSale.check(R.id.rd_50_kg_bag);
                        break;
                    case UNIT_HUNDRED_KG:
                        rdgUnitOfSale.check(R.id.rd_100_kg_bag);
                        break;
                    case UNIT_THOUSAND_KG:
                        rdgUnitOfSale.check(R.id.rd_per_tonne);
                        break;
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        validate(true);
    }

    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(marketOutletTitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> validate(false));
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
                    hideAll(true);
                    factoryTitle.setVisibility(View.VISIBLE);
                    starchFactoryCard.setVisibility(View.VISIBLE);
                    factoryRequired = true;
                    enumCassavaProduceType = null;
                    enumUnitOfSale = null;
                    enumUnitPrice = null;
                    unitPriceLocal = 0.0;
                    break;
                case R.id.rdOtherMarket:
                    selectedFactory = "NA";
                    otherMarketsRequired = true;
                    hideAll(false);
                    produceTypeTitle.setVisibility(View.VISIBLE);
                    produceTypeCard.setVisibility(View.VISIBLE);
                    break;
            }
        });

        rdgStarchFactories.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            int radioButtonId = radioGroup.getCheckedRadioButtonId();
            dataIsValid = false;
            if (radioButtonId > -1) {
                RadioButton radioButton = findViewById(radioButtonId);
                String itemTagIndex = (String) radioButton.getTag();
                if (itemTagIndex != null) {
                    StarchFactory selectedStarchFactory = objectBoxEntityProcessor.getSelectedStarchFactoryByTag(itemTagIndex);
                    if (selectedStarchFactory != null) {
                        selectedFactory = selectedStarchFactory.getFactoryName();
                    }
                    dataIsValid = true;
                }
            }
        });

        rdgProduceType.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            if (radioIndex == R.id.rdRoots) {
                enumCassavaProduceType = EnumCassavaProduceType.ROOTS;
            }

            //display the next card
            unitOfSaleTitle.setVisibility(View.VISIBLE);
            unitOfSaleCard.setVisibility(View.VISIBLE);
        });

        rdgUnitOfSale.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            RadioButton radioButton = (RadioButton) radioGroup.findViewById(radioIndex);
            switch (radioIndex) {
                case R.id.rd_per_kg:
                    enumUnitOfSale = EnumUnitOfSale.UNIT_ONE_KG;
                    break;
                case R.id.rd_50_kg_bag:
                    enumUnitOfSale = EnumUnitOfSale.UNIT_FIFTY_KG;
                    break;
                case R.id.rd_100_kg_bag:
                    enumUnitOfSale = EnumUnitOfSale.UNIT_HUNDRED_KG;
                    break;
                default:
                case R.id.rd_per_tonne:
                    enumUnitOfSale = EnumUnitOfSale.UNIT_THOUSAND_KG;
                    break;
            }


            unitOfSale = enumUnitOfSale.unitOfSale();
            dataIsValid = false;
//
        });


        btnFinish.setOnClickListener(view -> validate(false));
        btnCancel.setOnClickListener(view -> closeActivity(false));
    }

    public void onRadioButtonClicked(View radioButton) {
        if (radioButton != null && radioButton.isPressed()) {
            showUnitPriceDialog(currency, unitOfSale);
        }
    }

    @Override
    protected void validate(boolean backPressed) {

        if (factoryRequired) {
            if (Strings.isEmptyOrWhitespace(selectedFactory) || selectedFactory.equalsIgnoreCase("NA")) {
                showCustomWarningDialog("Invalid factory", "Please select a starch factory", "OK");
                return;
            }
        } else if (otherMarketsRequired) {
            if (enumCassavaProduceType == null) {
                showCustomWarningDialog("Invalid produce type", "Please specify a valid produce type", "OK");
                return;
            }
            if (enumUnitOfSale == null) {
                showCustomWarningDialog("Invalid unit of sale", "Please specify a valid unit of sale", "OK");
                return;
            }
            if (enumUnitPrice == null) {
                showCustomWarningDialog("Invalid unit price", "Please specify a valid unit price", "OK");
                return;
            }
            if (exactPriceSelected && Strings.isEmptyOrWhitespace(priceText)) {
                showCustomWarningDialog("Invalid unit price", "Please specify a valid unit price", "Retry");
                return;
            }

            if (exactPriceSelected) {
                unitPriceLocal = Double.parseDouble(priceText);
                enumUnitPrice = EnumUnitPrice.PRICE_EXACT;
                priceText = String.valueOf(cassavaMarketOutlet.getExactPrice());
            } else {
                priceText = "0";
                unitPriceLocal = 0;
                unitPriceLocal = enumUnitPrice.convertToLocalCurrency(currency);
            }
            Double minAmount = mathHelper.convertCurrency(minAmountUSD, currency);
            Double maxAmount = mathHelper.convertCurrency(maxAmountUSD, currency);

            dataIsValid = true;
            if (!(unitPriceLocal >= minAmount) || !(unitPriceLocal <= maxAmount)) {
                String message = String.format("Unit price should be between %s %s and %s %s", minAmount, currency, maxAmount, currency);
                Bundle bundle = new Bundle();
                bundle.putString("message", message);
                fireBaseEvents.logEvent("UNIT_PRICE_COMPARISON", bundle);
            }
        }

        if (!selectionMade) {
            showCustomWarningDialog("Nothing selected", "You have not made any selection", null);
            return;
        }


        if (dataIsValid) {
            if (cassavaMarketOutlet == null) {
                cassavaMarketOutlet = new CassavaMarketOutlet();
            }
            cassavaMarketOutlet.setStarchFactory(selectedFactory);
            cassavaMarketOutlet.setStarchFactoryRequired(factoryRequired);
            cassavaMarketOutlet.setEnumCassavaProduceType(enumCassavaProduceType);
            cassavaMarketOutlet.setEnumUnitOfSale(enumUnitOfSale);
            cassavaMarketOutlet.setEnumUnitPrice(enumUnitPrice);
            cassavaMarketOutlet.setExactPrice(unitPriceLocal);

            long id = objectBoxEntityProcessor.saveMarketOutlet(cassavaMarketOutlet);
            if (id > 0) {
                closeActivity(backPressed);
            }
        }
    }

    private void hideAll(boolean clearMarket) {
        if (clearMarket) {
            rdgStarchFactories.clearCheck();
        } else {
            rdgProduceType.clearCheck();
            rdgUnitOfSale.clearCheck();
        }

        factoryTitle.setVisibility(View.GONE);
        starchFactoryCard.setVisibility(View.GONE);

        produceTypeTitle.setVisibility(View.GONE);
        produceTypeCard.setVisibility(View.GONE);


        unitOfSaleTitle.setVisibility(View.GONE);
        unitOfSaleCard.setVisibility(View.GONE);
    }

    private void processStarchFactories() {
        final RestService restService = RestService.getInstance(queue, this);
        final ObjectMapper objectMapper = new ObjectMapper();
        restService.setParameters("v2/starch-factories", countryCode, 5000);

        restService.getJsonArrList(new IVolleyCallback() {
            @Override
            public void onSuccessJsonString(@NotNull String jsonStringResult) {
            }

            @Override
            public void onSuccessJsonArr(@NotNull JSONArray jsonArray) {
                try {
                    List<StarchFactory> starchFactoriesList = objectMapper.readValue(jsonArray.toString(), new TypeReference<List<StarchFactory>>() {
                    });

                    objectBoxEntityProcessor.saveStarchFactories(starchFactoriesList);
                    addFactoriesRadioButtons(starchFactoriesList);
                } catch (Exception ex) {
                    Timber.e("Error reading list :%s", ex.getMessage());
                }
            }

            @Override
            public void onSuccessJsonObject(@NotNull JSONObject jsonObject) {

            }

            @Override
            public void onError(@NotNull VolleyError volleyError) {
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

    private String labelText(double unitPriceLower, double unitPriceUpper, String currency, String uos) {
        //cross convert according to weight
        double priceLower = unitPriceLower;
        double priceHigher = unitPriceUpper;

        switch (enumUnitOfSale) {
            case UNIT_ONE_KG:
                priceLower = (unitPriceLower * EnumUnitOfSale.UNIT_ONE_KG.unitWeight()) / 1000;
                priceHigher = (unitPriceUpper * EnumUnitOfSale.UNIT_ONE_KG.unitWeight()) / 1000;
                break;
            case UNIT_FIFTY_KG:
                priceLower = (unitPriceLower * EnumUnitOfSale.UNIT_FIFTY_KG.unitWeight()) / 1000;
                priceHigher = (unitPriceUpper * EnumUnitOfSale.UNIT_FIFTY_KG.unitWeight()) / 1000;
                break;
            case UNIT_HUNDRED_KG:
                priceLower = (unitPriceLower * EnumUnitOfSale.UNIT_HUNDRED_KG.unitWeight()) / 1000;
                priceHigher = (unitPriceUpper * EnumUnitOfSale.UNIT_HUNDRED_KG.unitWeight()) / 1000;
                break;
        }

        minAmountUSD = priceLower; //minimum amount will be dynamic based on weight being sold, max amount will be constant
        double localLower = mathHelper.convertToLocalCurrency(priceLower, currency, 10);
        double localHigher = mathHelper.convertToLocalCurrency(priceHigher, currency, 10);

        String message = context.getString(R.string.unit_price_label, localLower, localHigher, currency, uos);
        if (!Strings.isEmptyOrWhitespace(priceText)) {
            setExactPriceLabel();
        }
        return message;
    }

    private void setExactPriceLabel() {
        //exactPriceText.setText(String.format(Locale.US, "%,.0f %s per %s", Double.parseDouble(priceText), currency, unitOfSale));
    }

    private void showUnitPriceDialog(String currency, String uos) {
        if (dialogOpen) {
            //do not open multiple dialogs
            return;
        }

        final Dialog dialog = new Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_cassava_unit_price);
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final TextView unitPriceTitle = dialog.findViewById(R.id.unitPriceTitle);
        final EditText etUnitPrice = dialog.findViewById(R.id.etUnitPrice);
        final RadioGroup rdgUnitPrice = dialog.findViewById(R.id.rdgUnitPrice);
        //update the radiobutton labels
        final RadioButton rd_20_30_price = dialog.findViewById(R.id.rd_20_30_price);
        final RadioButton rd_30_50_price = dialog.findViewById(R.id.rd_30_50_price);
        final RadioButton rd_50_100_price = dialog.findViewById(R.id.rd_50_100_price);
        final RadioButton rd_100_150_price = dialog.findViewById(R.id.rd_100_150_price);
        final RadioButton rd_150_200_price = dialog.findViewById(R.id.rd_150_200_price);
        //update labels according to earlier selected values

        if (exactPriceSelected) {
            etUnitPrice.setVisibility(View.VISIBLE);
            etUnitPrice.setText(priceText);
        }

        unitPriceTitle.setText(String.format(getString(R.string.lbl_unit_price_per), currency, unitOfSale));
        try {
            rd_20_30_price.setText(labelText(EnumUnitPrice.PRICE_RANGE_ONE.unitPricePerTonneLower(), EnumUnitPrice.PRICE_RANGE_ONE.unitPricePerTonneUpper(), currency, uos));
            rd_30_50_price.setText(labelText(EnumUnitPrice.PRICE_RANGE_TWO.unitPricePerTonneLower(), EnumUnitPrice.PRICE_RANGE_TWO.unitPricePerTonneUpper(), currency, uos));
            rd_50_100_price.setText(labelText(EnumUnitPrice.PRICE_RANGE_THREE.unitPricePerTonneLower(), EnumUnitPrice.PRICE_RANGE_THREE.unitPricePerTonneUpper(), currency, uos));
            rd_100_150_price.setText(labelText(EnumUnitPrice.PRICE_RANGE_FOUR.unitPricePerTonneLower(), EnumUnitPrice.PRICE_RANGE_FOUR.unitPricePerTonneUpper(), currency, uos));
            rd_150_200_price.setText(labelText(EnumUnitPrice.PRICE_RANGE_FIVE.unitPricePerTonneLower(), EnumUnitPrice.PRICE_RANGE_FIVE.unitPricePerTonneUpper(), currency, uos));
        } catch (Exception ex) {
            Timber.e(ex);
        }

        rdgUnitPrice.setOnCheckedChangeListener((group, radioIndex) -> {
            etUnitPrice.setVisibility(View.GONE);
            dataIsValid = false;
            exactPriceSelected = false;
            switch (radioIndex) {
                case R.id.rd_20_30_price:
                    enumUnitPrice = EnumUnitPrice.PRICE_RANGE_ONE;
                    break;
                case R.id.rd_30_50_price:
                    enumUnitPrice = EnumUnitPrice.PRICE_RANGE_TWO;
                    break;
                case R.id.rd_50_100_price:
                    enumUnitPrice = EnumUnitPrice.PRICE_RANGE_THREE;
                    break;
                case R.id.rd_100_150_price:
                    enumUnitPrice = EnumUnitPrice.PRICE_RANGE_FOUR;
                    break;
                case R.id.rd_150_200_price:
                    enumUnitPrice = EnumUnitPrice.PRICE_RANGE_FIVE;
                    break;
                case R.id.rd_exact_price:
                    exactPriceSelected = true;
                    enumUnitPrice = EnumUnitPrice.PRICE_EXACT;
                    etUnitPrice.setVisibility(View.VISIBLE);
                    break;
            }
        });
        dialog.findViewById(R.id.bt_cancel).setOnClickListener(v -> {
            dialog.dismiss();
            dialogOpen = false;
        });

        dialog.findViewById(R.id.bt_submit).setOnClickListener(view -> {
            if (enumUnitPrice == null) {
                Snackbar.make(view, "Please enter a valid amount", Snackbar.LENGTH_LONG).show();
                return;
            }
            if (exactPriceSelected) {
                priceText = etUnitPrice.getText().toString().trim();
                if (Strings.isEmptyOrWhitespace(priceText)) {
                    dataIsValid = false;
                    Snackbar.make(view, "Please enter a valid amount", Snackbar.LENGTH_LONG).show();
                    return;
                }
            }
            dialog.dismiss();
            dialogOpen = false;

        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
        dialogOpen = true;
    }
}
