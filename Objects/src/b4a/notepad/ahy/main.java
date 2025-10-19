package b4a.notepad.ahy;


import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = false;
	public static final boolean includeTitle = true;
    public static WeakReference<Activity> previousOne;
    public static boolean dontPause;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mostCurrent = this;
		if (processBA == null) {
			processBA = new BA(this.getApplicationContext(), null, null, "b4a.notepad.ahy", "b4a.notepad.ahy.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
        processBA.setActivityPaused(true);
        processBA.runHook("oncreate", this, null);
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
        WaitForLayout wl = new WaitForLayout();
        if (anywheresoftware.b4a.objects.ServiceHelper.StarterHelper.startFromActivity(this, processBA, wl, false))
		    BA.handler.postDelayed(wl, 5);

	}
	static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "b4a.notepad.ahy", "b4a.notepad.ahy.main");
        
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "b4a.notepad.ahy.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density, mostCurrent);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create " + (isFirst ? "(first time)" : "") + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
        try {
            if (processBA.subExists("activity_actionbarhomeclick")) {
                Class.forName("android.app.ActionBar").getMethod("setHomeButtonEnabled", boolean.class).invoke(
                    getClass().getMethod("getActionBar").invoke(this), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (processBA.runHook("oncreateoptionsmenu", this, new Object[] {menu}))
            return true;
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
        
		return true;
	}   
 @Override
 public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == 16908332) {
        processBA.raiseEvent(null, "activity_actionbarhomeclick");
        return true;
    }
    else
        return super.onOptionsItemSelected(item); 
}
@Override
 public boolean onPrepareOptionsMenu(android.view.Menu menu) {
    super.onPrepareOptionsMenu(menu);
    processBA.runHook("onprepareoptionsmenu", this, new Object[] {menu});
    return true;
    
 }
 protected void onStart() {
    super.onStart();
    processBA.runHook("onstart", this, null);
}
 protected void onStop() {
    super.onStop();
    processBA.runHook("onstop", this, null);
}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEventFromUI(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeydown", this, new Object[] {keyCode, event}))
            return true;
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeyup", this, new Object[] {keyCode, event}))
            return true;
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
		this.setIntent(intent);
        processBA.runHook("onnewintent", this, new Object[] {intent});
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null)
            return;
        if (this != mostCurrent)
			return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        if (!dontPause)
            BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        else
            BA.LogInfo("** Activity (main) Pause event (activity is not paused). **");
        if (mostCurrent != null)
            processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        if (!dontPause) {
            processBA.setActivityPaused(true);
            mostCurrent = null;
        }

        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        processBA.runHook("onpause", this, null);
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
        processBA.runHook("ondestroy", this, null);
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
        processBA.runHook("onresume", this, null);
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
            main mc = mostCurrent;
			if (mc == null || mc != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
            if (mc != mostCurrent)
                return;
		    processBA.raiseEvent(mc._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
        processBA.runHook("onactivityresult", this, new Object[] {requestCode, resultCode});
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}
    public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
        for (int i = 0;i < permissions.length;i++) {
            Object[] o = new Object[] {permissions[i], grantResults[i] == 0};
            processBA.raiseEventFromDifferentThread(null,null, 0, "activity_permissionresult", true, o);
        }
            
    }

