package com.troop6quincy.bottledrivetimelog.menufunctions;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.troop6quincy.bottledrivetimelog.R;
import com.troop6quincy.bottledrivetimelog.Scout;

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_total_money);
        editText = findViewById(R.id.totalMoney);

        final Intent intent = getIntent();
        final long sessionCurrency = intent.getLongExtra(SESSION_CURRENCY_KEY, -1);

        if (sessionCurrency >= 0) {
            editText.setValue(sessionCurrency);
        }

        final Button submitButton = findViewById(R.id.submit);
        final Button cancelButton = findViewById(R.id.cancel);

        submitButton.setOnClickListener(this::submitAction);
        cancelButton.setOnClickListener(this::cancelAction);
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
