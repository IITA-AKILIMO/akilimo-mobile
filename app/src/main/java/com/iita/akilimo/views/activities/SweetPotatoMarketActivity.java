package com.iita.akilimo.views.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.iita.akilimo.databinding.ActivitySweetPotatoMarketBinding;
import com.iita.akilimo.entities.AdviceStatus;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.PotatoMarket;
import com.iita.akilimo.entities.PotatoPrice;
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.interfaces.IVolleyCallback;
import com.iita.akilimo.rest.RestParameters;
import com.iita.akilimo.rest.RestService;
import com.iita.akilimo.utils.MathHelper;
import com.iita.akilimo.utils.Tools;
import com.iita.akilimo.utils.enums.EnumAdviceTasks;
import com.iita.akilimo.utils.enums.EnumPotatoProduceType;
import com.iita.akilimo.utils.enums.EnumUnitOfSale;
import com.iita.akilimo.views.fragments.dialog.SweetPotatoPriceDialogFragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class SweetPotatoMarketActivity extends BaseActivity {


    Toolbar toolbar;
    AppCompatTextView unitOfSalePotatoTitle;
    CardView unitOfSalePotatoCard;
    RadioGroup rdgPotatoProduceType;
    RadioGroup rdgUnitOfSalePotato;

    AppCompatButton btnFinish;
    AppCompatButton btnCancel;

    ActivitySweetPotatoMarketBinding binding;

    private MathHelper mathHelper;
    private PotatoMarket potatoMarket;
    private String enumPotatoProduceType;
    private List<PotatoPrice> potatoPriceList = null;
    private boolean selectionMade = false;

    private String unitOfSale;
    private EnumUnitOfSale unitOfSaleEnum = EnumUnitOfSale.FIFTY_KG;

    private int produceTypeRadioIndex;
    private int potatoUnitOfSaleRadioIndex;
    private int potatoUnitPriceRadioIndex;


    double unitPriceUSD = 0.0;
    double unitPriceLocal = 0.0;
    private double unitPrice = 0.0;
    private int unitWeight;

    private double minAmountUSD = 5.00;
    private double maxAmountUSD = 500.00;

    private boolean dialogOpen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySweetPotatoMarketBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = this;
        database = AppDatabase.getDatabase(context);

        queue = Volley.newRequestQueue(context);
        mathHelper = new MathHelper(this);

        ProfileInfo profileInfo = database.profileInfoDao().findOne();
        if (profileInfo != null) {
            countryCode = profileInfo.getCountryCode();
            currency = profileInfo.getCurrency();
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

        potatoMarket = database.potatoMarketDao().findOne();
        if (potatoMarket != null) {
            produceTypeRadioIndex = potatoMarket.getProduceTypeRadioIndex();
            potatoUnitOfSaleRadioIndex = potatoMarket.getPotatoUnitOfSaleRadioIndex();
            potatoUnitPriceRadioIndex = potatoMarket.getPotatoUnitPriceRadioIndex();


            unitPrice = potatoMarket.getUnitPrice();
            unitOfSale = potatoMarket.getUnitOfSale();
            unitWeight = potatoMarket.getUnitWeight();
            rdgPotatoProduceType.check(produceTypeRadioIndex);
            if (unitWeight > 0) {
                rdgUnitOfSalePotato.check(potatoUnitOfSaleRadioIndex);
            }
        }
        rdgUnitOfSalePotato.setOnCheckedChangeListener((group, radioIndex) -> {
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
                case R.id.rd_1000_kg_bag:
                    unitOfSale = EnumUnitOfSale.THOUSAND_KG.unitOfSale(context);
                    unitOfSaleEnum = EnumUnitOfSale.THOUSAND_KG;
                    unitWeight = EnumUnitOfSale.THOUSAND_KG.unitWeight();
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

        if (unitPrice <= 0) {
            showCustomWarningDialog(getString(R.string.lbl_invalid_tuber_price), getString(R.string.lbl_tuber_price_prompt));
            return;
        }

        try {
            if (potatoMarket == null) {
                potatoMarket = new PotatoMarket();
            }

            potatoMarket.setProduceType(enumPotatoProduceType);
            potatoMarket.setUnitOfSale(unitOfSale);
            potatoMarket.setUnitWeight(unitWeight);
            potatoMarket.setUnitPrice(unitPrice);

            potatoMarket.setProduceTypeRadioIndex(produceTypeRadioIndex);
            potatoMarket.setPotatoUnitPriceRadioIndex(potatoUnitPriceRadioIndex);
            potatoMarket.setPotatoUnitOfSaleRadioIndex(potatoUnitOfSaleRadioIndex);

            database.potatoMarketDao().insert(potatoMarket);
            database.adviceStatusDao().insert(new AdviceStatus(EnumAdviceTasks.MARKET_OUTLET_SWEET_POTATO.name(), true));
            closeActivity(backPressed);
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }


    }

    public void onPotatoUnitRadioButtonClicked(View radioButton) {
        if (radioButton != null && radioButton.isPressed()) {
            showPotatoUnitPriceDialog();
        }
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

                    if (potatoPriceList.size() > 0) {
                        database.potatoPriceDao().insertAll(potatoPriceList);
                    }

                } catch (Exception ex) {
                    Snackbar.make(unitOfSalePotatoCard, ex.getMessage(), Snackbar.LENGTH_LONG).show();
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
                    Crashlytics.log(error);
                    Snackbar.make(unitOfSalePotatoCard, error, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showPotatoUnitPriceDialog() {
        Bundle arguments = new Bundle();

        arguments.putString(SweetPotatoPriceDialogFragment.CURRENCY_CODE, currency);
        arguments.putString(SweetPotatoPriceDialogFragment.COUNTRY_CODE, countryCode);
        arguments.putDouble(SweetPotatoPriceDialogFragment.SELECTED_PRICE, unitPrice);
        arguments.putString(SweetPotatoPriceDialogFragment.UNIT_OF_SALE, unitOfSale);
        arguments.putParcelable(SweetPotatoPriceDialogFragment.ENUM_UNIT_OF_SALE, unitOfSaleEnum);

        SweetPotatoPriceDialogFragment priceDialogFragment = new SweetPotatoPriceDialogFragment(context);
        priceDialogFragment.setArguments(arguments);

        priceDialogFragment.setOnDismissListener((selectedPrice, isExactPrice) -> {
            if (isExactPrice) {
                unitPrice = selectedPrice;
            } else {
                unitPrice = mathHelper.convertToUnitWeightPrice(selectedPrice, unitWeight);
            }
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
}