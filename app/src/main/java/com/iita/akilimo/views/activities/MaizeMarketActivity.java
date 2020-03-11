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

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.gms.common.util.Strings;
import com.google.android.material.snackbar.Snackbar;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.MaizeMarketOutlet;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.utils.MathHelper;
import com.iita.akilimo.utils.enums.EnumMaizeProduceType;
import com.iita.akilimo.utils.enums.EnumUnitOfSale;
import com.iita.akilimo.utils.enums.EnumUnitPrice;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

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

    double unitPriceUSD = 0.0;
    double unitPriceLocal = 0.0;
    private double minAmountUSD = 5.00;
    private double maxAmountUSD = 500.00;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maize_market);
        ButterKnife.bind(this);

        context = this;
        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(context);
        mathHelper = new MathHelper(this);

        maizeMarketOutlet = objectBoxEntityProcessor.getMaizeMarketOutlet();

        MandatoryInfo mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();
        countryCode = mandatoryInfo.getCountryCode();
        currency = mandatoryInfo.getCurrency();

        initToolbar();
        initComponent();
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
    }

    @Override
    protected void validate(boolean backPressed) {

        dataIsValid = false;
        if (enumMaizeProduceType == null) {
            showCustomWarningDialog("Invalid produce type", "Please indicate maize produce type", "OK");
            return;
        }

        produceRadioIndex = rdgMaizeProduceType.getCheckedRadioButtonId();

        if (grainPriceRequired) {
            if (enumUnitOfSale == null) {
                showCustomWarningDialog("Invalid unit of sale", "Please indicate maize unit of sale", "OK");
                return;
            }
            if (exactPriceSelected) {
                if (Strings.isEmptyOrWhitespace(grainPrice)) {
                    showCustomWarningDialog("Invalid grain price", "Please provide valid grain price", "OK");
                    return;
                }
                unitPriceLocal = mathHelper.convertToDouble(grainPrice);
            } else {
                unitPriceLocal = enumUnitPrice.convertToLocalCurrency(currency, mathHelper);
            }
            grainUnitRadioIndex = rdgUnitOfSaleGrain.getCheckedRadioButtonId();
            dataIsValid = true;
        }

        if (cobPriceRequired) {
            cobPrice = editCobPrice.getText().toString();
            if (Strings.isEmptyOrWhitespace(cobPrice)) {
                showCustomWarningDialog("Invalid cob price", "Please provide valid cob price", "OK");
                return;
            }
            unitPriceLocal = mathHelper.convertToDouble(cobPrice);
            dataIsValid = true;
        }


        if (dataIsValid) {
            if (maizeMarketOutlet == null) {
                maizeMarketOutlet = new MaizeMarketOutlet();
            }

            maizeMarketOutlet.setEnumMaizeProduceType(enumMaizeProduceType);
            maizeMarketOutlet.setEnumUnitPrice(enumUnitPrice);
            maizeMarketOutlet.setEnumUnitOfSale(enumUnitOfSale);

            maizeMarketOutlet.setExactPrice(unitPriceLocal);
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

        return context.getString(R.string.unit_price_label, localLower, localHigher, currency, uos);
    }

    private void showUnitGrainPriceDialog(String currency, String uos) {
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
        //update the radiobutton labels
        final RadioButton rd_20_30_price = dialog.findViewById(R.id.rd_20_30_price);
        final RadioButton rd_30_50_price = dialog.findViewById(R.id.rd_30_50_price);
        final RadioButton rd_50_100_price = dialog.findViewById(R.id.rd_50_100_price);
        final RadioButton rd_100_150_price = dialog.findViewById(R.id.rd_100_150_price);
        final RadioButton rd_150_200_price = dialog.findViewById(R.id.rd_150_200_price);
        //update labels according to earlier selected values


        if (exactPriceSelected || enumUnitPrice == EnumUnitPrice.PRICE_EXACT) {
            etUnitPrice.setVisibility(View.VISIBLE);
            etUnitPrice.setText(grainPrice);
        }

        unitPriceTitle.setText(String.format(getString(R.string.lbl_grain_unit_price_per), currency, unitOfSale));
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
            if (grainUnitPriceRadioIndex <= 0) {
                rdgUnitOfSaleGrain.clearCheck();
            }
        });

        dialog.findViewById(R.id.bt_submit).setOnClickListener(view -> {
            if (enumUnitPrice == null) {
                Snackbar.make(view, "Please enter a valid amount", Snackbar.LENGTH_LONG).show();
                return;
            }
            if (exactPriceSelected) {
                grainPrice = etUnitPrice.getText().toString().trim();
                if (Strings.isEmptyOrWhitespace(grainPrice)) {
                    dataIsValid = false;
                    Snackbar.make(view, "Please enter a valid amount", Snackbar.LENGTH_LONG).show();
                    return;
                }
            } else {
                grainPrice = "0";
            }
            grainUnitPriceRadioIndex = rdgUnitPrice.getCheckedRadioButtonId();
            dialog.dismiss();
            dialogOpen = false;
        });


        rdgUnitPrice.check(grainUnitPriceRadioIndex);
        dialog.show();
        dialog.getWindow().setAttributes(lp);
        dialogOpen = true;
    }
}
