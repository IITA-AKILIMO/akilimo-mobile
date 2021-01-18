package com.iita.akilimo.views.activities;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.util.Strings;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.iita.akilimo.R;
import com.iita.akilimo.dao.AppDatabase;
import com.iita.akilimo.databinding.ActivityInvestmentAmountBinding;
import com.iita.akilimo.entities.InvestmentAmount;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.utils.CurrencyCode;
import com.iita.akilimo.utils.MathHelper;
import com.mynameismidori.currencypicker.ExtendedCurrency;

;import java.util.Objects;


public class InvestmentAmountActivity extends BaseActivity {

    private String LOG_TAG = InvestmentAmountActivity.class.getSimpleName();


    Toolbar toolbar;
    RadioGroup radioGroup;
    RadioButton rd_25_per_acre;
    RadioButton rd_50_per_acre;
    RadioButton rd_100_per_acre;
    RadioButton rd_150_per_acre;
    RadioButton rd_200_per_acre;
    RadioButton rd_exact_investment;
    EditText txtEditInvestmentAmount;
    TextInputLayout txtEditInvestmentAmountLayout;
    MaterialButton btnFinish;
    ActivityInvestmentAmountBinding binding;
    ExtendedCurrency extendedCurrency;

    String investmentAmountError;

    MathHelper mathHelper;
    private InvestmentAmount invAmount;
    private boolean isExactAmount;
    private boolean hasErrors;

    private String fieldAreaAcre;
    private String fieldArea;

    private double investmentAmountUSD;
    private double investmentAmountLocal;
    private double minInvestmentUSD = 1;
    private double minimumAmountUSD;
    private double minimumAmountLocal;
    private String selectedFieldArea;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInvestmentAmountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = this;
        database = AppDatabase.getDatabase(context);
        mathHelper = new MathHelper();

        toolbar = binding.toolbar;
        radioGroup = binding.radioInvestmentGroup;
        rd_25_per_acre = binding.rd25PerAcre;
        rd_50_per_acre = binding.rd50PerAcre;
        rd_100_per_acre = binding.rd100PerAcre;
        rd_150_per_acre = binding.rd150PerAcre;
        rd_200_per_acre = binding.rd200PerAcre;
        rd_exact_investment = binding.rdExactInvestment;
        txtEditInvestmentAmount = binding.editInvestmentAmount;
        txtEditInvestmentAmountLayout = binding.editInvestmentAmountLayout;
        btnFinish = binding.btnFinish;

        invAmount = database.investmentAmountDao().findOne();


        initToolbar();
        initComponent();
    }

    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.title_activity_investment_amount));
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
        investmentAmountError = getString(R.string.lbl_investment_amount_prompt);
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

            invAmount = database.investmentAmountDao().findOne();

            try {
                if (invAmount == null) {
                    invAmount = new InvestmentAmount();
                }
                invAmount.setInvestmentAmountUSD(investmentAmountUSD);
                invAmount.setMinInvestmentAmountUSD(minimumAmountUSD);
                invAmount.setInvestmentAmountLocal(investmentAmountLocal);
                invAmount.setMinInvestmentAmountLocal(minimumAmountLocal);
                invAmount.setFieldSize(fieldSizeAcre);

                database.investmentAmountDao().insert(invAmount);
                closeActivity(false);
            } catch (Exception ex) {
                Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
                Crashlytics.logException(ex);
            }
        });
        updateLabels();
        showCustomNotificationDialog();
    }

    @Override
    protected void validate(boolean backPressed) {
        throw new UnsupportedOperationException();
    }

    private void updateLabels() {

        ProfileInfo profileInfo = database.profileInfoDao().findOne();
        if (profileInfo != null) {
            countryCode = profileInfo.getCountryCode();
            String _currency = profileInfo.getCurrency();
            if (!Strings.isEmptyOrWhitespace(_currency)) {
                currency = _currency;
            }
        }

        currencyCode = currency;
        String currencySymbol = currency;
        String currencyName = currency;
        extendedCurrency = CurrencyCode.getCurrencySymbol(currency);
        if (extendedCurrency != null) {
            currencySymbol = extendedCurrency.getSymbol();
            currencyName = extendedCurrency.getName();
        }

        MandatoryInfo mandatoryInfo = database.mandatoryInfoDao().findOne();
        if (mandatoryInfo != null) {
            fieldSize = mandatoryInfo.getAreaSize();
            fieldSizeAcre = mandatoryInfo.getAreaSize();
            fieldArea = String.valueOf(fieldSize);
            fieldAreaAcre = String.valueOf(fieldSizeAcre);
            areaUnit = mandatoryInfo.getAreaUnit();
        }
        selectedFieldArea = String.format("%s %s", fieldSize, areaUnit);


        if (Strings.isEmptyOrWhitespace(selectedFieldArea)) {
            return;
        }
        String band_25 = getString(R.string.inv_25_usd_per_acre);
        String band_50 = getString(R.string.inv_50_usd_per_acre);
        String band_100 = getString(R.string.inv_100_usd_per_acre);
        String band_150 = getString(R.string.inv_150_usd_per_acre);
        String band_200 = getString(R.string.inv_200_usd_per_acre);

        String exactText = getString(R.string.exact_investment_x_per_field_area);
        String exactTextHint = getString(R.string.exact_investment_x_per_field_area_hint, currencyName, String.valueOf(fieldSize), areaUnit);
        String separator = getString(R.string.lbl_per_separator);

        //convert the currencies
        //@TODO move this data functionality to the API
        rd_25_per_acre.setText(mathHelper.convertCurrency(band_25, currencyCode, currencySymbol, areaUnit, fieldAreaAcre, selectedFieldArea, separator));
        rd_50_per_acre.setText(mathHelper.convertCurrency(band_50, currencyCode, currencySymbol, areaUnit, fieldAreaAcre, selectedFieldArea, separator));
        rd_100_per_acre.setText(mathHelper.convertCurrency(band_100, currencyCode, currencySymbol, areaUnit, fieldAreaAcre, selectedFieldArea, separator));
        rd_150_per_acre.setText(mathHelper.convertCurrency(band_150, currencyCode, currencySymbol, areaUnit, fieldAreaAcre, selectedFieldArea, separator));
        rd_200_per_acre.setText(mathHelper.convertCurrency(band_200, currencyCode, currencySymbol, areaUnit, fieldAreaAcre, selectedFieldArea, separator));

        rd_exact_investment.setText(exactText);
        txtEditInvestmentAmountLayout.setHint(exactTextHint);
        txtEditInvestmentAmount.setHint(exactTextHint);
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
        String amount = Objects.requireNonNull(txtEditInvestmentAmountLayout.getEditText()).getText().toString();
        if (!Strings.isEmptyOrWhitespace(amount)) {
            investmentAmountLocal = Double.parseDouble(amount);
            investmentAmountUSD = mathHelper.convertToUSD(investmentAmountLocal, currency);
        }

        minimumAmountUSD = mathHelper.computeInvestmentAmount(minInvestmentUSD, fieldSizeAcre, baseCurrency);
        minimumAmountLocal = mathHelper.convertToLocalCurrency(minimumAmountUSD, currency);
        hasErrors = investmentAmountLocal < minimumAmountLocal;
        return investmentAmountError = getString(R.string.lbl_investment_validation_msg, minimumAmountLocal, currencyCode);

    }
}
