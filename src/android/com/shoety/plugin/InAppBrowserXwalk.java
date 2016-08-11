package com.shoety.plugin;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.ValueCallback;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.XWalkCookieManager;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkView;

public class InAppBrowserXwalk extends CordovaPlugin
{
    private BrowserDialog dialog;
    private XWalkView xWalkWebView;
    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException
    {
        if(action.equals("open"))
        {
            this.callbackContext = callbackContext;
            this.openBrowser(data);
        }

        if(action.equals("close"))
        {
            this.closeBrowser();
        }

        if(action.equals("show"))
        {
            this.showBrowser();
        }

        if(action.equals("hide"))
        {
            this.hideBrowser();
        }

        if (action.equals("executeScript"))
        {
            this.executeScript(data.getString(0));
        }

        if(action.equals("navigateToUrl"))
        {
            this.navigateToUrl(data.getString(0), callbackContext);
        }

		if(action.equals("getUrl"))
        {
			this.getUrl(callbackContext);
		}

        return true;
    }

    private class ResourceClient extends XWalkResourceClient
    {
           ResourceClient(XWalkView view)
           {
               super(view);
           }

           @Override
           public void onLoadStarted(XWalkView view, String url)
           {
               try
               {
                   JSONObject obj = new JSONObject();
                   obj.put("type", "loadstart");
                   obj.put("url", url);

                   PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                   result.setKeepCallback(true);
                   callbackContext.sendPluginResult(result);
               }
               catch (JSONException ex)
               {
                   callbackContext.error(ex.getMessage());
               }
           }

           @Override
           public void doUpdateVisitedHistory(XWalkView view, String url, boolean isReload)
           {
               try
               {
                   JSONObject obj = new JSONObject();
                   obj.put("type", "updatevisithistory");
                   obj.put("url", url);
                   obj.put("isreload", isReload);

                   PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                   result.setKeepCallback(true);
                   callbackContext.sendPluginResult(result);
               }
               catch (JSONException ex)
               {
                   callbackContext.error(ex.getMessage());
               }
           }

           @Override
           public void onLoadFinished(XWalkView view, String url)
           {
               try
               {
                   JSONObject obj = new JSONObject();
                   obj.put("type", "loadstop");
                   obj.put("url", url);

                   PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                   result.setKeepCallback(true);
                   callbackContext.sendPluginResult(result);
               }
               catch (JSONException ex)
               {
                   callbackContext.error(ex.getMessage());
               }
           }
   }

    private void openBrowser(final JSONArray data) throws JSONException
    {
        final String url = data.getString(0);
        this.cordova.getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                dialog = new BrowserDialog(cordova.getActivity(), android.R.style.Theme_NoTitleBar);
                xWalkWebView = new XWalkView(cordova.getActivity(), cordova.getActivity());
                XWalkCookieManager mCookieManager = new XWalkCookieManager();
                mCookieManager.setAcceptCookie(true);
                mCookieManager.setAcceptFileSchemeCookies(true);
                xWalkWebView.setResourceClient(new ResourceClient(xWalkWebView));
                xWalkWebView.load(url, "");

                String toolbarColor = "#FFFFFF";
                int toolbarHeight = 80;
                String closeButtonText = "< Close";
                int closeButtonSize = 25;
                String closeButtonColor = "#000000";
                boolean openHidden = false;

                if(data.length() > 1)
                {
                    try
                    {
                        JSONObject options = new JSONObject(data.getString(1));

                        if(!options.isNull("toolbarColor"))
                        {
                            toolbarColor = options.getString("toolbarColor");
                        }

                        if(!options.isNull("toolbarHeight"))
                        {
                            toolbarHeight = options.getInt("toolbarHeight");
                        }

                        if(!options.isNull("closeButtonText"))
                        {
                            closeButtonText = options.getString("closeButtonText");
                        }

                        if(!options.isNull("closeButtonSize"))
                        {
                            closeButtonSize = options.getInt("closeButtonSize");
                        }

                        if(!options.isNull("closeButtonColor"))
                        {
                            closeButtonColor = options.getString("closeButtonColor");
                        }

                        if(!options.isNull("openHidden"))
                        {
                            openHidden = options.getBoolean("openHidden");
                        }
                    }
                    catch (JSONException ex)
                    {

                    }
                }

                LinearLayout main = new LinearLayout(cordova.getActivity());
                main.setOrientation(LinearLayout.VERTICAL);

                RelativeLayout toolbar = new RelativeLayout(cordova.getActivity());
                toolbar.setBackgroundColor(android.graphics.Color.parseColor(toolbarColor));
                toolbar.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, toolbarHeight));
                toolbar.setPadding(5, 5, 5, 5);

                TextView closeButton = new TextView(cordova.getActivity());
                closeButton.setText(closeButtonText);
                closeButton.setTextSize(closeButtonSize);
                closeButton.setTextColor(android.graphics.Color.parseColor(closeButtonColor));
                closeButton.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
                toolbar.addView(closeButton);

                closeButton.setOnClickListener(new View.OnClickListener()
                {
                     @Override
                     public void onClick(View v)
                     {
                         closeBrowser();
                     }
                 });

                main.addView(toolbar);
                main.addView(xWalkWebView);

                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
                dialog.setCancelable(true);
                LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
                dialog.addContentView(main, layoutParams);
                if(!openHidden)
                {
                    dialog.show();
                }
            }
        });
    }

    private void getUrl(final CallbackContext callbackContext)
    {
        this.cordova.getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    JSONObject obj = new JSONObject();
                    obj.put("url", xWalkWebView.getUrl());

                    PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);
                }
                catch (JSONException ex)
                {
                    callbackContext.error(ex.getMessage());
                }
            }
        });
    }

    private void navigateToUrl(final String url, final CallbackContext callbackContext)
    {
        this.cordova.getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    xWalkWebView.load(url, "");

                    JSONObject obj = new JSONObject();

                    PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);
                }
                catch (Exception ex)
                {
                    callbackContext.error(ex.getMessage());
                }
            }
        });
    }

    private void hideBrowser()
    {
        this.cordova.getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(dialog != null)
                {
                    dialog.hide();
                }
            }
        });
    }

    private void showBrowser()
    {
        this.cordova.getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(dialog != null)
                {
                    dialog.show();
                }
            }
        });
    }

    private void executeScript(String source)
    {
        final String finalScriptToInject = source;
        this.cordova.getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                xWalkWebView.evaluateJavascript(finalScriptToInject, new ValueCallback<String>()
                {
                    @Override
                    public void onReceiveValue(String scriptResult) {

                        try
                        {
                            JSONObject obj = new JSONObject();
                            obj.put("type", "jsCallback");
                            obj.put("result", scriptResult);

                            PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                            result.setKeepCallback(true);
                            callbackContext.sendPluginResult(result);
                        }
                        catch (JSONException ex)
                        {
                            callbackContext.error(ex.getMessage());
                        }
                    }
                });
            }
        });
    }

    private void closeBrowser()
    {
        this.cordova.getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                xWalkWebView.onDestroy();
                dialog.dismiss();

                try
                {
                    JSONObject obj = new JSONObject();
                    obj.put("type", "exit");

                    PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);
                }
                catch (JSONException ex)
                {
                    callbackContext.error(ex.getMessage());
                }
            }
        });
    }
}