public anywheresoftware.b4a.keywords.Common __c = null;
public static anywheresoftware.b4a.objects.Timer _tmrkursor = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txtnote = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btnsalin = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btnreplace = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txtreplacefrom = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txtreplaceto = null;
public b4a.util.BClipboard _clipboard = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txtlinenumber = null;
public static int _posisikursorsebelumnya = 0;
public b4a.notepad.ahy.starter _starter = null;

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static String  _activity_create(boolean _firsttime) throws Exception{
 //BA.debugLineNum = 29;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 30;BA.debugLine="Activity.Color = Colors.Black";
mostCurrent._activity.setColor(anywheresoftware.b4a.keywords.Common.Colors.Black);
 //BA.debugLineNum = 31;BA.debugLine="InitNotepad";
_initnotepad();
 //BA.debugLineNum = 32;BA.debugLine="tmrKursor.Initialize(\"tmrKursor\", 300)";
_tmrkursor.Initialize(processBA,"tmrKursor",(long) (300));
 //BA.debugLineNum = 33;BA.debugLine="tmrKursor.Enabled = True";
_tmrkursor.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 34;BA.debugLine="End Sub";
return "";
}
public static String  _btnreplace_click() throws Exception{
String _katalama = "";
String _katabaru = "";
 //BA.debugLineNum = 135;BA.debugLine="Sub btnReplace_Click";
 //BA.debugLineNum = 136;BA.debugLine="Dim kataLama As String = txtReplaceFrom.Text";
_katalama = mostCurrent._txtreplacefrom.getText();
 //BA.debugLineNum = 137;BA.debugLine="Dim kataBaru As String = txtReplaceTo.Text";
_katabaru = mostCurrent._txtreplaceto.getText();
 //BA.debugLineNum = 138;BA.debugLine="txtNote.Text = txtNote.Text.Replace(kataLama, kat";
mostCurrent._txtnote.setText(BA.ObjectToCharSequence(mostCurrent._txtnote.getText().replace(_katalama,_katabaru)));
 //BA.debugLineNum = 139;BA.debugLine="ToastMessageShow(\"Kata diganti!\", False)";
anywheresoftware.b4a.keywords.Common.ToastMessageShow(BA.ObjectToCharSequence("Kata diganti!"),anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 140;BA.debugLine="End Sub";
return "";
}
public static String  _btnsalin_click() throws Exception{
 //BA.debugLineNum = 130;BA.debugLine="Sub btnSalin_Click";
 //BA.debugLineNum = 131;BA.debugLine="Clipboard.SetText(txtNote.Text)";
mostCurrent._clipboard.setText(mostCurrent.activityBA,mostCurrent._txtnote.getText());
 //BA.debugLineNum = 132;BA.debugLine="ToastMessageShow(\"Semua teks telah disalin!\", Fal";
anywheresoftware.b4a.keywords.Common.ToastMessageShow(BA.ObjectToCharSequence("Semua teks telah disalin!"),anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 133;BA.debugLine="End Sub";
return "";
}
public static String  _globals() throws Exception{
 //BA.debugLineNum = 18;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 19;BA.debugLine="Private txtNote As EditText";
mostCurrent._txtnote = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 20;BA.debugLine="Private btnSalin As Button";
mostCurrent._btnsalin = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 21;BA.debugLine="Private btnReplace As Button";
mostCurrent._btnreplace = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 22;BA.debugLine="Private txtReplaceFrom As EditText";
mostCurrent._txtreplacefrom = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 23;BA.debugLine="Private txtReplaceTo As EditText";
mostCurrent._txtreplaceto = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 24;BA.debugLine="Private Clipboard As BClipboard";
mostCurrent._clipboard = new b4a.util.BClipboard();
 //BA.debugLineNum = 25;BA.debugLine="Private txtLineNumber As EditText";
mostCurrent._txtlinenumber = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 26;BA.debugLine="Private posisiKursorSebelumnya As Int = -1";
_posisikursorsebelumnya = (int) (-1);
 //BA.debugLineNum = 27;BA.debugLine="End Sub";
return "";
}
public static String  _highlightactiveline(int _posisikursor) throws Exception{
String _tekssebelum = "";
int _barisaktif = 0;
String[] _lines = null;
String _hasil = "";
int _i = 0;
 //BA.debugLineNum = 100;BA.debugLine="Sub HighlightActiveLine(posisiKursor As Int)";
 //BA.debugLineNum = 101;BA.debugLine="Dim teksSebelum As String = txtNote.Text.SubStrin";
_tekssebelum = mostCurrent._txtnote.getText().substring((int) (0),_posisikursor);
 //BA.debugLineNum = 102;BA.debugLine="Dim barisAktif As Int = Regex.Split(\"\\n\", teksSeb";
_barisaktif = anywheresoftware.b4a.keywords.Common.Regex.Split("\\n",_tekssebelum).length;
 //BA.debugLineNum = 104;BA.debugLine="Dim lines() As String = Regex.Split(\"\\n\", txtNote";
_lines = anywheresoftware.b4a.keywords.Common.Regex.Split("\\n",mostCurrent._txtnote.getText());
 //BA.debugLineNum = 105;BA.debugLine="Dim hasil As String = \"\"";
_hasil = "";
 //BA.debugLineNum = 106;BA.debugLine="For i = 0 To lines.Length - 1";
{
final int step5 = 1;
final int limit5 = (int) (_lines.length-1);
_i = (int) (0) ;
for (;_i <= limit5 ;_i = _i + step5 ) {
 //BA.debugLineNum = 107;BA.debugLine="If i + 1 = barisAktif Then";
if (_i+1==_barisaktif) { 
 //BA.debugLineNum = 108;BA.debugLine="hasil = hasil & (i + 1) & CRLF ' Baris aktif te";
_hasil = _hasil+BA.NumberToString((_i+1))+anywheresoftware.b4a.keywords.Common.CRLF;
 }else {
 //BA.debugLineNum = 110;BA.debugLine="hasil = hasil & (i + 1) & CRLF";
_hasil = _hasil+BA.NumberToString((_i+1))+anywheresoftware.b4a.keywords.Common.CRLF;
 };
 }
};
 //BA.debugLineNum = 113;BA.debugLine="txtLineNumber.Text = hasil";
mostCurrent._txtlinenumber.setText(BA.ObjectToCharSequence(_hasil));
 //BA.debugLineNum = 114;BA.debugLine="txtLineNumber.TextColor = Colors.Gray ' Default s";
mostCurrent._txtlinenumber.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Gray);
 //BA.debugLineNum = 115;BA.debugLine="txtLineNumber.SelectionStart = 0 ' Reset posisi k";
mostCurrent._txtlinenumber.setSelectionStart((int) (0));
 //BA.debugLineNum = 116;BA.debugLine="End Sub";
return "";
}
public static String  _initnotepad() throws Exception{
int _kolomlebar = 0;
 //BA.debugLineNum = 36;BA.debugLine="Sub InitNotepad";
 //BA.debugLineNum = 37;BA.debugLine="Dim kolomLebar As Int = (100%x - 30dip) / 2";
_kolomlebar = (int) ((anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA)-anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (30)))/(double)2);
 //BA.debugLineNum = 40;BA.debugLine="txtLineNumber.Initialize(\"\")";
