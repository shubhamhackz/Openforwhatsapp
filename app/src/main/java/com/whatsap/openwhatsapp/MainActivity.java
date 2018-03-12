package com.whatsap.openwhatsapp;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.net.URISyntaxException;
import java.util.TreeSet;

import mehdi.sakout.fancybuttons.FancyButton;

public class MainActivity extends Activity {
    private static final String PREF_PREFIX = "prefix";
    private static final String PREF_RECENT = "recent";
    private static final String REGEXP_RECENTS = ";";
    private ArrayAdapter<String> historyAdapter;
    private ListPopupWindowWrapper listPopupWindow;
    private int page = 0;
    private SharedPreferences pref;
    private FancyButton view_button;
    private TextView view_error;
    private TextView view_extraInfo;
    private EditText view_input_message;
    private EditText view_input_number;
    private EditText view_input_pref;
    private FancyButton view_next;
    private FancyButton view_prev;
    private View view_recents;
    private ScrollView view_scroll;
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.view_scroll = (ScrollView) findViewById(R.id.activity_main);
        this.view_error = (TextView) findViewById(R.id.txt_error);
        this.view_input_pref = (EditText) findViewById(R.id.edtTxt_prefix);
        this.view_input_number = (EditText) findViewById(R.id.edtTxt_number);
        this.view_input_message = (EditText) findViewById(R.id.edtTxt_message);
        this.view_button =  findViewById(R.id.btn_function);
        this.view_prev =  findViewById(R.id.btn_prevScreen);
        this.view_next =  findViewById(R.id.btn_nextScreen);
        this.view_extraInfo = (TextView) findViewById(R.id.txt_extraInfo);
        this.view_recents = findViewById(R.id.btn_recents);
        this.view_input_pref.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            
            public void afterTextChanged(Editable editable) {
                MainActivity.this.pref.edit().putString(MainActivity.PREF_PREFIX, editable.toString()).apply();
                MainActivity.this.view_error.setText(BuildConfig.FLAVOR);
            }
        });
        TextWatcher textWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            
            public void afterTextChanged(Editable editable) {
                MainActivity.this.updateOpenButton();
                MainActivity.this.view_error.setText(BuildConfig.FLAVOR);
            }
        };
        this.view_input_number.addTextChangedListener(textWatcher);
        this.view_input_message.addTextChangedListener(textWatcher);
        OnLongClickListener onLongClickListener = new OnLongClickListener() {
            public boolean onLongClick(View view) {
                MainActivity.this.button_longClick(view);
                return true;
            }
        };
        this.view_button.setOnLongClickListener(onLongClickListener);
        findViewById(R.id.txt_plus).setOnLongClickListener(onLongClickListener);
        this.view_recents.setOnLongClickListener(onLongClickListener);
        this.view_button.setEnabled(false);
        this.listPopupWindow = new ListPopupWindowWrapper(this, findViewById(R.id.linLay_inputNumber));
        this.historyAdapter = new ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item);
        this.listPopupWindow.setAdapter(this.historyAdapter);
        this.listPopupWindow.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String element = (String) MainActivity.this.historyAdapter.getItem(i);
                if (element != null) {
                    String[] parts = element.split(" ");
                    if (parts.length > 1) {
                        MainActivity.this.setNumber(parts[0], parts[1]);
                    }
                }
                MainActivity.this.listPopupWindow.dismiss();
            }
        });
        this.pref = getPreferences(0);
        setNumber(this.pref.getString(PREF_PREFIX, BuildConfig.FLAVOR), null);
        String savedNumbers = this.pref.getString(PREF_RECENT, BuildConfig.FLAVOR);
        this.historyAdapter.clear();
        if (savedNumbers.length() > 0) {
            for (String number : savedNumbers.split(REGEXP_RECENTS)) {
                this.historyAdapter.add(number);
            }
            this.view_recents.setVisibility(View.VISIBLE);
        } else {
            this.view_recents.setVisibility(View.INVISIBLE);
        }
        try {
            parseIntent(getIntent());
        } catch (Exception e) {
            Log.d("parseIntent@onCreate", e.toString());
        }
        setPage(0);
    }
    
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            parseIntent(intent);
        } catch (Exception e) {
            Log.d("parseIntent@onNewIntent", e.toString());
        }
    }
    
    private void parseIntent(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();
        Uri data = intent.getData();
        Log.d("intent", intent.toUri(0));
        Log.d("action", String.valueOf(action));
        Log.d("type", String.valueOf(type));
        String number = PhoneNumberUtils.getNumberFromIntent(intent, this);
        if (number != null) {
            Log.d("PhoneNumberUtils", number);
            number = PhoneNumberUtils.stringFromStringAndTOA(number, 145);
            if (number != null) {
                setNumber(null, number);
                Toast.makeText(this, R.string.toast_autoParser, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if ("text/plain".equals(type)) {
            String extra = intent.getStringExtra("android.intent.extra.TEXT");
            if (extra != null) {
                setNumber(null, extra);
                Toast.makeText(this, R.string.toast_autoParser, Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    public void button_click(View view) {
        switch (view.getId()) {
            case R.id.btn_function /*2130968577*/:
                switch (this.page) {
                    case 0:
                        openInWhatsapp(true);
                        return;
                    case 1:
                        shareLink(false);
                        return;
                    default:
                        return;
                }
            case R.id.btn_nextScreen /*2130968578*/:
                setPage(this.page + 1);
                return;
            case R.id.btn_prevScreen /*2130968579*/:
                setPage(this.page - 1);
                return;
            case R.id.btn_recents /*2130968580*/:
                this.listPopupWindow.show();
                return;
            case R.id.txt_plus /*2130968589*/:
                chooseCountryCode(false);
                return;
            default:
                return;
        }
    }
    
    public void button_longClick(View view) {
        switch (view.getId()) {
            case R.id.btn_function /*2130968577*/:
                switch (this.page) {
                    case 0:
                        new Builder(this).setTitle(R.string.alternate_title).setMessage(R.string.alternate_message).setNegativeButton(R.string.alternate_cancel, null).setPositiveButton(R.string.alternate_continue, new OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MainActivity.this.openInWhatsapp(true);
                            }
                        }).show();
                        return;
                    case 1:
                        shareLink(true);
                        return;
                    default:
                        return;
                }
            case R.id.btn_recents /*2130968580*/:
                this.historyAdapter.clear();
                this.pref.edit().remove(PREF_RECENT).apply();
                this.view_recents.setVisibility(View.INVISIBLE);
                return;
            case R.id.txt_plus /*2130968589*/:
                chooseCountryCode(true);
                return;
            default:
                return;
        }
    }
    
    public void setPage(int page) {
        if (page < 0) {
            page = 0;
        } else if (page > 1) {
            page = 1;
        }
        this.page = page;
        this.view_error.setText(BuildConfig.FLAVOR);
        switch (this.page) {
            case 0:
                this.view_input_message.setVisibility(View.INVISIBLE
                );
                this.view_prev.setVisibility(View.INVISIBLE);
                this.view_button.setText(getString(R.string.btn_openInWhatsapp));
                this.view_next.setVisibility(View.VISIBLE);
                this.view_extraInfo.setVisibility(View.INVISIBLE);
                break;
            case 1:
                this.view_input_message.setVisibility(View.VISIBLE);

                this.view_prev.setVisibility(View.VISIBLE);
                this.view_button.setText(getString(R.string.btn_shareLink));
                this.view_next.setVisibility(View.INVISIBLE);
                this.view_extraInfo.setVisibility(View.VISIBLE);
                break;
        }
        this.view_scroll.post(new Runnable() {
            public void run() {
                MainActivity.this.view_scroll.scrollTo(0, MainActivity.this.view_scroll.getMaxScrollAmount());
                MainActivity.this.view_input_number.requestFocus();
            }
        });
        updateOpenButton();
    }
    
    public void updateOpenButton() {
        boolean enabled = false;
        switch (this.page) {
            case 0:
                if (this.view_input_number.length() > 0) {
                    enabled = true;
                } else {
                    enabled = false;
                }
                break;
            case 1:
                enabled = this.view_input_number.length() > 0 || this.view_input_message.length() > 0;
                break;
        }
        this.view_button.setEnabled(enabled);
    }
    
    public void addItemToRecents() {
        String data = "+" + this.view_input_pref.getText().toString().replaceAll(" ", BuildConfig.FLAVOR) + " " + this.view_input_number.getText().toString().replaceAll(" ", BuildConfig.FLAVOR);
        this.historyAdapter.remove(data);
        this.historyAdapter.insert(data, 0);
        if (this.historyAdapter.getCount() > 5) {
            this.historyAdapter.remove(this.historyAdapter.getItem(5));
        }
        StringBuilder savedRecents = new StringBuilder();
        int i = 0;
        while (i < this.historyAdapter.getCount()) {
            savedRecents.append(i == 0 ? BuildConfig.FLAVOR : REGEXP_RECENTS).append((String) this.historyAdapter.getItem(i));
            i++;
        }
        this.pref.edit().putString(PREF_RECENT, savedRecents.toString()).apply();
        this.view_recents.setVisibility(View.VISIBLE);
    }
    
    public void setNumber(String prefix, String number) {
        if (prefix != null) {
            this.view_input_pref.setText(BuildConfig.FLAVOR);
            this.view_input_pref.append(prefix);
        }
        if (number != null) {
            this.view_input_number.setText(BuildConfig.FLAVOR);
            this.view_input_number.append(number);
        }
    }
    
    public String getNumber(boolean raw) {
        if (this.view_input_number.length() == 0) {
            return BuildConfig.FLAVOR;
        }
        return (raw ? BuildConfig.FLAVOR : "phone=") + (this.view_input_pref.getText().toString() + this.view_input_number.getText().toString()).replaceAll("^0+", BuildConfig.FLAVOR);
    }
    
    public String getMessage() {
        if (this.view_input_message.length() != 0) {
            return "text=" + Uri.encode(this.view_input_message.getText().toString());
        }
        return BuildConfig.FLAVOR;
    }
    
    public void openInWhatsapp(boolean shortcut) {
        Intent intent;
        if (shortcut) {
            try {
                intent = new Intent("android.intent.action.MainActivity");
                intent.setComponent(new ComponentName("com.whatsapp", "com.whatsapp.Conversation"));
                intent.putExtra("jid", getNumber(true) + "@s.whatsapp.net");
                intent.putExtra("displayname", "+" + getNumber(true));

                intent = Intent.parseUri("whatsapp://send/?" + getNumber(false), 0);
                startActivity(intent);
                addItemToRecents();
                this.view_error.setText(BuildConfig.FLAVOR);
            } catch (URISyntaxException e) {
                this.view_error.setText(R.string.error_badData);
                return;
            } catch (ActivityNotFoundException e2) {
                this.view_error.setText(R.string.error_nowhatsapp);
                return;
            }
        }

    }
    
    public void shareLink(boolean toClipboard) {
        String number = getNumber(false);
        String message = getMessage();
        StringBuilder append = new StringBuilder().append("http://api.whatsapp.com/send?").append(number);
        String str = (number.length() == 0 || message.length() == 0) ? BuildConfig.FLAVOR : "&";
        String url = append.append(str).append(message).toString();
        if (toClipboard) {
            ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).setText(url);
            Toast.makeText(this, R.string.toast_copied, Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent("android.intent.action.SEND");
            intent.putExtra("android.intent.extra.TEXT", url);
            intent.setType("text/plain");
            startActivity(Intent.createChooser(intent, getString(R.string.title_sendTo)));
        }
        if (number.length() != 0) {
            addItemToRecents();
        }
    }
    
    public void chooseCountryCode(boolean inverted) {
        TreeSet<String> sorted = new TreeSet();
        for (String element : getResources().getStringArray(R.array.countries_code)) {
            if (inverted) {
                String[] elements = element.split(": ");
                sorted.add(elements[1] + " : " + elements[0]);
            } else {
                sorted.add(element);
            }
        }
        final String[] sortedArray = (String[]) sorted.toArray(new String[0]);
        new Builder(this).setTitle(R.string.title_chooseCountry).setItems(sortedArray, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.setNumber(sortedArray[i], null);
            }
        }).show();
    }
}
