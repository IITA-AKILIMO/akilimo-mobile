package com.akilimo.mobile.views.activities;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.akilimo.mobile.entities.InvestmentAmount;
import com.akilimo.mobile.interfaces.IVolleyCallback;
import com.akilimo.mobile.entities.InvestmentAmountDto;
import com.akilimo.mobile.rest.RestParameters;
import com.akilimo.mobile.rest.RestService;
import com.android.volley.VolleyError;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.util.Strings;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.akilimo.mobile.R;
import com.akilimo.mobile.dao.AppDatabase;
import com.akilimo.mobile.databinding.ActivityInvestmentAmountBinding;
import com.akilimo.mobile.entities.AdviceStatus;
import com.akilimo.mobile.entities.MandatoryInfo;
import com.akilimo.mobile.entities.ProfileInfo;
import com.akilimo.mobile.inherit.BaseActivity;
import com.akilimo.mobile.utils.CurrencyCode;
import com.akilimo.mobile.utils.MathHelper;
import com.akilimo.mobile.utils.enums.EnumAdviceTasks;
import com.mynameismidori.currencypicker.ExtendedCurrency;

;import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.sentry.Sentry;


public class InvestmentAmountActivity extends BaseActivity {

    private String LOG_TAG = InvestmentAmountActivity.class.getSimpleName();

    Toolbar toolbar;
    RadioGroup rdgInvestmentAmount;
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
    private double maxInvestmentUSD = 1;
    private double minimumAmountUSD;
    private double minimumAmountLocal;
    private double maxAmountLocal;
    private double selectedPrice = -1.0;
    private String selectedFieldArea;

    private List<InvestmentAmountDto> investmentAmountList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInvestmentAmountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = this;
        database = AppDatabase.getDatabase(context);
        mathHelper = new MathHelper();

        toolbar = binding.toolbar;
        rdgInvestmentAmount = binding.rdgInvestmentAmount;
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