mostCurrent._txtlinenumber.Initialize(mostCurrent.activityBA,"");
 //BA.debugLineNum = 41;BA.debugLine="txtLineNumber.SingleLine = False";
mostCurrent._txtlinenumber.setSingleLine(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 42;BA.debugLine="txtLineNumber.TextColor = Colors.Gray";
mostCurrent._txtlinenumber.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Gray);
 //BA.debugLineNum = 43;BA.debugLine="txtLineNumber.Color = 0xFF1E1E1E";
mostCurrent._txtlinenumber.setColor(((int)0xff1e1e1e));
 //BA.debugLineNum = 44;BA.debugLine="txtLineNumber.TextSize = 16";
mostCurrent._txtlinenumber.setTextSize((float) (16));
 //BA.debugLineNum = 45;BA.debugLine="txtLineNumber.Gravity = Bit.Or(Gravity.TOP, Gravi";
mostCurrent._txtlinenumber.setGravity(anywheresoftware.b4a.keywords.Common.Bit.Or(anywheresoftware.b4a.keywords.Common.Gravity.TOP,anywheresoftware.b4a.keywords.Common.Gravity.CENTER));
 //BA.debugLineNum = 46;BA.debugLine="txtLineNumber.Enabled = False";
mostCurrent._txtlinenumber.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 47;BA.debugLine="txtLineNumber.InputType = Bit.Or(1, 131072)";
mostCurrent._txtlinenumber.setInputType(anywheresoftware.b4a.keywords.Common.Bit.Or((int) (1),(int) (131072)));
 //BA.debugLineNum = 48;BA.debugLine="Activity.AddView(txtLineNumber, 0, 10dip, 30dip,";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._txtlinenumber.getObject()),(int) (0),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10)),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (30)),(int) (anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (100),mostCurrent.activityBA)-anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (270))));
 //BA.debugLineNum = 51;BA.debugLine="txtNote.Initialize(\"txtNote\")";
mostCurrent._txtnote.Initialize(mostCurrent.activityBA,"txtNote");
 //BA.debugLineNum = 52;BA.debugLine="txtNote.SingleLine = False";
