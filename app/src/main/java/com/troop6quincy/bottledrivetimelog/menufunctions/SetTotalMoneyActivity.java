package com.troop6quincy.bottledrivetimelog.menufunctions;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.troop6quincy.bottledrivetimelog.R;
import com.troop6quincy.bottledrivetimelog.Scout;
import com.troop6quincy.bottledrivetimelog.SessionObject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

/**
 * Activity to set the total money earned.
 *
 * @author Joe Desmond
 * @since 1.0
 * @version 1.0
 */
public class SetTotalMoneyActivity extends AppCompatActivity {

    /**
     * Key at which the currency is stored. Used when the previous session's currency is passed to
     * this activity, and also when the newly set currency is passed to the calling activity.
     */
    public static final String SESSION_CURRENCY_KEY = "sessionCurrency";

    /**
     * The currency text box
     */
    private CurrencyEditText editText;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        final Intent intent = getIntent();
        final SessionObject session = (SessionObject) intent.getSerializableExtra(getResources().getString(R.string.session_object_key));

        if (session.darkThemeEnabled) {
            getDelegate().setLocalNightMode((AppCompatDelegate.MODE_NIGHT_YES));
        } else {
            getDelegate().setLocalNightMode((AppCompatDelegate.MODE_NIGHT_NO));
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_set_total_money);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editText = findViewById(R.id.totalMoney);

        final long sessionCurrency = session.totalMoney;

        if (sessionCurrency >= 0) {
            editText.setValue(sessionCurrency);
        }

        final Button submitButton = findViewById(R.id.submit);
        final Button cancelButton = findViewById(R.id.cancel);

        submitButton.setOnClickListener(this::submitAction);
        cancelButton.setOnClickListener(this::cancelAction);

        final AdView adView = findViewById(R.id.lowerBannerAd);
        AdRequest request = new AdRequest.Builder().build();
        adView.loadAd(request);
    }

    /**
     * Submit button click listener. When the submit button is pressed, the currency in the
     * {@link CurrencyEditText} is returned to the calling Activity as a long, referenced by
     * {@link #SESSION_CURRENCY_KEY}.
     *
     * @param view view, unused
     */
    private final void submitAction(final View view) {
        long currency = editText.getRawValue();

        final Intent resultIntent = new Intent();
        resultIntent.putExtra(SESSION_CURRENCY_KEY, currency);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    /**
     * Cancel button click listener. When the cancel button is pressed, this activity should finish
     * without providing a currency.
     *
     * @param view view, unused
     */
    private final void cancelAction(final View view) {
        final Intent resultIntent = new Intent();
        setResult(RESULT_CANCELED, resultIntent);
        finish();
    }
}
