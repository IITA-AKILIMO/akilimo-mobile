package com.iita.akilimo.views.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

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
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.PotatoMarketOutlet;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.interfaces.IVolleyCallback;
import com.iita.akilimo.models.PotatoPrice;
import com.iita.akilimo.rest.RestParameters;
import com.iita.akilimo.rest.RestService;
import com.iita.akilimo.utils.MathHelper;
import com.iita.akilimo.utils.Tools;
import com.iita.akilimo.utils.enums.EnumPotatoProduceType;
import com.iita.akilimo.utils.enums.EnumPotatoUnitPrice;
import com.iita.akilimo.utils.enums.EnumUnitOfSale;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;
import com.iita.akilimo.views.fragments.dialog.MaizePriceDialogFragment;
import com.iita.akilimo.views.fragments.dialog.SweetPotatoPriceDialogFragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class SweetPotatoMarketActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindString(R.string.lbl_sweet_potato_prices)
    String marketOutletTitle;

    @BindView(R.id.unitOfSalePotatoTitle)
    AppCompatTextView unitOfSalePotatoTitle;

    @BindView(R.id.unitOfSalePotatoCard)
    CardView unitOfSalePotatoCard;

    @BindView(R.id.rdgPotatoProduceType)
    RadioGroup rdgPotatoProduceType;
    @BindView(R.id.rdgUnitOfSalePotato)
    RadioGroup rdgUnitOfSalePotato;

    @BindView(R.id.btnFinish)
    AppCompatButton btnFinish;
    @BindView(R.id.btnCancel)
    AppCompatButton btnCancel;

    private MathHelper mathHelper;
    private PotatoMarketOutlet potatoMarketOutlet;
    private EnumPotatoProduceType enumPotatoProduceType;
    private EnumUnitOfSale enumUnitOfSale;
    private EnumPotatoUnitPrice enumUnitPrice;
    private List<PotatoPrice> potatoPriceList = null;
    private boolean selectionMade = false;

    private String unitOfSale;
    private String tuberPrice;
    private boolean exactPriceRequired;

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
        setContentView(R.layout.activity_sweet_potato_market);

        ButterKnife.bind(this);

        context = this;
        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(context);
        queue = Volley.newRequestQueue(context);
        mathHelper = new MathHelper(this);

        MandatoryInfo mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();
        countryCode = mandatoryInfo.getCountryCode();
        currency = mandatoryInfo.getCurrency();

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
                    objectBoxEntityProcessor.savePotatoPrice(potatoPriceList);
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
        getSupportActionBar().setTitle(marketOutletTitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> validate(false));
    }

    @Override
    protected void initComponent() {
        enumPotatoProduceType = EnumPotatoProduceType.TUBERS;

        potatoMarketOutlet = objectBoxEntityProcessor.getPotatoMarketOutlet();
        if (potatoMarketOutlet != null) {
            produceTypeRadioIndex = potatoMarketOutlet.getProduceTypeRadioIndex();
            potatoUnitOfSaleRadioIndex = potatoMarketOutlet.getPotatoUnitOfSaleRadioIndex();
            potatoUnitPriceRadioIndex = potatoMarketOutlet.getPotatoUnitPriceRadioIndex();

            rdgPotatoProduceType.check(produceTypeRadioIndex);
            rdgUnitOfSalePotato.check(potatoUnitOfSaleRadioIndex);

            tuberPrice = String.valueOf(potatoMarketOutlet.getExactPrice());

            enumUnitPrice = potatoMarketOutlet.getEnumPotatoUnitPrice();
            if (enumUnitPrice == EnumPotatoUnitPrice.PRICE_EXACT) {
                exactPriceRequired = true;
            }
        }
        rdgUnitOfSalePotato.setOnCheckedChangeListener((group, radioIndex) -> {
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
                case R.id.rd_1000_kg_bag:
                    enumUnitOfSale = EnumUnitOfSale.UNIT_THOUSAND_KG;
                    break;
            }

            if (enumUnitOfSale != null) {
                unitOfSale = enumUnitOfSale.unitOfSale();
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
        if (enumUnitOfSale == null) {
            showCustomWarningDialog(getString(R.string.lbl_invalid_sale_unit), getString(R.string.lbl_potato_sale_unit_prompt));
            return;
        }

        if (exactPrice <= 0) {
            showCustomWarningDialog(getString(R.string.lbl_invalid_tuber_price), getString(R.string.lbl_tuber_price_prompt));
            return;
        }

        if (potatoMarketOutlet == null) {
            potatoMarketOutlet = new PotatoMarketOutlet();
        }

        potatoMarketOutlet.setEnumPotatoProduceType(enumPotatoProduceType);
        potatoMarketOutlet.setEnumUnitOfSale(enumUnitOfSale);
        potatoMarketOutlet.setEnumPotatoUnitPrice(enumUnitPrice);
        potatoMarketOutlet.setExactPrice(exactPrice);

        potatoMarketOutlet.setProduceTypeRadioIndex(produceTypeRadioIndex);
        potatoMarketOutlet.setPotatoUnitPriceRadioIndex(potatoUnitPriceRadioIndex);
        potatoMarketOutlet.setPotatoUnitOfSaleRadioIndex(potatoUnitOfSaleRadioIndex);
        long id = objectBoxEntityProcessor.savePotatoMarketOutlet(potatoMarketOutlet);
        if (id > 0) {
            closeActivity(backPressed);
        }

    }

    public void onPotatoUnitRadioButtonClicked(View radioButton) {
        if (radioButton != null && radioButton.isPressed()) {
            showPotatoUnitPriceDialog(currency, unitOfSale);
        }
    }

    private void showPotatoUnitPriceDialog(String currency, String uos) {
        Bundle arguments = new Bundle();
        arguments.putString(SweetPotatoPriceDialogFragment.CURRENCY_CODE, currency);
        arguments.putString(SweetPotatoPriceDialogFragment.COUNTRY_CODE, countryCode);
        arguments.putString(SweetPotatoPriceDialogFragment.UNIT_OF_SALE, uos);
        arguments.putDouble(SweetPotatoPriceDialogFragment.SELECTED_PRICE, exactPrice);
        arguments.putDouble(SweetPotatoPriceDialogFragment.AVERAGE_PRICE, averagePrice);
        arguments.putParcelable(SweetPotatoPriceDialogFragment.ENUM_UNIT_OF_SALE, enumUnitOfSale);

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
}