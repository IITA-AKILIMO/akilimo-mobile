package com.iita.akilimo.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.util.Strings;
import com.google.android.material.snackbar.Snackbar;
import com.iita.akilimo.R;
import com.iita.akilimo.dao.AppDatabase;
import com.iita.akilimo.databinding.ActivityCassavaMarketBinding;
import com.iita.akilimo.entities.CassavaMarket;
import com.iita.akilimo.entities.CassavaPrice;
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.entities.StarchFactory;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.interfaces.IVolleyCallback;
import com.iita.akilimo.rest.RestParameters;
import com.iita.akilimo.rest.RestService;
import com.iita.akilimo.utils.MathHelper;
import com.iita.akilimo.utils.Tools;
import com.iita.akilimo.utils.enums.EnumCassavaProduceType;
import com.iita.akilimo.utils.enums.EnumUnitOfSale;
import com.iita.akilimo.utils.enums.EnumUseCase;
import com.iita.akilimo.views.fragments.dialog.CassavaPriceDialogFragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;


public class CassavaMarketActivity extends BaseActivity {

    private String LOG_TAG = CassavaMarketActivity.class.getSimpleName();

    public static String useCaseTag = "useCase";


    Toolbar toolbar;
    AppCompatTextView marketOutLetTitle;
    AppCompatTextView factoryTitle;
    AppCompatTextView unitOfSaleTitle;
    RadioGroup rdgMarketOutlet;
    RadioGroup rdgStarchFactories;
    RadioGroup rdgUnitOfSale;
    CardView marketOutletCard;
    CardView starchFactoryCard;
    CardView unitOfSaleCard;
    AppCompatButton btnFinish;
    AppCompatButton btnCancel;

    ActivityCassavaMarketBinding binding;
    MathHelper mathHelper;
    private String selectedFactory = "NA";

    String produceType;
    private double unitPrice = 0.0;
    String priceText;
    String unitOfSale;
    int unitWeight;
    EnumUnitOfSale unitOfSaleEnum = EnumUnitOfSale.ONE_KG;

    private CassavaMarket cassavaMarket;
    private List<CassavaPrice> cassavaPriceList = null;

