package com.iita.akilimo.views.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.iita.akilimo.databinding.ActivitySweetPotatoMarketBinding;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.PotatoMarketOutlet;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.interfaces.IVolleyCallback;
import com.iita.akilimo.models.PotatoPrice;
import com.iita.akilimo.rest.RestParameters;
import com.iita.akilimo.rest.RestService;
import com.iita.akilimo.utils.MathHelper;
import com.iita.akilimo.utils.RealmProcessor;
import com.iita.akilimo.utils.Tools;
import com.iita.akilimo.utils.enums.EnumPotatoProduceType;
import com.iita.akilimo.utils.enums.EnumUnitOfSale;
import com.iita.akilimo.views.fragments.dialog.SweetPotatoPriceDialogFragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import io.realm.Realm;

public class SweetPotatoMarketActivity extends BaseActivity {


    Toolbar toolbar;
    AppCompatTextView unitOfSalePotatoTitle;
    CardView unitOfSalePotatoCard;
    RadioGroup rdgPotatoProduceType;
    RadioGroup rdgUnitOfSalePotato;

    AppCompatButton btnFinish;
    AppCompatButton btnCancel;

    ActivitySweetPotatoMarketBinding binding;
    Realm myRealm;

    private MathHelper mathHelper;
    private PotatoMarketOutlet potatoMarketOutlet;
    private String enumPotatoProduceType;
    private List<PotatoPrice> potatoPriceList = null;
    private boolean selectionMade = false;

    private String unitOfSale;

    private int produceTypeRadioIndex;
    private int potatoUnitOfSaleRadioIndex;
    private int potatoUnitPriceRadioIndex;


    double unitPriceUSD = 0.0;
    double unitPriceLocal = 0.0;
    private double exactPrice = 0.0;
    private double averagePrice = 0.0;

    private double minAmountUSD = 5.00;
    private double maxAmountUSD = 500.00;

    private boolean dialogOpen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySweetPotatoMarketBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = this;
        realmProcessor = new RealmProcessor();
        myRealm = Realm.getDefaultInstance();

        queue = Volley.newRequestQueue(context);
        mathHelper = new MathHelper(this);

        MandatoryInfo mandatoryInfo = realmProcessor.getMandatoryInfo();
        if (mandatoryInfo != null) {
            countryCode = mandatoryInfo.getCountryCode();
            currency = mandatoryInfo.getCurrency();
        }

        toolbar = binding.toolbar;
        unitOfSalePotatoTitle = binding.potatoMarket.unitOfSalePotatoTitle;
        unitOfSalePotatoCard = binding.potatoMarket.unitOfSalePotatoCard;
        rdgPotatoProduceType = binding.potatoMarket.rdgPotatoProduceType;
        rdgUnitOfSalePotato = binding.potatoMarket.rdgUnitOfSalePotato;
        btnFinish = binding.potatoMarket.twoButtons.btnFinish;
        btnCancel = binding.potatoMarket.twoButtons.btnCancel;

