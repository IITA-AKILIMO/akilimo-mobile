package com.iita.akilimo.views.activities;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.common.util.Strings;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.InvestmentAmount;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.utils.MathHelper;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class InvestmentAmountActivity extends BaseActivity {

    private String LOG_TAG = InvestmentAmountActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindString(R.string.title_activity_investment_amount)
    String investmentAmount;

    @BindView(R.id.radioInvestmentGroup)
    RadioGroup radioGroup;

    @BindView(R.id.rd_25_per_acre)
    RadioButton rd_25_per_acre;

    @BindView(R.id.rd_50_per_acre)
    RadioButton rd_50_per_acre;

    @BindView(R.id.rd_100_per_acre)
    RadioButton rd_100_per_acre;

    @BindView(R.id.rd_150_per_acre)
    RadioButton rd_150_per_acre;

    @BindView(R.id.rd_200_per_acre)
    RadioButton rd_200_per_acre;

    @BindView(R.id.rd_exact_investment)
    RadioButton rd_exact_investment;
    @BindView(R.id.editInvestmentAmount)
    EditText txtEditInvestmentAmount;

    @BindView(R.id.editInvestmentAmountLayout)
    TextInputLayout txtEditInvestmentAmountLayout;

    @BindView(R.id.btnFinish)
    MaterialButton btnFinish;


    @BindString(R.string.lbl_investment_amount_prompt)
    String investmentAmountError;

    MathHelper mathHelper;
    private boolean isExactAmount;
    private boolean hasErrors;

    private String fieldAreaAcre;
    private String fieldArea;

    private double investmentAmountUSD;
    private double investmentAmountLocal;
    private double minInvestmentUSD = 25;
    private double minimumAmountUSD;
    private double minimumAmountLocal;
    private String selectedFieldArea;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investment_amount);
        ButterKnife.bind(this);
        context = this;
        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(this);
        mathHelper = new MathHelper();

        initToolbar();
        initComponent();
    }

    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(investmentAmount);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> {
            validateInvestmentAmount();
            if (!hasErrors) {
                closeActivity(false);
            } else {
                showCustomWarningDialog(getString(R.string.lbl_invalid_investment_amount), investmentAmountError);
            }
        });
    }

    @Override
    protected void initComponent() {
        radioGroup.setOnCheckedChangeListener((radioGroup1, radioChecked) -> {
            double amountToInvest = 0;
            isExactAmount = false;
            txtEditInvestmentAmountLayout.setVisibility(View.GONE);
            switch (radioChecked) {
                case R.id.rd_25_per_acre:
                    amountToInvest = 25;
                    break;
                case R.id.rd_50_per_acre:
                    amountToInvest = 50;
                    break;
                case R.id.rd_100_per_acre:
                    amountToInvest = 100;
                    break;
                case R.id.rd_150_per_acre:
                    amountToInvest = 150;
                    break;
                case R.id.rd_200_per_acre:
                    amountToInvest = 200;
                    break;
                case R.id.rd_exact_investment:
                    isExactAmount = true;
                    txtEditInvestmentAmountLayout.setVisibility(View.VISIBLE);
                    txtEditInvestmentAmountLayout.requestFocus();
                    return;
            }
            investmentAmountUSD = mathHelper.computeInvestmentAmount(amountToInvest, fieldSizeAcre, baseCurrency);
            investmentAmountLocal = mathHelper.convertToLocalCurrency(investmentAmountUSD, currency);

        });
        txtEditInvestmentAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                validateEditText(editable);
            }
        });
        btnFinish.setOnClickListener(view -> {
            validateInvestmentAmount();
            if (hasErrors) {
                showCustomWarningDialog(getString(R.string.lbl_invalid_investment_amount), investmentAmountError);
                return;
            }

            InvestmentAmount invAmount = objectBoxEntityProcessor.getInvestmentAmount();
            if (invAmount == null) {
                invAmount = new InvestmentAmount();
            }
            invAmount.setInvestmentAmountUSD(investmentAmountUSD);
            invAmount.setInvestmentAmountLocal(investmentAmountLocal);
            invAmount.setMinInvestmentAmountLocal(minimumAmountLocal);
            invAmount.setMinInvestmentAmountUSD(minimumAmountUSD);

            objectBoxEntityProcessor.saveInvestmentAmount(invAmount);
            closeActivity(false);
        });
        updateLabels();
        showCustomNotificationDialog();
    }

    @Override
    protected void validate(boolean backPressed) {
        throw new UnsupportedOperationException();
    }

    private void updateLabels() {

        MandatoryInfo mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();
        fieldSize = mandatoryInfo.getAreaSize();
        fieldSizeAcre = mandatoryInfo.getAcreAreaSize();
        fieldArea = String.valueOf(fieldSize);
        fieldAreaAcre = String.valueOf(fieldSizeAcre);

        areaUnit = mandatoryInfo.getAreaUnit();
        selectedFieldArea = String.format("%s %s", fieldSize, areaUnit);

        currency = mandatoryInfo.getCurrency();

        if (Strings.isEmptyOrWhitespace(selectedFieldArea)) {
            return;
        }
        String band_25 = getString(R.string.inv_25_usd_per_acre);
        String band_50 = getString(R.string.inv_50_usd_per_acre);
        String band_100 = getString(R.string.inv_100_usd_per_acre);
        String band_150 = getString(R.string.inv_150_usd_per_acre);
        String band_200 = getString(R.string.inv_200_usd_per_acre);

        String exactText = getString(R.string.exact_investment_x_per_field_area);

        //convert the currencies
        rd_25_per_acre.setText(mathHelper.convertCurrency(band_25, currency, areaUnit, fieldAreaAcre, selectedFieldArea));
        rd_50_per_acre.setText(mathHelper.convertCurrency(band_50, currency, areaUnit, fieldAreaAcre, selectedFieldArea));
        rd_100_per_acre.setText(mathHelper.convertCurrency(band_100, currency, areaUnit, fieldAreaAcre, selectedFieldArea));
        rd_150_per_acre.setText(mathHelper.convertCurrency(band_150, currency, areaUnit, fieldAreaAcre, selectedFieldArea));
        rd_200_per_acre.setText(mathHelper.convertCurrency(band_200, currency, areaUnit, fieldAreaAcre, selectedFieldArea));

        rd_exact_investment.setText(exactText);
        txtEditInvestmentAmount.setHint(exactText);
    }

    private void validateEditText(Editable editable) {
        investmentAmountError = validateInvestmentAmount();
        if (TextUtils.isEmpty(editable) || hasErrors) {
            txtEditInvestmentAmountLayout.setError(investmentAmountError);
        } else {
            txtEditInvestmentAmountLayout.setError(null);
        }
    }

    private String validateInvestmentAmount() {
        String amount = txtEditInvestmentAmountLayout.getEditText().getText().toString();
        if (!Strings.isEmptyOrWhitespace(amount)) {
            investmentAmountLocal = Double.parseDouble(amount);
            investmentAmountUSD = mathHelper.convertToUSD(investmentAmountLocal, currency);
        }

        minimumAmountUSD = mathHelper.computeInvestmentAmount(minInvestmentUSD, fieldSizeAcre, baseCurrency);
        minimumAmountLocal = mathHelper.convertToLocalCurrency(minimumAmountUSD, currency);
        hasErrors = investmentAmountLocal < minimumAmountLocal;
        return investmentAmountError = getString(R.string.lbl_investment_validation_msg, minimumAmountLocal, currency);

    }
}