    private boolean factoryRequired;
    private boolean otherMarketsRequired;
    private boolean dataIsValid;
    private boolean selectionMade;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_cassava_market);
        binding = ActivityCassavaMarketBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;

        //Set ui elements
        toolbar = binding.toolbar;
        marketOutLetTitle = binding.contentCassavaMarket.marketOutLetTitle;
        factoryTitle = binding.contentCassavaMarket.factoryTitle;
        unitOfSaleTitle = binding.contentCassavaMarket.unitOfSaleTitle;
        rdgMarketOutlet = binding.contentCassavaMarket.rdgMarketOutlet;
        rdgStarchFactories = binding.contentCassavaMarket.rdgStarchFactories;
        rdgUnitOfSale = binding.contentCassavaMarket.rdgUnitOfSale;
        marketOutletCard = binding.contentCassavaMarket.marketOutletCard;
        starchFactoryCard = binding.contentCassavaMarket.starchFactoryCard;
        unitOfSaleCard = binding.contentCassavaMarket.unitOfSaleCard;
        btnFinish = binding.contentCassavaMarket.twoButtons.btnFinish;
        btnCancel = binding.contentCassavaMarket.twoButtons.btnCancel;

        database = AppDatabase.getDatabase(context);
        queue = Volley.newRequestQueue(context);
        mathHelper = new MathHelper(this);

        cassavaMarket = database.cassavaMarketDao().findOne();
        if (cassavaMarket != null) {
            selectedFactory = cassavaMarket.getStarchFactory();
            unitPrice = cassavaMarket.getUnitPrice();
            unitOfSale = cassavaMarket.getUnitOfSale();
        }

        ProfileInfo profileInfo = database.profileInfoDao().findOne();
        if (profileInfo != null) {
            countryCode = profileInfo.getCountryCode();
            currency = profileInfo.getCurrency();
        }
        Intent intent = getIntent();
        if (intent != null) {
            enumUseCase = intent.getParcelableExtra(useCaseTag);
        }

        initToolbar();
        initComponent();
        processStarchFactories();
        processCassavaPrices();
        processData();
    }

    @Override
    public void onBackPressed() {
        validate(true);
    }

    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.title_activity_cassava_market_outlet));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> validate(false));
    }

    @Override
    protected void initComponent() {

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
                    produceType = EnumCassavaProduceType.ROOTS.produce();
                    unitOfSale = "NA";
                    unitPrice = 0.0;
                    break;
                case R.id.rdOtherMarket:
                    produceType = EnumCassavaProduceType.ROOTS.produce();
                    selectedFactory = "NA";
                    otherMarketsRequired = true;
                    hideAll(false);
                    unitOfSaleTitle.setVisibility(View.VISIBLE);
                    unitOfSaleCard.setVisibility(View.VISIBLE);
                    break;
            }
        });

        rdgStarchFactories.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            int radioButtonId = radioGroup.getCheckedRadioButtonId();
            dataIsValid = false;
            if (radioButtonId > -1) {
                RadioButton radioButton = findViewById(radioButtonId);
                String factoryNameCountry = (String) radioButton.getTag();
                if (factoryNameCountry != null) {
                    StarchFactory selectedStarchFactory = database.starchFactoryDao().findStarchFactoryByNameCountry(factoryNameCountry);
                    if (selectedStarchFactory != null) {
                        selectedFactory = selectedStarchFactory.getFactoryName();
                    }
                    dataIsValid = true;
                }
            }
        });

        rdgUnitOfSale.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
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
                case R.id.rd_per_tonne:
                    unitOfSale = EnumUnitOfSale.THOUSAND_KG.unitOfSale(context);
                    unitOfSaleEnum = EnumUnitOfSale.THOUSAND_KG;
                    unitWeight = EnumUnitOfSale.THOUSAND_KG.unitWeight();
                    break;
            }
            dataIsValid = false;
        });


        btnFinish.setOnClickListener(view -> validate(false));
        btnCancel.setOnClickListener(view -> closeActivity(false));
        if (enumUseCase == EnumUseCase.CIM) {
            produceType = EnumCassavaProduceType.ROOTS.produce();
            factoryRequired = false;
            otherMarketsRequired = false;
            selectionMade = true;
            marketOutLetTitle.setVisibility(View.GONE);
            marketOutletCard.setVisibility(View.GONE);
            unitOfSaleTitle.setVisibility(View.VISIBLE);
            unitOfSaleCard.setVisibility(View.VISIBLE);
        }

        showCustomNotificationDialog();
    }

    public void onRadioButtonClicked(View radioButton) {
        if (radioButton != null && radioButton.isPressed()) {
            showUnitPriceDialog();
        }
    }

    @Override
    protected void validate(boolean backPressed) {

        if (factoryRequired) {
            if (Strings.isEmptyOrWhitespace(selectedFactory) || selectedFactory.equalsIgnoreCase("NA")) {
                showCustomWarningDialog(getString(R.string.lbl_invalid_factory), getString(R.string.lbl_factory_prompt), getString(R.string.lbl_ok));
                return;
            }
        } else if (otherMarketsRequired) {
            if (produceType == null) {
                showCustomWarningDialog(getString(R.string.lbl_invalid_produce), getString(R.string.lbl_produce_prompt));
                return;
            }
            if (Strings.isEmptyOrWhitespace(unitOfSale)) {
                showCustomWarningDialog(getString(R.string.lbl_invalid_sale_unit), getString(R.string.lbl_sale_unit_prompt));
                return;
            }
            if (unitPrice <= 0) {
                showCustomWarningDialog(getString(R.string.lbl_invalid_unit_price), getString(R.string.lbl_unit_price_prompt));
                return;
            }

            dataIsValid = true;
        }

        if (!selectionMade) {
            showCustomWarningDialog(getString(R.string.lbl_nothing), getString(R.string.lbl_nothing_prompt));
            return;
        }


        if (dataIsValid) {
            try {
                if (cassavaMarket == null) {
                    cassavaMarket = new CassavaMarket();
                }
                cassavaMarket.setStarchFactory(selectedFactory);
                cassavaMarket.setStarchFactoryRequired(factoryRequired);
                cassavaMarket.setProduceType(produceType);
                cassavaMarket.setUnitOfSale(unitOfSale);
                cassavaMarket.setUnitWeight(unitWeight);
                cassavaMarket.setExactPrice(unitPrice);
                cassavaMarket.setUnitPrice(unitPrice);

                database.cassavaMarketDao().insert(cassavaMarket);
                closeActivity(backPressed);
            } catch (Exception ex) {
                Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
                Crashlytics.logException(ex);
            }
        }
    }

    private void hideAll(boolean clearMarket) {
        if (clearMarket) {
            rdgStarchFactories.clearCheck();
        } else {
            rdgUnitOfSale.clearCheck();
        }

        factoryTitle.setVisibility(View.GONE);
        starchFactoryCard.setVisibility(View.GONE);

        unitOfSaleTitle.setVisibility(View.GONE);
        unitOfSaleCard.setVisibility(View.GONE);
    }


    private void processStarchFactories() {
        final RestService restService = RestService.getInstance(queue, this);
        final ObjectMapper objectMapper = new ObjectMapper();
        final RestParameters restParameters = new RestParameters(
                "v2/starch-factories", countryCode
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
                    List<StarchFactory> starchFactoriesList = objectMapper.readValue(jsonArray.toString(), new TypeReference<List<StarchFactory>>() {
                    });
                    if (starchFactoriesList.size() > 0) {
                        database.starchFactoryDao().insertAll(starchFactoriesList);
                        addFactoriesRadioButtons(starchFactoriesList);
                    }
                } catch (Exception ex) {
                    Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
                    Crashlytics.logException(ex);
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

    private void processCassavaPrices() {
        final RestService restService = RestService.getInstance(queue, this);
        final ObjectMapper objectMapper = new ObjectMapper();
        final RestParameters restParameters = new RestParameters(
                String.format("v3/cassava-prices/country/%s", countryCode),
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
                    cassavaPriceList = objectMapper.readValue(jsonArray.toString(), new TypeReference<List<CassavaPrice>>() {
                    });
                    if (cassavaPriceList.size() > 0) {
                        database.cassavaPriceDao().insertAll(cassavaPriceList);
                    }
                } catch (Exception ex) {
                    Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    Crashlytics.logException(ex);
                    Crashlytics.log(ex.getMessage());
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

    protected void processData() {
        List<StarchFactory> starchFactoriesList = database.starchFactoryDao().findStarchFactoriesByCountry(countryCode);
        addFactoriesRadioButtons(starchFactoriesList);
        cassavaMarket = database.cassavaMarketDao().findOne();

        if (cassavaMarket != null) {
            boolean sfRequired = cassavaMarket.isStarchFactoryRequired();
            priceText = String.valueOf(cassavaMarket.getExactPrice());
            rdgMarketOutlet.check(sfRequired ? R.id.rdFactory : R.id.rdOtherMarket);
            if (!sfRequired) {
                produceType = cassavaMarket.getProduceType();
                unitOfSale = cassavaMarket.getUnitOfSale();
                unitPrice = cassavaMarket.getUnitPrice();
                switch (unitOfSaleEnum) {
                    case ONE_KG:
                        rdgUnitOfSale.check(R.id.rd_per_kg);
                        break;
                    case FIFTY_KG:
                        rdgUnitOfSale.check(R.id.rd_50_kg_bag);
                        break;
                    case HUNDRED_KG:
                        rdgUnitOfSale.check(R.id.rd_100_kg_bag);
                        break;
                    case THOUSAND_KG:
                        rdgUnitOfSale.check(R.id.rd_per_tonne);
                        break;
                }
            }
        }
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

                params.setMargins(0, 0, 0, dimension);
                radioButton.setLayoutParams(params);

                radioButton.setText(radioLabel);

                rdgStarchFactories.addView(radioButton);

                if (factory.getFactoryName().equals(selectedFactory)) {
                    radioButton.setChecked(true);
                }
            }
        }

    }


    private void showUnitPriceDialog() {
        Bundle arguments = new Bundle();
        arguments.putString(CassavaPriceDialogFragment.CURRENCY_CODE, currency);
        arguments.putString(CassavaPriceDialogFragment.COUNTRY_CODE, countryCode);
        arguments.putDouble(CassavaPriceDialogFragment.SELECTED_PRICE, unitPrice);
        arguments.putDouble(CassavaPriceDialogFragment.AVERAGE_PRICE, unitPrice);
        arguments.putString(CassavaPriceDialogFragment.UNIT_OF_SALE, unitOfSale);
        arguments.putParcelable(CassavaPriceDialogFragment.ENUM_UNIT_OF_SALE, unitOfSaleEnum);

        CassavaPriceDialogFragment priceDialogFragment = new CassavaPriceDialogFragment(context);
        priceDialogFragment.setArguments(arguments);

        priceDialogFragment.setOnDismissListener((selectedPrice, isExactPrice) -> {
            unitPrice = isExactPrice ? selectedPrice : mathHelper.convertToUnitWeightPrice(selectedPrice, unitWeight);
        });

        FragmentTransaction fragmentTransaction;
        if (getFragmentManager() != null) {
            fragmentTransaction = getSupportFragmentManager().beginTransaction();
            Fragment prev = getSupportFragmentManager().findFragmentByTag(CassavaPriceDialogFragment.ARG_ITEM_ID);
            if (prev != null) {
                fragmentTransaction.remove(prev);
            }
            fragmentTransaction.addToBackStack(null);
            priceDialogFragment.show(getSupportFragmentManager(), CassavaPriceDialogFragment.ARG_ITEM_ID);
        }
    }
}