        rdgInvestmentAmount.setOnCheckedChangeListener((group, checkedId) -> {
            int radioButtonId = group.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(radioButtonId);
            long itemTagIndex = (long) radioButton.getTag();

            InvestmentAmountDto inv = database.investmentAmountDtoDao().findOneByInvestmentId(itemTagIndex);
            if (inv != null) {
                investmentAmountLocal = inv.getInvestmentAmount();
                if (inv.getSortOrder() != 0) {
                    isExactAmount = false;
                    txtEditInvestmentAmountLayout.setVisibility(View.GONE);
                } else {
                    isExactAmount = true;
                    txtEditInvestmentAmountLayout.setVisibility(View.VISIBLE);
                    txtEditInvestmentAmountLayout.requestFocus();
                }
            }
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
                double amountToInvestRaw = mathHelper.computeInvestmentForSpecifiedAreaUnit(investmentAmountLocal, fieldSize, areaUnit);
                double amountToInvest = mathHelper.roundToNDecimalPlaces(amountToInvestRaw, 2);
                invAmount.setInvestmentAmountUSD(investmentAmountUSD);
                invAmount.setMinInvestmentAmountUSD(minimumAmountUSD);
                invAmount.setInvestmentAmountLocal(amountToInvest);
                invAmount.setMinInvestmentAmountLocal(minimumAmountLocal);
                invAmount.setFieldSize(fieldSizeAcre);

                database.investmentAmountDao().insert(invAmount);
                database.adviceStatusDao().insert(new AdviceStatus(EnumAdviceTasks.INVESTMENT_AMOUNT.name(), true));

                closeActivity(false);
            } catch (Exception ex) {
                Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                Sentry.captureException(ex);
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

        InvestmentAmount investmentAmount = database.investmentAmountDao().findOne();
        if (investmentAmount != null) {
            selectedPrice = investmentAmount.getInvestmentAmountLocal();
        }
        MandatoryInfo mandatoryInfo = database.mandatoryInfoDao().findOne();
        if (mandatoryInfo != null) {
            fieldSize = mandatoryInfo.getAreaSize();
            fieldSizeAcre = mandatoryInfo.getAreaSize();
            fieldArea = String.valueOf(fieldSize);
            fieldAreaAcre = String.valueOf(fieldSizeAcre);
            areaUnit = mandatoryInfo.getAreaUnit();
            areaUnitText = mandatoryInfo.getDisplayAreaUnit();
        }
        selectedFieldArea = String.format(getString(R.string.lbl_investment_amount_label), fieldArea, areaUnitText);

        loadInvestmentAmount(); //load amount from API
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

    private void loadInvestmentAmount() {

        final RestParameters restParameters = new RestParameters(
                String.format("v1/investment-amount/%s/country", countryCode),
                countryCode);

        final RestService restService = RestService.getInstance(queue, this);
        restService.setParameters(restParameters);

        restService.getJsonArrList(new IVolleyCallback() {
            @Override
            public void onSuccessJsonString(String jsonStringResult) {

            }

            @Override
            public void onSuccessJsonArr(JSONArray jsonArray) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    investmentAmountList = objectMapper.readValue(jsonArray.toString(), new TypeReference<List<InvestmentAmountDto>>() {
                    });
                    if (investmentAmountList.size() > 0) {
                        database.investmentAmountDtoDao().insertAll(investmentAmountList);
                        addInvestmentRadioButtons(investmentAmountList);
                    } else {
                        Toast.makeText(context, getString(R.string.lbl_investment_amount_load_error), Toast.LENGTH_LONG).show();
                    }

                } catch (Exception ex) {
                    String error = ex.getMessage();
                    Toast.makeText(context, getString(R.string.lbl_investment_amount_load_error), Toast.LENGTH_SHORT).show();
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onSuccessJsonObject(JSONObject jsonObject) {
            }

            @Override
            public void onError(@NotNull VolleyError ex) {
                Toast.makeText(context, getString(R.string.lbl_investment_amount_load_error), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addInvestmentRadioButtons(List<InvestmentAmountDto> investmentAmountList) {
        rdgInvestmentAmount.removeAllViews();
        currencySymbol = currencyCode;
        ExtendedCurrency extendedCurrency = CurrencyCode.getCurrencySymbol(currencyCode);
        if (extendedCurrency != null) {
            currencySymbol = extendedCurrency.getSymbol();
        }

        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int mediumSpacing = 30;
        params.setMargins(0, mediumSpacing, 0, mediumSpacing);

        String exactTextHint = getString(R.string.exact_investment_x_per_field_area_hint, currencyName, String.valueOf(fieldSize), areaUnit);
        String separator = getString(R.string.lbl_per_separator);

        txtEditInvestmentAmountLayout.setHint(exactTextHint);
        txtEditInvestmentAmount.setHint(exactTextHint);

        for (InvestmentAmountDto pricesResp : investmentAmountList) {
            minimumAmountLocal = pricesResp.getMinInvestmentAmount();
            maxAmountLocal = pricesResp.getMaxInvestmentAmount();
            long listIndex = pricesResp.getInvestmentId();

            RadioButton radioButton = new RadioButton(context);
            radioButton.setId(View.generateViewId());
            radioButton.setTag(listIndex);

            double price = pricesResp.getInvestmentAmount();
            double amountToInvest = mathHelper.computeInvestmentForSpecifiedAreaUnit(price, fieldSize, areaUnit);
            String radioLabel = setLabel(amountToInvest, currencyCode, currencySymbol, areaUnit, fieldAreaAcre, selectedFieldArea, separator);
            if (price == 0) {
                radioLabel = context.getString(R.string.exact_investment_x_per_field_area);
            }

            radioButton.setText(radioLabel);
            radioButton.setLayoutParams(params);

            rdgInvestmentAmount.addView(radioButton);
            //set relevant radio button as selected based on the price range
            double refAmount = mathHelper.roundToNDecimalPlaces(amountToInvest, 2);
            if (refAmount == selectedPrice) {
                radioButton.setChecked(true);
            }
        }
    }

    private String setLabel(Double amountToInvest, String currencyCode, String currencySymbol,
                            String areaUnit, String fieldAreaAcre, String selectedFieldArea, String separator) {

        String formattedNumber = mathHelper.formatNumber(amountToInvest, currencySymbol);
        return String.format("%s %s %s", formattedNumber, separator, selectedFieldArea);
    }
}
