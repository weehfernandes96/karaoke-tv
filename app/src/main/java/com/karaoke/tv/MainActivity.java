package com.karaoke.tv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends Activity {
    private WebView web;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        web.setBackgroundColor(0xFF14082E);
        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onPermissionRequest(PermissionRequest r) { r.grant(r.getResources()); }
        });
        web.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView v, String url) {
                // o palco pede um clique em "Iniciar" para liberar o som: aqui ele e clicado sozinho
                v.evaluateJavascript("(function(){var t=0,i=setInterval(function(){var b=document.getElementById('btnStart');if(b&&b.offsetParent){b.click();clearInterval(i)}if(++t>20)clearInterval(i)},500)})()", null);
            }
            @Override public void onReceivedError(WebView v, int c, String d, String u) {
                v.postDelayed(new Runnable() { public void run() { web.reload(); } }, 5000);
            }
        });
        web.loadUrl(readUrl());
    }

    private String readUrl() {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(getAssets().open("url.txt"), "UTF-8"));
            String l = r.readLine(); r.close();
            return l.trim();
        } catch (Exception e) { return "about:blank"; }
    }

    // botão Voltar do controle: pergunta se quer fechar o app
    @Override public boolean onKeyDown(int k, KeyEvent e) {
        if (k == KeyEvent.KEYCODE_BACK) { askExit(); return true; }
        return super.onKeyDown(k, e);
    }

    private void askExit() {
        new AlertDialog.Builder(this)
            .setTitle("Fechar o Karaokê?")
            .setMessage("A música vai parar.")
            .setPositiveButton("Sim, fechar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int w) { closeAll(); }
            })
            .setNegativeButton("Não", null)
            .show();
    }

    private void closeAll() {
        try { web.loadUrl("about:blank"); web.destroy(); } catch (Exception e) { /* ok */ }
        finishAndRemoveTask();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    // saiu do app pelo botão Home: para a música e encerra por completo
    @Override protected void onStop() {
        super.onStop();
        if (!isChangingConfigurations()) closeAll();
    }
}