        initToolbar();
        initComponent();
        processPotatoPrices();
    }

    private void processPotatoPrices() {
        final RestService restService = RestService.getInstance(queue, this);
        final ObjectMapper objectMapper = new ObjectMapper();
        final RestParameters restParameters = new RestParameters(
                String.format("v3/potato-prices/country/%s", countryCode),
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
                    potatoPriceList = objectMapper.readValue(jsonArray.toString(), new TypeReference<List<PotatoPrice>>() {
                    });
                    //objectBoxEntityProcessor.savePotatoPrice(potatoPriceList);
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
                    Snackbar.make(unitOfSalePotatoCard, error, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.lbl_sweet_potato_prices));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> validate(false));
    }

    @Override
    protected void initComponent() {
        enumPotatoProduceType = EnumPotatoProduceType.TUBERS.produce();

        potatoMarketOutlet = realmProcessor.getPotatoMarketOutlet();
        if (potatoMarketOutlet != null) {
            produceTypeRadioIndex = potatoMarketOutlet.getProduceTypeRadioIndex();
            potatoUnitOfSaleRadioIndex = potatoMarketOutlet.getPotatoUnitOfSaleRadioIndex();
            potatoUnitPriceRadioIndex = potatoMarketOutlet.getPotatoUnitPriceRadioIndex();

            rdgPotatoProduceType.check(produceTypeRadioIndex);
            rdgUnitOfSalePotato.check(potatoUnitOfSaleRadioIndex);
            exactPrice = potatoMarketOutlet.getUnitPrice();
        }
        rdgUnitOfSalePotato.setOnCheckedChangeListener((group, radioIndex) -> {
            switch (radioIndex) {
                case R.id.rd_per_kg:
                    unitOfSale = EnumUnitOfSale.ONE_KG.unitOfSale();
                    break;
                case R.id.rd_50_kg_bag:
                    unitOfSale = EnumUnitOfSale.FIFTY_KG.unitOfSale();
                    break;
                case R.id.rd_100_kg_bag:
                    unitOfSale = EnumUnitOfSale.HUNDRED_KG.unitOfSale();
                    break;
                case R.id.rd_1000_kg_bag:
                    unitOfSale = EnumUnitOfSale.THOUSAND_KG.unitOfSale();
                    break;
            }
        });
        btnFinish.setOnClickListener(view -> validate(false));
        btnCancel.setOnClickListener(view -> closeActivity(false));

        showCustomNotificationDialog();
    }

    @Override
    protected void validate(boolean backPressed) {
        produceTypeRadioIndex = rdgPotatoProduceType.getCheckedRadioButtonId();
        potatoUnitOfSaleRadioIndex = rdgUnitOfSalePotato.getCheckedRadioButtonId();
        if (enumPotatoProduceType == null) {
            showCustomWarningDialog(getString(R.string.lbl_invalid_produce), getString(R.string.lbl_potato_produce_prompt));
            return;
        }
        if (Strings.isEmptyOrWhitespace(unitOfSale)) {
            showCustomWarningDialog(getString(R.string.lbl_invalid_sale_unit), getString(R.string.lbl_potato_sale_unit_prompt));
            return;
        }

        if (exactPrice <= 0) {
            showCustomWarningDialog(getString(R.string.lbl_invalid_tuber_price), getString(R.string.lbl_tuber_price_prompt));
            return;
        }

        try {
            myRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    if (potatoMarketOutlet == null) {
                        potatoMarketOutlet = realm.createObject(PotatoMarketOutlet.class);
                    }

                    potatoMarketOutlet.setProduceType(enumPotatoProduceType);
                    potatoMarketOutlet.setUnitOfSale(unitOfSale);
                    potatoMarketOutlet.setUnitPrice(exactPrice);

                    potatoMarketOutlet.setProduceTypeRadioIndex(produceTypeRadioIndex);
                    potatoMarketOutlet.setPotatoUnitPriceRadioIndex(potatoUnitPriceRadioIndex);
                    potatoMarketOutlet.setPotatoUnitOfSaleRadioIndex(potatoUnitOfSaleRadioIndex);

                }
            });
            closeActivity(backPressed);
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }


    }

    public void onPotatoUnitRadioButtonClicked(View radioButton) {
        if (radioButton != null && radioButton.isPressed()) {
            //we need to validate the data first since we are passing null values there
            showPotatoUnitPriceDialog();
        }
    }

    private void showPotatoUnitPriceDialog() {
        Bundle arguments = new Bundle();
        //check if values are null
        arguments.putString(SweetPotatoPriceDialogFragment.CURRENCY_CODE, currency);
        arguments.putString(SweetPotatoPriceDialogFragment.COUNTRY_CODE, countryCode);
        arguments.putDouble(SweetPotatoPriceDialogFragment.SELECTED_PRICE, exactPrice);
        arguments.putDouble(SweetPotatoPriceDialogFragment.AVERAGE_PRICE, averagePrice);
        arguments.putString(SweetPotatoPriceDialogFragment.UNIT_OF_SALE, unitOfSale);

        SweetPotatoPriceDialogFragment priceDialogFragment = new SweetPotatoPriceDialogFragment();
        priceDialogFragment.setArguments(arguments);

        priceDialogFragment.setOnDismissListener((selectedPrice, selectedAveragePrice) -> {
            exactPrice = selectedPrice;
            averagePrice = selectedAveragePrice;
        });

        FragmentTransaction fragmentTransaction;
        if (getFragmentManager() != null) {
            fragmentTransaction = getSupportFragmentManager().beginTransaction();
            Fragment prev = getSupportFragmentManager().findFragmentByTag(SweetPotatoPriceDialogFragment.ARG_ITEM_ID);
            if (prev != null) {
                fragmentTransaction.remove(prev);
            }
            fragmentTransaction.addToBackStack(null);
            priceDialogFragment.show(getSupportFragmentManager(), SweetPotatoPriceDialogFragment.ARG_ITEM_ID);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myRealm.close();
    }
}