mostCurrent._txtnote.setSingleLine(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 53;BA.debugLine="txtNote.TextColor = Colors.White";
mostCurrent._txtnote.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 54;BA.debugLine="txtNote.Color = 0xFF2C2C2C";
mostCurrent._txtnote.setColor(((int)0xff2c2c2c));
 //BA.debugLineNum = 55;BA.debugLine="txtNote.TextSize = 16";
mostCurrent._txtnote.setTextSize((float) (16));
 //BA.debugLineNum = 56;BA.debugLine="txtNote.InputType = Bit.Or(1, Bit.Or(131072, 1638";
mostCurrent._txtnote.setInputType(anywheresoftware.b4a.keywords.Common.Bit.Or((int) (1),anywheresoftware.b4a.keywords.Common.Bit.Or((int) (131072),(int) (16384))));
 //BA.debugLineNum = 57;BA.debugLine="txtNote.Gravity = Bit.Or(Gravity.TOP, Gravity.LEF";
mostCurrent._txtnote.setGravity(anywheresoftware.b4a.keywords.Common.Bit.Or(anywheresoftware.b4a.keywords.Common.Gravity.TOP,anywheresoftware.b4a.keywords.Common.Gravity.LEFT));
 //BA.debugLineNum = 58;BA.debugLine="Activity.AddView(txtNote, 30dip, 10dip, 100%x - 4";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._txtnote.getObject()),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (30)),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10)),(int) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA)-anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (40))),(int) (anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (100),mostCurrent.activityBA)-anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (270))));
 //BA.debugLineNum = 61;BA.debugLine="txtReplaceFrom.Initialize(\"\")";
mostCurrent._txtreplacefrom.Initialize(mostCurrent.activityBA,"");
 //BA.debugLineNum = 62;BA.debugLine="txtReplaceFrom.Hint = \"Find\"";
mostCurrent._txtreplacefrom.setHint("Find");
 //BA.debugLineNum = 63;BA.debugLine="txtReplaceFrom.Color = 0xFF3A3A3A";
mostCurrent._txtreplacefrom.setColor(((int)0xff3a3a3a));
 //BA.debugLineNum = 64;BA.debugLine="txtReplaceFrom.TextColor = Colors.White";
mostCurrent._txtreplacefrom.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 65;BA.debugLine="txtReplaceFrom.InputType = Bit.Or(1, 16384)";
mostCurrent._txtreplacefrom.setInputType(anywheresoftware.b4a.keywords.Common.Bit.Or((int) (1),(int) (16384)));
 //BA.debugLineNum = 66;BA.debugLine="Activity.AddView(txtReplaceFrom, 10dip, txtNote.T";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._txtreplacefrom.getObject()),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10)),(int) (mostCurrent._txtnote.getTop()+mostCurrent._txtnote.getHeight()+anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10))),_kolomlebar,anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (40)));
 //BA.debugLineNum = 69;BA.debugLine="txtReplaceTo.Initialize(\"\")";
mostCurrent._txtreplaceto.Initialize(mostCurrent.activityBA,"");
 //BA.debugLineNum = 70;BA.debugLine="txtReplaceTo.Hint = \"Replace\"";
mostCurrent._txtreplaceto.setHint("Replace");
 //BA.debugLineNum = 71;BA.debugLine="txtReplaceTo.Color = 0xFF3A3A3A";
mostCurrent._txtreplaceto.setColor(((int)0xff3a3a3a));
 //BA.debugLineNum = 72;BA.debugLine="txtReplaceTo.TextColor = Colors.White";
mostCurrent._txtreplaceto.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 73;BA.debugLine="txtReplaceTo.InputType = Bit.Or(1, 16384)";
mostCurrent._txtreplaceto.setInputType(anywheresoftware.b4a.keywords.Common.Bit.Or((int) (1),(int) (16384)));
 //BA.debugLineNum = 74;BA.debugLine="Activity.AddView(txtReplaceTo, txtReplaceFrom.Lef";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._txtreplaceto.getObject()),(int) (mostCurrent._txtreplacefrom.getLeft()+_kolomlebar+anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10))),mostCurrent._txtreplacefrom.getTop(),_kolomlebar,anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (40)));
 //BA.debugLineNum = 77;BA.debugLine="btnReplace.Initialize(\"btnReplace\")";
mostCurrent._btnreplace.Initialize(mostCurrent.activityBA,"btnReplace");
 //BA.debugLineNum = 78;BA.debugLine="btnReplace.Text = \"Ganti Kata\"";
mostCurrent._btnreplace.setText(BA.ObjectToCharSequence("Ganti Kata"));
 //BA.debugLineNum = 79;BA.debugLine="btnReplace.Color = 0xFF555555";
mostCurrent._btnreplace.setColor(((int)0xff555555));
 //BA.debugLineNum = 80;BA.debugLine="btnReplace.TextColor = Colors.White";
mostCurrent._btnreplace.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 81;BA.debugLine="Activity.AddView(btnReplace, 10dip, txtReplaceFro";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._btnreplace.getObject()),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10)),(int) (mostCurrent._txtreplacefrom.getTop()+mostCurrent._txtreplacefrom.getHeight()+anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10))),(int) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA)-anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (20))),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (40)));
 //BA.debugLineNum = 84;BA.debugLine="btnSalin.Initialize(\"btnSalin\")";
