package com.iita.akilimo.views.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.util.Strings;
import com.google.android.material.snackbar.Snackbar;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.MaizeMarketOutlet;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.interfaces.IVolleyCallback;
import com.iita.akilimo.models.MaizePrice;
import com.iita.akilimo.rest.RestParameters;
import com.iita.akilimo.rest.RestService;
import com.iita.akilimo.utils.MathHelper;
import com.iita.akilimo.utils.Tools;
import com.iita.akilimo.utils.enums.EnumMaizeProduceType;
import com.iita.akilimo.utils.enums.EnumUnitOfSale;
import com.iita.akilimo.utils.enums.EnumUnitPrice;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;
import com.iita.akilimo.views.fragments.dialog.MaizePriceDialogFragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MaizeMarketActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindString(R.string.title_activity_maize_market_outlet)
    String marketOutletTitle;

    @BindView(R.id.unitOfSaleGrainTitle)
    AppCompatTextView unitOfSaleGrainTitle;
    @BindView(R.id.maizeCobPriceTitle)
    AppCompatTextView maizeCobPriceTitle;
    @BindView(R.id.lblCurrency)
    AppCompatTextView lblCurrency;

    @BindView(R.id.unitOfSaleGrainCard)
    CardView unitOfSaleGrainCard;
    @BindView(R.id.maizeCobPriceCard)
    CardView maizeCobPriceCard;

    @BindView(R.id.rdgMaizeProduceType)
    RadioGroup rdgMaizeProduceType;

    @BindView(R.id.rdgUnitOfSaleGrain)
    RadioGroup rdgUnitOfSaleGrain;

    @BindView(R.id.editCobPrice)
    EditText editCobPrice;

    @BindView(R.id.btnFinish)
    AppCompatButton btnFinish;
    @BindView(R.id.btnCancel)
    AppCompatButton btnCancel;

    private MathHelper mathHelper;
    private MaizeMarketOutlet maizeMarketOutlet;
    private EnumMaizeProduceType enumMaizeProduceType;
    private EnumUnitOfSale enumUnitOfSale;
    private EnumUnitPrice enumUnitPrice;
    private List<MaizePrice> maizePriceList = null;
    private boolean selectionMade = false;

    private int produceRadioIndex;
    private int grainUnitRadioIndex;
    private int grainUnitPriceRadioIndex;
    private String grainPrice;
    private String cobPrice;
    private String unitOfSale;
    private boolean dialogOpen;
    private boolean dataIsValid;
    private boolean exactPriceSelected;
    private boolean grainPriceRequired;
    private boolean cobPriceRequired;

    private double unitPriceUSD = 0.0;
    private double exactPrice = 0.0;
    private double averagePrice = 0.0;

    private double minAmountUSD = 5.00;
    private double maxAmountUSD = 500.00;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maize_market);
        ButterKnife.bind(this);

        context = this;
        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(context);
        queue = Volley.newRequestQueue(context);
        mathHelper = new MathHelper(this);

        maizeMarketOutlet = objectBoxEntityProcessor.getMaizeMarketOutlet();

        MandatoryInfo mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();
        countryCode = mandatoryInfo.getCountryCode();
        currency = mandatoryInfo.getCurrency();

        initToolbar();
        initComponent();
        processMaizePrices();
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
                    objectBoxEntityProcessor.saveMaizePrice(maizePriceList);
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
                    Snackbar.make(maizeCobPriceCard, error, Snackbar.LENGTH_LONG).show();
                }
            }
        });
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

        lblCurrency.setText(currency);
        rdgMaizeProduceType.setOnCheckedChangeListener((group, radioIndex) -> {
            grainPriceRequired = false;
            cobPriceRequired = false;
            switch (radioIndex) {
                case R.id.rdDryGrain:
                    unitOfSaleGrainTitle.setVisibility(View.VISIBLE);
                    unitOfSaleGrainCard.setVisibility(View.VISIBLE);
                    maizeCobPriceTitle.setVisibility(View.GONE);
                    maizeCobPriceCard.setVisibility(View.GONE);
                    enumMaizeProduceType = EnumMaizeProduceType.GRAIN;
                    grainPriceRequired = true;
                    break;
                case R.id.rdFreshCobs:
                    unitOfSaleGrainTitle.setVisibility(View.GONE);
                    unitOfSaleGrainCard.setVisibility(View.GONE);
                    maizeCobPriceTitle.setVisibility(View.VISIBLE);
                    maizeCobPriceCard.setVisibility(View.VISIBLE);
                    enumMaizeProduceType = EnumMaizeProduceType.FRESH_COB;
                    enumUnitOfSale = EnumUnitOfSale.NA;
                    enumUnitPrice = EnumUnitPrice.PRICE_EXACT;
                    cobPriceRequired = true;
                    break;
            }
        });

        rdgUnitOfSaleGrain.setOnCheckedChangeListener((group, radioIndex) -> {
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
            }

            if (enumUnitOfSale != null) {
                unitOfSale = enumUnitOfSale.unitOfSale();
            }
        });

        btnFinish.setOnClickListener(view -> validate(false));
        btnCancel.setOnClickListener(view -> closeActivity(false));

        if (maizeMarketOutlet != null) {
            enumMaizeProduceType = maizeMarketOutlet.getEnumMaizeProduceType();
            enumUnitOfSale = maizeMarketOutlet.getEnumUnitOfSale();
            enumUnitPrice = maizeMarketOutlet.getEnumUnitPrice();

            grainUnitRadioIndex = maizeMarketOutlet.getGrainUnitRadioIndex();
            produceRadioIndex = maizeMarketOutlet.getProduceRadioIndex();
            grainUnitPriceRadioIndex = maizeMarketOutlet.getGrainUnitPriceRadioIndex();


            grainPrice = String.valueOf(maizeMarketOutlet.getExactPrice());
            cobPrice = String.valueOf(maizeMarketOutlet.getExactPrice());

            unitOfSale = enumUnitOfSale.unitOfSale();
            rdgMaizeProduceType.check(produceRadioIndex);

            if (enumMaizeProduceType == EnumMaizeProduceType.GRAIN) {
                rdgUnitOfSaleGrain.check(grainUnitRadioIndex);
            } else if (enumMaizeProduceType == EnumMaizeProduceType.FRESH_COB) {
                editCobPrice.setText(grainPrice);
            }
        }

        showCustomNotificationDialog();
    }

    @Override
    protected void validate(boolean backPressed) {

        dataIsValid = false;
        if (enumMaizeProduceType == null) {
            showCustomWarningDialog(getString(R.string.lbl_invalid_produce), getString(R.string.lbl_maize_produce_prompt));
            return;
        }

        produceRadioIndex = rdgMaizeProduceType.getCheckedRadioButtonId();

        if (grainPriceRequired) {
            if (enumUnitOfSale == null) {
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
            cobPrice = editCobPrice.getText().toString();
            if (Strings.isEmptyOrWhitespace(cobPrice)) {
                showCustomWarningDialog(getString(R.string.lbl_invalid_cob_price), getString(R.string.lbl_cob_price_prompt));
                return;
            }
            exactPrice = mathHelper.convertToDouble(cobPrice);
            dataIsValid = true;
        }


        if (dataIsValid) {
            if (maizeMarketOutlet == null) {
                maizeMarketOutlet = new MaizeMarketOutlet();
            }

            maizeMarketOutlet.setEnumMaizeProduceType(enumMaizeProduceType);
            maizeMarketOutlet.setEnumUnitPrice(enumUnitPrice);
            maizeMarketOutlet.setEnumUnitOfSale(enumUnitOfSale);

            maizeMarketOutlet.setExactPrice(exactPrice);
            maizeMarketOutlet.setGrainUnitPriceRadioIndex(grainUnitPriceRadioIndex);
            maizeMarketOutlet.setGrainUnitRadioIndex(grainUnitRadioIndex);
            maizeMarketOutlet.setProduceRadioIndex(produceRadioIndex);

            long id = objectBoxEntityProcessor.saveMaizeMarketOutlet(maizeMarketOutlet);
            if (id > 0) {
                closeActivity(backPressed);
            }
        }
    }

    public void onGrainUnitRadioButtonClicked(View radioButton) {
        if (radioButton != null && radioButton.isPressed()) {
            showUnitGrainPriceDialog(currency, unitOfSale);
        }
    }

    private void showUnitGrainPriceDialog(String currency, String uos) {
        Bundle arguments = new Bundle();
        arguments.putString(MaizePriceDialogFragment.CURRENCY_CODE, currency);
        arguments.putString(MaizePriceDialogFragment.COUNTRY_CODE, countryCode);
        arguments.putString(MaizePriceDialogFragment.UNIT_OF_SALE, uos);
        arguments.putDouble(MaizePriceDialogFragment.SELECTED_PRICE, exactPrice);
        arguments.putDouble(MaizePriceDialogFragment.AVERAGE_PRICE, averagePrice);
        arguments.putParcelable(MaizePriceDialogFragment.ENUM_UNIT_OF_SALE, enumUnitOfSale);

        MaizePriceDialogFragment priceDialogFragment = new MaizePriceDialogFragment();
        priceDialogFragment.setArguments(arguments);

        priceDialogFragment.setOnDismissListener((selectedPrice, selectedAveragePrice) -> {
            exactPrice = selectedPrice;
            averagePrice = selectedAveragePrice;
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
}
