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

import com.google.android.gms.common.util.Strings;
import com.google.android.material.snackbar.Snackbar;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.PotatoMarketOutlet;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.utils.MathHelper;
import com.iita.akilimo.utils.enums.EnumPotatoProduceType;
import com.iita.akilimo.utils.enums.EnumPotatoUnitPrice;
import com.iita.akilimo.utils.enums.EnumUnitOfSale;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;

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

    private String unitOfSale;
    private String tuberPrice;
    private boolean exactPrice;

    private int produceTypeRadioIndex;
    private int potatoUnitOfSaleRadioIndex;
    private int potatoUnitPriceRadioIndex;


    double unitPriceUSD = 0.0;
    double unitPriceLocal = 0.0;
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
        mathHelper = new MathHelper(this);

        MandatoryInfo mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();
        countryCode = mandatoryInfo.getCountryCode();
        currency = mandatoryInfo.getCurrency();

        initToolbar();
        initComponent();

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
                exactPrice = true;
            }
        }
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
//                unitOfSalePotatoTitle.setVisibility(View.VISIBLE);
//                unitOfSalePotatoCard.setVisibility(View.VISIBLE);
                unitOfSale = enumUnitOfSale.unitOfSale();
            }
        });
        btnFinish.setOnClickListener(view -> validate(false));
        btnCancel.setOnClickListener(view -> closeActivity(false));
    }

    @Override
    protected void validate(boolean backPressed) {
        produceTypeRadioIndex = rdgPotatoProduceType.getCheckedRadioButtonId();
        potatoUnitOfSaleRadioIndex = rdgUnitOfSalePotato.getCheckedRadioButtonId();
        if (enumPotatoProduceType == null) {
            showCustomWarningDialog("Invalid produce type", "Please indicate sweet potato produce type", "OK");
            return;
        }
        if (enumUnitOfSale == null) {
            showCustomWarningDialog("Invalid unit of sale", "Please indicate sweet potato unit of sale", "OK");
            return;
        }

        if (exactPrice) {
            if (Strings.isEmptyOrWhitespace(tuberPrice)) {
                showCustomWarningDialog("Invalid tuber price", "Please provide valid tuber price", "OK");
                return;
            }
            unitPriceLocal = mathHelper.convertToDouble(tuberPrice);
        } else {
            unitPriceLocal = enumUnitPrice.convertToLocalCurrency(currency, mathHelper);
        }

        if (potatoMarketOutlet == null) {
            potatoMarketOutlet = new PotatoMarketOutlet();
        }

        potatoMarketOutlet.setEnumPotatoProduceType(enumPotatoProduceType);
        potatoMarketOutlet.setEnumUnitOfSale(enumUnitOfSale);
        potatoMarketOutlet.setEnumPotatoUnitPrice(enumUnitPrice);
        potatoMarketOutlet.setExactPrice(unitPriceLocal);

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

    private String labelText(double unitPriceLower, double unitPriceUpper, String currency, String uos) {
        //cross convert according to weight
        double priceLower = 0;
        double priceHigher = 0;
        int nearestRounding = 10;

        switch (enumUnitOfSale) {
            case UNIT_ONE_KG:
                priceLower = (unitPriceLower * EnumUnitOfSale.UNIT_ONE_KG.unitWeight()) / 1000;
                priceHigher = (unitPriceUpper * EnumUnitOfSale.UNIT_ONE_KG.unitWeight()) / 1000;
                break;
            case UNIT_FIFTY_KG:
                priceLower = (unitPriceLower * EnumUnitOfSale.UNIT_FIFTY_KG.unitWeight()) / 1000;
                priceHigher = (unitPriceUpper * EnumUnitOfSale.UNIT_FIFTY_KG.unitWeight()) / 1000;
//                nearestRounding = 50;
                break;
            case UNIT_HUNDRED_KG:
                priceLower = (unitPriceLower * EnumUnitOfSale.UNIT_HUNDRED_KG.unitWeight()) / 1000;
                priceHigher = (unitPriceUpper * EnumUnitOfSale.UNIT_HUNDRED_KG.unitWeight()) / 1000;
//                nearestRounding = 100;
                break;
            case UNIT_THOUSAND_KG:
                priceLower = (unitPriceLower * EnumUnitOfSale.UNIT_THOUSAND_KG.unitWeight()) / 1000;
                priceHigher = (unitPriceUpper * EnumUnitOfSale.UNIT_THOUSAND_KG.unitWeight()) / 1000;
                nearestRounding = 100;
                break;
        }

        minAmountUSD = priceLower; //minimum amount will be dynamic based on weight being sold, max amount will be constant
        double localLower = mathHelper.convertToLocalCurrency(priceLower, currency, nearestRounding);
        double localHigher = mathHelper.convertToLocalCurrency(priceHigher, currency, nearestRounding);

        return context.getString(R.string.unit_price_label, localLower, localHigher, currency, uos);
    }

    private void showPotatoUnitPriceDialog(String currency, String uos) {
        int oldIndex = 0;
        if (dialogOpen) {
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

        final RadioButton rd_20_30_price = dialog.findViewById(R.id.rd_20_30_price);
        final RadioButton rd_30_50_price = dialog.findViewById(R.id.rd_30_50_price);
        final RadioButton rd_50_100_price = dialog.findViewById(R.id.rd_50_100_price);

        dialog.findViewById(R.id.rd_100_150_price).setVisibility(View.GONE);
        dialog.findViewById(R.id.rd_150_200_price).setVisibility(View.GONE);
        //update labels according to earlier selected values

        if (exactPrice || enumUnitPrice == EnumPotatoUnitPrice.PRICE_EXACT) {
            etUnitPrice.setVisibility(View.VISIBLE);
            etUnitPrice.setText(tuberPrice);
        }

        unitPriceTitle.setText(String.format(getString(R.string.lbl_sweet_potato_tuber_unit_price_per), currency, uos));
        try {
            rd_20_30_price.setText(labelText(EnumPotatoUnitPrice.PRICE_RANGE_ONE.unitPricePerTonneLower(), EnumPotatoUnitPrice.PRICE_RANGE_ONE.unitPricePerTonneUpper(), currency, uos));
            rd_30_50_price.setText(labelText(EnumPotatoUnitPrice.PRICE_RANGE_TWO.unitPricePerTonneLower(), EnumPotatoUnitPrice.PRICE_RANGE_TWO.unitPricePerTonneUpper(), currency, uos));
            rd_50_100_price.setText(labelText(EnumPotatoUnitPrice.PRICE_RANGE_THREE.unitPricePerTonneLower(), EnumPotatoUnitPrice.PRICE_RANGE_THREE.unitPricePerTonneUpper(), currency, uos));
        } catch (Exception ex) {
            Timber.e(ex);
        }

        rdgUnitPrice.setOnCheckedChangeListener((group, radioIndex) -> {
            etUnitPrice.setVisibility(View.GONE);
            exactPrice = false;
            switch (radioIndex) {
                case R.id.rd_20_30_price:
                    enumUnitPrice = EnumPotatoUnitPrice.PRICE_RANGE_ONE;
                    break;
                case R.id.rd_30_50_price:
                    enumUnitPrice = EnumPotatoUnitPrice.PRICE_RANGE_TWO;
                    break;
                case R.id.rd_50_100_price:
                    enumUnitPrice = EnumPotatoUnitPrice.PRICE_RANGE_THREE;
                    break;
                case R.id.rd_exact_price:
                    exactPrice = true;
                    enumUnitPrice = EnumPotatoUnitPrice.PRICE_EXACT;
                    etUnitPrice.setVisibility(View.VISIBLE);
                    break;
            }
        });

        dialog.findViewById(R.id.bt_cancel).setOnClickListener(v -> {
            dialog.dismiss();
            dialogOpen = false;
            rdgUnitOfSalePotato.clearCheck();
            rdgUnitOfSalePotato.check(potatoUnitOfSaleRadioIndex);
        });

        dialog.findViewById(R.id.bt_submit).setOnClickListener(view -> {
            if (enumUnitPrice == null) {
                Snackbar.make(view, "Please enter a valid amount", Snackbar.LENGTH_LONG).show();
                return;
            }
            if (exactPrice) {
                tuberPrice = etUnitPrice.getText().toString().trim();
                if (Strings.isEmptyOrWhitespace(tuberPrice)) {
                    Snackbar.make(view, "Please enter a valid amount", Snackbar.LENGTH_LONG).show();
                    return;
                }
            } else {
                tuberPrice = "0";
            }
            potatoUnitOfSaleRadioIndex = rdgUnitOfSalePotato.getCheckedRadioButtonId();
            potatoUnitPriceRadioIndex = rdgUnitPrice.getCheckedRadioButtonId();
            dialog.dismiss();
            dialogOpen = false;
        });

        rdgUnitPrice.check(potatoUnitPriceRadioIndex);
        dialog.show();
        dialog.getWindow().setAttributes(lp);
        dialogOpen = true;
    }
}