mostCurrent._btnsalin.Initialize(mostCurrent.activityBA,"btnSalin");
 //BA.debugLineNum = 85;BA.debugLine="btnSalin.Text = \"Salin Semua\"";
mostCurrent._btnsalin.setText(BA.ObjectToCharSequence("Salin Semua"));
 //BA.debugLineNum = 86;BA.debugLine="btnSalin.Color = 0xFF777777";
mostCurrent._btnsalin.setColor(((int)0xff777777));
 //BA.debugLineNum = 87;BA.debugLine="btnSalin.TextColor = Colors.White";
mostCurrent._btnsalin.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 88;BA.debugLine="Activity.AddView(btnSalin, 10dip, btnReplace.Top";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._btnsalin.getObject()),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10)),(int) (mostCurrent._btnreplace.getTop()+mostCurrent._btnreplace.getHeight()+anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10))),(int) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA)-anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (20))),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (40)));
 //BA.debugLineNum = 89;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
    
    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        main._process_globals();
starter._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 14;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 15;BA.debugLine="Private tmrKursor As Timer";
_tmrkursor = new anywheresoftware.b4a.objects.Timer();
 //BA.debugLineNum = 16;BA.debugLine="End Sub";
return "";
}
public static String  _tmrkursor_tick() throws Exception{
int _posisisekarang = 0;
 //BA.debugLineNum = 118;BA.debugLine="Sub tmrKursor_Tick";
 //BA.debugLineNum = 119;BA.debugLine="Dim posisiSekarang As Int = txtNote.SelectionStar";
_posisisekarang = mostCurrent._txtnote.getSelectionStart();
 //BA.debugLineNum = 120;BA.debugLine="If posisiSekarang <> posisiKursorSebelumnya Then";
if (_posisisekarang!=_posisikursorsebelumnya) { 
 //BA.debugLineNum = 121;BA.debugLine="posisiKursorSebelumnya = posisiSekarang";
_posisikursorsebelumnya = _posisisekarang;
 //BA.debugLineNum = 122;BA.debugLine="HighlightActiveLine(posisiSekarang)";
_highlightactiveline(_posisisekarang);
 };
 //BA.debugLineNum = 124;BA.debugLine="End Sub";
return "";
}
public static String  _txtnote_textchanged(String _old,String _new) throws Exception{
 //BA.debugLineNum = 126;BA.debugLine="Sub txtNote_TextChanged (Old As String, New As Str";
 //BA.debugLineNum = 127;BA.debugLine="UpdateLineNumbers";
_updatelinenumbers();
 //BA.debugLineNum = 128;BA.debugLine="End Sub";
return "";
}
public static String  _updatelinenumbers() throws Exception{
String[] _lines = null;
String _hasil = "";
int _i = 0;
 //BA.debugLineNum = 91;BA.debugLine="Sub UpdateLineNumbers";
 //BA.debugLineNum = 92;BA.debugLine="Dim lines() As String = Regex.Split(\"\\n\", txtNote";
_lines = anywheresoftware.b4a.keywords.Common.Regex.Split("\\n",mostCurrent._txtnote.getText());
 //BA.debugLineNum = 93;BA.debugLine="Dim hasil As String = \"\"";
_hasil = "";
 //BA.debugLineNum = 94;BA.debugLine="For i = 0 To lines.Length - 1";
{
final int step3 = 1;
final int limit3 = (int) (_lines.length-1);
_i = (int) (0) ;
for (;_i <= limit3 ;_i = _i + step3 ) {
 //BA.debugLineNum = 95;BA.debugLine="hasil = hasil & (i + 1) & CRLF";
_hasil = _hasil+BA.NumberToString((_i+1))+anywheresoftware.b4a.keywords.Common.CRLF;
 }
};
 //BA.debugLineNum = 97;BA.debugLine="txtLineNumber.Text = hasil";
mostCurrent._txtlinenumber.setText(BA.ObjectToCharSequence(_hasil));
 //BA.debugLineNum = 98;BA.debugLine="End Sub";
return "";
}
}
