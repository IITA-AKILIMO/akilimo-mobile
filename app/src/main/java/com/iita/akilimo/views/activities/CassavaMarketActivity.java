package com.iita.akilimo.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

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
import com.iita.akilimo.entities.CassavaMarketOutlet;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.interfaces.IPriceDialogDismissListener;
import com.iita.akilimo.interfaces.IVolleyCallback;
import com.iita.akilimo.models.CassavaPrice;
import com.iita.akilimo.models.StarchFactory;
import com.iita.akilimo.rest.RestParameters;
import com.iita.akilimo.rest.RestService;
import com.iita.akilimo.utils.MathHelper;
import com.iita.akilimo.utils.Tools;
import com.iita.akilimo.utils.enums.EnumCassavaProduceType;
import com.iita.akilimo.utils.enums.EnumUnitOfSale;
import com.iita.akilimo.utils.enums.EnumUnitPrice;
import com.iita.akilimo.utils.enums.EnumUseCase;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;
import com.iita.akilimo.views.fragments.dialog.CassavaPriceDialogFragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class CassavaMarketActivity extends BaseActivity {

    private String LOG_TAG = CassavaMarketActivity.class.getSimpleName();

    public static String useCaseTag = "useCase";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindString(R.string.title_activity_cassava_market_outlet)
    String marketOutletLabel;

    @BindView(R.id.marketOutLetTitle)
    AppCompatTextView marketOutLetTitle;

    @BindView(R.id.factoryTitle)
    AppCompatTextView factoryTitle;

    @BindView(R.id.unitOfSaleTitle)
    AppCompatTextView unitOfSaleTitle;

    @BindView(R.id.rdgMarketOutlet)
    RadioGroup rdgMarketOutlet;

    @BindView(R.id.rdgStarchFactories)
    RadioGroup rdgStarchFactories;

    @BindView(R.id.rdgUnitOfSale)
    RadioGroup rdgUnitOfSale;

    @BindView(R.id.marketOutletCard)
    CardView marketOutletCard;

    @BindView(R.id.starchFactoryCard)
    CardView starchFactoryCard;


    @BindView(R.id.unitOfSaleCard)
    CardView unitOfSaleCard;

    @BindView(R.id.btnFinish)
    AppCompatButton btnFinish;
    @BindView(R.id.btnCancel)
    AppCompatButton btnCancel;


    MathHelper mathHelper;
    private String selectedFactory = "NA";

    EnumCassavaProduceType enumCassavaProduceType;
    EnumUnitOfSale enumUnitOfSale;
    EnumUnitPrice enumUnitPrice;

    double unitPriceLocal = 0.0;

    String priceText;
    String unitOfSale;
    private CassavaMarketOutlet cassavaMarketOutlet;
    private List<CassavaPrice> cassavaPriceList = null;

    private boolean factoryRequired;
    private boolean otherMarketsRequired;
    private boolean dataIsValid;
    private boolean selectionMade;


    private double averagePrice = 0.0;
    private double exactPrice = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cassava_market);
        ButterKnife.bind(this);
        context = this;

        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(context);
        queue = Volley.newRequestQueue(context);
        mathHelper = new MathHelper(this);

        cassavaMarketOutlet = objectBoxEntityProcessor.getCassavaMarketOutlet();
        if (cassavaMarketOutlet == null) {
            cassavaMarketOutlet = new CassavaMarketOutlet();
        } else {
            selectedFactory = cassavaMarketOutlet.getStarchFactory();
        }

        MandatoryInfo mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();
        countryCode = mandatoryInfo.getCountryCode();
        currency = mandatoryInfo.getCurrency();

        Intent intent = getIntent();
        if (intent != null) {
            useCase = intent.getParcelableExtra(useCaseTag);
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
        getSupportActionBar().setTitle(marketOutletLabel);
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
                    enumCassavaProduceType = null;
                    enumUnitOfSale = null;
                    enumUnitPrice = null;
                    unitPriceLocal = 0.0;
                    break;
                case R.id.rdOtherMarket:
                    enumCassavaProduceType = EnumCassavaProduceType.ROOTS;
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

        rdgUnitOfSale.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
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
                case R.id.rd_per_tonne:
                    enumUnitOfSale = EnumUnitOfSale.UNIT_THOUSAND_KG;
                    break;
            }
            if (enumUnitOfSale != null) {
                unitOfSale = enumUnitOfSale.unitOfSale();
            }
            dataIsValid = false;
        });


        btnFinish.setOnClickListener(view -> validate(false));
        btnCancel.setOnClickListener(view -> closeActivity(false));
        if (useCase == EnumUseCase.CIM) {
            enumCassavaProduceType = EnumCassavaProduceType.ROOTS;
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
            showUnitPriceDialog(currency, unitOfSale);
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
            if (enumCassavaProduceType == null) {
                showCustomWarningDialog(getString(R.string.lbl_invalid_produce), getString(R.string.lbl_produce_prompt));
                return;
            }
            if (enumUnitOfSale == null) {
                showCustomWarningDialog(getString(R.string.lbl_invalid_sale_unit), getString(R.string.lbl_sale_unit_prompt));
                return;
            }
            if (exactPrice <= 0) {
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
            if (cassavaMarketOutlet == null) {
                cassavaMarketOutlet = new CassavaMarketOutlet();
            }
            cassavaMarketOutlet.setStarchFactory(selectedFactory);
            cassavaMarketOutlet.setStarchFactoryRequired(factoryRequired);
            cassavaMarketOutlet.setEnumCassavaProduceType(enumCassavaProduceType);
            cassavaMarketOutlet.setEnumUnitOfSale(enumUnitOfSale);
            cassavaMarketOutlet.setEnumUnitPrice(enumUnitPrice);
            cassavaMarketOutlet.setExactPrice(exactPrice);
            cassavaMarketOutlet.setAveragePrice(averagePrice);

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
                    objectBoxEntityProcessor.saveStarchFactories(starchFactoriesList);
                    addFactoriesRadioButtons(starchFactoriesList);
                } catch (Exception ex) {
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
                    objectBoxEntityProcessor.saveCassavaPrice(cassavaPriceList);
                } catch (Exception ex) {
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


    private void showUnitPriceDialog(String currency, String uos) {
        Bundle arguments = new Bundle();
        arguments.putString(CassavaPriceDialogFragment.CURRENCY_CODE, currency);
        arguments.putString(CassavaPriceDialogFragment.COUNTRY_CODE, countryCode);
        arguments.putString(CassavaPriceDialogFragment.UNIT_OF_SALE, uos);
        arguments.putDouble(CassavaPriceDialogFragment.SELECTED_PRICE, exactPrice);
        arguments.putDouble(CassavaPriceDialogFragment.AVERAGE_PRICE, averagePrice);
        arguments.putParcelable(CassavaPriceDialogFragment.ENUM_UNIT_OF_SALE, enumUnitOfSale);

        CassavaPriceDialogFragment priceDialogFragment = new CassavaPriceDialogFragment();
        priceDialogFragment.setArguments(arguments);

        priceDialogFragment.setOnDismissListener((selectedPrice, selectedAveragePrice) -> {
            exactPrice = selectedPrice;
            averagePrice = selectedAveragePrice;
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
