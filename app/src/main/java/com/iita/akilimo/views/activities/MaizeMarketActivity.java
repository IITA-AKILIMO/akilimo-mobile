package com.iita.akilimo.views.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.util.Strings;
import com.google.android.material.snackbar.Snackbar;
import com.iita.akilimo.R;
import com.iita.akilimo.dao.AppDatabase;
import com.iita.akilimo.databinding.ActivityMaizeMarketBinding;
import com.iita.akilimo.entities.Currency;
import com.iita.akilimo.entities.MaizeMarket;
import com.iita.akilimo.entities.MaizePrice;
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.interfaces.IVolleyCallback;
import com.iita.akilimo.rest.RestParameters;
import com.iita.akilimo.rest.RestService;
import com.iita.akilimo.utils.MathHelper;
import com.iita.akilimo.utils.Tools;
import com.iita.akilimo.utils.enums.EnumMaizeProduceType;
import com.iita.akilimo.utils.enums.EnumUnitOfSale;
import com.iita.akilimo.views.fragments.dialog.MaizePriceDialogFragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class MaizeMarketActivity extends BaseActivity {

    Toolbar toolbar;

    AppCompatTextView unitOfSaleGrainTitle;
    AppCompatTextView maizeCobPriceTitle;
    AppCompatTextView lblPricePerCob;

    CardView unitOfSaleGrainCard;
    CardView maizeCobPriceCard;

    RadioGroup rdgMaizeProduceType;
    RadioGroup rdgUnitOfSaleGrain;


    AppCompatButton btnPickCobPrice;
    AppCompatButton btnFinish;
    AppCompatButton btnCancel;

    ActivityMaizeMarketBinding binding;

    private MathHelper mathHelper;
    private MaizeMarket maizeMarket;
    private String produceType;
    private Double unitPrice;
    private List<MaizePrice> maizePriceList = null;
    private boolean selectionMade = false;

    private int produceRadioIndex;
    private int grainUnitRadioIndex;
    private int grainUnitPriceRadioIndex;
    private String grainPrice;
    private String cobPrice;
    private String unitOfSale;
    private EnumUnitOfSale unitOfSaleEnum = EnumUnitOfSale.ONE_KG;
    private boolean dialogOpen;
    private boolean dataIsValid;
    private boolean exactPriceSelected;
    private boolean grainPriceRequired;
    private boolean cobPriceRequired;

    private double unitPriceUSD = 0.0;
    private int unitWeight;
    private double exactPrice = 0.0;
    private double averagePrice = 0.0;

    private double minAmountUSD = 5.00;
    private double maxAmountUSD = 500.00;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMaizeMarketBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = this;
        database = AppDatabase.getDatabase(context);
        queue = Volley.newRequestQueue(context);
        mathHelper = new MathHelper(this);

        toolbar = binding.toolbar;
        unitOfSaleGrainTitle = binding.marketContent.unitOfSaleGrainTitle;
        maizeCobPriceTitle = binding.marketContent.maizeCobPriceTitle;
        lblPricePerCob = binding.marketContent.lblPricePerCob;
        unitOfSaleGrainCard = binding.marketContent.unitOfSaleGrainCard;
        maizeCobPriceCard = binding.marketContent.maizeCobPriceCard;
        rdgMaizeProduceType = binding.marketContent.rdgMaizeProduceType;
        rdgUnitOfSaleGrain = binding.marketContent.rdgUnitOfSaleGrain;
        btnPickCobPrice = binding.marketContent.btnPickCobPrice;
        btnFinish = binding.marketContent.twoButtons.btnFinish;
        btnCancel = binding.marketContent.twoButtons.btnCancel;


        maizeMarket = database.maizeMarketDao().findOne();

        ProfileInfo profileInfo = database.profileInfoDao().findOne();
        if (profileInfo != null) {
            countryCode = profileInfo.getCountryCode();
            currency = profileInfo.getCurrency();

            Currency myCurrency = database.currencyDao().findOneByCurrencyCode(currencyCode);
            currencyName = myCurrency.getCurrencyName();
        }

        initToolbar();
        initComponent();
        processMaizePrices();
    }


    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.title_activity_maize_market_outlet));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> validate(false));
    }

    @Override
    protected void initComponent() {

        lblPricePerCob.setText(currency);
        rdgMaizeProduceType.setOnCheckedChangeListener((group, radioIndex) -> {
            grainPriceRequired = false;
            cobPriceRequired = false;
            switch (radioIndex) {
                case R.id.rdDryGrain:
                    unitOfSaleGrainTitle.setVisibility(View.VISIBLE);
                    unitOfSaleGrainCard.setVisibility(View.VISIBLE);
                    maizeCobPriceTitle.setVisibility(View.GONE);
                    maizeCobPriceCard.setVisibility(View.GONE);
                    produceType = EnumMaizeProduceType.GRAIN.produce();
                    grainPriceRequired = true;
                    break;
                case R.id.rdFreshCobs:
                    unitOfSaleGrainTitle.setVisibility(View.GONE);
                    unitOfSaleGrainCard.setVisibility(View.GONE);
                    maizeCobPriceTitle.setVisibility(View.VISIBLE);
                    maizeCobPriceCard.setVisibility(View.VISIBLE);
                    produceType = EnumMaizeProduceType.FRESH_COB.produce();
                    unitOfSale = EnumUnitOfSale.NA.unitOfSale(context);
                    unitOfSaleEnum = EnumUnitOfSale.NA;
                    unitPrice = -1.0;
                    cobPriceRequired = true;

                    maizeCobPriceTitle.setText(getString(R.string.lbl_price_per_cob_in_currency_unit, currencyName));
                    break;
            }
        });

        rdgUnitOfSaleGrain.setOnCheckedChangeListener((group, radioIndex) -> {
            switch (radioIndex) {
                case R.id.rd_per_kg:
                    unitOfSale = EnumUnitOfSale.ONE_KG.unitOfSale(context);
                    unitOfSaleEnum = EnumUnitOfSale.ONE_KG;
                    unitWeight = EnumUnitOfSale.ONE_KG.unitWeight();
                    break;
                case R.id.rd_50_kg_bag:
                    unitOfSale = EnumUnitOfSale.FIFTY_KG.unitOfSale(context);
                    unitOfSaleEnum = EnumUnitOfSale.FIFTY_KG;
                    unitWeight = EnumUnitOfSale.FIFTY_KG.unitWeight();
                    break;
                case R.id.rd_100_kg_bag:
                    unitOfSale = EnumUnitOfSale.HUNDRED_KG.unitOfSale(context);
                    unitOfSaleEnum = EnumUnitOfSale.HUNDRED_KG;
                    unitWeight = EnumUnitOfSale.HUNDRED_KG.unitWeight();
                    break;
            }
        });

        btnFinish.setOnClickListener(view -> validate(false));
        btnCancel.setOnClickListener(view -> closeActivity(false));
        btnPickCobPrice.setOnClickListener(view -> {
            unitOfSale = EnumUnitOfSale.FRESH_COB.unitOfSale(context);
            unitWeight = EnumUnitOfSale.FRESH_COB.unitWeight();
            unitOfSaleEnum = EnumUnitOfSale.FRESH_COB;
            showUnitGrainPriceDialog("cob");
        });

        //@TODO Ensure you cater for dry cob and fresh cob selections
        if (maizeMarket != null) {
            produceType = maizeMarket.getProduceType();
            unitOfSale = maizeMarket.getUnitOfSale();
            unitPrice = maizeMarket.getUnitPrice();
            unitWeight = maizeMarket.getUnitWeight();

            grainUnitRadioIndex = maizeMarket.getGrainUnitRadioIndex();
            produceRadioIndex = maizeMarket.getProduceRadioIndex();
            grainUnitPriceRadioIndex = maizeMarket.getGrainUnitPriceRadioIndex();


            grainPrice = String.valueOf(maizeMarket.getExactPrice());
            cobPrice = String.valueOf(maizeMarket.getExactPrice());

            rdgMaizeProduceType.check(produceRadioIndex);

            if (produceType.equals(EnumMaizeProduceType.GRAIN.produce())) {
                rdgUnitOfSaleGrain.check(grainUnitRadioIndex);
            }
        }

        showCustomNotificationDialog();
    }

    @Override
    protected void validate(boolean backPressed) {

        dataIsValid = false;
        if (produceType == null) {
            showCustomWarningDialog(getString(R.string.lbl_invalid_produce), getString(R.string.lbl_maize_produce_prompt));
            return;
        }

        produceRadioIndex = rdgMaizeProduceType.getCheckedRadioButtonId();

        if (grainPriceRequired) {
            if (Strings.isEmptyOrWhitespace(unitOfSale)) {
                showCustomWarningDialog(getString(R.string.lbl_invalid_sale_unit), getString(R.string.lbl_maize_sale_unit_prompt));
                return;
            }
            if (exactPriceSelected) {
                if (exactPrice <= 0) {
                    showCustomWarningDialog(getString(R.string.lbl_invalid_grain_price), getString(R.string.lbl_grain_price_prompt));
                    return;
                }
            }
            grainUnitRadioIndex = rdgUnitOfSaleGrain.getCheckedRadioButtonId();
            dataIsValid = true;
        }

        if (cobPriceRequired) {
            if (unitPrice <= 0) {
                showCustomWarningDialog(getString(R.string.lbl_invalid_cob_price), getString(R.string.lbl_cob_price_prompt));
                return;
            }
            dataIsValid = true;
        }


        if (dataIsValid) {
            try {

                if (maizeMarket == null) {
                    maizeMarket = new MaizeMarket();
                }

                maizeMarket.setProduceType(produceType);
                maizeMarket.setUnitPrice(unitPrice);
                maizeMarket.setUnitOfSale(unitOfSale);
                maizeMarket.setUnitWeight(unitWeight);
                maizeMarket.setExactPrice(exactPrice);

                maizeMarket.setGrainUnitPriceRadioIndex(grainUnitPriceRadioIndex);
                maizeMarket.setGrainUnitRadioIndex(grainUnitRadioIndex);
                maizeMarket.setProduceRadioIndex(produceRadioIndex);

                database.maizeMarketDao().insert(maizeMarket);
                closeActivity(backPressed);
            } catch (Exception ex) {
                Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
                Crashlytics.logException(ex);
            }

        }
    }

    public void onGrainUnitRadioButtonClicked(View radioButton) {
        if (radioButton != null && radioButton.isPressed()) {
            showUnitGrainPriceDialog("grain");
        }
    }

    private void showUnitGrainPriceDialog(String produceType) {
        Bundle arguments = new Bundle();
        arguments.putString(MaizePriceDialogFragment.CURRENCY_CODE, currency);
        arguments.putString(MaizePriceDialogFragment.CURRENCY_NAME, currencyName);
        arguments.putString(MaizePriceDialogFragment.COUNTRY_CODE, countryCode);
        arguments.putDouble(MaizePriceDialogFragment.SELECTED_PRICE, exactPrice);
        arguments.putDouble(MaizePriceDialogFragment.AVERAGE_PRICE, averagePrice);
        arguments.putString(MaizePriceDialogFragment.UNIT_OF_SALE, unitOfSale);
        arguments.putString(MaizePriceDialogFragment.PRODUCE_TYPE, produceType);
        arguments.putParcelable(MaizePriceDialogFragment.ENUM_UNIT_OF_SALE, unitOfSaleEnum);

        MaizePriceDialogFragment priceDialogFragment = new MaizePriceDialogFragment(context);
        priceDialogFragment.setArguments(arguments);

        priceDialogFragment.setOnDismissListener((selectedPrice, isExactPrice) -> {
            unitPrice = isExactPrice ? selectedPrice : mathHelper.convertToUnitWeightPrice(selectedPrice, unitWeight);
        });

        FragmentTransaction fragmentTransaction;
        if (getFragmentManager() != null) {
            fragmentTransaction = getSupportFragmentManager().beginTransaction();
            Fragment prev = getSupportFragmentManager().findFragmentByTag(MaizePriceDialogFragment.ARG_ITEM_ID);
            if (prev != null) {
                fragmentTransaction.remove(prev);
            }
            fragmentTransaction.addToBackStack(null);
            priceDialogFragment.show(getSupportFragmentManager(), MaizePriceDialogFragment.ARG_ITEM_ID);
        }
    }


    private void processMaizePrices() {
        final RestService restService = RestService.getInstance(queue, this);
        final ObjectMapper objectMapper = new ObjectMapper();
        final RestParameters restParameters = new RestParameters(
                String.format("v3/maize-prices/country/%s", countryCode),
                countryCode
        );
        restParameters.setInitialTimeout(5000);

        restService.setParameters(restParameters);

        restService.getJsonArrList(new IVolleyCallback() {
            @Override
            public void onSuccessJsonString(@NotNull String jsonStringResult) {
            }

            @Override
            public void onSuccessJsonArr(@NotNull JSONArray jsonArray) {
                try {
                    maizePriceList = objectMapper.readValue(jsonArray.toString(), new TypeReference<List<MaizePrice>>() {
                    });

                    if (maizePriceList.size() > 0) {
                        database.maizePriceDao().insertAll(maizePriceList);
                    }
                } catch (JsonProcessingException ex) {
                    Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
                    Crashlytics.logException(ex);
                    Snackbar.make(maizeCobPriceCard, ex.getMessage(), Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSuccessJsonObject(@NotNull JSONObject jsonObject) {

            }

            @Override
            public void onError(@NotNull VolleyError volleyError) {
                String error = Tools.parseNetworkError(volleyError).getMessage();
                if (!Strings.isEmptyOrWhitespace(error)) {
                    Snackbar.make(maizeCobPriceCard, error, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
}
